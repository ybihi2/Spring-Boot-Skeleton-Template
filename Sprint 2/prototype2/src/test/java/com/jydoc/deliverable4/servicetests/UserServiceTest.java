package com.jydoc.deliverable4.servicetests;

import com.jydoc.deliverable4.model.auth.AuthorityModel;
import com.jydoc.deliverable4.model.UserModel;
import com.jydoc.deliverable4.repositories.AuthorityRepository;
import com.jydoc.deliverable4.repositories.UserRepository;
import com.jydoc.deliverable4.services.UserService;
import com.jydoc.deliverable4.services.UserValidationHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthorityRepository authorityRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserValidationHelper validationHelper;

    @InjectMocks
    private UserService userService;

    private UserModel testUser;
    private AuthorityModel testAuthority;

    @BeforeEach
    void setUp() {
        testUser = UserModel.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password("encodedPassword")
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .authorities(new HashSet<>())
                .build();

        testAuthority = AuthorityModel.builder()
                .authority("ROLE_USER")
                .users(new HashSet<>())
                .build();
        testUser.addAuthority(testAuthority);
    }

    /* ======================== User Management Tests ======================== */

    @Test
    void findActiveUser_ValidUsername_ReturnsUser() {
        when(userRepository.findByUsernameOrEmail("testuser"))
                .thenReturn(Optional.of(testUser));

        Optional<UserModel> result = userService.findActiveUser("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository).findByUsernameOrEmail("testuser");
    }

    @Test
    void findActiveUser_DisabledUser_ReturnsEmpty() {
        testUser.setEnabled(false);
        when(userRepository.findByUsernameOrEmail("testuser"))
                .thenReturn(Optional.of(testUser));

        Optional<UserModel> result = userService.findActiveUser("testuser");

        assertFalse(result.isPresent());
    }

    @Test
    void getUserCount_ReturnsCount() {
        when(userRepository.count()).thenReturn(5L);

        long count = userService.getUserCount();

        assertEquals(5L, count);
        verify(userRepository).count();
    }

    @Test
    void existsById_UserExists_ReturnsTrue() {
        when(userRepository.existsById(1L)).thenReturn(true);

        boolean exists = userService.existsById(1L);

        assertTrue(exists);
    }

    @Test
    void getAllUsers_ReturnsUserList() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        List<UserModel> users = userService.getAllUsers();

        assertEquals(1, users.size());
        assertEquals("testuser", users.get(0).getUsername());
    }

    @Test
    void getUserById_ValidId_ReturnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<UserModel> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void updateUser_SavesUser() {
        userService.updateUser(testUser);

        verify(userRepository).save(testUser);
    }

    @Test
    void deleteUser_DeletesById() {
        // Mock existsById() to return true (if your service checks existence)
        when(userRepository.existsById(1L)).thenReturn(true);

        // Mock deleteById to do nothing
        doNothing().when(userRepository).deleteById(1L);

        // Execute
        userService.deleteUser(1L);

        // Verify deletion was called
        verify(userRepository).deleteById(1L);
    }

    @Test
    void findByUsername_ValidUsername_ReturnsUser() {
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));

        UserModel result = userService.findByUsername("testuser");

        assertEquals("testuser", result.getUsername());
    }

    @Test
    void findByUsername_InvalidUsername_ThrowsException() {
        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            userService.findByUsername("unknown");
        });
    }
}