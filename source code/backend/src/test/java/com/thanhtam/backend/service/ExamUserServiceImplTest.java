package com.thanhtam.backend.unittest;

import com.thanhtam.backend.entity.Exam;
import com.thanhtam.backend.entity.ExamUser;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.repository.ExamRepository;
import com.thanhtam.backend.repository.ExamUserRepository;
import com.thanhtam.backend.service.ExamUserServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Unit tests for ExamUserServiceImpl using actual MySQL database connections.
 * These tests verify that the service methods work correctly with the database.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional  // Ensures all database changes are rolled back after each test
public class ExamUserServiceImplTest {

    @Autowired
    private ExamUserServiceImpl examUserService;

    @Autowired
    private ExamUserRepository examUserRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private com.thanhtam.backend.repository.UserRepository userRepository;

    private Exam testExam;
    private User testUser;
    private ExamUser testExamUser;
    private List<Exam> multipleExams;
    private List<User> testUsers;
    private List<ExamUser> testExamUsers;

    /**
     * Set up test data before each test.
     * Creates and saves test users, exams, and exam users to the database.
     */
    @Before
    public void setUp() {
        // Create and save multiple test users
        testUsers = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            User user = new User();
            user.setUsername("testuser_" + i + "_" + System.currentTimeMillis()); // Ensure unique username
            user.setEmail("test_" + i + "_" + System.currentTimeMillis() + "@example.com");
            user.setPassword("password");
            user = userRepository.save(user);
            testUsers.add(user);
        }

        // Set the first user as the main test user
        testUser = testUsers.get(0);

        // Create and save a test exam
        testExam = new Exam();
        testExam.setTitle("Test Exam");
        testExam.setDurationExam(60);
        testExam.setShuffle(true);
        testExam.setCanceled(false);
        testExam.setBeginExam(new Date());
        testExam.setFinishExam(new Date(System.currentTimeMillis() + 3600000)); // 1h later
        testExam.setQuestionData("[]");
        testExam.setCreatedBy(testUser);
        testExam = examRepository.save(testExam);

        // Create multiple exams for testing pagination and filtering
        multipleExams = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Exam exam = new Exam();
            exam.setTitle("Test Exam " + i);
            exam.setDurationExam(30 + i * 10);
            exam.setShuffle(i % 2 == 0);
            exam.setCanceled(false);
            exam.setBeginExam(new Date());
            exam.setFinishExam(new Date(System.currentTimeMillis() + 3600000));
            exam.setQuestionData("[]");
            exam.setCreatedBy(testUser);
            exam = examRepository.save(exam);
            multipleExams.add(exam);
        }

        // Create exam users for each user and exam combination
        testExamUsers = new ArrayList<>();
        for (User user : testUsers) {
            for (Exam exam : multipleExams) {
                ExamUser examUser = new ExamUser();
                examUser.setUser(user);
                examUser.setExam(exam);
                examUser.setIsStarted(false);
                examUser.setIsFinished(false);
                examUser.setRemainingTime(exam.getDurationExam() * 60);
                examUser.setTotalPoint(-1.0);
                examUser = examUserRepository.save(examUser);
                testExamUsers.add(examUser);
            }
        }

        // Set the first exam user as the main test exam user
        testExamUser = testExamUsers.get(0);
    }

    /**
     * Test creating exam users for an exam and a list of users.
     */
    @Test
    public void testCreate() {
        // Create a new exam
        Exam newExam = new Exam();
        newExam.setTitle("New Test Exam");
        newExam.setDurationExam(30);
        newExam.setShuffle(false);
        newExam.setCanceled(false);
        newExam.setBeginExam(new Date());
        newExam.setFinishExam(new Date(System.currentTimeMillis() + 1800000)); // 30min later
        newExam.setQuestionData("[]");
        newExam.setCreatedBy(testUser);
        newExam = examRepository.save(newExam);

        // Create a list of users
        List<User> users = new ArrayList<>();
        users.add(testUsers.get(1));
        users.add(testUsers.get(2));

        // Create exam users
        examUserService.create(newExam, users);

        // Verify that exam users were created
        List<ExamUser> createdExamUsers = examUserRepository.findAllByExam_Id(newExam.getId());
        Assert.assertEquals(2, createdExamUsers.size());

        // Verify that each user has an exam user
        for (User user : users) {
            boolean found = false;
            for (ExamUser examUser : createdExamUsers) {
                if (examUser.getUser().getId().equals(user.getId())) {
                    found = true;
                    break;
                }
            }
            Assert.assertTrue("Exam user not found for user: " + user.getUsername(), found);
        }
    }

    /**
     * Test retrieving exam list by username.
     */
    @Test
    public void testGetExamListByUsername() {
        // Get exam list by username
        List<ExamUser> examUsers = examUserService.getExamListByUsername(testUser.getUsername());

        // Verify the results
        Assert.assertNotNull(examUsers);
        Assert.assertTrue(examUsers.size() > 0);

        // Verify that all exam users belong to the test user
        for (ExamUser examUser : examUsers) {
            Assert.assertEquals(testUser.getUsername(), examUser.getUser().getUsername());
        }
    }

    /**
     * Test finding an exam user by exam ID and username.
     */
    @Test
    public void testFindByExamAndUser() {
        // Find exam user by exam ID and username
        ExamUser foundExamUser = examUserService.findByExamAndUser(testExamUser.getExam().getId(), testExamUser.getUser().getUsername());

        // Verify the results
        Assert.assertNotNull(foundExamUser);
        Assert.assertEquals(testExamUser.getExam().getId(), foundExamUser.getExam().getId());
        Assert.assertEquals(testExamUser.getUser().getUsername(), foundExamUser.getUser().getUsername());
    }

    /**
     * Test finding an exam user by exam ID and username when it doesn't exist.
     */
    @Test
    public void testFindByExamAndUser_NotFound() {
        // Find exam user by non-existent exam ID and username
        ExamUser foundExamUser = examUserService.findByExamAndUser(999999L, "nonExistentUser");

        // Verify the results
        Assert.assertNull(foundExamUser);
    }

    /**
     * Test updating an exam user.
     */
    @Test
    public void testUpdate() {
        // Update the exam user
        testExamUser.setIsStarted(true);
        testExamUser.setIsFinished(true);
        testExamUser.setTotalPoint(85.5);
        examUserService.update(testExamUser);

        // Retrieve the updated exam user
        Optional<ExamUser> updatedExamUser = examUserRepository.findById(testExamUser.getId());

        // Verify the results
        Assert.assertTrue(updatedExamUser.isPresent());
        Assert.assertTrue(updatedExamUser.get().getIsStarted());
        Assert.assertTrue(updatedExamUser.get().getIsFinished());
        Assert.assertEquals(85.5, updatedExamUser.get().getTotalPoint(), 0.01);
    }

    /**
     * Test finding an exam user by ID.
     */
    @Test
    public void testFindExamUserById() {
        // Find exam user by ID
        Optional<ExamUser> foundExamUser = examUserService.findExamUserById(testExamUser.getId());

        // Verify the results
        Assert.assertTrue(foundExamUser.isPresent());
        Assert.assertEquals(testExamUser.getId(), foundExamUser.get().getId());
    }

    /**
     * Test finding an exam user by ID when it doesn't exist.
     */
    @Test
    public void testFindExamUserById_NotFound() {
        // Find exam user by non-existent ID
        Optional<ExamUser> foundExamUser = examUserService.findExamUserById(999999L);

        // Verify the results
        Assert.assertFalse(foundExamUser.isPresent());
    }

    /**
     * Test getting complete exams for a user in a course.
     * Note: This test may need adjustment based on your actual database schema and data.
     */
    @Test
    public void testGetCompleteExams() {
        // This test requires a course ID, which may not be available in the test environment
        // For now, we'll just verify that the method doesn't throw an exception
        try {
            List<ExamUser> completeExams = examUserService.getCompleteExams(1L, testUser.getUsername());
            Assert.assertNotNull(completeExams);
        } catch (Exception e) {
            // If an exception is thrown, the test will fail
            Assert.fail("Exception thrown: " + e.getMessage());
        }
    }

    /**
     * Test finding all exam users by exam ID.
     */
    @Test
    public void testFindAllByExam_Id() {
        // Find all exam users by exam ID
        List<ExamUser> examUsers = examUserService.findAllByExam_Id(testExamUser.getExam().getId());

        // Verify the results
        Assert.assertNotNull(examUsers);
        Assert.assertTrue(examUsers.size() > 0);

        // Verify that all exam users belong to the test exam
        for (ExamUser examUser : examUsers) {
            Assert.assertEquals(testExamUser.getExam().getId(), examUser.getExam().getId());
        }
    }



    /**
     * Test finding all exam users by exam ID when exam has no users.
     */
    @Test
    public void testFindAllByExam_Id_Empty() {
        // Create a new exam
        Exam newExam = new Exam();
        newExam.setTitle("Empty Test Exam");
        newExam.setDurationExam(30);
        newExam.setShuffle(false);
        newExam.setCanceled(false);
        newExam.setBeginExam(new Date());
        newExam.setFinishExam(new Date(System.currentTimeMillis() + 1800000));
        newExam.setQuestionData("[]");
        newExam.setCreatedBy(testUser);
        newExam = examRepository.save(newExam);

        // Find all exam users for the new exam
        List<ExamUser> examUsers = examUserService.findAllByExam_Id(newExam.getId());

        // Verify the results
        Assert.assertNotNull(examUsers);
        Assert.assertTrue(examUsers.isEmpty());
    }



    /**
     * Test getting complete exams when user has no completed exams in the course.
     */
    @Test
    public void testGetCompleteExams_Empty() {
        // Create a new user
        User newUser = new User();
        newUser.setUsername("newuser_" + System.currentTimeMillis());
        newUser.setEmail("new_" + System.currentTimeMillis() + "@example.com");
        newUser.setPassword("password");
        newUser = userRepository.save(newUser);

        // Get complete exams for the new user in a course
        List<ExamUser> completeExams = examUserService.getCompleteExams(1L, newUser.getUsername());

        // Verify the results
        Assert.assertNotNull(completeExams);
        Assert.assertTrue(completeExams.isEmpty());
    }

    /**
     * Test creating exam users with empty user list.
     */
    @Test
    public void testCreate_EmptyUserList() {
        // Create a new exam
        Exam newExam = new Exam();
        newExam.setTitle("Empty Users Test Exam");
        newExam.setDurationExam(30);
        newExam.setShuffle(false);
        newExam.setCanceled(false);
        newExam.setBeginExam(new Date());
        newExam.setFinishExam(new Date(System.currentTimeMillis() + 1800000));
        newExam.setQuestionData("[]");
        newExam.setCreatedBy(testUser);
        newExam = examRepository.save(newExam);

        // Create exam users with empty user list
        examUserService.create(newExam, new ArrayList<>());

        // Verify that no exam users were created
        List<ExamUser> createdExamUsers = examUserRepository.findAllByExam_Id(newExam.getId());
        Assert.assertNotNull(createdExamUsers);
        Assert.assertTrue(createdExamUsers.isEmpty());
    }

    /**
     * Test finding an exam user by exam ID and username with empty username.
     */
    @Test
    public void testFindByExamAndUser_EmptyUsername() {
        // Find exam user by exam ID and empty username
        ExamUser foundExamUser = examUserService.findByExamAndUser(testExamUser.getExam().getId(), "");

        // Verify the results
        Assert.assertNull(foundExamUser);
    }

    /**
     * Test finding an exam user by exam ID and username with negative exam ID.
     */
    @Test
    public void testFindByExamAndUser_NegativeExamId() {
        // Find exam user by negative exam ID and username
        ExamUser foundExamUser = examUserService.findByExamAndUser(-1L, testUser.getUsername());

        // Verify the results
        Assert.assertNull(foundExamUser);
    }

    /**
     * Test finding an exam user by ID with negative ID.
     */
    @Test
    public void testFindExamUserById_NegativeId() {
        // Find exam user by negative ID
        Optional<ExamUser> foundExamUser = examUserService.findExamUserById(-1L);

        // Verify the results
        Assert.assertFalse(foundExamUser.isPresent());
    }

    /**
     * Test finding all exam users by exam ID with negative ID.
     */
    @Test
    public void testFindAllByExam_Id_NegativeId() {
        // Find all exam users by negative exam ID
        List<ExamUser> examUsers = examUserService.findAllByExam_Id(-1L);

        // Verify the results
        Assert.assertNotNull(examUsers);
        Assert.assertTrue(examUsers.isEmpty());
    }



    /**
     * Test getting complete exams with empty username.
     */
    @Test
    public void testGetCompleteExams_EmptyUsername() {
        // Get complete exams with empty username
        List<ExamUser> completeExams = examUserService.getCompleteExams(1L, "");

        // Verify the results
        Assert.assertNotNull(completeExams);
        Assert.assertTrue(completeExams.isEmpty());
    }

    /**
     * Test getting exam list by empty username.
     */
    @Test
    public void testGetExamListByUsername_EmptyUsername() {
        // Get exam list by empty username
        List<ExamUser> examUsers = examUserService.getExamListByUsername("");

        // Verify the results
        Assert.assertNotNull(examUsers);
        Assert.assertTrue(examUsers.isEmpty());
    }

    /**
     * Test creating exam users with null exam.
     * Expected output: Should handle null exam gracefully
     */
    @Test
    public void testCreate_NullExam() {
        // Create a list of users
        List<User> users = new ArrayList<>();
        users.add(testUsers.get(1));
        users.add(testUsers.get(2));

        // Create exam users with null exam
        examUserService.create(null, users);

        // Verify that no exam users were created
        List<ExamUser> createdExamUsers = examUserRepository.findAll();
        Assert.assertNotNull(createdExamUsers);
        // The size should be the same as before since no new exam users were created
        Assert.assertEquals(testExamUsers.size(), createdExamUsers.size());
    }

    /**
     * Test creating exam users with null user list.
     * Expected output: Should handle null user list gracefully
     */
    @Test
    public void testCreate_NullUserList() {
        // Create a new exam
        Exam newExam = new Exam();
        newExam.setTitle("Null Users Test Exam");
        newExam.setDurationExam(30);
        newExam.setShuffle(false);
        newExam.setCanceled(false);
        newExam.setBeginExam(new Date());
        newExam.setFinishExam(new Date(System.currentTimeMillis() + 1800000));
        newExam.setQuestionData("[]");
        newExam.setCreatedBy(testUser);
        newExam = examRepository.save(newExam);

        // Create exam users with null user list
        examUserService.create(newExam, null);

        // Verify that no exam users were created
        List<ExamUser> createdExamUsers = examUserRepository.findAllByExam_Id(newExam.getId());
        Assert.assertNotNull(createdExamUsers);
        Assert.assertTrue(createdExamUsers.isEmpty());
    }


    /**
     * Test updating exam user with null values.
     * Expected output: Should handle null values gracefully
     */
    @Test
    public void testUpdate_NullValues() {
        // Create a new exam user
        ExamUser examUser = new ExamUser();
        examUser.setUser(testUser);
        examUser.setExam(testExam);
        examUser.setIsStarted(false);
        examUser.setIsFinished(false);
        examUser.setRemainingTime(testExam.getDurationExam() * 60);
        examUser.setTotalPoint(-1.0);
        examUser = examUserRepository.save(examUser);

        // Update with null values
        examUser.setIsStarted(null);
        examUser.setIsFinished(null);
        examUser.setRemainingTime(0);
        examUser.setTotalPoint(null);
        examUserService.update(examUser);

        // Retrieve the updated exam user
        Optional<ExamUser> updatedExamUser = examUserRepository.findById(examUser.getId());

        // Verify the results
        Assert.assertTrue(updatedExamUser.isPresent());
        Assert.assertFalse(updatedExamUser.get().getIsStarted()); // Should default to false
        Assert.assertFalse(updatedExamUser.get().getIsFinished()); // Should default to false
        Assert.assertEquals(testExam.getDurationExam() * 60, updatedExamUser.get().getRemainingTime()); // Should keep original value
        Assert.assertEquals(-1.0, updatedExamUser.get().getTotalPoint(), 0.01); // Should keep original value
    }

    /**
     * Test updating exam user with invalid values.
     * Expected output: Should handle invalid values gracefully
     */
    @Test
    public void testUpdate_InvalidValues() {
        // Create a new exam user
        ExamUser examUser = new ExamUser();
        examUser.setUser(testUser);
        examUser.setExam(testExam);
        examUser.setIsStarted(false);
        examUser.setIsFinished(false);
        examUser.setRemainingTime(testExam.getDurationExam() * 60);
        examUser.setTotalPoint(-1.0);
        examUser = examUserRepository.save(examUser);

        // Update with invalid values
        examUser.setRemainingTime(-100); // Negative remaining time
        examUser.setTotalPoint(150.0); // Score over 100
        examUserService.update(examUser);

        // Retrieve the updated exam user
        Optional<ExamUser> updatedExamUser = examUserRepository.findById(examUser.getId());

        // Verify the results
        Assert.assertTrue(updatedExamUser.isPresent());
        Assert.assertEquals(-100, updatedExamUser.get().getRemainingTime()); // Should accept negative time
        Assert.assertEquals(150.0, updatedExamUser.get().getTotalPoint(), 0.01); // Should accept score over 100
    }

    /**
     * Test updating non-existent exam user.
     * Expected output: Should handle non-existent exam user gracefully
     */
    @Test
    public void testUpdate_NonExistentExamUser() {
        // Create a new exam user but don't save it
        ExamUser examUser = new ExamUser();
        examUser.setId(999999L); // Non-existent ID
        examUser.setUser(testUser);
        examUser.setExam(testExam);
        examUser.setIsStarted(true);
        examUser.setIsFinished(true);
        examUser.setRemainingTime(0);
        examUser.setTotalPoint(85.5);

        // Try to update non-existent exam user
        examUserService.update(examUser);

        // Verify that no exam user was created/updated
        Optional<ExamUser> updatedExamUser = examUserRepository.findById(999999L);
        Assert.assertFalse(updatedExamUser.isPresent());
    }
}