package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.*;
import com.thanhtam.backend.repository.*;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for StatisticsServiceImpl
 * This class contains test cases to verify the functionality of StatisticsServiceImpl
 * including counting totals, calculating changes, and retrieving statistics
 */
@RunWith(org.springframework.test.context.junit4.SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback
public class StatisticsServiceTest {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ExamUserRepository examUserRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StatisticsServiceImpl statisticsService;

    private static User testUser;
    private static Exam testExam;
    private static Question testQuestion;
    private static ExamUser testExamUser;
    private static boolean initialized = false;

    /**
     * Setup method that runs before each test
     * Creates and saves test entities needed for the tests
     */
    @Before
    public void setUp() {
        if (!initialized) {
            initialized = true;
            
            // Create test user
            testUser = new User();
            testUser.setUsername("testuser");
            testUser.setEmail("test@example.com");
            testUser.setDeleted(false);
            testUser.setCreatedDate(new Date());
            testUser = userRepository.save(testUser);

            // Create test exam
            testExam = new Exam();
            testExam.setTitle("Test Exam");
            testExam.setCanceled(true);
            testExam.setCreatedDate(new Date());
            testExam = examRepository.save(testExam);

            // Create test question
            testQuestion = new Question();
            testQuestion.setQuestionText("Test Question");
            testQuestion.setCreatedDate(new Date());
            testQuestion = questionRepository.save(testQuestion);

            // Create test exam user
            testExamUser = new ExamUser();
            testExamUser.setExam(testExam);
            testExamUser.setUser(testUser);
            testExamUser.setTimeFinish(new Date());
            testExamUser = examUserRepository.save(testExamUser);
        }
    }

    /**
     * Test case TC001: Count total exams
     * Purpose: Verify that the service can correctly count
     * the total number of exams
     */
    @Test
    public void testCountExamTotal() {
        // When
        long count = statisticsService.countExamTotal();

        // Then
        assertTrue(count > 0);
        assertEquals(examRepository.count(), count);
    }

    /**
     * Test case TC002: Count total questions
     * Purpose: Verify that the service can correctly count
     * the total number of questions
     */
    @Test
    public void testCountQuestionTotal() {
        // When
        long count = statisticsService.countQuestionTotal();

        // Then
        assertTrue(count > 0);
        assertEquals(questionRepository.count(), count);
    }

    /**
     * Test case TC003: Count total accounts
     * Purpose: Verify that the service can correctly count
     * the total number of accounts
     */
    @Test
    public void testCountAccountTotal() {
        // When
        long count = statisticsService.countAccountTotal();

        // Then
        assertTrue(count > 0);
        assertEquals(userRepository.count(), count);
    }

    /**
     * Test case TC004: Count total exam users
     * Purpose: Verify that the service can correctly count
     * the total number of exam users
     */
    @Test
    public void testCountExamUserTotal() {
        // When
        long count = statisticsService.countExamUserTotal();

        // Then
        assertTrue(count > 0);
        assertEquals(examUserRepository.count(), count);
    }

    /**
     * Test case TC005: Get exam user change percentage
     * Purpose: Verify that the service can correctly calculate
     * the percentage change in exam users between current and last week
     */
    @Test
    public void testGetChangeExamUser() {
        // When
        Double change = statisticsService.getChangeExamUser();

        // Then
        assertNotNull(change);
    }

    /**
     * Test case TC006: Get exam user counts for last seven days
     * Purpose: Verify that the service can correctly count
     * exam users for each of the last seven days
     */
    @Test
    public void testCountExamUserLastedSevenDaysTotal() {
        // When
        List<Long> counts = statisticsService.countExamUserLastedSevenDaysTotal();

        // Then
        assertNotNull(counts);
        assertTrue(counts.size() <= 7);
    }

    /**
     * Test case TC007: Get question change percentage
     * Purpose: Verify that the service can correctly calculate
     * the percentage change in questions between current and last week
     */
    @Test
    public void testGetChangeQuestion() {
        // When
        Double change = statisticsService.getChangeQuestion();

        // Then
        assertNotNull(change);
    }

    /**
     * Test case TC008: Get account change percentage
     * Purpose: Verify that the service can correctly calculate
     * the percentage change in accounts between current and last week
     */
    @Test
    public void testGetChangeAccount() {
        // When
        Double change = statisticsService.getChangeAccount();

        // Then
        assertNotNull(change);
    }

    /**
     * Test case TC009: Get exam change percentage
     * Purpose: Verify that the service can correctly calculate
     * the percentage change in exams between current and last week
     */
    @Test
    public void testGetChangeExam() {
        // When
        Double change = statisticsService.getChangeExam();

        // Then
        assertNotNull(change);
    }

    /**
     * Test case TC010: Test isSameDay method
     * Purpose: Verify that the service can correctly determine
     * if two dates are on the same day
     */
    @Test
    public void testIsSameDay() {
        // Given
        DateTime now = new DateTime();
        DateTime sameDay = now.withHourOfDay(12);
        DateTime differentDay = now.plusDays(1);

        // Then
        assertTrue(StatisticsServiceImpl.isSameDay(now, sameDay));
        assertFalse(StatisticsServiceImpl.isSameDay(now, differentDay));
    }

    /**
     * Test case TC011: Test isSameWeek method
     * Purpose: Verify that the service can correctly determine
     * if two dates are in the same week
     */
    @Test
    public void testIsSameWeek() {
        // Given
        DateTime now = new DateTime();
        DateTime sameWeek = now.plusDays(2);
        DateTime differentWeek = now.plusWeeks(1);

        // Then
        assertTrue(StatisticsServiceImpl.isSameWeek(now, sameWeek));
        assertFalse(StatisticsServiceImpl.isSameWeek(now, differentWeek));
    }

    /**
     * Test case TC012: Test isLastWeek method
     * Purpose: Verify that the service can correctly determine
     * if a date is from last week
     */
    @Test
    public void testIsLastWeek() {
        // Given
        DateTime now = new DateTime();
        DateTime lastWeek = now.minusWeeks(1);
        DateTime twoWeeksAgo = now.minusWeeks(2);

        // Then
        assertTrue(StatisticsServiceImpl.isLastWeek(now, lastWeek));
        assertFalse(StatisticsServiceImpl.isLastWeek(now, twoWeeksAgo));
    }
} 