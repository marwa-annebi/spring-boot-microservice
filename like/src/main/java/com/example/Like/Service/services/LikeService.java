//package com.example.Like.Service.services;
//
//import com.example.Like.Service.client.PostClient;
//import com.example.Like.Service.client.UserClient;
//import com.example.Like.Service.dtos.CreateLikeDto;
//import com.example.Like.Service.models.Like;
//import com.example.Like.Service.models.Post;
//import com.example.Like.Service.models.User;
//import com.example.Like.Service.repositories.LikeRepository;
//import lombok.RequiredArgsConstructor;
//import org.bson.types.ObjectId;
//import org.springframework.data.crossstore.ChangeSetPersister;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@RequiredArgsConstructor
//@Service
//public class LikeService {
//
//    private final LikeRepository likeRepository;
//    private final PostClient postClient; // Feign client to interact with Post Microservice
//    private final UserClient userClient; // Feign client to interact with User Microservice
//    private final MongoTemplate mongoTemplate;
//    /**
//     * Add a like to a post.
//     */
//    public Like addLike(CreateLikeDto createLikeDto) {
//        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        System.out.println("userDetails: " + userDetails);
//        // Fetch user and post using Feign clients
//        User user = userClient.findByEmail(userDetails.getEmail());
//        Post post = postClient.getPostById(createLikeDto.getPostId());
//
//        // Check if the like already exists
//        Optional<Like> existingLike = likeRepository.findByLikedByAndPost(user, post);
//        if (existingLike.isPresent() && existingLike.get().isTop()) {
//            throw new IllegalArgumentException("User already liked this post.");
//        }
//
//        // Create and save the Like
//        Like like = Like.builder()
//                .likedBy(user)
//                .post(post)
//                .top(true)
//                .build();
//        return likeRepository.save(like);
//    }
//
//    /**
//     * Add a dislike to a post.
//     */
//    public Like addDislike(CreateLikeDto createLikeDto) {
//        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        System.out.println("userDetails: " + userDetails);
//        // Fetch user and post using Feign clients
//        User user = userClient.findByEmail(userDetails.getEmail());
//        Post post = postClient.getPostById(createLikeDto.getPostId());
//
//        // Check if the dislike already exists
//        Optional<Like> existingDislike = likeRepository.findByLikedByAndPost(user, post);
//        if (existingDislike.isPresent() && !existingDislike.get().isTop()) {
//            throw new IllegalArgumentException("User already disliked this post.");
//        }
//
//        // Create and save the Dislike
//        Like dislike = Like.builder()
//                .likedBy(user)
//                .post(post)
//                .top(false)
//                .build();
//        return likeRepository.save(dislike);
//    }
//
//    /**
//     * Remove a like or dislike from a post.
//     */
//    public void removeLikeOrDislike(CreateLikeDto createLikeDto) {
//        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        System.out.println("userDetails: " + userDetails);
//
//        User user = userClient.findByEmail(userDetails.getEmail());
//        Post post = postClient.getPostById(createLikeDto.getPostId());
//
//        // Build query to find existing like/dislike
//        Query query = new Query(Criteria.where("likedBy").is(user.getId()).and("post").is(post.getId()));
//        Like existingLike = mongoTemplate.findOne(query, Like.class);
//
//        // Remove the like/dislike if it exists
//        if (existingLike != null) {
//            mongoTemplate.remove(query, Like.class);
//        }
//    }
//
//    /**
//     * Get likes count for multiple posts.
//     */
//    public Map<String, Long> getLikesCount(List<String> postIds) {
//
//        System.out.println("hello"+postIds);
//        return postIds.stream()
//                .collect(Collectors.toMap(
//                        postId -> postId,
//                        postId -> {
//                            Query query = new Query(Criteria.where("post").is(new ObjectId(postId)).and("top").is(true));
//                            return mongoTemplate.count(query, Like.class);
//                        }
//                ));
//    }
//
//    public Map<String, Long> getDisLikesCount(List<String> postIds) {
//
//        System.out.println("hello"+postIds);
//        return postIds.stream()
//                .collect(Collectors.toMap(
//                        postId -> postId,
//                        postId -> {
//                            Query query = new Query(Criteria.where("post").is(postId).and("top").is(false));
//                            return mongoTemplate.count(query, Like.class);
//                        }
//                ));
//    }
//
//    /**
//     * Check if a user liked specific posts.
//     */
//    public Map<String, Boolean> getUserLikes(List<String> postIds) {
//        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        User user = userClient.findByEmail(userDetails.getEmail());
//        return postIds.stream()
//                .collect(Collectors.toMap(
//                        postId -> postId,
//                        postId -> {
//                            Query query = new Query(Criteria.where("likedBy").is(user.getId()).and("post").is(postId).and("top").is(true));
//                            return mongoTemplate.exists(query, Like.class);
//                        }
//                ));
//    }
//
//    public Map<String, Boolean> getUserDislikes(List<String> postIds) {
//        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        System.out.println("userDetails: " + userDetails);
//
//        User user = userClient.findByEmail(userDetails.getEmail());
//        return postIds.stream()
//                .collect(Collectors.toMap(
//                        postId -> postId,
//                        postId -> {
//                            Query query = new Query(Criteria.where("likedBy").is(user.getId()).and("post").is(postId).and("top").is(false));
//                            return mongoTemplate.exists(query, Like.class);
//                        }
//                ));
//    }
//
//}

package com.example.Like.Service.services;
import com.example.Like.Service.client.PostClient;
import com.example.Like.Service.client.UserClient;
import com.example.Like.Service.dtos.CreateLikeDto;
import com.example.Like.Service.models.Like;
import com.example.Like.Service.models.Post;
import com.example.Like.Service.models.User;
import com.example.Like.Service.repositories.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class LikeService {
    private static final Logger logger = LoggerFactory.getLogger(LikeService.class);

    private final LikeRepository likeRepository;
    private final PostClient postClient;
    private final UserClient userClient;

    /**
     * Ajout d'un like pour un post
     */
    public Like addLike(CreateLikeDto createLikeDto) {
        logger.info("Tentative d'ajout d'un like pour le post ID: {}", createLikeDto.getPostId());

        User user = getCurrentUser();
        Post post = postClient.getPostById(createLikeDto.getPostId());

        Optional<Like> existingInteraction = likeRepository.findByLikedByAndPost(user, post);

        // Supprime un dislike existant avant d'ajouter un like
        if (existingInteraction.isPresent()) {
            likeRepository.delete(existingInteraction.get());
            if (existingInteraction.get().isTop()) {
                logger.warn("Utilisateur {} a déjà aimé le post {}", user.getEmail(), post.getId());
                throw new IllegalArgumentException("Vous avez déjà aimé ce post.");
            }
        }

        // Crée et enregistre un nouveau like
        Like like = Like.builder()
                .likedBy(user)
                .post(post)
                .top(true)
                .build();
        likeRepository.save(like);

        logger.info("Like ajouté avec succès pour le post ID: {} par l'utilisateur: {}", post.getId(), user.getEmail());
        return like;
    }

    /**
     * Ajout d'un dislike pour un post
     */
    public Like addDislike(CreateLikeDto createLikeDto) {
        logger.info("Tentative d'ajout d'un dislike pour le post ID: {}", createLikeDto.getPostId());

        User user = getCurrentUser();
        Post post = postClient.getPostById(createLikeDto.getPostId());

        Optional<Like> existingInteraction = likeRepository.findByLikedByAndPost(user, post);

        // Supprime un like existant avant d'ajouter un dislike
        if (existingInteraction.isPresent()) {
            likeRepository.delete(existingInteraction.get());
            if (!existingInteraction.get().isTop()) {
                logger.warn("Utilisateur {} a déjà disliké le post {}", user.getEmail(), post.getId());
                throw new IllegalArgumentException("Vous avez déjà disliké ce post.");
            }
        }

        // Crée et enregistre un nouveau dislike
        Like dislike = Like.builder()
                .likedBy(user)
                .post(post)
                .top(false)
                .build();
        likeRepository.save(dislike);

        logger.info("Dislike ajouté avec succès pour le post ID: {} par l'utilisateur: {}", post.getId(), user.getEmail());
        return dislike;
    }
    public Map<String, Long> getLikesCount(List<String> postIds) {
        return postIds.stream()
                .collect(Collectors.toMap(
                        postId -> postId,
                        postId -> likeRepository.countByPostAndTop(new Post(postId), true)
                ));
    }

    public Map<String, Long> getDislikesCount(List<String> postIds) {
        return postIds.stream()
                .collect(Collectors.toMap(
                        postId -> postId,
                        postId -> likeRepository.countByPostAndTop(new Post(postId), false)
                ));
    }

    /**
     * Suppression d'une interaction (like ou dislike)
     */
    public void removeLikeOrDislike(CreateLikeDto createLikeDto) {
        logger.info("Tentative de suppression d'une interaction pour le post ID: {}", createLikeDto.getPostId());

        User user = getCurrentUser();
        Post post = postClient.getPostById(createLikeDto.getPostId());

        likeRepository.findByLikedByAndPost(user, post)
                .ifPresentOrElse(
                        interaction -> {
                            likeRepository.delete(interaction);
                            logger.info("Interaction supprimée avec succès pour le post ID: {} par l'utilisateur: {}", post.getId(), user.getEmail());
                        },
                        () -> {
                            logger.warn("Aucune interaction trouvée pour le post ID: {} par l'utilisateur: {}", post.getId(), user.getEmail());
                            throw new IllegalArgumentException("Aucune interaction trouvée pour ce post.");
                        }
                );
    }
    public Map<String, Boolean> getUserLikes(List<String> postIds) {
        User user = getCurrentUser();
        return postIds.stream()
                .collect(Collectors.toMap(
                        postId -> postId,
                        postId -> likeRepository.findByLikedByAndPost(user, new Post(postId))
                                .map(Like::isTop).orElse(false)
                ));
    }
    public Map<String, Boolean> getCurrentUserPostReaction(String postId) {
        User currentUser = getCurrentUser();
        Optional<Like> like = likeRepository.findByLikedByAndPost(currentUser, new Post(postId));

        // Map contenant les informations sur les likes/dislikes
        Map<String, Boolean> reactionMap = new HashMap<>();
        reactionMap.put("liked", like.isPresent() && like.get().isTop()); // true si liked
        reactionMap.put("disliked", like.isPresent() && !like.get().isTop()); // true si disliked
        return reactionMap;
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

//    private User getCurrentUser() {
//        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//    }
    /**
     * Récupère le nombre de likes pour une liste de posts
     */
//    public Map<String, Long> getLikesCount(List<String> postIds) {
//        logger.info("Calcul du nombre de likes pour les posts: {}", postIds);
//        return postIds.stream()
//                .collect(Collectors.toMap(
//                        postId -> postId,
//                        postId -> likeRepository.countByPostAndTop(new Post(postId), true)
//                ));
//    }

    /**
     * Récupère l'état des likes d'un utilisateur pour une liste de posts
     */
//    public Map<String, Boolean> getUserLikes(List<String> postIds) {
//        User user = getCurrentUser();
//        logger.info("Vérification des likes de l'utilisateur {} pour les posts: {}", user.getEmail(), postIds);
//
//        return postIds.stream()
//                .collect(Collectors.toMap(
//                        postId -> postId,
//                        postId -> likeRepository.findByLikedByAndPost(user, new Post(postId))
//                                .map(Like::isTop).orElse(false)
//                ));
//    }
//
//    /**
//     * Récupération de l'utilisateur connecté
//     */
//    private User getCurrentUser() {
//        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        logger.debug("Utilisateur connecté: {}", user);
//        return user;
//    }
//    public Map<String, Map<String, Long>> getLikesAndDislikesCount(List<String> postIds) {
//        return postIds.stream().collect(Collectors.toMap(
//                postId -> postId,
//                postId -> {
//                    long likes = likeRepository.countByPostAndTop(new Post(postId), true);
//                    long dislikes = likeRepository.countByPostAndTop(new Post(postId), false);
//                    Map<String, Long> counts = new HashMap<>();
//                    counts.put("likes", likes);
//                    counts.put("dislikes", dislikes);
//                    return counts;
//                }
//        ));
//    }

}

//package com.example.Like.Service.services;
//
//import com.example.Like.Service.client.PostClient;
//import com.example.Like.Service.client.UserClient;
//import com.example.Like.Service.dtos.CreateLikeDto;
//import com.example.Like.Service.models.Like;
//import com.example.Like.Service.models.Post;
//import com.example.Like.Service.models.User;
//import com.example.Like.Service.repositories.LikeRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@RequiredArgsConstructor
//@Service
//public class LikeService {
//    private final LikeRepository likeRepository;
//    private final PostClient postClient;
//    private final UserClient userClient;
//
////    public Like addLike(CreateLikeDto createLikeDto) {
////        User user = getCurrentUser();
////        Post post = postClient.getPostById(createLikeDto.getPostId());
////
////        Optional<Like> existingLike = likeRepository.findByLikedByAndPost(user, post);
////        if (existingLike.isPresent() && existingLike.get().isTop()) {
////            throw new IllegalArgumentException("User already liked this post.");
////        }
////
////        Like like = Like.builder()
////                .likedBy(user)
////                .post(post)
////                .top(true)
////                .build();
////        return likeRepository.save(like);
////    }
////public Like addLike(CreateLikeDto createLikeDto) {
////    User user = getCurrentUser();
////    Post post = postClient.getPostById(createLikeDto.getPostId());
////
////    Optional<Like> existingLike = likeRepository.findByLikedByAndPost(user, post);
////    if (existingLike.isPresent() && existingLike.get().isTop()) {
////        throw new IllegalArgumentException("Vous avez déjà aimé ce post.");
////    }
////
////    Like like = Like.builder()
////            .likedBy(user)
////            .post(post)
////            .top(true) // Indique un like
////            .build();
////    return likeRepository.save(like);
////}
//public Like addLike(CreateLikeDto createLikeDto) {
//    User user = getCurrentUser();
//    Post post = postClient.getPostById(createLikeDto.getPostId());
//
//    Optional<Like> existingInteraction = likeRepository.findByLikedByAndPost(user, post);
//
//    // Supprimer un dislike existant si présent
//    if (existingInteraction.isPresent()) {
//        likeRepository.delete(existingInteraction.get());
//        if (existingInteraction.get().isTop()) {
//            throw new IllegalArgumentException("Vous avez déjà aimé ce post.");
//        }
//    }
//
//    // Ajouter un like
//    Like like = Like.builder()
//            .likedBy(user)
//            .post(post)
//            .top(true) // Indique un like
//            .build();
//    return likeRepository.save(like);
//}
//
//    public Like addDislike(CreateLikeDto createLikeDto) {
//        User user = getCurrentUser();
//        Post post = postClient.getPostById(createLikeDto.getPostId());
//
//        Optional<Like> existingDislike = likeRepository.findByLikedByAndPost(user, post);
//        if (existingDislike.isPresent() && !existingDislike.get().isTop()) {
//            throw new IllegalArgumentException("User already disliked this post.");
//        }
//
//        Like dislike = Like.builder()
//                .likedBy(user)
//                .post(post)
//                .top(false)
//                .build();
//        return likeRepository.save(dislike);
//    }
//
//
//    public void removeLikeOrDislike(CreateLikeDto createLikeDto) {
//        User user = getCurrentUser();
//        Post post = postClient.getPostById(createLikeDto.getPostId());
//
//        likeRepository.findByLikedByAndPost(user, post).ifPresent(likeRepository::delete);
//    }
//
//
//    public Map<String, Long> getLikesCount(List<String> postIds) {
//        return postIds.stream()
//                .collect(Collectors.toMap(
//                        postId -> postId,
//                        postId -> likeRepository.countByPostAndTop(new Post(postId), true)
//                ));
//    }
//
//
//    public Map<String, Boolean> getUserLikes(List<String> postIds) {
//        User user = getCurrentUser();
//        return postIds.stream()
//                .collect(Collectors.toMap(
//                        postId -> postId,
//                        postId -> likeRepository.findByLikedByAndPost(user, new Post(postId))
//                                .map(Like::isTop).orElse(false)
//                ));
//    }
//
//    private User getCurrentUser() {
//        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//    }
//}
