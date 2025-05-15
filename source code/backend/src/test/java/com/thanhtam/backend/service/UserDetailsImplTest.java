package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.ultilities.ERole;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Unit Test for UserDetailsImpl
 * 
 * This test class verifies the functionality of the UserDetailsImpl implementation.
 * UserDetailsImpl is a core security class that implements Spring Security's UserDetails
 * interface and provides user authentication and authorization information.
 * 
 * The tests focus on:
 * - Proper construction of UserDetailsImpl objects
 * - Correct mapping of User entities to UserDetailsImpl objects
 * - Accurate representation of user authorities (roles)
 * - Proper implementation of UserDetails interface methods
 * - Correct equals and hashCode behavior
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class UserDetailsImplTest {

    // Test data
    private User testUser;
    private Role studentRole;
    private Role lecturerRole;
    private Role adminRole;
    private Set<Role> roles;

    /**
     * Setup method that runs before each test.
     * Creates test data including a user with various roles.
     */
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

        // Create a set of roles
        roles = new HashSet<>();
        roles.add(studentRole);
        
        // Create a test user with roles
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRoles(roles);
    }

    /**
     * Test Case ID: TC01
     * Test Objective: Verify that UserDetailsImpl.build correctly maps User entity to UserDetailsImpl
     * Input: User entity with ID, username, email, password, and single role
     * Expected Output: UserDetailsImpl with matching field values and mapped authority
     */
    @Test
    public void build_ShouldCreateUserDetailsImplFromUser() {
        // Act - Build UserDetailsImpl from User entity
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Assert - Verify mapping is correct
        assertNotNull("UserDetails should not be null", userDetails);
        assertEquals("ID should match", testUser.getId(), userDetails.getId());
        assertEquals("Username should match", testUser.getUsername(), userDetails.getUsername());
        assertEquals("Email should match", testUser.getEmail(), userDetails.getEmail());
        assertEquals("Password should match", testUser.getPassword(), userDetails.getPassword());
        
        // Check authorities
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertNotNull("Authorities should not be null", authorities);
        assertEquals("Should have exactly one authority", 1, authorities.size());
        assertTrue("Should have ROLE_STUDENT authority", 
                authorities.contains(new SimpleGrantedAuthority(ERole.ROLE_STUDENT.name())));
    }

    /**
     * Test Case ID: TC02
     * Test Objective: Verify that UserDetailsImpl.build correctly maps multiple roles to authorities
     * Input: User entity with multiple roles
     * Expected Output: UserDetailsImpl with all roles mapped to authorities
     */
    @Test
    public void build_WithMultipleRoles_ShouldCreateUserDetailsImplWithAllRoles() {
        // Arrange - Set multiple roles for the user
        Set<Role> multipleRoles = new HashSet<>();
        multipleRoles.add(studentRole);
        multipleRoles.add(lecturerRole);
        multipleRoles.add(adminRole);
        testUser.setRoles(multipleRoles);

        // Act - Build UserDetailsImpl from User entity
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Assert - Verify all roles are mapped to authorities
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertNotNull("Authorities should not be null", authorities);
        assertEquals("Should have exactly three authorities", 3, authorities.size());
        assertTrue("Should have ROLE_STUDENT authority", 
                authorities.contains(new SimpleGrantedAuthority(ERole.ROLE_STUDENT.name())));
        assertTrue("Should have ROLE_LECTURER authority", 
                authorities.contains(new SimpleGrantedAuthority(ERole.ROLE_LECTURER.name())));
        assertTrue("Should have ROLE_ADMIN authority", 
                authorities.contains(new SimpleGrantedAuthority(ERole.ROLE_ADMIN.name())));
    }

    /**
     * Test Case ID: TC03
     * Test Objective: Verify that build method correctly handles empty roles
     * Input: User entity with empty roles set
     * Expected Output: UserDetailsImpl with empty authorities collection
     */
    @Test
    public void build_WithEmptyRoles_ShouldCreateEmptyAuthorities() {
        // Arrange - Set empty roles for the user
        testUser.setRoles(new HashSet<>());
        
        // Act - Build UserDetailsImpl from User entity
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);
        
        // Assert - Verify authorities are empty
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertNotNull("Authorities should not be null", authorities);
        assertEquals("Authorities should be empty", 0, authorities.size());
    }

    /**
     * Test Case ID: TC04
     * Test Objective: Verify that getAuthorities returns the correct authorities
     * Input: UserDetailsImpl with authorities
     * Expected Output: Collection of authorities matching the input
     */
    @Test
    public void getAuthorities_ShouldReturnAuthorities() {
        // Arrange - Build UserDetailsImpl from User entity
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Act - Get authorities
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // Assert - Verify authorities match
        assertNotNull("Authorities should not be null", authorities);
        assertEquals("Should have exactly one authority", 1, authorities.size());
        assertTrue("Should have ROLE_STUDENT authority", 
                authorities.contains(new SimpleGrantedAuthority(ERole.ROLE_STUDENT.name())));
    }

    /**
     * Test Case ID: TC05
     * Test Objective: Verify that getId returns the correct ID
     * Input: UserDetailsImpl with ID
     * Expected Output: ID matching the input
     */
    @Test
    public void getId_ShouldReturnUserId() {
        // Arrange - Build UserDetailsImpl from User entity
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Act - Get ID
        Long id = userDetails.getId();

        // Assert - Verify ID matches
        assertEquals("ID should match", testUser.getId(), id);
    }

    /**
     * Test Case ID: TC06
     * Test Objective: Verify that getEmail returns the correct email
     * Input: UserDetailsImpl with email
     * Expected Output: Email matching the input
     */
    @Test
    public void getEmail_ShouldReturnUserEmail() {
        // Arrange - Build UserDetailsImpl from User entity
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Act - Get email
        String email = userDetails.getEmail();

        // Assert - Verify email matches
        assertEquals("Email should match", testUser.getEmail(), email);
    }

    /**
     * Test Case ID: TC07
     * Test Objective: Verify that getPassword returns the correct password
     * Input: UserDetailsImpl with password
     * Expected Output: Password matching the input
     */
    @Test
    public void getPassword_ShouldReturnUserPassword() {
        // Arrange - Build UserDetailsImpl from User entity
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Act - Get password
        String password = userDetails.getPassword();

        // Assert - Verify password matches
        assertEquals("Password should match", testUser.getPassword(), password);
    }

    /**
     * Test Case ID: TC08
     * Test Objective: Verify that getUsername returns the correct username
     * Input: UserDetailsImpl with username
     * Expected Output: Username matching the input
     */
    @Test
    public void getUsername_ShouldReturnUserUsername() {
        // Arrange - Build UserDetailsImpl from User entity
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Act - Get username
        String username = userDetails.getUsername();

        // Assert - Verify username matches
        assertEquals("Username should match", testUser.getUsername(), username);
    }

    /**
     * Test Case ID: TC09
     * Test Objective: Verify that isAccountNonExpired always returns true
     * Input: UserDetailsImpl
     * Expected Output: true
     */
    @Test
    public void isAccountNonExpired_ShouldReturnTrue() {
        // Arrange - Build UserDetailsImpl from User entity
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Act & Assert - Verify method returns true
        assertTrue("Account should be non-expired", userDetails.isAccountNonExpired());
    }

    /**
     * Test Case ID: TC10
     * Test Objective: Verify that isAccountNonLocked always returns true
     * Input: UserDetailsImpl
     * Expected Output: true
     */
    @Test
    public void isAccountNonLocked_ShouldReturnTrue() {
        // Arrange - Build UserDetailsImpl from User entity
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Act & Assert - Verify method returns true
        assertTrue("Account should be non-locked", userDetails.isAccountNonLocked());
    }

    /**
     * Test Case ID: TC11
     * Test Objective: Verify that isCredentialsNonExpired always returns true
     * Input: UserDetailsImpl
     * Expected Output: true
     */
    @Test
    public void isCredentialsNonExpired_ShouldReturnTrue() {
        // Arrange - Build UserDetailsImpl from User entity
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Act & Assert - Verify method returns true
        assertTrue("Credentials should be non-expired", userDetails.isCredentialsNonExpired());
    }

    /**
     * Test Case ID: TC12
     * Test Objective: Verify that isEnabled always returns true
     * Input: UserDetailsImpl
     * Expected Output: true
     */
    @Test
    public void isEnabled_ShouldReturnTrue() {
        // Arrange - Build UserDetailsImpl from User entity
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Act & Assert - Verify method returns true
        assertTrue("Account should be enabled", userDetails.isEnabled());
    }

    /**
     * Test Case ID: TC13
     * Test Objective: Verify that equals returns true for the same object
     * Input: UserDetailsImpl compared with itself
     * Expected Output: true
     */
    @Test
    public void equals_SameObject_ShouldReturnTrue() {
        // Arrange - Build UserDetailsImpl from User entity
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Act & Assert - Verify equals returns true for same object
        assertTrue("Object should equal itself", userDetails.equals(userDetails));
    }

    /**
     * Test Case ID: TC14
     * Test Objective: Verify that equals returns false for null
     * Input: UserDetailsImpl compared with null
     * Expected Output: false
     */
    @Test
    public void equals_NullObject_ShouldReturnFalse() {
        // Arrange - Build UserDetailsImpl from User entity
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Act & Assert - Verify equals returns false for null
        assertFalse("Object should not equal null", userDetails.equals(null));
    }

    /**
     * Test Case ID: TC15
     * Test Objective: Verify that equals returns false for different class
     * Input: UserDetailsImpl compared with different class
     * Expected Output: false
     */
    @Test
    public void equals_DifferentClass_ShouldReturnFalse() {
        // Arrange - Build UserDetailsImpl from User entity
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Act & Assert - Verify equals returns false for different class
        assertFalse("Object should not equal different class", userDetails.equals("Not a UserDetailsImpl"));
    }

    /**
     * Test Case ID: TC16
     * Test Objective: Verify that equals returns true for objects with same ID
     * Input: Two UserDetailsImpl objects with same ID but different other fields
     * Expected Output: true
     */
    @Test
    public void equals_SameId_ShouldReturnTrue() {
        // Arrange - Build first UserDetailsImpl from test user
        UserDetailsImpl userDetails1 = UserDetailsImpl.build(testUser);
        
        // Create a different user with the same ID
        User user2 = new User();
        user2.setId(1L); // Same ID as testUser
        user2.setUsername("differentuser");
        user2.setEmail("different@example.com");
        user2.setPassword("differentpassword");
        Set<Role> roles = new HashSet<>();
        roles.add(studentRole);
        user2.setRoles(roles);
        
        // Build second UserDetailsImpl
        UserDetailsImpl userDetails2 = UserDetailsImpl.build(user2);

        // Act & Assert - Verify equals returns true for same ID
        assertTrue("Objects with same ID should be equal", userDetails1.equals(userDetails2));
        assertTrue("Objects with same ID should be equal (symmetric)", userDetails2.equals(userDetails1));
    }

    /**
     * Test Case ID: TC17
     * Test Objective: Verify that equals returns false for objects with different IDs
     * Input: Two UserDetailsImpl objects with different IDs
     * Expected Output: false
     */
    @Test
    public void equals_DifferentId_ShouldReturnFalse() {
        // Arrange - Build first UserDetailsImpl from test user
        UserDetailsImpl userDetails1 = UserDetailsImpl.build(testUser);
        
        // Create a different user with a different ID
        User user2 = new User();
        user2.setId(2L); // Different ID from testUser
        user2.setUsername("testuser");
        user2.setEmail("test@example.com");
        user2.setPassword("password");
        Set<Role> roles = new HashSet<>();
        roles.add(studentRole);
        user2.setRoles(roles);
        
        // Build second UserDetailsImpl
        UserDetailsImpl userDetails2 = UserDetailsImpl.build(user2);

        // Act & Assert - Verify equals returns false for different IDs
        assertFalse("Objects with different IDs should not be equal", userDetails1.equals(userDetails2));
        assertFalse("Objects with different IDs should not be equal (symmetric)", userDetails2.equals(userDetails1));
    }
    
    /**
     * Test Case ID: TC18
     * Test Objective: Verify that equals handles null ID correctly
     * Input: Two UserDetailsImpl objects, one with null ID
     * Expected Output: false
     */
    @Test
    public void equals_NullId_ShouldReturnFalse() {
        // Arrange - Create user with null ID
        User userWithNullId = new User();
        userWithNullId.setId(null);
        userWithNullId.setUsername("nulliduser");
        userWithNullId.setEmail("nullid@example.com");
        userWithNullId.setPassword("password");
        userWithNullId.setRoles(roles);
        
        // Build UserDetailsImpl objects
        UserDetailsImpl userDetails1 = UserDetailsImpl.build(userWithNullId);
        UserDetailsImpl userDetails2 = UserDetailsImpl.build(testUser);
        
        // Act & Assert - Verify equals handles null ID correctly
        assertFalse("Object with null ID should not equal object with non-null ID", 
                userDetails1.equals(userDetails2));
        assertFalse("Object with non-null ID should not equal object with null ID", 
                userDetails2.equals(userDetails1));
    }
    
    /**
     * Test Case ID: TC19
     * Test Objective: Verify that equals returns true for objects with both null IDs
     * Input: Two UserDetailsImpl objects with null IDs
     * Expected Output: true
     */
    @Test
    public void equals_BothNullIds_ShouldReturnTrue() {
        // Arrange - Create users with null IDs
        User user1 = new User();
        user1.setId(null);
        user1.setUsername("nullid1");
        user1.setEmail("nullid1@example.com");
        user1.setPassword("password1");
        user1.setRoles(roles);
        
        User user2 = new User();
        user2.setId(null);
        user2.setUsername("nullid2");
        user2.setEmail("nullid2@example.com");
        user2.setPassword("password2");
        user2.setRoles(roles);
        
        // Build UserDetailsImpl objects
        UserDetailsImpl userDetails1 = UserDetailsImpl.build(user1);
        UserDetailsImpl userDetails2 = UserDetailsImpl.build(user2);
        
        // Act & Assert - Verify equals returns true for both null IDs
        assertTrue("Objects with both null IDs should be equal", userDetails1.equals(userDetails2));
        assertTrue("Objects with both null IDs should be equal (symmetric)", userDetails2.equals(userDetails1));
    }

}
