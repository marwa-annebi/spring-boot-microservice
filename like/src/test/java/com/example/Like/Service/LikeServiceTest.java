package com.example.Like.Service;

import com.example.Like.Service.client.PostClient;
import com.example.Like.Service.client.UserClient;
import com.example.Like.Service.dtos.CreateLikeDto;
import com.example.Like.Service.models.Like;
import com.example.Like.Service.models.Post;
import com.example.Like.Service.models.User;
import com.example.Like.Service.repositories.LikeRepository;
import com.example.Like.Service.services.LikeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@RunWith(MockitoJUnitRunner.class)
class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private PostClient postClient;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private LikeService likeService;

    private User user;
    private Post post;
    private Like like;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User("1", "testUser", "Test", "User", "test@example.com", "password", null, null);
        post = new Post("1", "Test post", null, user, null, null);
        like = Like.builder().id("1").likedBy(user).post(post).top(true).build();
    }

    @Test
    void testAddLike() {
        // Mock user authentication
        User mockUser = new User("1", "testUser", "Test", "User", "test@example.com", "password", null, null);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(mockUser, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Prepare data
        CreateLikeDto createLikeDto = new CreateLikeDto(post.getId());
        when(postClient.getPostById(post.getId())).thenReturn(post);
        when(likeRepository.findByLikedByAndPost(mockUser, post)).thenReturn(Optional.empty());

        Like savedLike = Like.builder().id("1").likedBy(mockUser).post(post).top(true).build();
        when(likeRepository.save(any(Like.class))).thenReturn(savedLike);

        // Execute the method
        Like result = likeService.addLike(createLikeDto);

        // Assertions
        assertNotNull(result);
        assertEquals(post.getId(), result.getPost().getId());
        assertTrue(result.isTop());
        verify(likeRepository, times(1)).save(any(Like.class));

        // Clear the SecurityContext after the test
        SecurityContextHolder.clearContext();
    }

    @Test
    void testAddDislike() {
        // Configurer une authentification fictive
        User mockUser = User.builder()
                .id("1")
                .userName("testUser")
                .email("test@example.com")
                .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockUser, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Préparer les données nécessaires
        CreateLikeDto createLikeDto = new CreateLikeDto(post.getId());
        when(postClient.getPostById(post.getId())).thenReturn(post);
        when(likeRepository.findByLikedByAndPost(mockUser, post)).thenReturn(Optional.empty());

        // Simuler la sauvegarde d'un dislike
        Like savedDislike = Like.builder()
                .id("1")
                .likedBy(mockUser)
                .post(post)
                .top(false)
                .build();
        when(likeRepository.save(any(Like.class))).thenReturn(savedDislike);

        // Appeler la méthode et effectuer les assertions
        Like result = likeService.addDislike(createLikeDto);

        assertNotNull(result);
        assertEquals(post.getId(), result.getPost().getId());
        assertFalse(result.isTop());
        verify(likeRepository, times(1)).save(any(Like.class));

        // Réinitialiser le contexte de sécurité pour éviter les fuites dans d'autres tests
        SecurityContextHolder.clearContext();
    }

    @Test
    void testRemoveLikeOrDislike() {
        // Configurer une authentification fictive
        User mockUser = User.builder()
                .id("1")
                .userName("testUser")
                .email("test@example.com")
                .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockUser, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Préparer les données nécessaires
        CreateLikeDto createLikeDto = new CreateLikeDto(post.getId());
        when(postClient.getPostById(post.getId())).thenReturn(post);
        when(likeRepository.findByLikedByAndPost(mockUser, post)).thenReturn(Optional.of(like));

        // Appeler la méthode et effectuer les assertions
        likeService.removeLikeOrDislike(createLikeDto);

        // Vérifier que la méthode `delete` a été appelée sur le dépôt
        verify(likeRepository, times(1)).delete(like);

        // Réinitialiser le contexte de sécurité pour éviter les fuites dans d'autres tests
        SecurityContextHolder.clearContext();
    }


    @Test
    void testGetLikeDetails() {
        when(likeRepository.findByPostId(post.getId())).thenReturn(List.of(like));

        var result = likeService.getLikeDetails(post.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(post.getId(), result.get(0).getPost().getId());
        verify(likeRepository, times(1)).findByPostId(post.getId());
    }
}