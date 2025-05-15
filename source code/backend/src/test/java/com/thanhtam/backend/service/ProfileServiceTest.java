package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.Profile;
import com.thanhtam.backend.repository.ProfileRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit Test for ProfileService
 * 
 * This test class verifies the functionality of the ProfileService implementation.
 * It tests both createProfile and getAllProfiles methods with various inputs to ensure all branches are covered.
 * 
 * Test environment:
 * - Uses Spring Boot test context
 * - Runs with transactional boundaries
 * - Performs rollback after each test
 * - Cleans database before and after tests using SQL scripts
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
@Rollback // Ensures database changes are rolled back after each test
@Sql(scripts = "/cleanup-profile.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-profile.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ProfileServiceTest {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ProfileRepository profileRepository;

    // Test data
    private Profile testProfile;

    /**
     * Setup method that runs before each test.
     * Creates a test profile in the database for use in tests.
     */
    @Before
    public void setUp() {
        // Clean up existing data to ensure a clean state
        cleanupTestData();
        
        // Create test profile with valid data
        testProfile = new Profile();
        testProfile.setFirstName("Test");
        testProfile.setLastName("User");
        testProfile.setImage("test-image.jpg");
        profileRepository.save(testProfile);
    }
    
    /**
     * Teardown method that runs after each test.
     * Cleans up any test data to maintain isolation between tests.
     */
    @After
    public void tearDown() {
        // Clean up after each test
        cleanupTestData();
    }
    
    /**
     * Helper method to clean up test data.
     * Deletes all profiles from the repository.
     */
    private void cleanupTestData() {
        // Delete all test profiles
        profileRepository.deleteAll();
    }

    /**
     * Test Case ID: TC01
     * Test Objective: Verify that a new profile can be created with valid data
     * Input: Profile with firstName="New", lastName="Profile", image="new-image.jpg"
     * Expected Output: Saved profile with generated ID and matching attributes
     */
    @Test
    public void createProfile_ShouldCreateNewProfile() {
        // Arrange - Create a new profile with valid data
        Profile newProfile = new Profile();
        newProfile.setFirstName("New");
        newProfile.setLastName("Profile");
        newProfile.setImage("new-image.jpg");
        
        // Act - Call the service method to create the profile
        Profile savedProfile = profileService.createProfile(newProfile);
        
        // Assert - Verify the profile was saved correctly
        assertNotNull("Saved profile should not be null", savedProfile);
        assertNotNull("Saved profile should have an ID", savedProfile.getId());
        assertEquals("First name should match", newProfile.getFirstName(), savedProfile.getFirstName());
        assertEquals("Last name should match", newProfile.getLastName(), savedProfile.getLastName());
        assertEquals("Image should match", newProfile.getImage(), savedProfile.getImage());
        
        // Verify database state - Check if profile exists in database
        Profile profileInDb = profileRepository.findById(savedProfile.getId()).orElse(null);
        assertNotNull("Profile should exist in database", profileInDb);
    }
    
    /**
     * Test Case ID: TC02
     * Test Objective: Verify that a profile can be created with null values
     * Input: Profile with null firstName, lastName, and image
     * Expected Output: Saved profile with generated ID and null attributes
     */
    @Test
    public void createProfile_WithNullValues_ShouldCreateProfileWithNullValues() {
        // Arrange - Create a new profile with null values
        Profile newProfile = new Profile();
        // Leave firstName, lastName, and image as null
        
        // Act - Call the service method to create the profile
        Profile savedProfile = profileService.createProfile(newProfile);
        
        // Assert - Verify the profile was saved with null values
        assertNotNull("Saved profile should not be null", savedProfile);
        assertNotNull("Saved profile should have an ID", savedProfile.getId());
        assertNull("First name should be null", savedProfile.getFirstName());
        assertNull("Last name should be null", savedProfile.getLastName());
        assertNull("Image should be null", savedProfile.getImage());
        
        // Verify database state
        Profile profileInDb = profileRepository.findById(savedProfile.getId()).orElse(null);
        assertNotNull("Profile should exist in database", profileInDb);
        assertNull("First name should be null in database", profileInDb.getFirstName());
    }
    
    /**
     * Test Case ID: TC03
     * Test Objective: Verify that a profile can be created with empty strings
     * Input: Profile with empty strings for firstName, lastName, and image
     * Expected Output: Saved profile with generated ID and empty string attributes
     */
    @Test
    public void createProfile_WithEmptyStrings_ShouldCreateProfileWithEmptyStrings() {
        // Arrange - Create a new profile with empty strings
        Profile newProfile = new Profile();
        newProfile.setFirstName("");
        newProfile.setLastName("");
        newProfile.setImage("");
        
        // Act - Call the service method to create the profile
        Profile savedProfile = profileService.createProfile(newProfile);
        
        // Assert - Verify the profile was saved with empty strings
        assertNotNull("Saved profile should not be null", savedProfile);
        assertNotNull("Saved profile should have an ID", savedProfile.getId());
        assertEquals("First name should be empty", "", savedProfile.getFirstName());
        assertEquals("Last name should be empty", "", savedProfile.getLastName());
        assertEquals("Image should be empty", "", savedProfile.getImage());
        
        // Verify database state
        Profile profileInDb = profileRepository.findById(savedProfile.getId()).orElse(null);
        assertNotNull("Profile should exist in database", profileInDb);
        assertEquals("First name should be empty in database", "", profileInDb.getFirstName());
    }
    
    /**
     * Test Case ID: TC04
     * Test Objective: Verify that an existing profile can be updated
     * Input: Existing profile with updated firstName, lastName, and image
     * Expected Output: Updated profile with same ID but new attribute values
     */
    @Test
    public void createProfile_WithExistingId_ShouldUpdateExistingProfile() {
        // Arrange - Get existing profile and update its attributes
        Profile existingProfile = profileRepository.findById(testProfile.getId()).orElse(null);
        assertNotNull("Test profile should exist", existingProfile);
        
        existingProfile.setFirstName("Updated");
        existingProfile.setLastName("Profile");
        existingProfile.setImage("updated-image.jpg");
        
        // Act - Call the service method to update the profile
        Profile updatedProfile = profileService.createProfile(existingProfile);
        
        // Assert - Verify the profile was updated correctly
        assertNotNull("Updated profile should not be null", updatedProfile);
        assertEquals("ID should remain the same", testProfile.getId(), updatedProfile.getId());
        assertEquals("First name should be updated", "Updated", updatedProfile.getFirstName());
        assertEquals("Last name should be updated", "Profile", updatedProfile.getLastName());
        assertEquals("Image should be updated", "updated-image.jpg", updatedProfile.getImage());
        
        // Verify database state
        Profile profileInDb = profileRepository.findById(testProfile.getId()).orElse(null);
        assertNotNull("Profile should exist in database", profileInDb);
        assertEquals("First name should be updated in database", "Updated", profileInDb.getFirstName());
    }

    /**
     * Test Case ID: TC05
     * Test Objective: Verify that a profile can be created with long string values
     * Input: Profile with long strings (255 characters) for firstName, lastName, and image
     * Expected Output: Saved profile with long string attributes
     */
    @Test
    public void createProfile_WithLongStrings_ShouldCreateProfileWithLongStrings() {
        // Arrange - Create a new profile with long strings
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 255; i++) {
            sb.append('a');
        }
        String longString = sb.toString(); // Create a string of 255 'a' characters

        Profile newProfile = new Profile();
        newProfile.setFirstName(longString);
        newProfile.setLastName(longString);
        newProfile.setImage(longString);

        // Act - Call the service method to create the profile
        Profile savedProfile = profileService.createProfile(newProfile);

        // Assert - Verify the profile was saved with long strings
        assertNotNull("Saved profile should not be null", savedProfile);
        assertNotNull("Saved profile should have an ID", savedProfile.getId());
        assertEquals("First name should match long string", longString, savedProfile.getFirstName());
        assertEquals("Last name should match long string", longString, savedProfile.getLastName());
        assertEquals("Image should match long string", longString, savedProfile.getImage());

        // Verify database state
        Profile profileInDb = profileRepository.findById(savedProfile.getId()).orElse(null);
        assertNotNull("Profile should exist in database", profileInDb);
        assertEquals("First name should match long string in database", longString, profileInDb.getFirstName());
    }

    /**
     * Test Case ID: TC06
     * Test Objective: Verify that a profile can be created with special characters
     * Input: Profile with special characters for firstName, lastName, and image
     * Expected Output: Saved profile with special character attributes
     */
    @Test
    public void createProfile_WithSpecialCharacters_ShouldCreateProfileWithSpecialCharacters() {
        // Arrange - Create a new profile with special characters
        String specialChars = "!@#$%^&*()_+{}[]|\"':;,.<>?/~`";

        Profile newProfile = new Profile();
        newProfile.setFirstName(specialChars);
        newProfile.setLastName(specialChars);
        newProfile.setImage(specialChars);

        // Act - Call the service method to create the profile
        Profile savedProfile = profileService.createProfile(newProfile);

        // Assert - Verify the profile was saved with special characters
        assertNotNull("Saved profile should not be null", savedProfile);
        assertNotNull("Saved profile should have an ID", savedProfile.getId());
        assertEquals("First name should contain special characters", specialChars, savedProfile.getFirstName());
        assertEquals("Last name should contain special characters", specialChars, savedProfile.getLastName());
        assertEquals("Image should contain special characters", specialChars, savedProfile.getImage());

        // Verify database state
        Profile profileInDb = profileRepository.findById(savedProfile.getId()).orElse(null);
        assertNotNull("Profile should exist in database", profileInDb);
        assertEquals("First name should contain special characters in database", specialChars, profileInDb.getFirstName());
    }

    /**
     * Test Case ID: TC07
     * Test Objective: Verify that getAllProfiles returns all profiles in the database
     * Input: Database with multiple profiles
     * Expected Output: List containing all profiles
     */
    @Test
    public void getAllProfiles_ShouldReturnAllProfiles() {
        // Arrange - Create additional profiles
        Profile profile1 = new Profile();
        profile1.setFirstName("First");
        profile1.setLastName("User");
        profileRepository.save(profile1);
        
        Profile profile2 = new Profile();
        profile2.setFirstName("Second");
        profile2.setLastName("User");
        profileRepository.save(profile2);
        
        // Act - Call the service method to get all profiles
        List<Profile> profiles = profileService.getAllProfiles();
        
        // Assert - Verify all profiles are returned
        assertNotNull("Profiles list should not be null", profiles);
        assertEquals("Should return 3 profiles", 3, profiles.size()); // testProfile + 2 new profiles
        
        // Verify database state - Count profiles in database
        long count = profileRepository.count();
        assertEquals("Database should contain 3 profiles", 3, count);
    }
    
    /**
     * Test Case ID: TC08
     * Test Objective: Verify that getAllProfiles returns an empty list when no profiles exist
     * Input: Empty database
     * Expected Output: Empty list
     */
    @Test
    public void getAllProfiles_NoProfiles_ShouldReturnEmptyList() {
        // Arrange - Delete all profiles
        profileRepository.deleteAll();
        
        // Act - Call the service method to get all profiles
        List<Profile> profiles = profileService.getAllProfiles();
        
        // Assert - Verify an empty list is returned
        assertNotNull("Profiles list should not be null", profiles);
        assertTrue("Profiles list should be empty", profiles.isEmpty());
        
        // Verify database state
        long count = profileRepository.count();
        assertEquals("Database should be empty", 0, count);
    }
    

}