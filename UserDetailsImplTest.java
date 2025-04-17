package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.ultilities.ERole;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

@SpringBootTest

public class UserDetailsImplTest {

    private User testUser;
    private Role studentRole;
    private Role lecturerRole;
    private Role adminRole;

    @Before
    public void setUp() {
        // Create roles
        studentRole = new Role();
        studentRole.setId(1L);
        studentRole.setName(ERole.ROLE_STUDENT);

        lecturerRole = new Role();
        lecturerRole.setId(2L);
        lecturerRole.setName(ERole.ROLE_LECTURER);

        adminRole = new Role();
        adminRole.setId(3L);
        adminRole.setName(ERole.ROLE_ADMIN);

        // Create a test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        
        // Set roles for the user
        Set<Role> roles = new HashSet<>();
        roles.add(studentRole);
        testUser.setRoles(roles);
    }

    //TC01
    @Test
    public void build_ShouldCreateUserDetailsImplFromUser() {
        // Act
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Assert
        assertNotNull(userDetails);
        assertEquals(testUser.getId(), userDetails.getId());
        assertEquals(testUser.getUsername(), userDetails.getUsername());
        assertEquals(testUser.getEmail(), userDetails.getEmail());
        assertEquals(testUser.getPassword(), userDetails.getPassword());
        
        // Check authorities
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority(ERole.ROLE_STUDENT.name())));
    }

    //TC02
    @Test
    public void build_WithMultipleRoles_ShouldCreateUserDetailsImplWithAllRoles() {
        // Arrange
        Set<Role> roles = new HashSet<>();
        roles.add(studentRole);
        roles.add(lecturerRole);
        roles.add(adminRole);
        testUser.setRoles(roles);

        // Act
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Assert
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertNotNull(authorities);
        assertEquals(3, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority(ERole.ROLE_STUDENT.name())));
        assertTrue(authorities.contains(new SimpleGrantedAuthority(ERole.ROLE_LECTURER.name())));
        assertTrue(authorities.contains(new SimpleGrantedAuthority(ERole.ROLE_ADMIN.name())));
    }

    //TC03
    @Test
    public void getAuthorities_ShouldReturnAuthorities() {
        // Arrange
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Act
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // Assert
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority(ERole.ROLE_STUDENT.name())));
    }

    //TC04
    @Test
    public void getId_ShouldReturnUserId() {
        // Arrange
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Act
        Long id = userDetails.getId();

        // Assert
        assertEquals(testUser.getId(), id);
    }

    //TC05
    @Test
    public void getEmail_ShouldReturnUserEmail() {
        // Arrange
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Act
        String email = userDetails.getEmail();

        // Assert
        assertEquals(testUser.getEmail(), email);
    }

    //TC06
    @Test
    public void getPassword_ShouldReturnUserPassword() {
        // Arrange
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Act
        String password = userDetails.getPassword();

        // Assert
        assertEquals(testUser.getPassword(), password);
    }

    //TC07
    @Test
    public void getUsername_ShouldReturnUserUsername() {
        // Arrange
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Act
        String username = userDetails.getUsername();

        // Assert
        assertEquals(testUser.getUsername(), username);
    }

    //TC08
    @Test
    public void isAccountNonExpired_ShouldReturnTrue() {
        // Arrange
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Act & Assert
        assertTrue(userDetails.isAccountNonExpired());
    }

    //TC09
    @Test
    public void isAccountNonLocked_ShouldReturnTrue() {
        // Arrange
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Act & Assert
        assertTrue(userDetails.isAccountNonLocked());
    }

    //TC10
    @Test
    public void isCredentialsNonExpired_ShouldReturnTrue() {
        // Arrange
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Act & Assert
        assertTrue(userDetails.isCredentialsNonExpired());
    }

    //TC11
    @Test
    public void isEnabled_ShouldReturnTrue() {
        // Arrange
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Act & Assert
        assertTrue(userDetails.isEnabled());
    }

    //TC12
    @Test
    public void equals_SameObject_ShouldReturnTrue() {
        // Arrange
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Act & Assert
        assertTrue(userDetails.equals(userDetails));
    }

    //TC13
    @Test
    public void equals_NullObject_ShouldReturnFalse() {
        // Arrange
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Act & Assert
        assertFalse(userDetails.equals(null));
    }

    //TC14
    @Test
    public void equals_DifferentClass_ShouldReturnFalse() {
        // Arrange
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Act & Assert
        assertFalse(userDetails.equals("Not a UserDetailsImpl"));
    }

    //TC15
    @Test
    public void equals_SameId_ShouldReturnTrue() {
        // Arrange
        UserDetailsImpl userDetails1 = UserDetailsImpl.build(testUser);
        
        User user2 = new User();
        user2.setId(1L); // Same ID as testUser
        user2.setUsername("differentuser");
        user2.setEmail("different@example.com");
        user2.setPassword("differentpassword");
        Set<Role> roles = new HashSet<>();
        roles.add(studentRole);
        user2.setRoles(roles);
        
        UserDetailsImpl userDetails2 = UserDetailsImpl.build(user2);

        // Act & Assert
        assertTrue(userDetails1.equals(userDetails2));
    }

    //TC16
    @Test
    public void equals_DifferentId_ShouldReturnFalse() {
        // Arrange
        UserDetailsImpl userDetails1 = UserDetailsImpl.build(testUser);
        
        User user2 = new User();
        user2.setId(2L); // Different ID from testUser
        user2.setUsername("testuser");
        user2.setEmail("test@example.com");
        user2.setPassword("password");
        Set<Role> roles = new HashSet<>();
        roles.add(studentRole);
        user2.setRoles(roles);
        
        UserDetailsImpl userDetails2 = UserDetailsImpl.build(user2);

        // Act & Assert
        assertFalse(userDetails1.equals(userDetails2));
    }
}
