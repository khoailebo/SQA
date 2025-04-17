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
import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
//@ActiveProfiles("test")
@Transactional
@Rollback
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "/cleanup-profile.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-profile.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ProfileServiceTest {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ProfileRepository profileRepository;

    private Profile testProfile;

    @Before
    public void setUp() {
        // Clean up existing data
        cleanupTestData();
        
        // Create test profile
        testProfile = new Profile();
        testProfile.setFirstName("Test");
        testProfile.setLastName("User");
        testProfile.setImage("test-image.jpg");
        profileRepository.save(testProfile);
    }
    
    @After
    public void tearDown() {
        // Clean up after each test
        cleanupTestData();
    }
    
    private void cleanupTestData() {
        // Delete test profiles
        profileRepository.deleteAll();
    }

    //TC01
    @Test
    public void createProfile_ShouldCreateNewProfile() {
        // Arrange
        Profile newProfile = new Profile();
        newProfile.setFirstName("New");
        newProfile.setLastName("Profile");
        newProfile.setImage("new-image.jpg");
        
        // Act
        Profile savedProfile = profileService.createProfile(newProfile);
        
        // Assert
        assertNotNull(savedProfile);
        assertNotNull(savedProfile.getId());
        assertEquals(newProfile.getFirstName(), savedProfile.getFirstName());
        assertEquals(newProfile.getLastName(), savedProfile.getLastName());
        assertEquals(newProfile.getImage(), savedProfile.getImage());
    }
    
    //TC02
    @Test
    public void createProfile_WithNullValues_ShouldCreateProfileWithNullValues() {
        // Arrange
        Profile newProfile = new Profile();
        // Leave firstName, lastName, and image as null
        
        // Act
        Profile savedProfile = profileService.createProfile(newProfile);
        
        // Assert
        assertNotNull(savedProfile);
        assertNotNull(savedProfile.getId());
        assertNull(savedProfile.getFirstName());
        assertNull(savedProfile.getLastName());
        assertNull(savedProfile.getImage());
    }
    
    //TC03
    @Test
    public void createProfile_WithEmptyStrings_ShouldCreateProfileWithEmptyStrings() {
        // Arrange
        Profile newProfile = new Profile();
        newProfile.setFirstName("");
        newProfile.setLastName("");
        newProfile.setImage("");
        
        // Act
        Profile savedProfile = profileService.createProfile(newProfile);
        
        // Assert
        assertNotNull(savedProfile);
        assertNotNull(savedProfile.getId());
        assertEquals("", savedProfile.getFirstName());
        assertEquals("", savedProfile.getLastName());
        assertEquals("", savedProfile.getImage());
    }
    
    //TC04
    @Test
    public void createProfile_WithExistingId_ShouldUpdateExistingProfile() {
        // Arrange
        Profile existingProfile = profileRepository.findById(testProfile.getId()).orElse(null);
        assertNotNull(existingProfile);
        
        existingProfile.setFirstName("Updated");
        existingProfile.setLastName("Profile");
        existingProfile.setImage("updated-image.jpg");
        
        // Act
        Profile updatedProfile = profileService.createProfile(existingProfile);
        
        // Assert
        assertNotNull(updatedProfile);
        assertEquals(testProfile.getId(), updatedProfile.getId());
        assertEquals("Updated", updatedProfile.getFirstName());
        assertEquals("Profile", updatedProfile.getLastName());
        assertEquals("updated-image.jpg", updatedProfile.getImage());
    }
    
    //TC05
    @Test
    public void getAllProfiles_ShouldReturnAllProfiles() {
        // Arrange
        // Create additional profiles
        Profile profile1 = new Profile();
        profile1.setFirstName("First");
        profile1.setLastName("User");
        profileRepository.save(profile1);
        
        Profile profile2 = new Profile();
        profile2.setFirstName("Second");
        profile2.setLastName("User");
        profileRepository.save(profile2);
        
        // Act
        List<Profile> profiles = profileService.getAllProfiles();
        
        // Assert
        assertNotNull(profiles);
        assertEquals(3, profiles.size()); // testProfile + 2 new profiles
    }
    
    //TC06
    @Test
    public void getAllProfiles_NoProfiles_ShouldReturnEmptyList() {
        // Arrange
        profileRepository.deleteAll();
        
        // Act
        List<Profile> profiles = profileService.getAllProfiles();
        
        // Assert
        assertNotNull(profiles);
        assertTrue(profiles.isEmpty());
    }
    
//    //TC07
//    @Test
//    public void createProfile_WithLongStrings_ShouldCreateProfileWithLongStrings() {
//        // Arrange
//        String longString = "a".repeat(255); // Create a string of 255 'a' characters
//
//        Profile newProfile = new Profile();
//        newProfile.setFirstName(longString);
//        newProfile.setLastName(longString);
//        newProfile.setImage(longString);
//
//        // Act
//        Profile savedProfile = profileService.createProfile(newProfile);
//
//        // Assert
//        assertNotNull(savedProfile);
//        assertNotNull(savedProfile.getId());
//        assertEquals(longString, savedProfile.getFirstName());
//        assertEquals(longString, savedProfile.getLastName());
//        assertEquals(longString, savedProfile.getImage());
//    }
//
    //TC08
    @Test
    public void createProfile_WithSpecialCharacters_ShouldCreateProfileWithSpecialCharacters() {
        // Arrange
        String specialChars = "!@#$%^&*()_+{}[]|\"':;,.<>?/~`";
        
        Profile newProfile = new Profile();
        newProfile.setFirstName(specialChars);
        newProfile.setLastName(specialChars);
        newProfile.setImage(specialChars);
        
        // Act
        Profile savedProfile = profileService.createProfile(newProfile);
        
        // Assert
        assertNotNull(savedProfile);
        assertNotNull(savedProfile.getId());
        assertEquals(specialChars, savedProfile.getFirstName());
        assertEquals(specialChars, savedProfile.getLastName());
        assertEquals(specialChars, savedProfile.getImage());
    }
}