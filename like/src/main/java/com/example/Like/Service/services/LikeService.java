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
import org.springframework.web.client.RestTemplate;

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



    public List<Like> getLikeDetails(String postId) {
        return likeRepository.findByPostId(postId);
    }

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

    public Map<String, Boolean> getUserDislikes(List<String> postIds) {
        User user = getCurrentUser();
        return postIds.stream()
                .collect(Collectors.toMap(
                        postId -> postId,
                        postId -> likeRepository.findByLikedByAndPost(user, new Post(postId))
                                .map(like -> !like.isTop()) // Vérifie si le dislike existe
                                .orElse(false)
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
    public Map<String, Long> getLikeDislikeCounts(String postId) {
        Post post = new Post(postId);

        // Count likes
        long likeCount = likeRepository.countByPostAndTop(post, true);

        // Count dislikes
        long dislikeCount = likeRepository.countByPostAndTop(post, false);

        // Return the counts
        return Map.of(
                "likes", likeCount,
                "dislikes", dislikeCount
        );
    }

}