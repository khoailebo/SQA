package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.Intake;
import com.thanhtam.backend.entity.Profile;
import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.repository.IntakeRepository;
import com.thanhtam.backend.repository.ProfileRepository;
import com.thanhtam.backend.repository.RoleRepository;
import com.thanhtam.backend.repository.UserRepository;
import com.thanhtam.backend.ultilities.ERole;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit Test for UserService
 * 
 * This test class verifies the functionality of the UserService implementation.
 * It tests various methods including user creation, retrieval, and updates
 * to ensure all branches are covered.
 * 
 * Test environment:
 * - Uses Spring Boot test context
 * - Runs with transactional boundaries
 * - Performs rollback after each test
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
@Rollback // Ensures database changes are rolled back after each test
@Sql(scripts = "/cleanup-user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class UserServiceTest {

    // Service under test
    @Autowired
    private UserService userService;
    
    // Dependencies
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private ProfileRepository profileRepository;
    
    @Autowired
    private IntakeRepository intakeRepository;

    // Test data
    private User testUser;
    private Profile testProfile;
    private Role studentRole;
    private Role lecturerRole;
    private Role adminRole;
    private Intake testIntake;

    /**
     * Setup method that runs before each test.
     * Creates test data including profile, user, roles, and intake.
     */
    @Before
    public void setUp() {
        // Create test profile
        testProfile = new Profile();
        testProfile.setFirstName("Test");
        testProfile.setLastName("User");
        testProfile.setImage("test-image.jpg");
        profileRepository.save(testProfile);

        // Create a test user with a unique username
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setDeleted(false);
        testUser.setProfile(testProfile);
        userRepository.save(testUser);

        // Get or create roles
        studentRole = roleService.findByName(ERole.ROLE_STUDENT)
                .orElseGet(() -> roleRepository.save(new Role(null, ERole.ROLE_STUDENT)));

        lecturerRole = roleService.findByName(ERole.ROLE_LECTURER)
                .orElseGet(() -> roleRepository.save(new Role(null, ERole.ROLE_LECTURER)));

        adminRole = roleService.findByName(ERole.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(null, ERole.ROLE_ADMIN)));

        // Create test intake
        testIntake = new Intake();
        testIntake.setName("Test Intake");
        testIntake.setIntakeCode("TI" + System.currentTimeMillis());
        intakeRepository.save(testIntake);
    }
    
    /**
     * Teardown method that runs after each test.
     * Cleans up any test data and security context.
     */
    @After
    public void tearDown() {
        // Clear security context to prevent test interference
        SecurityContextHolder.clearContext();
    }

    /**
     * Test Case ID: TC01
     * Test Objective: Verify that existsByUsername returns false for non-existent username
     * Input: Non-existent username
     * Expected Output: False
     */
    @Test
    public void existsByUsername_NotExists_ShouldReturnFalse() {
        // Arrange - Use a username that doesn't exist
        String nonExistentUsername = "nonexistent" + System.currentTimeMillis();
        
        // Act - Check if username exists
        boolean exists = userService.existsByUsername(nonExistentUsername);
        
        // Assert - Should return false
        assertFalse("Should return false for non-existent username", exists);
        
        // Verify database state
        Optional<User> user = userRepository.findByUsername(nonExistentUsername);
        assertFalse("User should not exist in database", user.isPresent());
    }
    
    /**
     * Test Case ID: TC02
     * Test Objective: Verify that existsByUsername returns true for existing username
     * Input: Existing username
     * Expected Output: True
     */
    @Test
    public void existsByUsername_Exists_ShouldReturnTrue() {
        // Arrange - Use the test user's username
        
        // Act - Check if username exists
        boolean exists = userService.existsByUsername(testUser.getUsername());
        
        // Assert - Should return true
        assertTrue("Should return true for existing username", exists);
        
        // Verify database state
        Optional<User> user = userRepository.findByUsername(testUser.getUsername());
        assertTrue("User should exist in database", user.isPresent());
    }
    
    /**
     * Test Case ID: TC03
     * Test Objective: Verify that a user can be created successfully
     * Input: User with valid data
     * Expected Output: Saved user with generated ID and matching attributes
     */
    @Test
    public void createUser_Success_ShouldCreateUserWithCorrectData() {
        // Arrange - Create a new user with valid data
        User newUser = new User();
        newUser.setUsername("newuser" + System.currentTimeMillis());
        newUser.setEmail("new" + System.currentTimeMillis() + "@example.com");
        newUser.setProfile(testProfile);
        
        // Act - Call the service method to create the user
        User savedUser = userService.createUser(newUser);
        
        // Assert - Verify the user was saved correctly
        assertNotNull("Saved user should not be null", savedUser);
        assertNotNull("Saved user should have an ID", savedUser.getId());
        assertEquals("Username should match", newUser.getUsername(), savedUser.getUsername());
        assertEquals("Email should match", newUser.getEmail(), savedUser.getEmail());
        assertNotNull("Roles should not be null", savedUser.getRoles());
        assertTrue("User should have at least one role", savedUser.getRoles().size() > 0);
        
        // Verify database state
        Optional<User> userInDb = userRepository.findById(savedUser.getId());
        assertTrue("User should exist in database", userInDb.isPresent());
        assertEquals("Username in database should match", newUser.getUsername(), userInDb.get().getUsername());
    }

    /**
     * Test Case ID: TC04
     * Test Objective: Verify that a user created with no roles is assigned the student role
     * Input: User with null roles
     * Expected Output: User with student role assigned
     */
    @Test
    public void createUser_WithNoRoles_ShouldAssignStudentRole() {
        // Arrange - Create a user with no roles
        User newUser = new User();
        newUser.setUsername("noroles" + System.currentTimeMillis());
        newUser.setEmail("noroles" + System.currentTimeMillis() + "@example.com");
        newUser.setProfile(testProfile);
        newUser.setRoles(null);
        
        // Act - Call the service method to create the user
        User savedUser = userService.createUser(newUser);
        
        // Assert - Verify student role was assigned
        assertNotNull("Saved user should not be null", savedUser);
        assertNotNull("Roles should not be null", savedUser.getRoles());
        assertEquals("Should have exactly one role", 1, savedUser.getRoles().size());
        assertTrue("Should have student role", savedUser.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_STUDENT));
        
        // Verify database state
        Optional<User> userInDb = userRepository.findById(savedUser.getId());
        assertTrue("User should exist in database", userInDb.isPresent());
        assertEquals("Should have one role in database", 1, userInDb.get().getRoles().size());
    }
    
    /**
     * Test Case ID: TC05
     * Test Objective: Verify that a user created with student role keeps only that role
     * Input: User with student role
     * Expected Output: User with only student role assigned
     */
    @Test
    public void createUser_WithStudentRole_ShouldKeepStudentRole() {
        // Arrange - Create a user with student role
        User newUser = new User();
        newUser.setUsername("student" + System.currentTimeMillis());
        newUser.setEmail("student" + System.currentTimeMillis() + "@example.com");
        newUser.setProfile(testProfile);
        
        Set<Role> roles = new HashSet<>();
        roles.add(studentRole);
        newUser.setRoles(roles);
        
        // Act - Call the service method to create the user
        User savedUser = userService.createUser(newUser);
        
        // Assert - Verify only student role is assigned
        assertNotNull("Saved user should not be null", savedUser);
        assertNotNull("Roles should not be null", savedUser.getRoles());
        assertEquals("Should have exactly one role", 1, savedUser.getRoles().size());
        assertTrue("Should have student role", savedUser.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_STUDENT));
        
        // Verify database state
        Optional<User> userInDb = userRepository.findById(savedUser.getId());
        assertTrue("User should exist in database", userInDb.isPresent());
        assertEquals("Should have one role in database", 1, userInDb.get().getRoles().size());
    }
    
    /**
     * Test Case ID: TC06
     * Test Objective: Verify that a user created with lecturer role is assigned both lecturer and student roles
     * Input: User with lecturer role
     * Expected Output: User with both lecturer and student roles
     */
    @Test
    public void createUser_WithLecturerRole_ShouldAssignLecturerAndStudentRoles() {
        // Arrange - Create a user with lecturer role
        User newUser = new User();
        newUser.setUsername("lecturer" + System.currentTimeMillis());
        newUser.setEmail("lecturer" + System.currentTimeMillis() + "@example.com");
        newUser.setProfile(testProfile);
        
        Set<Role> roles = new HashSet<>();
        roles.add(lecturerRole);
        newUser.setRoles(roles);
        
        // Act - Call the service method to create the user
        User savedUser = userService.createUser(newUser);
        
        // Assert - Verify both lecturer and student roles are assigned
        assertNotNull("Saved user should not be null", savedUser);
        assertNotNull("Roles should not be null", savedUser.getRoles());
        assertEquals("Should have exactly two roles", 2, savedUser.getRoles().size());
        assertTrue("Should have lecturer role", savedUser.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_LECTURER));
        assertTrue("Should have student role", savedUser.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_STUDENT));
        
        // Verify database state
        Optional<User> userInDb = userRepository.findById(savedUser.getId());
        assertTrue("User should exist in database", userInDb.isPresent());
        assertEquals("Should have two roles in database", 2, userInDb.get().getRoles().size());
    }
    
    /**
     * Test Case ID: TC07
     * Test Objective: Verify that a user created with admin role is assigned all roles
     * Input: User with admin role
     * Expected Output: User with admin, lecturer, and student roles
     */
    @Test
    public void createUser_WithAdminRole_ShouldAssignAllRoles() {
        // Arrange - Create a user with admin role
        User newUser = new User();
        newUser.setUsername("admin" + System.currentTimeMillis());
        newUser.setEmail("admin" + System.currentTimeMillis() + "@example.com");
        newUser.setProfile(testProfile);
        
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        newUser.setRoles(roles);
        
        // Act - Call the service method to create the user
        User savedUser = userService.createUser(newUser);
        
        // Assert - Verify all roles are assigned
        assertNotNull("Saved user should not be null", savedUser);
        assertNotNull("Roles should not be null", savedUser.getRoles());
        assertEquals("Should have exactly three roles", 3, savedUser.getRoles().size());
        assertTrue("Should have admin role", savedUser.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_ADMIN));
        assertTrue("Should have lecturer role", savedUser.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_LECTURER));
        assertTrue("Should have student role", savedUser.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_STUDENT));
        
        // Verify database state
        Optional<User> userInDb = userRepository.findById(savedUser.getId());
        assertTrue("User should exist in database", userInDb.isPresent());
        assertEquals("Should have three roles in database", 3, userInDb.get().getRoles().size());
    }
    
    /**
     * Test Case ID: TC08
     * Test Objective: Verify that a user created with multiple roles is assigned all roles
     * Input: User with admin and lecturer roles
     * Expected Output: User with admin, lecturer, and student roles
     */
    @Test
    public void createUser_WithMultipleRoles_ShouldAssignAllRoles() {
        // Arrange - Create a user with multiple roles
        User newUser = new User();
        newUser.setUsername("multirole" + System.currentTimeMillis());
        newUser.setEmail("multirole" + System.currentTimeMillis() + "@example.com");
        newUser.setProfile(testProfile);
        
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        roles.add(lecturerRole);
        newUser.setRoles(roles);
        
        // Act - Call the service method to create the user
        User savedUser = userService.createUser(newUser);
        
        // Assert - Verify all roles are assigned
        assertNotNull("Saved user should not be null", savedUser);
        assertNotNull("Roles should not be null", savedUser.getRoles());
        assertEquals("Should have exactly three roles", 3, savedUser.getRoles().size());
        assertTrue("Should have admin role", savedUser.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_ADMIN));
        assertTrue("Should have lecturer role", savedUser.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_LECTURER));
        assertTrue("Should have student role", savedUser.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_STUDENT));
        
        // Verify database state
        Optional<User> userInDb = userRepository.findById(savedUser.getId());
        assertTrue("User should exist in database", userInDb.isPresent());
        assertEquals("Should have three roles in database", 3, userInDb.get().getRoles().size());
    }
    
    /**
     * Test Case ID: TC09
     * Test Objective: Verify that a user created with an intake has the intake assigned
     * Input: User with intake
     * Expected Output: User with intake assigned
     */
    @Test
    public void createUser_WithIntake_ShouldCopyIntake() {
        // Arrange - Create a user with intake
        User newUser = new User();
        newUser.setUsername("withintake" + System.currentTimeMillis());
        newUser.setEmail("withintake" + System.currentTimeMillis() + "@example.com");
        newUser.setProfile(testProfile);
        newUser.setIntake(testIntake);
        
        // Act - Call the service method to create the user
        User savedUser = userService.createUser(newUser);
        
        // Assert - Verify intake is assigned
        assertNotNull("Saved user should not be null", savedUser);
        assertNotNull("Intake should not be null", savedUser.getIntake());
        assertEquals("Intake name should match", testIntake.getName(), savedUser.getIntake().getName());
        assertEquals("Intake code should match", testIntake.getIntakeCode(), savedUser.getIntake().getIntakeCode());
        
        // Verify database state
        Optional<User> userInDb = userRepository.findById(savedUser.getId());
        assertTrue("User should exist in database", userInDb.isPresent());
        assertNotNull("Intake should not be null in database", userInDb.get().getIntake());
        assertEquals("Intake ID should match in database", testIntake.getId(), userInDb.get().getIntake().getId());
    }
    
    /**
     * Test Case ID: TC10
     * Test Objective: Verify that a user created without an intake has no intake assigned
     * Input: User with null intake
     * Expected Output: User with null intake
     */
    @Test
    public void createUser_WithoutIntake_ShouldNotSetIntake() {
        // Arrange - Create a user without intake
        User newUser = new User();
        newUser.setUsername("nointake" + System.currentTimeMillis());
        newUser.setEmail("nointake" + System.currentTimeMillis() + "@example.com");
        newUser.setProfile(testProfile);
        newUser.setIntake(null);
        
        // Act - Call the service method to create the user
        User savedUser = userService.createUser(newUser);
        
        // Assert - Verify intake is null
        assertNotNull("Saved user should not be null", savedUser);
        assertNull("Intake should be null", savedUser.getIntake());
        
        // Verify database state
        Optional<User> userInDb = userRepository.findById(savedUser.getId());
        assertTrue("User should exist in database", userInDb.isPresent());
        assertNull("Intake should be null in database", userInDb.get().getIntake());
    }
    
    /**
     * Test Case ID: TC11
     * Test Objective: Verify that a user's password is encoded during creation
     * Input: User with password
     * Expected Output: User with encoded password
     */
    @Test
    public void createUser_ShouldEncodePassword() {
        // Arrange - Create a user
        User newUser = new User();
        newUser.setUsername("password" + System.currentTimeMillis());
        newUser.setEmail("password" + System.currentTimeMillis() + "@example.com");
        newUser.setProfile(testProfile);
        String originalUsername = newUser.getUsername();
        
        // Act - Call the service method to create the user
        User savedUser = userService.createUser(newUser);
        
        // Assert - Verify password is encoded
        assertNotNull("Saved user should not be null", savedUser);
        assertNotEquals("Password should be encoded", originalUsername, savedUser.getPassword());
        // The createUser method uses the username as the password to encode
        assertTrue("Password should match when decoded", 
                passwordEncoder.matches(originalUsername, savedUser.getPassword()));
        
        // Verify database state
        Optional<User> userInDb = userRepository.findById(savedUser.getId());
        assertTrue("User should exist in database", userInDb.isPresent());
        assertNotEquals("Password should be encoded in database", 
                originalUsername, userInDb.get().getPassword());
    }
    
    /**
     * Test Case ID: TC12
     * Test Objective: Verify that a user's profile is copied during creation
     * Input: User with profile
     * Expected Output: User with copied profile
     */
    @Test
    public void createUser_ShouldCopyProfile() {
        // Arrange - Create a user with profile
        User newUser = new User();
        newUser.setUsername("profile" + System.currentTimeMillis());
        newUser.setEmail("profile" + System.currentTimeMillis() + "@example.com");
        newUser.setProfile(testProfile);
        
        // Act - Call the service method to create the user
        User savedUser = userService.createUser(newUser);
        
        // Assert - Verify profile is copied
        assertNotNull("Saved user should not be null", savedUser);
        assertNotNull("Profile should not be null", savedUser.getProfile());
        assertEquals("First name should match", testProfile.getFirstName(), savedUser.getProfile().getFirstName());
        assertEquals("Last name should match", testProfile.getLastName(), savedUser.getProfile().getLastName());
        assertEquals("Image should match", testProfile.getImage(), savedUser.getProfile().getImage());
        
        // Verify database state
        Optional<User> userInDb = userRepository.findById(savedUser.getId());
        assertTrue("User should exist in database", userInDb.isPresent());
        assertNotNull("Profile should not be null in database", userInDb.get().getProfile());
        assertEquals("Profile first name should match in database", 
                testProfile.getFirstName(), userInDb.get().getProfile().getFirstName());
    }
    
    /**
     * Test Case ID: TC13
     * Test Objective: Verify that getUserByUsername returns a user when it exists
     * Input: Existing username
     * Expected Output: Optional containing the user
     */
    @Test
    public void getUserByUsername_UserExists_ShouldReturnUser() {
        // Act - Call the service method to get user by username
        Optional<User> foundUser = userService.getUserByUsername(testUser.getUsername());
        
        // Assert - Verify user is returned
        assertTrue("User should be present", foundUser.isPresent());
        assertEquals("Username should match", testUser.getUsername(), foundUser.get().getUsername());
        assertEquals("Email should match", testUser.getEmail(), foundUser.get().getEmail());
        
        // Verify database state
        Optional<User> userInDb = userRepository.findByUsername(testUser.getUsername());
        assertTrue("User should exist in database", userInDb.isPresent());
    }
    
    /**
     * Test Case ID: TC14
     * Test Objective: Verify that getUserByUsername returns empty when user doesn't exist
     * Input: Non-existent username
     * Expected Output: Empty Optional
     */
    @Test
    public void getUserByUsername_UserDoesNotExist_ShouldReturnEmptyOptional() {
        // Arrange - Use a non-existent username
        String nonExistentUsername = "nonexistent" + System.currentTimeMillis();
        
        // Act - Call the service method to get user by username
        Optional<User> foundUser = userService.getUserByUsername(nonExistentUsername);
        
        // Assert - Verify empty optional is returned
        assertFalse("User should not be present", foundUser.isPresent());
        
        // Verify database state
        Optional<User> userInDb = userRepository.findByUsername(nonExistentUsername);
        assertFalse("User should not exist in database", userInDb.isPresent());
    }
    
    /**
     * Test Case ID: TC15
     * Test Objective: Verify that getUserName returns the authenticated username
     * Input: Authentication with username
     * Expected Output: Username from authentication
     */
    @Test
    public void getUserName_AuthenticationExists_ShouldReturnUsername() {
        // Arrange - Set up authentication
        String testUsername = "testAuthUser";
        Authentication auth = new UsernamePasswordAuthenticationToken(testUsername, "password");
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        // Act - Call the service method to get username
        String username = userService.getUserName();
        
        // Assert - Verify username is returned
        assertEquals("Username should match authentication", testUsername, username);
    }
    
    /**
     * Test Case ID: TC16
     * Test Objective: Verify that getUserName handles no authentication gracefully
     * Input: No authentication
     * Expected Output: "anonymousUser" or graceful error handling
     */
    @Test
    public void getUserName_NoAuthentication_ShouldReturnAnonymousUser() {
        // Arrange - Clear authentication
        SecurityContextHolder.clearContext();
        
        // Act & Assert - Call the service method and verify behavior
        try {
            String username = userService.getUserName();
            // If no exception is thrown, the username should be "anonymousUser"
            assertEquals("Should return anonymousUser", "anonymousUser", username);
        } catch (Exception e) {
            // If an exception is thrown, the test should fail
            fail("Exception thrown when no authentication exists: " + e.getMessage());
        }
    }
    
    /**
     * Test Case ID: TC17
     * Test Objective: Verify that existsByEmail returns true for existing email
     * Input: Existing email
     * Expected Output: True
     */
    @Test
    public void existsByEmail_EmailExists_ShouldReturnTrue() {
        // Act - Call the service method to check if email exists
        boolean exists = userService.existsByEmail(testUser.getEmail());
        
        // Assert - Verify true is returned
        assertTrue("Should return true for existing email", exists);
        
        // Verify database state
        Optional<User> userInDb = userRepository.findByEmail(testUser.getEmail());
        assertTrue("User should exist in database", userInDb.isPresent());
    }
    
    /**
     * Test Case ID: TC18
     * Test Objective: Verify that existsByEmail returns false for non-existent email
     * Input: Non-existent email
     * Expected Output: False
     */
    @Test
    public void existsByEmail_EmailDoesNotExist_ShouldReturnFalse() {
        // Arrange - Use a non-existent email
        String nonExistentEmail = "nonexistent" + System.currentTimeMillis() + "@example.com";
        
        // Act - Call the service method to check if email exists
        boolean exists = userService.existsByEmail(nonExistentEmail);
        
        // Assert - Verify false is returned
        assertFalse("Should return false for non-existent email", exists);
        
        // Verify database state
        Optional<User> userInDb = userRepository.findByEmail(nonExistentEmail);
        assertFalse("User should not exist in database", userInDb.isPresent());
    }
    
    /**
     * Test Case ID: TC19
     * Test Objective: Verify that existsByEmail handles null email gracefully
     * Input: Null email
     * Expected Output: False or graceful error handling
     */
    @Test
    public void existsByEmail_NullEmail_ShouldHandleGracefully() {
        // Act & Assert - Call the service method and verify behavior
        try {
            boolean exists = userService.existsByEmail(null);
            // If no exception is thrown, the result should be false
            assertFalse("Should return false for null email", exists);
        } catch (Exception e) {
            // If an exception is thrown, check if it's expected
            assertTrue("Exception should be of expected type", 
                    e instanceof IllegalArgumentException || e instanceof NullPointerException);
        }
    }
    
    /**
     * Test Case ID: TC20
     * Test Objective: Verify that updateUser updates user information
     * Input: User with updated email
     * Expected Output: User with updated email in database
     */
    @Test
    public void updateUser_ShouldUpdateUserInformation() {
        // Arrange - Update user email
        String newEmail = "updated" + System.currentTimeMillis() + "@example.com";
        testUser.setEmail(newEmail);
        
        // Act - Call the service method to update user
        userService.updateUser(testUser);
        
        // Assert - Verify user is updated
        Optional<User> updatedUser = userService.getUserByUsername(testUser.getUsername());
        assertTrue("User should be present", updatedUser.isPresent());
        assertEquals("Email should be updated", newEmail, updatedUser.get().getEmail());
        
        // Verify database state
        Optional<User> userInDb = userRepository.findById(testUser.getId());
        assertTrue("User should exist in database", userInDb.isPresent());
        assertEquals("Email should be updated in database", newEmail, userInDb.get().getEmail());
    }
    
    /**
     * Test Case ID: TC21
     * Test Objective: Verify that updateUser updates user profile
     * Input: User with updated profile
     * Expected Output: User with updated profile in database
     */
    @Test
    public void updateUser_WithNewProfile_ShouldUpdateProfile() {
        // Arrange - Create new profile and update user
        Profile newProfile = new Profile();
        newProfile.setFirstName("Updated");
        newProfile.setLastName("Name");
        newProfile.setImage("updated-image.jpg");
        profileRepository.save(newProfile);
        
        testUser.setProfile(newProfile);
        
        // Act - Call the service method to update user
        userService.updateUser(testUser);
        
        // Assert - Verify profile is updated
        Optional<User> updatedUser = userService.getUserByUsername(testUser.getUsername());
        assertTrue("User should be present", updatedUser.isPresent());
        assertEquals("First name should be updated", "Updated", updatedUser.get().getProfile().getFirstName());
        assertEquals("Last name should be updated", "Name", updatedUser.get().getProfile().getLastName());
        assertEquals("Image should be updated", "updated-image.jpg", updatedUser.get().getProfile().getImage());
        
        // Verify database state
        Optional<User> userInDb = userRepository.findById(testUser.getId());
        assertTrue("User should exist in database", userInDb.isPresent());
        assertEquals("Profile should be updated in database", 
                "Updated", userInDb.get().getProfile().getFirstName());
    }
    
    /**
     * Test Case ID: TC22
     * Test Objective: Verify that updateUser updates user roles
     * Input: User with updated roles
     * Expected Output: User with updated roles in database
     */
    @Test
    public void updateUser_WithNewRoles_ShouldUpdateRoles() {
        // Arrange - Update user roles
        Set<Role> newRoles = new HashSet<>();
        newRoles.add(lecturerRole);
        testUser.setRoles(newRoles);
        
        // Act - Call the service method to update user
        userService.updateUser(testUser);
        
        // Assert - Verify roles are updated
        Optional<User> updatedUser = userService.getUserByUsername(testUser.getUsername());
        assertTrue("User should be present", updatedUser.isPresent());
        assertEquals("Should have one role", 1, updatedUser.get().getRoles().size());
        assertTrue("Should have lecturer role", updatedUser.get().getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_LECTURER));
        
        // Verify database state
        Optional<User> userInDb = userRepository.findById(testUser.getId());
        assertTrue("User should exist in database", userInDb.isPresent());
        assertEquals("Should have one role in database", 1, userInDb.get().getRoles().size());
    }
    
    /**
     * Test Case ID: TC23
     * Test Objective: Verify that updateUser handles null user gracefully
     * Input: Null user
     * Expected Output: Exception thrown
     */
    @Test
    public void updateUser_WithNullUser_ShouldHandleGracefully() {
        // Act & Assert - Call the service method and verify behavior
        try {
            userService.updateUser(null);
            // If no exception is thrown, the test should fail
            fail("Expected an exception to be thrown");
        } catch (Exception e) {
            // If an exception is thrown, that's expected
            assertNotNull("Exception should not be null", e);
            assertTrue("Exception should be of expected type", 
                    e instanceof IllegalArgumentException || e instanceof NullPointerException);
        }
    }
    
    /**
     * Test Case ID: TC24
     * Test Objective: Verify that findUserById returns a user when it exists
     * Input: Existing user ID
     * Expected Output: Optional containing the user
     */
    @Test
    public void findUserById_UserExists_ShouldReturnUser() {
        // Act - Call the service method to find user by ID
        Optional<User> foundUser = userService.findUserById(testUser.getId());
        
        // Assert - Verify user is returned
        assertTrue("User should be present", foundUser.isPresent());
        assertEquals("ID should match", testUser.getId(), foundUser.get().getId());
        assertEquals("Username should match", testUser.getUsername(), foundUser.get().getUsername());
        
        // Verify database state
        Optional<User> userInDb = userRepository.findById(testUser.getId());
        assertTrue("User should exist in database", userInDb.isPresent());
    }
    
    /**
     * Test Case ID: TC25
     * Test Objective: Verify that findUserById returns empty when user doesn't exist
     * Input: Non-existent user ID
     * Expected Output: Empty Optional
     */
    @Test
    public void findUserById_UserDoesNotExist_ShouldReturnEmptyOptional() {
        // Arrange - Use a non-existent user ID
        Long nonExistentId = 999999L;
        
        // Act - Call the service method to find user by ID
        Optional<User> foundUser = userService.findUserById(nonExistentId);
        
        // Assert - Verify empty optional is returned
        assertFalse("User should not be present", foundUser.isPresent());
        
        // Verify database state
        Optional<User> userInDb = userRepository.findById(nonExistentId);
        assertFalse("User should not exist in database", userInDb.isPresent());
    }
}

