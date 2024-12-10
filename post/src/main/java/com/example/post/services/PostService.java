package com.example.post.services;

import com.example.post.client.CommentClient;
import com.example.post.client.LikeClient;
import com.example.post.client.UserClient;
import com.example.post.dtos.CreateDto;
import com.example.post.dtos.LikeDetailsDto;
import com.example.post.dtos.UpdatePostDto;
import com.example.post.dtos.WithLikesCount;
import com.example.post.models.Post;
import com.example.post.models.User;
import com.example.post.repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import static org.springframework.data.mongodb.core.query.Query.query;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;
    private final CommentClient commentClient;
private final MongoTemplate mongoTemplate;
private  final  ImageService imageService;
    public static final String UPLOAD_DIRECTORY = System.getProperty("user.dir") + "/uploads";
    private final UserClient userClient;
    private final LikeClient likeClient; // Feign client or REST template for Like service

    public com.example.post.dto.PostResponseDto getPostWithDetails(String postId) {
        // Fetch the post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
System.out.println(post);
        // Fetch user details from User service
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Fetch like details from Like service
        LikeDetailsDto likeDetails = (LikeDetailsDto) likeClient.getLikeDetails(postId);

        // Map to PostResponseDto
        return com.example.post.dto.PostResponseDto.builder()
                .id(post.getId())
                .description(post.getDescription())
                .images(post.getImages())
                .postedById(user.getId())
                .postedByUserName(user.getUserName())
                .postedByFirstName(user.getFirstName())
                .postedByLastName(user.getLastName())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .likedByCurrentUser(true)
                .nbLike(10)
                .build();
    }
    // Méthode pour récupérer tous les posts avec pagination
    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }


// Récupérer les posts de l'utilisateur authentifié
public Page<Post> findPostsByCurrentUser(String userId, Pageable pageable) {
    return postRepository.findByPostedBy_Id(userId, pageable);
}

    private User getCurrentUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("Authenticated user: " + user.getEmail());
        return user;
    }

    private Post createToPost(CreateDto createPostDto) {
        Post post = new Post();
        post.setDescription(createPostDto.getDescription());
        return post;
    }

    public Post createPost(CreateDto createPostDto, List<MultipartFile> images) throws IOException {

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Post post = createToPost(createPostDto);
        post.setPostedBy(currentUser);

        // Save images
        List<String> imagePaths = imageService.saveImages(images);
        post.setImages(imagePaths);

        // Save the post to MongoDB
        Post savedPost = postRepository.save(post);

        // Update user's post list in MongoDB
        mongoTemplate.update(User.class)
                .matching(query(Criteria.where("_id").is(currentUser.getId())))
                .apply(new Update().push("posts", new ObjectId(savedPost.getId())))
                .first();

        return savedPost;
    }

    // Suppression d'un post
    public void deletePost(String postId) {
        if (!postRepository.existsById(postId)) {
            throw new IllegalArgumentException("Post not found");
        }
        postRepository.deleteById(postId);
    }

    public PageImpl<WithLikesCount> searchPosts(String userId, String searchTerm, Pageable pageable) {
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria matchCriteria = Criteria.where("postedBy").ne(new ObjectId(userId));
        AggregationOperation matchOperation = Aggregation.match(matchCriteria);
        operations.add(matchOperation);

        // Add search term filter
        if (StringUtils.hasText(searchTerm)) {
            Criteria searchCriteria = Criteria.where("description")
                    .regex(Pattern.compile(searchTerm, Pattern.CASE_INSENSITIVE));
            operations.add(Aggregation.match(searchCriteria));
        }

        // Count likes for each post


        // Sort and paginate
        operations.add(Aggregation.sort(Sort.by(Sort.Direction.DESC, "createdAt")));
        operations.add(Aggregation.skip(pageable.getOffset()));
        operations.add(Aggregation.limit(pageable.getPageSize()));

        // Execute aggregation
        Aggregation aggregation = Aggregation.newAggregation(operations);
        List<WithLikesCount> results = mongoTemplate.aggregate(aggregation, "posts", WithLikesCount.class).getMappedResults();

        // Count documents
        long count = getCountOfMatchedDocuments(userId,searchTerm, false);

        return new PageImpl<>(results, pageable, count);
    }
    private long getCountOfMatchedDocuments(String userId, String searchTerm, boolean myPosts) {
        Criteria criteria;

        // Check if fetching "myPosts" or "not my posts"
        if (myPosts) {
            // Match posts by the current user
            criteria = Criteria.where("postedBy").is(new ObjectId(userId));
        } else {
            // Match posts NOT by the current user
            criteria = Criteria.where("postedBy").ne(new ObjectId(userId));
        }

        // If a searchTerm is provided, match by description
        if (StringUtils.hasText(searchTerm)) {
            criteria = criteria.and("description").regex(".*" + searchTerm + ".*", "i"); // Case-insensitive search
        }

        // Build the query with the criteria
        Query query = query(criteria);

        // Return the count of matched documents
        return mongoTemplate.count(query, "posts");
    }

    public Optional<Post> getPostById(String id) {
        return postRepository.findById(id);
    }
    public Post editPost(String id, UpdatePostDto updatePostDto, List<MultipartFile> images) throws IOException {
        // Retrieve the post by ID
        Optional<Post> optionalPost = postRepository.findById(id);
        if (optionalPost.isEmpty()) {
            throw new RuntimeException("Post not found");
        }

        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));

        // Build the update for the description field
        Update update = new Update();
        update.set("description", updatePostDto.getDescription());

        // Execute the update and retrieve the updated document
        return mongoTemplate.findAndModify(query, update, Post.class);
    }
    public Map<String, Object> getPostWithComments(String postId) {
        // Fetch post details
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        // Fetch comment count using CommentClient
        long commentCount = commentClient.getCommentCountByPostId(postId).get("count");

        // Aggregate data
        return Map.of(
                "post", post,
                "commentCount", commentCount
        );
    }

}

























//    public Post editPost(String postId, CreateDto createDto, List<String> updatedImages) {
//        Post post = postRepository.findById(postId)
//                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
//
//        // Mettre à jour les champs
//        post.setTitle(createDto.getTitle());
//        post.setDescription(createDto.getDescription());
//        post.setUpdatedAt(new Date());
//
//        // Mettre à jour les images si nécessaire
//        if (updatedImages != null && !updatedImages.isEmpty()) {
//            post.setImages(updatedImages);
//        }
//
//        return postRepository.save(post);
//    }
// Modification d'un post
//    public Post editPost(String postId, CreateDto createDto, List<MultipartFile> images) throws IOException {
//        Post post = postRepository.findById(postId)
//                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
//
//        // Mise à jour des champs
//        post.setTitle(createDto.getTitle());
//        post.setDescription(createDto.getDescription());
//        post.setUpdatedAt(new Date());
//
//        // Mise à jour des images
//        if (images != null && !images.isEmpty()) {
//            List<String> imagePaths = new ArrayList<>();
//            for (MultipartFile file : images) {
//                String fileName = file.getOriginalFilename();
//                Path path = Paths.get(UPLOAD_DIRECTORY, fileName);
//                Files.createDirectories(path.getParent());
//                Files.write(path, file.getBytes());
//                imagePaths.add(fileName);
//            }
//            post.setImages(imagePaths);
//        }
//
//        return postRepository.save(post);
//    }
//    public Post editPost(String postId, CreateDto createDto, List<MultipartFile> images) throws IOException {
//        // Récupérer le post à mettre à jour
//        Post post = postRepository.findById(postId)
//                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
//
//        // Mise à jour des champs du post
//        post.setTitle(createDto.getTitle());
//        post.setDescription(createDto.getDescription());
//        post.setUpdatedAt(new Date());
//
//        // Gestion des images
//        List<String> updatedImagePaths = new ArrayList<>(post.getImages() != null ? post.getImages() : new ArrayList<>());
//
//        if (images != null && !images.isEmpty()) {
//            for (MultipartFile file : images) {
//                // Valider le type de fichier (par exemple, seulement les images JPEG/PNG)
//                String contentType = file.getContentType();
//                if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
//                    throw new IllegalArgumentException("Only JPEG and PNG files are allowed");
//                }
//
//                // Sauvegarder les fichiers
//                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
//                Path path = Paths.get(UPLOAD_DIRECTORY, fileName);
//                Files.createDirectories(path.getParent());
//                Files.write(path, file.getBytes());
//
//                // Ajouter les nouveaux fichiers au chemin
//                updatedImagePaths.add(fileName);
//            }
//        }
//
//        post.setImages(updatedImagePaths);
//
//        // Sauvegarder les modifications
//        return postRepository.save(post);
//    }
//    public Post create(CreateDto createDto, List<MultipartFile> images) throws IOException {
//        Post post = new Post();
//        post.setTitle(createDto.getTitle());
//        post.setDescription(createDto.getDescription());
//        post.setCreatedBy(createDto.getCreatedBy());
//        post.setCreatedAt(new Date());
//
//        // Sauvegarde des images
//        List<String> imagePaths = new ArrayList<>();
//        if (images != null) {
//            for (MultipartFile file : images) {
//                String fileName = file.getOriginalFilename();
//                Path path = Paths.get(UPLOAD_DIRECTORY, fileName);
//                Files.createDirectories(path.getParent());
//                Files.write(path, file.getBytes());
//                imagePaths.add(fileName);
//            }
//        }
//        post.setImages(imagePaths);
//
//        return postRepository.save(post);
//    }

//    public List<Post> getMyPosts() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication == null || !authentication.isAuthenticated()) {
//            throw new IllegalArgumentException("User is not authenticated");
//        }
//
//        String currentUser = (String) authentication.getPrincipal();
//        return postRepository.findByCreatedBy(currentUser);
//    }

//    public Page<Post> findPostsByCurrentUser(Pageable pageable) {
//        String currentUser = getCurrentUserName();
//        System.out.println("Current User: " + currentUser); // Log l'utilisateur actuel
//        if (currentUser == null) {
//            throw new IllegalStateException("No authenticated user found");
//        }
//        return postRepository.findByCreated_By(currentUser, pageable);
//    }


// Helper pour obtenir le nom d'utilisateur de l'utilisateur actuel
//    private String getCurrentUserName() {
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        if (principal instanceof UserDetails) {
//            return ((UserDetails) principal).getUsername();
//        } else {
//            return principal.toString();
//        }
//    }


//    public List<Post> getAllPosts() {
//        return postRepository.findAll();
//    }

//    public Page<Post> findPostsByCurrentUser(Pageable pageable) {
//        User currentUser = getCurrentUser();
//        return postRepository.findByPostedBy_Id(currentUser.getId(), pageable);
//    }
//    public Page<Post> getPostsByPageAndUsername(String username, Pageable pageable) {
//        // Correctement appeler la méthode avec deux arguments
//        return postRepository.findByCreatedBy(username, pageable);
//    }


//    public Page<Post> findPostsByCurrentUser(Pageable pageable) {
//        User currentUser = getCurrentUser();
//        return postRepository.findByPostedById(currentUser.getId(), pageable);
//    }
//    private User getCurrentUser() {
//        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        System.out.println("Authenticated user: " + user.getEmail());
//        return user;
//    }
//    public Post editUserPost(String postId, UpdatePostDto updatePostDto, List<MultipartFile> images) throws IOException {
//        // Fetch the post
//        Post existingPost = postRepository.findById(postId)
//                .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + postId));
//
//        // Verify the owner
//        User currentUser = getCurrentUser();
//        if (existingPost.getPostedBy() == null || !existingPost.getPostedBy().getId().equals(currentUser.getId())) {
//            throw new SecurityException("Unauthorized to edit this post.");
//        }
//
//        // Update fields
//        if (updatePostDto.getDescription() != null) {
//            existingPost.setDescription(updatePostDto.getDescription());
//        }
//
//        // Handle images
//        if (images != null && !images.isEmpty()) {
//            List<String> imagePaths = imageService.saveImages(images);
//            existingPost.getImages().addAll(imagePaths); // Append new images
//        }
//
//        // Save updated post
//        return postRepository.save(existingPost);
//    }
//public Post editPost(String postId, UpdatePostDto updatePostDto, List<MultipartFile> images) throws IOException {
//    // Fetch the post
//    Post existingPost = postRepository.findById(postId)
//            .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + postId));
//
//    // Get the current user
//    User currentUser = getCurrentUser();
//
//    // Verify ownership
//
//
//    // Update post fields
//    if (updatePostDto.getDescription() != null) {
//        existingPost.setDescription(updatePostDto.getDescription());
//    }
//
//    // Update images
//    if (images != null && !images.isEmpty()) {
//        List<String> imagePaths = imageService.saveImages(images);
//        existingPost.getImages().addAll(imagePaths);
//    }
//
//    // Save the updated post
//    return postRepository.save(existingPost);
//}
//    public List<String> saveImages(List<MultipartFile> images) throws IOException {
//        List<String> imagePaths = new ArrayList<>();
//        Path uploadDirectory = Paths.get(UPLOAD_DIRECTORY);
//
//        // Ensure the upload directory exists
//        if (!Files.exists(uploadDirectory)) {
//            Files.createDirectories(uploadDirectory);
//        }
//
//        for (MultipartFile image : images) {
//            if (!image.isEmpty()) {
//                String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
//                Path filePath = uploadDirectory.resolve(fileName);
//
//                // Save the file
//                try (var inputStream = image.getInputStream()) {
//                    Files.copy(inputStream, filePath);
//                }
//
//                imagePaths.add(filePath.toString());
//            }
//        }
//        return imagePaths;
//    }




//package com.example.post.services;
//
//import com.example.post.client.LikeClient;
//import com.example.post.client.UserClient;
//import com.example.post.config.SecurityUtils;
//import com.example.post.dtos.*;
//import com.example.post.models.Post;
//import com.example.post.models.User;
//import com.example.post.repositories.PostRepository;
//import lombok.RequiredArgsConstructor;
//import org.bson.types.ObjectId;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.aggregation.*;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.data.mongodb.core.query.Update;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.*;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
//import static org.springframework.data.mongodb.core.query.Query.query;
//
//@RequiredArgsConstructor
//@Service
//public class PostService {
//    private final PostRepository postRepository;
//    private final UserClient userClient;
//    private final MongoTemplate mongoTemplate;
//    private final ImageService imageService;
//    private final LikeClient likeClient; // Inject LikeClient
//    public static String UPLOAD_DIRECTORY = System.getProperty("user.dir") + "/uploads";
//
//    // Helper method to map CreateDto to Post
//    private Post createToPost(CreateDto createPostDto) {
//        Post post = new Post();
//        post.setDescription(createPostDto.getDescription());
//        return post;
//    }
//
//    public Post create(CreateDto createPostDto, List<MultipartFile> images) throws IOException {
//        // Get authenticated user
//        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        System.out.println("userDetails: " + userDetails);
//
//        // Fetch user by email
//        User user = userClient.findByEmail(userDetails.getEmail());
//        System.out.println("user: " + user);
//        if (user == null) {
//            throw new IllegalArgumentException("User not found.");
//        }
//
//        Post post = createToPost(createPostDto);
//        post.setPostedBy(user);
//
//        // Save images
//        List<String> imagePaths = imageService.saveImages(images);
//        post.setImages(imagePaths);
//
//        // Save the post to MongoDB
//        Post savedPost = postRepository.save(post);
//
//        // Update user's post list in MongoDB
//        mongoTemplate.update(User.class)
//                .matching(query(Criteria.where("_id").is(user.getId())))
//                .apply(new Update().push("posts", new ObjectId(savedPost.getId())))
//                .first();
//
//        return savedPost;
//    }
//
//    public Optional<Post> getPostById(String id) {
//        return postRepository.findById(id);
//    }
//
//    public void deletePostById(String id) {
//        postRepository.deleteById(id);
//    }
//
//    public Page<WithLikesCount> findAllByPageAndUsername(String searchTerm, Pageable pageable) {
//        List<AggregationOperation> operations = new ArrayList<>();
//
//        // Get authenticated user
//        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        System.out.println("userDetails: " + userDetails);
//
//        // Fetch user by email
//        User user = userClient.findByEmail(userDetails.getEmail());
//        System.out.println("user: " + user);
//
//        if (user == null) {
//            throw new IllegalArgumentException("User not found.");
//        }
//
//
//        // Exclude posts by the same user
//        Criteria matchCriteria = Criteria.where("postedBy").ne(new ObjectId(user.getId().toString()));
//        AggregationOperation matchOperation = Aggregation.match(matchCriteria);
//        operations.add(matchOperation);
//
//        // Add search term filter
//        if (StringUtils.hasText(searchTerm)) {
//            Criteria searchCriteria = Criteria.where("description")
//                    .regex(Pattern.compile(searchTerm, Pattern.CASE_INSENSITIVE));
//            operations.add(Aggregation.match(searchCriteria));
//        }
//
//        // Count likes for each post
//
//
//        // Sort and paginate
//        operations.add(Aggregation.sort(Sort.by(Sort.Direction.DESC, "createdAt")));
//        operations.add(Aggregation.skip(pageable.getOffset()));
//        operations.add(Aggregation.limit(pageable.getPageSize()));
//
//        // Execute aggregation
//        Aggregation aggregation = Aggregation.newAggregation(operations);
//        List<WithLikesCount> results = mongoTemplate.aggregate(aggregation, "posts", WithLikesCount.class).getMappedResults();
//
//        // Count documents
//        long count = getCountOfMatchedDocuments(searchTerm, false);
//
//        return new PageImpl<>(results, pageable, count);
//    }
//
//    private long getCountOfMatchedDocuments(String searchTerm, boolean myPosts) {
//        // Get authenticated user
//        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        System.out.println("userDetails: " + userDetails);
//
//        // Fetch user by email
//        User user = userClient.findByEmail(userDetails.getEmail());
//        System.out.println("user: " + user);
//
//        if (user == null) {
//            throw new IllegalArgumentException("User not found.");
//        }
//
//        if (myPosts) {
//            Criteria criteria = Criteria.where("postedBy").is(new ObjectId(user.getId()));
//            return mongoTemplate.count(Query.query(criteria), "posts");
//        } else if (StringUtils.isEmpty(searchTerm)) {
//            return mongoTemplate.count(new Query(), "posts");
//        } else {
//            LookupOperation lookupOperation = Aggregation.lookup("users", "postedBy", "_id", "user");
//            MatchOperation matchOperation = Aggregation.match(Criteria.where("user.username").regex(Pattern.compile(searchTerm, Pattern.CASE_INSENSITIVE)));
//            Aggregation aggregation = Aggregation.newAggregation(lookupOperation, matchOperation);
//            return mongoTemplate.aggregate(aggregation, "posts", Map.class).getMappedResults().size();
//        }
//    }
//    public Page<Post> findPostsByCurrentUser(Pageable pageable) {
//        User currentUser = getCurrentUser();
//        return postRepository.findByPostedById(currentUser.getId(), pageable);
//    }
//
//    // Helper pour obtenir l'utilisateur connecté
//    private User getCurrentUser() {
//        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//    }
////    public Post editPost(String postId, UpdatePostDto updatePostDto, List<MultipartFile> images) throws IOException {
////        Post existingPost = postRepository.findById(postId)
////                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
////
////        // Mise à jour de la description
////        existingPost.setDescription(updatePostDto.getDescription());
////
////        // Si des images sont fournies, les mettre à jour
////        if (images != null && !images.isEmpty()) {
////            List<String> imagePaths = imageService.saveImages(images);
////            existingPost.setImages(imagePaths);
////        }
////
////        // Sauvegarder les modifications
////        return postRepository.save(existingPost);
////    }
//public Post editUserPost(String postId, UpdatePostDto updatePostDto, List<MultipartFile> images) throws IOException {
//    User currentUser = getCurrentUser(); // Utilisateur authentifié
//
//    // Vérifier si le post existe et appartient à l'utilisateur connecté
//    Post existingPost = postRepository.findById(postId)
//            .orElseThrow(() -> new IllegalArgumentException("Post not found or you are not authorized to edit this post"));
//
//    if (!existingPost.getPostedBy().getId().equals(currentUser.getId())) {
//        throw new IllegalArgumentException("Unauthorized: You can only edit your own posts");
//    }
//
//    // Mise à jour de la description
//    existingPost.setDescription(updatePostDto.getDescription());
//
//    // Si des images sont fournies, les mettre à jour
//    if (images != null && !images.isEmpty()) {
//        List<String> imagePaths = imageService.saveImages(images);
//        existingPost.setImages(imagePaths);
//    }
//
//    return postRepository.save(existingPost);
//}
//
//}


// Helper pour obtenir l'utilisateur connecté
//    private User getCurrentUser() {
//        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//    }
//
//package com.example.post.services;
//
//        import com.example.post.dtos.CreatePostDto;
//        import com.example.post.dtos.PostResponseDto;
//        import com.example.post.dtos.UpdatePostDto;
//        import com.example.post.models.Post;
//        import com.example.post.repositories.PostRepository;
//        import lombok.RequiredArgsConstructor;
//        import org.springframework.stereotype.Service;
//
//        import java.util.List;
//        import java.util.stream.Collectors;
//
//@RequiredArgsConstructor
//@Service
//public class PostService {
//    private final PostRepository postRepository;
//
//    public PostResponseDto createPost(CreatePostDto createPostDto) {
//        Post post = Post.builder()
//                .description(createPostDto.getDescription())
//                .images(createPostDto.getImages())
//                .createdBy(createPostDto.getCreatedBy())
//                .build();
//
//        post = postRepository.save(post);
//        return mapToResponseDto(post);
//    }
//
//    public List<PostResponseDto> getAllPosts() {
//        return postRepository.findAll().stream()
//                .map(this::mapToResponseDto)
//                .collect(Collectors.toList());
//    }
//
//    public PostResponseDto getPostById(String id) {
//        Post post = postRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Post non trouvé"));
//        return mapToResponseDto(post);
//    }
//
//    public List<PostResponseDto> getPostsByUser(String userId) {
//        return postRepository.findByCreatedBy(userId).stream()
//                .map(this::mapToResponseDto)
//                .collect(Collectors.toList());
//    }
//
//    public PostResponseDto updatePost(String id, UpdatePostDto updatePostDto) {
//        Post post = postRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Post non trouvé"));
//
//        post.setDescription(updatePostDto.getDescription());
//        post.setImages(updatePostDto.getImages());
//        post = postRepository.save(post);
//
//        return mapToResponseDto(post);
//    }
//
//    public void deletePost(String id) {
//        if (!postRepository.existsById(id)) {
//            throw new IllegalArgumentException("Post non trouvé");
//        }
//        postRepository.deleteById(id);
//    }
//
//    private PostResponseDto mapToResponseDto(Post post) {
//        return PostResponseDto.builder()
//                .id(post.getId())
//                .description(post.getDescription())
//                .images(post.getImages())
//                .createdBy(post.getCreatedBy())
//                .createdAt(post.getCreatedAt())
//                .updatedAt(post.getUpdatedAt())
//                .build();
//    }
//}


//    public Page<WithLikesCount> myPosts(Pageable pageable) {
//        // Get authenticated user
//        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        User user = userClient.findByEmail(userDetails.getEmail());
//
//        if (user == null) {
//            throw new IllegalArgumentException("User not found.");
//        }
//
//        // Fetch posts for the user
//        Criteria criteria = Criteria.where("postedBy").is(new ObjectId(user.getId().toString()));
//        Query query = new Query(criteria).with(pageable);
//        List<Post> posts = mongoTemplate.find(query, Post.class, "posts");
//
//        // Fetch post IDs
//        List<String> postIds = posts.stream()
//                .map(post -> post.getId()) // Explicitly convert ObjectId to String
//                .collect(Collectors.toList());
//
//System.out.println(postIds);
//        // Fetch likes, dislikes, and user-specific interactions using LikeClient
//        Map<String, Integer> likesCount = likeClient.getLikesCount(postIds);
//        Map<String, Integer> dislikesCount = likeClient.getDislikesCount(postIds);
//        Map<String, Boolean> likedByUser = likeClient.getUserLikes(postIds);
//        Map<String, Boolean> dislikedByUser = likeClient.getUserDislikes(postIds);
//
//        // Aggregate data into DTO
//        List<WithLikesCount> enrichedPosts = posts.stream().map(post -> {
//            WithLikesCount enrichedPost = new WithLikesCount();
//            enrichedPost.setId(post.getId().toString());
//            enrichedPost.setDescription(post.getDescription());
//            enrichedPost.setImages(post.getImages());
//            enrichedPost.setPostedBy(post.getPostedBy().toString());
//            enrichedPost.setCreatedAt(post.getCreatedAt());
//            enrichedPost.setUpdatedAt(post.getUpdatedAt());
//            enrichedPost.setLikesCount(likesCount.getOrDefault(post.getId().toString(), 0));
//            enrichedPost.setDisliked(dislikesCount.getOrDefault(post.getId().toString(), 0) > 0);
//            enrichedPost.setLiked(likedByUser.getOrDefault(post.getId().toString(), false));
//            enrichedPost.setDisliked(dislikedByUser.getOrDefault(post.getId().toString(), false));
//            return enrichedPost;
//        }).collect(Collectors.toList());
//
//        long totalPosts = mongoTemplate.count(query.skip(0).limit(0), "posts");
//        return new PageImpl<>(enrichedPosts, pageable, totalPosts);
//    }

