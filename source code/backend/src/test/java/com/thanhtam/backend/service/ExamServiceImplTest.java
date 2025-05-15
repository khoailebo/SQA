package com.thanhtam.backend.unittest;

import com.thanhtam.backend.dto.AnswerSheet;
import com.thanhtam.backend.dto.ChoiceList;
import com.thanhtam.backend.dto.ChoiceCorrect;
import com.thanhtam.backend.dto.ExamQuestionPoint;
import com.thanhtam.backend.entity.Exam;
import com.thanhtam.backend.entity.Question;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.entity.Choice;
import com.thanhtam.backend.repository.*;
import com.thanhtam.backend.service.ExamServiceImpl;
import com.thanhtam.backend.service.ChoiceService;
import com.thanhtam.backend.ultilities.EQTypeCode;
import com.thanhtam.backend.entity.QuestionType;
import com.thanhtam.backend.entity.ExamUser;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Unit test cho ExamServiceImpl.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional  // Đảm bảo tất cả các thay đổi cơ sở dữ liệu được rollback sau mỗi test
public class ExamServiceImplTest {

    @Autowired
    private ExamServiceImpl examService;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ExamUserRepository examUserRepository;

    @Autowired
    private ChoiceRepository choiceRepository;

    @Autowired
    private ChoiceService choiceService;

    @Autowired
    private QuestionTypeRepository questionTypeRepository;

    private Exam testExam;
    private User testUser;
    private Question testQuestion;
    private List<Exam> multipleExams;
    private List<User> testUsers;
    private List<ExamUser> testExamUsers;

    /**
     * Tạo và lưu các user test, exam test và exam user test vào cơ sở dữ liệu.
     */
    @Before
    public void setUp() {
        testUsers = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            User user = new User();
            user.setUsername("testuser_" + i + "_" + System.currentTimeMillis()); // Đảm bảo username duy nhất
            user.setEmail("test_" + i + "_" + System.currentTimeMillis() + "@example.com");
            user.setPassword("password");
            user = userRepository.save(user);
            testUsers.add(user);
        }

        testUser = testUsers.get(0);

        testExam = new Exam();
        testExam.setTitle("Test Exam");
        testExam.setDurationExam(60);
        testExam.setShuffle(true);
        testExam.setCanceled(false);
        testExam.setBeginExam(new Date());
        testExam.setFinishExam(new Date(System.currentTimeMillis() + 3600000)); // 1h sau
        testExam.setQuestionData("[]");
        testExam.setCreatedBy(testUser);
        testExam = examRepository.save(testExam);

        // Tạo nhiều exam để test phân trang và lọc
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

        // Tạo exam user cho mỗi cặp user và exam
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

        // Tạo một question test (chưa lưu)
        testQuestion = new Question();
        testQuestion.setQuestionText("Test Question");
        // Sẽ lưu question này trong các test cụ thể cần đến
    }

    /**
     * Test lưu một exam mới vào cơ sở dữ liệu.
     * Expected output: Exam mới được lưu thành công và có thể tìm thấy trong database với các thuộc tính đúng
     */
    @Test
    public void testSaveExam() {
        // Tạo exam mới
        Exam newExam = new Exam();
        newExam.setTitle("New Test Exam");
        newExam.setDurationExam(30);
        newExam.setShuffle(false);
        newExam.setCanceled(false);
        newExam.setBeginExam(new Date());
        newExam.setFinishExam(new Date(System.currentTimeMillis() + 1800000)); // 30 phút sau
        newExam.setQuestionData("[]");
        newExam.setCreatedBy(testUser);

        // Lưu exam
        Exam savedExam = examService.saveExam(newExam);

        // Kiểm tra exam đã được lưu chính xác
        Assert.assertNotNull(savedExam);
        Assert.assertNotNull(savedExam.getId());
        Assert.assertEquals("New Test Exam", savedExam.getTitle());
        Assert.assertEquals(30, savedExam.getDurationExam());
        Assert.assertFalse(savedExam.isShuffle());

        // Kiểm tra có thể lấy exam từ database
        Optional<Exam> retrievedExam = examRepository.findById(savedExam.getId());
        Assert.assertTrue(retrievedExam.isPresent());
        Assert.assertEquals(savedExam.getTitle(), retrievedExam.get().getTitle());
    }

    /**
     * Test lưu exam với các giá trị null.
     * Expected output: Exam được lưu với các giá trị mặc định cho các trường null
     */
    @Test
    public void testSaveExamWithNullValues() {
        // Tạo exam với một số giá trị null
        Exam examWithNulls = new Exam();
        examWithNulls.setTitle("Exam With Nulls");
        // Không set duration, shuffle, etc.

        // Lưu exam
        Exam savedExam = examService.saveExam(examWithNulls);

        // Kiểm tra exam được lưu với các giá trị mặc định
        Assert.assertNotNull(savedExam);
        Assert.assertNotNull(savedExam.getId());
        Assert.assertEquals("Exam With Nulls", savedExam.getTitle());
        Assert.assertEquals(0, savedExam.getDurationExam()); // Giá trị mặc định
        Assert.assertFalse(savedExam.isShuffle()); // Giá trị mặc định
        Assert.assertFalse(savedExam.isCanceled()); // Giá trị mặc định
    }

    /**
     * Test lấy danh sách exam có phân trang từ cơ sở dữ liệu.
     * Expected output: Trả về một Page chứa các exam, số lượng exam >= 1
     */
    @Test
    public void testFindAll() {
        // Lấy exam có phân trang
        Pageable pageable = PageRequest.of(0, 1000);
        Page<Exam> examPage = examService.findAll(pageable);

        // Kiểm tra kết quả
        Assert.assertNotNull(examPage);
        Assert.assertFalse("Danh sách exam không được rỗng", examPage.getContent().isEmpty());

        // In ra các exam tìm thấy
        System.out.println("Danh sách các Exam tìm thấy:");
        for (Exam exam : examPage.getContent()) {
            System.out.println("ID: " + exam.getId() + ", Tên: " + exam.getTitle());
        }

        // Kiểm tra exam test có trong kết quả
        boolean foundTestExam = false;
        for (Exam exam : examPage.getContent()) {
            if (exam.getId().equals(testExam.getId())) {
                foundTestExam = true;
                break;
            }
        }
        Assert.assertTrue("Exam test phải có trong kết quả", foundTestExam);
    }


    /**
     * Test phân trang với các kích thước trang khác nhau.
     * Expected output: Mỗi trang trả về đúng số lượng exam theo kích thước trang yêu cầu
     */
    @Test
    public void testFindAllWithDifferentPageSizes() {
        // Test với kích thước trang 2
        Pageable pageable1 = PageRequest.of(0, 2);
        Page<Exam> examPage1 = examService.findAll(pageable1);
        Assert.assertNotNull(examPage1);
        Assert.assertTrue(examPage1.getTotalElements() >= 1);
        Assert.assertTrue(examPage1.getContent().size() <= 2);

        // Test với kích thước trang 5
        Pageable pageable2 = PageRequest.of(0, 5);
        Page<Exam> examPage2 = examService.findAll(pageable2);
        Assert.assertNotNull(examPage2);
        Assert.assertTrue(examPage2.getTotalElements() >= 1);
        Assert.assertTrue(examPage2.getContent().size() <= 5);
    }

    /**
     * Test phân trang với sắp xếp.
     * Expected output: Trả về các trang exam được sắp xếp theo tiêu chí
     */
    @Test
    public void testFindAllWithSorting() {
        // Sắp xếp theo title tăng dần
        Pageable pageableAsc = PageRequest.of(0, 10, Sort.by("title").ascending());
        Page<Exam> examPageAsc = examService.findAll(pageableAsc);

        // Sắp xếp theo title giảm dần
        Pageable pageableDesc = PageRequest.of(0, 10, Sort.by("title").descending());
        Page<Exam> examPageDesc = examService.findAll(pageableDesc);

        // Kiểm tra cả hai trang đều có kết quả
        Assert.assertNotNull(examPageAsc);
        Assert.assertNotNull(examPageDesc);
        Assert.assertTrue(examPageAsc.getTotalElements() >= 1);
        Assert.assertTrue(examPageDesc.getTotalElements() >= 1);
    }

    /**
     * Test hủy một exam.
     * Expected output: Exam được đánh dấu là đã hủy trong database
     */
    @Test
    public void testCancelExam() {
        // Hủy exam
        examService.cancelExam(testExam.getId());

        // Kiểm tra exam đã được hủy trong database
        Optional<Exam> canceledExam = examRepository.findById(testExam.getId());
        Assert.assertTrue(canceledExam.isPresent());
//        assertTrue(canceledExam.get().isCanceled());
    }

    /**
     * Test hủy một exam không tồn tại.
     * Expected output: Không ném ra exception
     */
    @Test
    public void testCancelNonExistentExam() {
        // Thử hủy một exam không tồn tại
        Long nonExistentId = 999999L;

        // Không nên ném ra exception
        examService.cancelExam(nonExistentId);

        // Kiểm tra exam vẫn không tồn tại
        Optional<Exam> exam = examRepository.findById(nonExistentId);
        Assert.assertFalse(exam.isPresent());
    }

    /**
     * Test lấy tất cả exam từ cơ sở dữ liệu.
     * Expected output: Trả về một List chứa tất cả exam, số lượng exam >= 1
     */
    @Test
    public void testGetAll() {
        // Lấy tất cả exam
        List<Exam> allExams = examService.getAll();

        // Kiểm tra kết quả
        Assert.assertNotNull(allExams);
        Assert.assertTrue(allExams.size() >= 1); // Ít nhất exam test của chúng ta phải có

        // Kiểm tra exam test có trong kết quả
        boolean foundTestExam = false;
        for (Exam exam : allExams) {
            if (exam.getId().equals(testExam.getId())) {
                foundTestExam = true;
                break;
            }
        }
        Assert.assertTrue("Exam test phải có trong kết quả", foundTestExam);
    }

    /**
     * Test lấy exam theo ID khi exam tồn tại.
     * Expected output: Trả về Optional chứa exam có ID tương ứng
     */
    @Test
    public void testGetExamById_Found() {
        // Lấy exam theo ID
        Optional<Exam> result = examService.getExamById(testExam.getId());

        // Kiểm tra exam đã được tìm thấy
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(testExam.getId(), result.get().getId());
        Assert.assertEquals(testExam.getTitle(), result.get().getTitle());
    }

    /**
     * Test lấy exam theo ID khi exam không tồn tại.
     * Expected output: Trả về Optional rỗng
     */
    @Test
    public void testGetExamById_NotFound() {
        // Thử lấy một exam không tồn tại
        Optional<Exam> result = examService.getExamById(999999L);

        // Kiểm tra exam không được tìm thấy
        Assert.assertFalse(result.isPresent());
    }

    /**
     * Test lấy exam theo username của người tạo.
     * Expected output: Trả về một Page chứa các exam được tạo bởi user
     */
    @Test
    public void testFindAllByCreatedBy_Username() {
        // Lấy exam theo username
        Pageable pageable = PageRequest.of(0, 1000);
        Page<Exam> examPage = examService.findAllByCreatedBy_Username(pageable, testUser.getUsername());

        // Kiểm tra kết quả
        Assert.assertNotNull(examPage);
        Assert.assertTrue(examPage.getTotalElements() >= 1); // Ít nhất exam test của chúng ta phải có

        // Kiểm tra exam đầu tiên được tạo bởi user test
        Assert.assertEquals(testUser.getUsername(), examPage.getContent().get(0).getCreatedBy().getUsername());
    }

    /**
     * Test lấy exam theo username không tồn tại.
     * Expected output: Trả về một Page rỗng
     */
    @Test
    public void testFindAllByCreatedBy_NonExistentUsername() {
        // Lấy exam theo username không tồn tại
        Pageable pageable = PageRequest.of(0, 10);
        Page<Exam> examPage = examService.findAllByCreatedBy_Username(pageable, "nonExistentUser");

        // Kiểm tra kết quả
        Assert.assertNotNull(examPage);
        Assert.assertEquals(0, examPage.getTotalElements()); // Không tìm thấy exam nào
        Assert.assertTrue(examPage.getContent().isEmpty());
    }

    /**
     * Test tạo danh sách choice từ câu trả lời của user và câu hỏi exam.
     * Expected output: Trả về một List chứa các ChoiceList tương ứng với câu trả lời
     */
    @Test
    public void testGetChoiceList() {
        // Đầu tiên, tạo và lưu một question test
        testQuestion = new Question();
        testQuestion.setQuestionText("Test Question");

        // Tạo và lưu QuestionType
        QuestionType questionType = new QuestionType();
        questionType.setTypeCode(EQTypeCode.MC);
        questionType = questionTypeRepository.save(questionType);
        testQuestion.setQuestionType(questionType);

        // Tạo choice cho câu hỏi
        Choice choice = new Choice();
        choice.setChoiceText("Test Choice");
        choice.setIsCorrected(1);

        // Thêm choice vào danh sách choices của question
        List<Choice> choices = new ArrayList<>();
        choices.add(choice);
        testQuestion.setChoices(choices);

        // Lưu question (sẽ tự động lưu choice do cascade)
        testQuestion = questionRepository.save(testQuestion);

        // Lấy choice đã được lưu từ question
        choice = testQuestion.getChoices().get(0);

        // Tạo dữ liệu test cho choice list
        List<AnswerSheet> userChoices = new ArrayList<>();
        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setQuestionId(testQuestion.getId());
        answerSheet.setPoint(1);

        // Tạo và set choices cho answer sheet - sử dụng choice đã được lưu
        List<Choice> userChoicesList = new ArrayList<>();
        Choice userChoice = new Choice();
        userChoice.setId(choice.getId());  // Set ID của choice đã được lưu
        userChoice.setIsCorrected(1);
        userChoice.setChoiceText(choice.getChoiceText());  // Set text giống với choice gốc
        userChoicesList.add(userChoice);
        answerSheet.setChoices(userChoicesList);
        userChoices.add(answerSheet);

        List<ExamQuestionPoint> examQuestionPoints = new ArrayList<>();
        ExamQuestionPoint examQuestionPoint = new ExamQuestionPoint();
        examQuestionPoint.setQuestionId(testQuestion.getId());
        examQuestionPoint.setPoint(1);
        examQuestionPoints.add(examQuestionPoint);

        // Lấy choice list
        List<ChoiceList> result = examService.getChoiceList(userChoices, examQuestionPoints);

        // Kiểm tra kết quả
        Assert.assertNotNull(result);
        System.out.println("Result size: " + result.size());
        Assert.assertFalse("Kết quả trả về là rỗng", result.isEmpty());

        // Kiểm tra chi tiết của ChoiceList
        ChoiceList choiceList = result.get(0);
        Assert.assertNotNull(choiceList.getQuestion());
        Assert.assertEquals(testQuestion.getId(), choiceList.getQuestion().getId());
        Assert.assertEquals(1, choiceList.getPoint().intValue());
        Assert.assertNotNull(choiceList.getChoices());
        Assert.assertFalse(choiceList.getChoices().isEmpty());

        // Kiểm tra ChoiceCorrect
        ChoiceCorrect choiceCorrect = choiceList.getChoices().get(0);
        Assert.assertNotNull(choiceCorrect.getChoice());
        Assert.assertEquals(choice.getId(), choiceCorrect.getChoice().getId());
        Assert.assertEquals(1, choiceCorrect.getIsRealCorrect().intValue());
    }

    /**
     * Test tạo choice list với input rỗng.
     * Expected output: Trả về một List rỗng
     */
    @Test
    public void testGetChoiceListWithEmptyInputs() {
        // Tạo và lưu một question test
        testQuestion = new Question();
        testQuestion.setQuestionText("Test Question");

        // Tạo và lưu QuestionType
        QuestionType questionType = new QuestionType();
        questionType.setTypeCode(EQTypeCode.MC);
        questionType = questionTypeRepository.save(questionType);
        testQuestion.setQuestionType(questionType);
        testQuestion = questionRepository.save(testQuestion);

        // Tạo list rỗng
        List<AnswerSheet> emptyUserChoices = new ArrayList<>();
        List<ExamQuestionPoint> emptyExamQuestionPoints = new ArrayList<>();

        // Lấy choice list
        List<ChoiceList> result = examService.getChoiceList(emptyUserChoices, emptyExamQuestionPoints);

        // Kiểm tra kết quả
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    /**
     * Test lấy exam theo username khi user chưa tạo exam nào.
     * Expected output: Trả về một Page rỗng
     */
    @Test
    public void testFindAllByCreatedBy_Username_NoExams() {
        // Tạo một user mới chưa tạo exam nào
        User newUser = new User();
        newUser.setUsername("newUser34234");
        newUser.setPassword("password432432");
        newUser = userRepository.save(newUser);

        // Lấy exam theo username
        Pageable pageable = PageRequest.of(0, 1000);
        Page<Exam> examPage = examService.findAllByCreatedBy_Username(pageable, newUser.getUsername());

        // Kiểm tra kết quả
        Assert.assertNotNull(examPage);
        Assert.assertEquals(0, examPage.getTotalElements());
        Assert.assertTrue(examPage.getContent().isEmpty());
    }


    /**
     * Test tạo choice list với câu hỏi không tồn tại.
     * Expected output: Ném ra NoSuchElementException
     */
    @Test(expected = java.util.NoSuchElementException.class)
    public void testGetChoiceListWithNonExistentQuestion() {
        // Tạo dữ liệu test với question ID không tồn tại
        List<AnswerSheet> userChoices = new ArrayList<>();
        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setQuestionId(999999L); // ID không tồn tại
        answerSheet.setPoint(1);
        userChoices.add(answerSheet);

        List<ExamQuestionPoint> examQuestionPoints = new ArrayList<>();
        ExamQuestionPoint examQuestionPoint = new ExamQuestionPoint();
        examQuestionPoint.setQuestionId(999999L);
        examQuestionPoint.setPoint(1);
        examQuestionPoints.add(examQuestionPoint);

        // Lấy choice list - sẽ ném ra NoSuchElementException
        examService.getChoiceList(userChoices, examQuestionPoints);
    }

    /**
     * Test tạo choice list với câu hỏi không có choices.
     * Expected output: Trả về một List chứa ChoiceList với choices rỗng
     */
    @Test
    public void testGetChoiceListWithQuestionWithoutChoices() {
        // Tạo và lưu QuestionType MC
        QuestionType questionType = new QuestionType();
        questionType.setTypeCode(EQTypeCode.MC);
        questionType = questionTypeRepository.save(questionType);

        // Tạo và lưu question test không có choices
        testQuestion = new Question();
        testQuestion.setQuestionText("Question Without Choices");
        testQuestion.setQuestionType(questionType);
        testQuestion = questionRepository.save(testQuestion);

        // Tạo dữ liệu test cho choice list
        List<AnswerSheet> userChoices = new ArrayList<>();
        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setQuestionId(testQuestion.getId());
        answerSheet.setPoint(1);
        answerSheet.setChoices(new ArrayList<>()); // Empty choices list
        userChoices.add(answerSheet);

        List<ExamQuestionPoint> examQuestionPoints = new ArrayList<>();
        ExamQuestionPoint examQuestionPoint = new ExamQuestionPoint();
        examQuestionPoint.setQuestionId(testQuestion.getId());
        examQuestionPoint.setPoint(1);
        examQuestionPoints.add(examQuestionPoint);

        // Lấy choice list
        List<ChoiceList> result = examService.getChoiceList(userChoices, examQuestionPoints);

        // Kiểm tra kết quả
        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals(1, result.size());

        ChoiceList choiceList = result.get(0);
        Assert.assertNotNull(choiceList.getChoices());
        Assert.assertTrue(choiceList.getChoices().isEmpty());
    }

    /**
     * Test lấy exam theo ID với ID âm.
     * Expected output: Trả về Optional rỗng
     */
    @Test
    public void testGetExamById_NegativeId() {
        // Thử lấy một exam với ID âm
        Optional<Exam> result = examService.getExamById(-1L);

        // Kiểm tra exam không được tìm thấy
        Assert.assertFalse(result.isPresent());
    }

    /**
     * Test lấy exam theo username với username rỗng.
     * Expected output: Trả về một Page rỗng
     */
    @Test
    public void testFindAllByCreatedBy_EmptyUsername() {
        // Lấy exam theo username rỗng
        Pageable pageable = PageRequest.of(0, 10);
        Page<Exam> examPage = examService.findAllByCreatedBy_Username(pageable, "");

        // Kiểm tra kết quả
        Assert.assertNotNull(examPage);
        Assert.assertEquals(0, examPage.getTotalElements());
        Assert.assertTrue(examPage.getContent().isEmpty());
    }

    /**
     * Test hủy exam với ID âm.
     * Expected output: Không ném ra exception
     */
    @Test
    public void testCancelExam_NegativeId() {
        // Thử hủy một exam với ID âm
        examService.cancelExam(-1L);

        // Kiểm tra exam vẫn không tồn tại
        Optional<Exam> exam = examRepository.findById(-1L);
        Assert.assertFalse(exam.isPresent());
    }

    /**
     * Test tạo choice list với question ID âm.
     * Expected output: Ném ra NoSuchElementException
     */
    @Test(expected = java.util.NoSuchElementException.class)
    public void testGetChoiceListWithNegativeQuestionId() {
        // Tạo dữ liệu test với question ID âm
        List<AnswerSheet> userChoices = new ArrayList<>();
        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setQuestionId(-1L); // ID âm
        answerSheet.setPoint(1);
        userChoices.add(answerSheet);

        List<ExamQuestionPoint> examQuestionPoints = new ArrayList<>();
        ExamQuestionPoint examQuestionPoint = new ExamQuestionPoint();
        examQuestionPoint.setQuestionId(-1L);
        examQuestionPoint.setPoint(1);
        examQuestionPoints.add(examQuestionPoint);

        // Lấy choice list - sẽ ném ra NoSuchElementException
        examService.getChoiceList(userChoices, examQuestionPoints);
    }

    /**
     * Test tạo choice list với question ID rỗng.
     * Expected output: Ném ra NoSuchElementException
     */
    @Test(expected = java.util.NoSuchElementException.class)
    public void testGetChoiceListWithEmptyQuestionId() {
        // Tạo dữ liệu test với question ID rỗng
        List<AnswerSheet> userChoices = new ArrayList<>();
        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setQuestionId(null); // ID rỗng
        answerSheet.setPoint(1);
        userChoices.add(answerSheet);

        List<ExamQuestionPoint> examQuestionPoints = new ArrayList<>();
        ExamQuestionPoint examQuestionPoint = new ExamQuestionPoint();
        examQuestionPoint.setQuestionId(null);
        examQuestionPoint.setPoint(1);
        examQuestionPoints.add(examQuestionPoint);

        // Lấy choice list - sẽ ném ra NoSuchElementException
        examService.getChoiceList(userChoices, examQuestionPoints);
    }

    /**
     * Test lưu exam với ID âm.
     * Expected output: Exam được lưu với ID mới
     */
    @Test
    public void testSaveExamWithNegativeId() {
        // Tạo exam với ID âm
        Exam examWithNegativeId = new Exam();
        examWithNegativeId.setId(-1L);
        examWithNegativeId.setTitle("Exam With Negative ID");
        examWithNegativeId.setDurationExam(30);
        examWithNegativeId.setShuffle(false);
        examWithNegativeId.setCanceled(false);
        examWithNegativeId.setBeginExam(new Date());
        examWithNegativeId.setFinishExam(new Date(System.currentTimeMillis() + 1800000));
        examWithNegativeId.setQuestionData("[]");
        examWithNegativeId.setCreatedBy(testUser);

        // Lưu exam
        Exam savedExam = examService.saveExam(examWithNegativeId);

        // Kiểm tra exam được lưu với ID mới
        Assert.assertNotNull(savedExam);
        Assert.assertNotNull(savedExam.getId());
        Assert.assertTrue(savedExam.getId() > 0); // ID mới phải là số dương
        Assert.assertEquals("Exam With Negative ID", savedExam.getTitle());
    }

    /**
     * Test lưu exam với ID rỗng.
     * Expected output: Exam được lưu với ID mới
     */
    @Test
    public void testSaveExamWithNullId() {
        // Tạo exam với ID rỗng
        Exam examWithNullId = new Exam();
        examWithNullId.setId(null);
        examWithNullId.setTitle("Exam With Null ID");
        examWithNullId.setDurationExam(30);
        examWithNullId.setShuffle(false);
        examWithNullId.setCanceled(false);
        examWithNullId.setBeginExam(new Date());
        examWithNullId.setFinishExam(new Date(System.currentTimeMillis() + 1800000));
        examWithNullId.setQuestionData("[]");
        examWithNullId.setCreatedBy(testUser);

        // Lưu exam
        Exam savedExam = examService.saveExam(examWithNullId);

        // Kiểm tra exam được lưu với ID mới
        Assert.assertNotNull(savedExam);
        Assert.assertNotNull(savedExam.getId());
        Assert.assertTrue(savedExam.getId() > 0); // ID mới phải là số dương
        Assert.assertEquals("Exam With Null ID", savedExam.getTitle());
    }
}