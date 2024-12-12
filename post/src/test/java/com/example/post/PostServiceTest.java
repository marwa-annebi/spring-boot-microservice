package com.example.post;

import com.example.post.dtos.UpdatePostDto;
import com.example.post.models.Post;
import com.example.post.models.User;
import com.example.post.repositories.PostRepository;
import com.example.post.services.ImageService;
import com.example.post.services.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@RunWith(MockitoJUnitRunner.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private PostService postService;

    @Captor
    private ArgumentCaptor<Post> postArgumentCaptor;
    @Mock
    private  MongoTemplate mongoTemplate;
    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock authenticated user
        mockUser = User.builder()
                .id("userId")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(mockUser);
        when(authentication.isAuthenticated()).thenReturn(true);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }




    @Test
    void testGetAllPosts() {
        // Arrange
        Post post1 = Post.builder().id("1").description("Post 1").build();
        Post post2 = Post.builder().id("2").description("Post 2").build();
        Page<Post> mockPage = new PageImpl<>(List.of(post1, post2), PageRequest.of(0, 10), 2);

        when(postRepository.findAll(any(PageRequest.class))).thenReturn(mockPage);

        // Act
        Page<Post> posts = postService.getAllPosts(PageRequest.of(0, 10));

        // Assert
        assertNotNull(posts);
        assertEquals(2, posts.getTotalElements());
        assertEquals("Post 1", posts.getContent().get(0).getDescription());
    }

    @Test
    void testDeletePost_Success() {
        // Arrange
        String postId = "postId";
        when(postRepository.existsById(postId)).thenReturn(true);

        // Act
        postService.deletePost(postId);

        // Assert
        verify(postRepository, times(1)).deleteById(postId);
    }

    @Test
    void testDeletePost_PostNotFound() {
        // Arrange
        String postId = "invalidPostId";
        when(postRepository.existsById(postId)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            postService.deletePost(postId);
        });

        assertEquals("Post not found", exception.getMessage());
    }



    @Test
    void testEditPost_PostNotFound() {
        // Arrange
        String postId = "invalidPostId";
        UpdatePostDto updatePostDto = new UpdatePostDto();
        updatePostDto.setDescription("Updated Description");

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            postService.editPost(postId, updatePostDto, Collections.emptyList());
        });

        assertEquals("Post not found", exception.getMessage());
    }
}