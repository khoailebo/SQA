package com.thanhtam.backend.unittest;

/**
 * Bảng test case cho StatisticsServiceImpl
 *
 * STT | Function | Testcase | Mô tả chức năng | Mục đích | Input | Expected Output | Ghi chú | Kết quả
 * ----|----------|----------|-----------------|----------|--------|-----------------|---------|--------
 * 1 | CountExamTotal | testCountExamTotal | Đếm tổng số bài kiểm tra | Kiểm tra chức năng đếm tổng số bài kiểm tra | statisticsService.countExamTotal() | Số lượng bài kiểm tra >= 1 | | Pass
 * 2 | CountQuestionTotal | testCountQuestionTotal | Đếm tổng số câu hỏi | Kiểm tra chức năng đếm tổng số câu hỏi | statisticsService.countQuestionTotal() | Số lượng câu hỏi >= 1 | | Pass
 * 3 | CountAccountTotal | testCountAccountTotal | Đếm tổng số tài khoản | Kiểm tra chức năng đếm tổng số tài khoản | statisticsService.countAccountTotal() | Số lượng tài khoản >= 1 | | Pass
 * 4 | CountExamUserTotal | testCountExamUserTotal | Đếm tổng số lượt làm bài | Kiểm tra chức năng đếm tổng số lượt làm bài | statisticsService.countExamUserTotal() | Số lượng lượt làm bài >= 1 | | Pass
 * 5 | GetChangeExamUser | testGetChangeExamUser | Tính toán sự thay đổi số lượt làm bài | Kiểm tra chức năng tính tỷ lệ thay đổi | statisticsService.getChangeExamUser() | Kết quả không null và là số thập phân | | Pass
 * 6 | CountExamUserLastedSevenDaysTotal | testCountExamUserLastedSevenDaysTotal | Đếm số lượt làm bài 7 ngày gần nhất | Kiểm tra chức năng đếm theo ngày | statisticsService.countExamUserLastedSevenDaysTotal() | List 7 phần tử tương ứng 7 ngày | | Pass
 * 7 | GetChangeQuestion | testGetChangeQuestion | Tính toán sự thay đổi số câu hỏi | Kiểm tra chức năng tính tỷ lệ thay đổi | statisticsService.getChangeQuestion() | Kết quả không null và là số thập phân | | Pass
 * 8 | GetChangeAccount | testGetChangeAccount | Tính toán sự thay đổi số tài khoản | Kiểm tra chức năng tính tỷ lệ thay đổi | statisticsService.getChangeAccount() | Kết quả không null và là số thập phân | | Pass
 * 9 | GetChangeExam | testGetChangeExam | Tính toán sự thay đổi số bài kiểm tra | Kiểm tra chức năng tính tỷ lệ thay đổi | statisticsService.getChangeExam() | Kết quả không null và là số thập phân | | Pass
 * 10 | IsSameDay | testIsSameDay | Kiểm tra cùng ngày | Kiểm tra chức năng so sánh ngày | StatisticsServiceImpl.isSameDay(now, now) | true nếu cùng ngày | | Pass
 * 11 | IsSameWeek | testIsSameWeek | Kiểm tra cùng tuần | Kiểm tra chức năng so sánh tuần | StatisticsServiceImpl.isSameWeek(now, now) | true nếu cùng tuần | | Pass
 * 12 | IsLastWeek | testIsLastWeek | Kiểm tra tuần trước | Kiểm tra chức năng xác định tuần trước | StatisticsServiceImpl.isLastWeek(now, lastWeek) | true nếu là tuần trước | | Pass
 */

import com.thanhtam.backend.entity.Exam;
import com.thanhtam.backend.entity.ExamUser;
import com.thanhtam.backend.entity.Question;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.repository.ExamRepository;
import com.thanhtam.backend.repository.ExamUserRepository;
import com.thanhtam.backend.repository.QuestionRepository;
import com.thanhtam.backend.repository.UserRepository;
import com.thanhtam.backend.service.StatisticsServiceImpl;
import org.joda.time.DateTime;
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

import static org.junit.Assert.*;

/**
 * Unit test cho StatisticsServiceImpl.
 * Mục đích: Kiểm tra các chức năng thống kê của hệ thống
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional  // Đảm bảo tất cả các thay đổi cơ sở dữ liệu được rollback sau mỗi test
public class StatisticsServiceImplTest {

    @Autowired
    private StatisticsServiceImpl statisticsService;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ExamUserRepository examUserRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Exam testExam;
    private Question testQuestion;
    private ExamUser testExamUser;

    /**
     * Khởi tạo dữ liệu test trước mỗi test case.
     * Tạo các đối tượng cần thiết: User, Exam, Question, ExamUser
     */
    @Before
    public void setUp() {
        // Create test user
        testUser = new User();
        testUser.setUsername("testuser_" + System.currentTimeMillis());
        testUser.setEmail("test_" + System.currentTimeMillis() + "@example.com");
        testUser.setPassword("password");
        testUser = userRepository.save(testUser);

        // Create test exam
        testExam = new Exam();
        testExam.setTitle("Test Exam");
        testExam.setDurationExam(60);
        testExam.setShuffle(true);
        testExam.setCanceled(false);
        testExam.setBeginExam(new Date());
        testExam.setFinishExam(new Date(System.currentTimeMillis() + 3600000));
        testExam.setQuestionData("[]");
        testExam.setCreatedBy(testUser);
        testExam = examRepository.save(testExam);

        // Create test question
        testQuestion = new Question();
        testQuestion.setQuestionText("Test Question");
        testQuestion = questionRepository.save(testQuestion);

        // Create test exam user
        testExamUser = new ExamUser();
        testExamUser.setUser(testUser);
        testExamUser.setExam(testExam);
        testExamUser.setIsStarted(false);
        testExamUser.setIsFinished(false);
        testExamUser.setRemainingTime(testExam.getDurationExam() * 60);
        testExamUser.setTotalPoint(-1.0);
        testExamUser = examUserRepository.save(testExamUser);
    }

    /**
     * Test đếm tổng số bài kiểm tra.
     * Mục đích: Kiểm tra chức năng đếm tổng số bài kiểm tra trong hệ thống
     * Expected output: Số lượng bài kiểm tra phải >= 1 (ít nhất là bài test đã tạo)
     */
    @Test
    public void testCountExamTotal() {
        long count = statisticsService.countExamTotal();
        assertTrue("Exam count should be at least 1", count >= 1);
    }

    /**
     * Test đếm tổng số câu hỏi.
     * Mục đích: Kiểm tra chức năng đếm tổng số câu hỏi trong hệ thống
     * Expected output: Số lượng câu hỏi phải >= 1 (ít nhất là câu hỏi test đã tạo)
     */
    @Test
    public void testCountQuestionTotal() {
        long count = statisticsService.countQuestionTotal();
        assertTrue("Question count should be at least 1", count >= 1);
    }

    /**
     * Test đếm tổng số tài khoản.
     * Mục đích: Kiểm tra chức năng đếm tổng số tài khoản trong hệ thống
     * Expected output: Số lượng tài khoản phải >= 1 (ít nhất là tài khoản test đã tạo)
     */
    @Test
    public void testCountAccountTotal() {
        long count = statisticsService.countAccountTotal();
        assertTrue("Account count should be at least 1", count >= 1);
    }

    /**
     * Test đếm tổng số lượt làm bài kiểm tra.
     * Mục đích: Kiểm tra chức năng đếm tổng số lượt làm bài kiểm tra trong hệ thống
     * Expected output: Số lượng lượt làm bài phải >= 1 (ít nhất là lượt làm bài test đã tạo)
     */
    @Test
    public void testCountExamUserTotal() {
        long count = statisticsService.countExamUserTotal();
        assertTrue("ExamUser count should be at least 1", count >= 1);
    }

    /**
     * Test tính toán sự thay đổi số lượt làm bài kiểm tra.
     * Mục đích: Kiểm tra chức năng tính toán tỷ lệ thay đổi số lượt làm bài giữa tuần này và tuần trước
     * Expected output:
     * - Kết quả không được null
     * - Kết quả là một số thập phân biểu thị phần trăm thay đổi
     */
    @Test
    public void testGetChangeExamUser() {
        // Create exam users with different dates
        List<ExamUser> examUsers = new ArrayList<>();

        // Current week exam user
        ExamUser currentWeekExamUser = new ExamUser();
        currentWeekExamUser.setUser(testUser);
        currentWeekExamUser.setExam(testExam);
        currentWeekExamUser.setTimeFinish(new Date());
        examUsers.add(currentWeekExamUser);

        // Last week exam user
        ExamUser lastWeekExamUser = new ExamUser();
        lastWeekExamUser.setUser(testUser);
        lastWeekExamUser.setExam(testExam);
        lastWeekExamUser.setTimeFinish(new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000));
        examUsers.add(lastWeekExamUser);

        examUserRepository.saveAll(examUsers);

        Double change = statisticsService.getChangeExamUser();
        assertNotNull("Change should not be null", change);
    }

    /**
     * Test đếm số lượt làm bài kiểm tra trong 7 ngày gần nhất.
     * Mục đích: Kiểm tra chức năng đếm số lượt làm bài kiểm tra cho mỗi ngày trong 7 ngày gần nhất
     * Expected output:
     * - Kết quả không được null
     * - Kết quả là một list có 7 phần tử tương ứng với 7 ngày
     */
    @Test
    public void testCountExamUserLastedSevenDaysTotal() {
        // Create exam users for the last 7 days
        List<ExamUser> examUsers = new ArrayList<>();
        DateTime now = new DateTime();

        for (int i = 0; i < 7; i++) {
            ExamUser examUser = new ExamUser();
            examUser.setUser(testUser);
            examUser.setExam(testExam);
            examUser.setTimeFinish(now.minusDays(i).toDate());
            examUsers.add(examUser);
        }

        examUserRepository.saveAll(examUsers);

        List<Long> counts = statisticsService.countExamUserLastedSevenDaysTotal();
        assertNotNull("Counts should not be null", counts);
        assertEquals("Should have 7 days of counts", 7, counts.size());
    }

    /**
     * Test tính toán sự thay đổi số câu hỏi.
     * Mục đích: Kiểm tra chức năng tính toán tỷ lệ thay đổi số câu hỏi giữa tuần này và tuần trước
     * Expected output:
     * - Kết quả không được null
     * - Kết quả là một số thập phân biểu thị phần trăm thay đổi
     */
    @Test
    public void testGetChangeQuestion() {
        // Create questions with different dates
        List<Question> questions = new ArrayList<>();

        // Current week question
        Question currentWeekQuestion = new Question();
        currentWeekQuestion.setQuestionText("Current Week Question");
        currentWeekQuestion.setCreatedDate(new Date());
        questions.add(currentWeekQuestion);

        // Last week question
        Question lastWeekQuestion = new Question();
        lastWeekQuestion.setQuestionText("Last Week Question");
        lastWeekQuestion.setCreatedDate(new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000));
        questions.add(lastWeekQuestion);

        questionRepository.saveAll(questions);

        Double change = statisticsService.getChangeQuestion();
        assertNotNull("Change should not be null", change);
    }

    /**
     * Test tính toán sự thay đổi số tài khoản.
     * Mục đích: Kiểm tra chức năng tính toán tỷ lệ thay đổi số tài khoản giữa tuần này và tuần trước
     * Expected output:
     * - Kết quả không được null
     * - Kết quả là một số thập phân biểu thị phần trăm thay đổi
     */
    @Test
    public void testGetChangeAccount() {
        // Create users with different dates
        List<User> users = new ArrayList<>();

        // Current week user
        User currentWeekUser = new User();
        currentWeekUser.setUsername("currentweek_" + System.currentTimeMillis());
        currentWeekUser.setEmail("currentweek_" + System.currentTimeMillis() + "@example.com");
        currentWeekUser.setPassword("password");
        currentWeekUser.setCreatedDate(new Date());
        users.add(currentWeekUser);

        // Last week user
        User lastWeekUser = new User();
        lastWeekUser.setUsername("lastweek_" + System.currentTimeMillis());
        lastWeekUser.setEmail("lastweek_" + System.currentTimeMillis() + "@example.com");
        lastWeekUser.setPassword("password");
        lastWeekUser.setCreatedDate(new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000));
        users.add(lastWeekUser);

        userRepository.saveAll(users);

        Double change = statisticsService.getChangeAccount();
        assertNotNull("Change should not be null", change);
    }

    /**
     * Test tính toán sự thay đổi số bài kiểm tra.
     * Mục đích: Kiểm tra chức năng tính toán tỷ lệ thay đổi số bài kiểm tra giữa tuần này và tuần trước
     * Expected output:
     * - Kết quả không được null
     * - Kết quả là một số thập phân biểu thị phần trăm thay đổi
     */
    @Test
    public void testGetChangeExam() {
        // Create exams with different dates
        List<Exam> exams = new ArrayList<>();

        // Current week exam
        Exam currentWeekExam = new Exam();
        currentWeekExam.setTitle("Current Week Exam");
        currentWeekExam.setCreatedDate(new Date());
        currentWeekExam.setCreatedBy(testUser);
        exams.add(currentWeekExam);

        // Last week exam
        Exam lastWeekExam = new Exam();
        lastWeekExam.setTitle("Last Week Exam");
        lastWeekExam.setCreatedDate(new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000));
        lastWeekExam.setCreatedBy(testUser);
        exams.add(lastWeekExam);

        examRepository.saveAll(exams);

        Double change = statisticsService.getChangeExam();
        assertNotNull("Change should not be null", change);
    }

    /**
     * Test kiểm tra hai ngày có phải là cùng một ngày không.
     * Mục đích: Kiểm tra chức năng so sánh ngày
     * Expected output:
     * - Trả về true nếu hai ngày là cùng một ngày
     * - Trả về false nếu hai ngày khác nhau
     */
    @Test
    public void testIsSameDay() {
        DateTime now = new DateTime();
        assertTrue("Same day should return true",
                StatisticsServiceImpl.isSameDay(now, now));

        DateTime differentDay = now.plusDays(1);
        assertFalse("Different day should return false",
                StatisticsServiceImpl.isSameDay(now, differentDay));
    }

    /**
     * Test kiểm tra hai ngày có phải là cùng một tuần không.
     * Mục đích: Kiểm tra chức năng so sánh tuần
     * Expected output:
     * - Trả về true nếu hai ngày thuộc cùng một tuần
     * - Trả về false nếu hai ngày thuộc hai tuần khác nhau
     */
    @Test
    public void testIsSameWeek() {
        DateTime now = new DateTime();
        assertTrue("Same week should return true",
                StatisticsServiceImpl.isSameWeek(now, now));

        DateTime differentWeek = now.plusWeeks(1);
        assertFalse("Different week should return false",
                StatisticsServiceImpl.isSameWeek(now, differentWeek));
    }

    /**
     * Test kiểm tra một ngày có phải là tuần trước của ngày hiện tại không.
     * Mục đích: Kiểm tra chức năng xác định tuần trước
     * Expected output:
     * - Trả về true nếu ngày thứ hai là tuần trước của ngày thứ nhất
     * - Trả về false nếu ngày thứ hai không phải là tuần trước của ngày thứ nhất
     */
    @Test
    public void testIsLastWeek() {
        DateTime now = new DateTime();
        DateTime lastWeek = now.minusWeeks(1);
        assertTrue("Last week should return true",
                StatisticsServiceImpl.isLastWeek(now, lastWeek));

        DateTime thisWeek = now;
        assertFalse("This week should return false",
                StatisticsServiceImpl.isLastWeek(now, thisWeek));
    }
}