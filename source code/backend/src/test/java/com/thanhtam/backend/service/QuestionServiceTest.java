package com.thanhtam.backend.service;

import com.thanhtam.backend.dto.AnswerSheet;
import com.thanhtam.backend.dto.ExamQuestionPoint;
import com.thanhtam.backend.entity.*;
import com.thanhtam.backend.repository.*;
import com.thanhtam.backend.service.QuestionServiceImpl;
import com.thanhtam.backend.ultilities.DifficultyLevel;
import com.thanhtam.backend.ultilities.EQTypeCode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lớp test cho QuestionServiceImpl
 * Lớp này chứa các test case để kiểm tra chức năng của QuestionServiceImpl
 * bao gồm việc quản lý câu hỏi (thêm, sửa, xóa, tìm kiếm, chuyển đổi)
 */
@RunWith(org.springframework.test.context.junit4.SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback
public class QuestionServiceTest {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private ChoiceRepository choiceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionTypeRepository questionTypeRepository;

    @Autowired
    private QuestionServiceImpl questionService;

    static Question savedQuestion;
    static QuestionType questionType;
    static Part part;
    static List<Choice> choices;
    static Choice choice1;
    static Choice choice2;
    static User testUser;
    static boolean initail = false;

    /**
     * Phương thức setup chạy trước mỗi test
     * Tạo và lưu các đối tượng cần thiết cho test:
     * - Loại câu hỏi (True/False)
     * - Phần thi
     * - Các lựa chọn (True/False)
     * - Người dùng test
     * - Câu hỏi mẫu
     */
    @Before
    public void setUp() {
        // Tạo các lựa chọn

        if (!initail) {
            initail = true;
            questionType = new QuestionType();
            questionType.setTypeCode(EQTypeCode.TF);
            questionType.setDescription("True/False Question");
            questionType = questionTypeRepository.save(questionType);

            // Tạo và lưu phần thi
            part = new Part();
            part.setName("Test Part");
            part = partRepository.save(part);

            // Tạo người dùng test
            testUser = new User();
            testUser.setUsername("testuser");
            testUser.setEmail("test@example.com");
            userRepository.save(testUser);

            choices = new ArrayList<>();
            choice1 = new Choice();
            choice1.setChoiceText("True");
            choice1.setIsCorrected(1);
            choices.add(choice1);

            choice2 = new Choice();
            choice2.setChoiceText("False");
            choice2.setIsCorrected(0);
            choices.add(choice2);

//            choiceRepository.save(choice1);
//            choiceRepository.save(choice2);

            // Tạo và lưu câu hỏi
            Question q = new Question();
            q.setQuestionText("What is Java?");
            q.setDifficultyLevel(DifficultyLevel.EASY);
            q.setQuestionType(questionType);
            q.setPart(part);
            q.setChoices(choices);
            q.setDeleted(false);
            q.setCreatedBy(testUser);
//            questionService.save();
            savedQuestion = questionRepository.save(q);

//            choiceRepository.saveAll(choices);
        }
        // Tạo và lưu loại câu hỏil
    }

    /**
     * Test case TC001: Tìm câu hỏi theo ID với câu hỏi tồn tại
     * Mục đích: Kiểm tra service có thể lấy đúng thông tin câu hỏi
     * khi tìm kiếm bằng ID tồn tại
     */
    @Test
    public void testGetQuestionById_WithExistingQuestion() {
        // Khi
        Optional<Question> result = questionService.getQuestionById(savedQuestion.getId());

        // Thì
        assertTrue(result.isPresent());
        assertEquals("What is Java?", result.get().getQuestionText());
        assertEquals(DifficultyLevel.EASY, result.get().getDifficultyLevel());
        assertEquals(questionType.getTypeCode(), result.get().getQuestionType().getTypeCode());
        assertEquals(part.getName(), result.get().getPart().getName());
    }

    /**
     * Test case TC002: Tìm câu hỏi theo ID không tồn tại
     * Mục đích: Kiểm tra service xử lý đúng khi tìm kiếm
     * câu hỏi với ID không tồn tại
     */
    @Test
    public void testGetQuestionById_WithNonExistingQuestion() {
        // Khi
        Optional<Question> result = questionService.getQuestionById(999L);

        // Thì
        assertFalse(result.isPresent());
    }

    /**
     * Test case TC003: Tìm câu hỏi theo ID null
     * Mục đích: Kiểm tra service xử lý đúng khi tìm kiếm
     * câu hỏi với ID null
     */
    @Test
    public void testGetQuestionById_WithNullId() {
        // Khi
        Optional<Question> result = questionService.getQuestionById(null);

        // Thì
        assertFalse(result.isPresent());
    }

    /**
     * Test case TC004: Tìm câu hỏi theo phần thi với phần thi tồn tại
     * Mục đích: Kiểm tra service có thể lấy danh sách câu hỏi
     * thuộc một phần thi cụ thể
     */
    @Test
    public void testGetQuestionByPart_WithExistingPart() {
        // Khi
        List<Question> questions = questionService.getQuestionByPart(part);

        // Thì
        assertFalse(questions.isEmpty());
        assertEquals(savedQuestion.getId(), questions.get(0).getId());
        assertEquals(part.getId(), questions.get(0).getPart().getId());
    }

    /**
     * Test case TC005: Tìm câu hỏi theo phần thi không tồn tại
     * Mục đích: Kiểm tra service xử lý đúng khi tìm kiếm
     * câu hỏi theo phần thi không tồn tại
     */
    @Test
    public void testGetQuestionByPart_WithNonExistingPart() {
        // Khi
        List<Question> questions = questionService.getQuestionByPart(new Part());

        // Thì
        assertTrue(questions.isEmpty());
    }

    /**
     * Test case TC006: Tìm câu hỏi theo phần thi null
     * Mục đích: Kiểm tra service xử lý đúng khi tìm kiếm
     * câu hỏi theo phần thi null
     */
    @Test
    public void testGetQuestionByPart_WithNullPart() {
        // Khi
        List<Question> questions = questionService.getQuestionByPart(null);

        // Thì
        assertTrue(questions.isEmpty());
    }

    /**
     * Test case TC007: Tìm câu hỏi theo loại câu hỏi với loại câu hỏi tồn tại
     * Mục đích: Kiểm tra service có thể lấy danh sách câu hỏi
     * thuộc một loại câu hỏi cụ thể
     */
    @Test
    public void testGetQuestionByQuestionType_WithExistingType() {
        // Khi
        List<Question> questions = questionService.getQuestionByQuestionType(questionType);

        // Thì
        assertFalse(questions.isEmpty());
        assertEquals(savedQuestion.getId(), questions.get(0).getId());
        assertEquals(questionType.getTypeCode(), questions.get(0).getQuestionType().getTypeCode());
    }

    /**
     * Test case TC008: Tìm câu hỏi theo loại câu hỏi không tồn tại
     * Mục đích: Kiểm tra service xử lý đúng khi tìm kiếm
     * câu hỏi theo loại câu hỏi không tồn tại
     */
    @Test
    public void testGetQuestionByQuestionType_WithNonExistingType() {
        // Khi
        List<Question> questions = questionService.getQuestionByQuestionType(new QuestionType());

        // Thì
        assertTrue(questions.isEmpty());
    }

    /**
     * Test case TC009: Tìm câu hỏi theo loại câu hỏi null
     * Mục đích: Kiểm tra service xử lý đúng khi tìm kiếm
     * câu hỏi theo loại câu hỏi null
     */
    @Test
    public void testGetQuestionByQuestionType_WithNullType() {
        // Khi
        List<Question> questions = questionService.getQuestionByQuestionType(null);

        // Thì
        assertTrue(questions.isEmpty());
    }

    /**
     * Test case TC010: Lấy danh sách câu hỏi với điểm số với câu hỏi tồn tại
     * Mục đích: Kiểm tra service có thể lấy danh sách câu hỏi
     * kèm theo điểm số tương ứng
     */
    @Test
    public void testGetQuestionPointList_WithExistingQuestions() {
        // Cho
        List<ExamQuestionPoint> examQuestionPoints = new ArrayList<>();
        ExamQuestionPoint point = new ExamQuestionPoint();
        point.setQuestionId(savedQuestion.getId());
        point.setPoint(5);
        examQuestionPoints.add(point);

        // Khi
        List<Question> questions = questionService.getQuestionPointList(examQuestionPoints);

        // Thì
        assertFalse(questions.isEmpty());
        assertEquals(savedQuestion.getId(), questions.get(0).getId());
    }

    /**
     * Test case TC011: Lấy danh sách câu hỏi với điểm số với câu hỏi không tồn tại
     * Mục đích: Kiểm tra service xử lý đúng khi lấy danh sách câu hỏi
     * với điểm số của câu hỏi không tồn tại
     */
    @Test
    public void testGetQuestionPointList_WithNonExistingQuestions() {
        // Cho
        List<ExamQuestionPoint> examQuestionPoints = new ArrayList<>();
        ExamQuestionPoint point = new ExamQuestionPoint();
        point.setQuestionId(999L);
        point.setPoint(5);
        examQuestionPoints.add(point);

        // Khi
        List<Question> questions = questionService.getQuestionPointList(examQuestionPoints);

        // Thì
        assertTrue(questions.isEmpty());
    }

    /**
     * Test case TC012: Lấy danh sách câu hỏi với điểm số với danh sách rỗng
     * Mục đích: Kiểm tra service xử lý đúng khi lấy danh sách câu hỏi
     * với danh sách điểm số rỗng
     */
    @Test
    public void testGetQuestionPointList_WithEmptyList() {
        // Cho
        List<ExamQuestionPoint> examQuestionPoints = new ArrayList<>();

        // Khi
        List<Question> questions = questionService.getQuestionPointList(examQuestionPoints);

        // Thì
        assertTrue(questions.isEmpty());
    }

    /**
     * Test case TC013: Chuyển đổi danh sách câu hỏi thành phiếu trả lời với câu hỏi tồn tại
     * Mục đích: Kiểm tra service có thể chuyển đổi danh sách câu hỏi
     * thành định dạng phiếu trả lời
     */
    @Test
    public void testConvertFromQuestionList_WithExistingQuestions() {
        // Cho
        List<Question> questionList = new ArrayList<>();
        questionList.add(savedQuestion);

        // Khi
        List<AnswerSheet> answerSheets = questionService.convertFromQuestionList(questionList);

        // Thì
        assertFalse(answerSheets.isEmpty());
        assertEquals(savedQuestion.getId(), answerSheets.get(0).getQuestionId());
        assertEquals(2, answerSheets.get(0).getChoices().size());
        assertEquals(0, answerSheets.get(0).getChoices().get(1).getIsCorrected());
//        assertEquals(new Integer(5),answerSheets.get(0).getPoint());
    }

    /**
     * Test case TC014: Chuyển đổi danh sách câu hỏi rỗng thành phiếu trả lời
     * Mục đích: Kiểm tra service xử lý đúng khi chuyển đổi
     * danh sách câu hỏi rỗng thành phiếu trả lời
     */
    @Test
    public void testConvertFromQuestionList_WithEmptyList() {
        // Cho
        List<Question> emptyList = new ArrayList<>();

        // Khi
        List<AnswerSheet> answerSheets = questionService.convertFromQuestionList(emptyList);

        // Thì
        assertTrue(answerSheets.isEmpty());
    }

    /**
     * Test case TC015: Lấy danh sách tất cả câu hỏi
     * Mục đích: Kiểm tra service có thể lấy được danh sách đầy đủ
     * các câu hỏi
     */
    @Test
    public void testGetQuestionList() {
        // Khi
        List<Question> questions = questionService.getQuestionList();

        // Thì
        assertFalse(questions.isEmpty());
        assertTrue(questions.stream().anyMatch(q -> q.getId().equals(savedQuestion.getId())));
    }

    /**
     * Test case TC016: Tìm câu hỏi theo phần thi với phân trang
     * Mục đích: Kiểm tra service có thể lấy danh sách câu hỏi
     * thuộc một phần thi với phân trang
     */
    @Test
    public void testFindQuestionsByPart_WithPagination() {
        // Cho
        Pageable pageable = PageRequest.of(0, 10);

        // Khi
        Page<Question> questions = questionService.findQuestionsByPart(pageable, part);

        // Thì
        assertTrue(questions.hasContent());
        assertEquals(savedQuestion.getId(), questions.getContent().get(0).getId());
    }

    /**
     * Test case TC017: Tìm câu hỏi theo phần thi và chưa bị xóa
     * Mục đích: Kiểm tra service có thể lấy danh sách câu hỏi
     * thuộc một phần thi và chưa bị xóa
     */
    @Test
    public void testFindQuestionsByPartAndDeletedFalse() {
        // Cho
        Pageable pageable = PageRequest.of(0, 10);

        // Khi
        Page<Question> questions = questionService.findQuestionsByPartAndDeletedFalse(pageable, part);

        // Thì
        assertTrue(questions.hasContent());
        assertEquals(savedQuestion.getId(), questions.getContent().get(0).getId());
        assertFalse(questions.getContent().get(0).isDeleted());
    }

    /**
     * Test case TC018: Tìm câu hỏi theo phần thi, người tạo và chưa bị xóa
     * Mục đích: Kiểm tra service có thể lấy danh sách câu hỏi
     * thuộc một phần thi, được tạo bởi người dùng cụ thể và chưa bị xóa
     */
    @Test
    public void testFindQuestionsByPart_IdAndCreatedBy_UsernameAndDeletedFalse() {
        // Cho
        Pageable pageable = PageRequest.of(0, 10);

        // Khi
        Page<Question> questions = questionService.findQuestionsByPart_IdAndCreatedBy_UsernameAndDeletedFalse(
                pageable, part.getId(), testUser.getUsername());

        // Thì
        assertTrue(questions.hasContent());
        assertEquals(savedQuestion.getId(), questions.getContent().get(0).getId());
        assertEquals(testUser.getUsername(), questions.getContent().get(0).getCreatedBy().getUsername());
        assertFalse(questions.getContent().get(0).isDeleted());
    }

    /**
     * Test case TC019: Tìm tất cả câu hỏi với phân trang
     * Mục đích: Kiểm tra service có thể lấy danh sách tất cả câu hỏi
     * với phân trang
     */
    @Test
    public void testFindAllQuestions_WithPagination() {
        // Cho
        Pageable pageable = PageRequest.of(0, 10);

        // Khi
        Page<Question> questions = questionService.findAllQuestions(pageable);

        // Thì
        assertTrue(questions.hasContent());
        assertEquals(10,questions.getContent().size());
        assertEquals(0,questions.getNumber());
//        assertTrue(questions.stream().anyMatch(q -> q.getId().equals(savedQuestion.getId())));
    }

    /**
     * Test case TC020: Tìm nội dung câu hỏi theo ID với câu hỏi tồn tại
     * Mục đích: Kiểm tra service có thể lấy nội dung câu hỏi
     * theo ID
     */
    @Test
    public void testFindQuestionTextById_WithExistingQuestion() {
        // Khi
        String questionText = questionService.findQuestionTextById(savedQuestion.getId());

        // Thì
        assertNotNull(questionText);
        assertEquals("What is Java?", questionText);
    }

    /**
     * Test case TC021: Tìm nội dung câu hỏi theo ID không tồn tại
     * Mục đích: Kiểm tra service xử lý đúng khi tìm kiếm
     * nội dung câu hỏi với ID không tồn tại
     */
    @Test
    public void testFindQuestionTextById_WithNonExistingQuestion() {
        // Khi
        String questionText = questionService.findQuestionTextById(999L);

        // Thì
        assertNull(questionText);
    }

    /**
     * Test case TC022: Tìm nội dung câu hỏi theo ID null
     * Mục đích: Kiểm tra service xử lý đúng khi tìm kiếm
     * nội dung câu hỏi với ID null
     */
    @Test
    public void testFindQuestionTextById_WithNullId() {
        // Khi
        String questionText = questionService.findQuestionTextById(null);

        // Thì
        assertNull(questionText);
    }

    /**
     * Test case TC023: Tìm câu hỏi theo phần thi và người tạo
     * Mục đích: Kiểm tra service có thể lấy danh sách câu hỏi
     * thuộc một phần thi và được tạo bởi người dùng cụ thể
     */
    @Test
    public void testFindQuestionsByPart_IdAndCreatedBy_Username() {
        // Cho
        Pageable pageable = PageRequest.of(0, 10);

        // Khi
        Page<Question> questions = questionService.findQuestionsByPart_IdAndCreatedBy_Username(
                pageable, part.getId(), testUser.getUsername());

        // Thì
        assertTrue(questions.hasContent());
        assertEquals(savedQuestion.getId(), questions.getContent().get(0).getId());
        assertEquals(testUser.getUsername(), questions.getContent().get(0).getCreatedBy().getUsername());
    }

    /**
     * Test case TC024: Tìm câu hỏi theo người tạo
     * Mục đích: Kiểm tra service có thể lấy danh sách câu hỏi
     * được tạo bởi người dùng cụ thể
     */
    @Test
    public void testFindQuestionsByCreatedBy_Username() {
        // Cho
        Pageable pageable = PageRequest.of(0, 10);

        // Khi
        Page<Question> questions = questionService.findQuestionsByCreatedBy_Username(
                pageable, testUser.getUsername());

        // Thì
        assertTrue(questions.hasContent());
        assertEquals(savedQuestion.getId(), questions.getContent().get(0).getId());
        assertEquals(testUser.getUsername(), questions.getContent().get(0).getCreatedBy().getUsername());
    }

    /**
     * Test case TC025: Lưu câu hỏi mới với mức độ khó EASY
     * Mục đích: Kiểm tra service có thể lưu câu hỏi mới
     * và tự động tính điểm dựa trên mức độ khó
     */
    @Test
    public void testSave_WithEasyDifficulty() {
        // Cho
        Question easyQuestion = createTestQuestion(DifficultyLevel.EASY);

        // Khi
        questionService.save(easyQuestion);

        // Thì
        Optional<Question> saved = questionRepository.findById(easyQuestion.getId());
        assertTrue(saved.isPresent());
        assertEquals(5, saved.get().getPoint());
    }

    /**
     * Test case TC026: Lưu câu hỏi mới với mức độ khó MEDIUM
     * Mục đích: Kiểm tra service có thể lưu câu hỏi mới
     * và tự động tính điểm dựa trên mức độ khó
     */
    @Test
    public void testSave_WithMediumDifficulty() {
        // Cho
        Question mediumQuestion = createTestQuestion(DifficultyLevel.MEDIUM);

        // Khi
        questionService.save(mediumQuestion);

        // Thì
        Optional<Question> saved = questionRepository.findById(mediumQuestion.getId());
        assertTrue(saved.isPresent());
        assertEquals(10, saved.get().getPoint());
    }

    /**
     * Test case TC027: Lưu câu hỏi mới với mức độ khó HARD
     * Mục đích: Kiểm tra service có thể lưu câu hỏi mới
     * và tự động tính điểm dựa trên mức độ khó
     */
    @Test
    public void testSave_WithHardDifficulty() {
        // Cho
        Question hardQuestion = createTestQuestion(DifficultyLevel.HARD);

        // Khi
        questionService.save(hardQuestion);

        // Thì
        Optional<Question> saved = questionRepository.findById(hardQuestion.getId());
        assertTrue(saved.isPresent());
        assertEquals(15, saved.get().getPoint());
    }

    /**
     * Test case TC028: Lưu câu hỏi với nội dung rỗng
     * Mục đích: Kiểm tra service xử lý đúng khi lưu
     * câu hỏi với nội dung rỗng
     */
    @Test
    public void testSave_WithEmptyText() {
        // Cho
        Question emptyQuestion = createTestQuestion(DifficultyLevel.EASY);
        emptyQuestion.setQuestionText("");

        // Khi
        questionService.save(emptyQuestion);

        // Thì
        Optional<Question> saved = questionRepository.findById(emptyQuestion.getId());
        assertTrue(saved.isPresent());
        assertEquals("", saved.get().getQuestionText());
    }

    /**
     * Test case TC029: Lưu câu hỏi với nội dung null
     * Mục đích: Kiểm tra service xử lý đúng khi lưu
     * câu hỏi với nội dung null
     */
    @Test
    public void testSave_WithNullText() {
        // Cho
        Question nullQuestion = createTestQuestion(DifficultyLevel.EASY);
        nullQuestion.setQuestionText(null);

        // Khi
        questionService.save(nullQuestion);

        // Thì
        Optional<Question> saved = questionRepository.findById(nullQuestion.getId());
        assertTrue(saved.isPresent());
        assertNull(saved.get().getQuestionText());
    }

    /**
     * Test case TC030: Lưu câu hỏi với phần thi null
     * Mục đích: Kiểm tra service xử lý đúng khi lưu
     * câu hỏi với phần thi null
     */
    @Test
    public void testSave_WithNullPart() {
        // Cho
        Question nullPartQuestion = createTestQuestion(DifficultyLevel.EASY);
        nullPartQuestion.setPart(null);

        // Khi
        questionService.save(nullPartQuestion);

        // Thì
        Optional<Question> saved = questionRepository.findById(nullPartQuestion.getId());
        assertTrue(saved.isPresent());
        assertNull(saved.get().getPart());
    }

    /**
     * Test case TC031: Lưu câu hỏi với loại câu hỏi null
     * Mục đích: Kiểm tra service xử lý đúng khi lưu
     * câu hỏi với loại câu hỏi null
     */
    @Test
    public void testSave_WithNullQuestionType() {
        // Cho
        Question nullTypeQuestion = createTestQuestion(DifficultyLevel.EASY);
        nullTypeQuestion.setQuestionType(null);

        // Khi
        questionService.save(nullTypeQuestion);

        // Thì
        Optional<Question> saved = questionRepository.findById(nullTypeQuestion.getId());
        assertTrue(saved.isPresent());
        assertNull(saved.get().getQuestionType());
    }

    /**
     * Test case TC032: Lưu câu hỏi với danh sách lựa chọn rỗng
     * Mục đích: Kiểm tra service xử lý đúng khi lưu
     * câu hỏi với danh sách lựa chọn rỗng
     */
    @Test
    public void testSave_WithEmptyChoices() {
        // Cho
        Question emptyChoicesQuestion = createTestQuestion(DifficultyLevel.EASY);
        emptyChoicesQuestion.setChoices(new ArrayList<>());

        // Khi
        questionService.save(emptyChoicesQuestion);

        // Thì
        Optional<Question> saved = questionRepository.findById(emptyChoicesQuestion.getId());
        assertTrue(saved.isPresent());
        assertTrue(saved.get().getChoices().isEmpty());
    }

    /**
     * Test case TC033: Lưu câu hỏi với danh sách lựa chọn null
     * Mục đích: Kiểm tra service xử lý đúng khi lưu
     * câu hỏi với danh sách lựa chọn null
     */
    @Test
    public void testSave_WithNullChoices() {
        // Cho
        Question nullChoicesQuestion = createTestQuestion(DifficultyLevel.EASY);
        nullChoicesQuestion.setChoices(null);

        // Khi
        questionService.save(nullChoicesQuestion);

        // Thì
        Optional<Question> saved = questionRepository.findById(nullChoicesQuestion.getId());
        assertTrue(saved.isPresent());
        assertNull(saved.get().getChoices());
    }

    /**
     * Test case TC034: Cập nhật câu hỏi
     * Mục đích: Kiểm tra service có thể cập nhật thông tin
     * của một câu hỏi
     */
    @Test
    public void testUpdate() {
        // Cho
        savedQuestion.setQuestionText("Updated Question");
        savedQuestion.setDifficultyLevel(DifficultyLevel.MEDIUM);

        // Khi
        questionService.update(savedQuestion);

        // Thì
        Optional<Question> updated = questionRepository.findById(savedQuestion.getId());
        assertTrue(updated.isPresent());
        assertEquals("Updated Question", updated.get().getQuestionText());
        assertEquals(DifficultyLevel.MEDIUM, updated.get().getDifficultyLevel());
    }

    /**
     * Test case TC035: Xóa câu hỏi
     * Mục đích: Kiểm tra service có thể xóa một câu hỏi
     */
    @Test
    public void testDelete() {
        // Khi
        questionService.delete(savedQuestion.getId());

        // Thì
        Optional<Question> deleted = questionRepository.findById(savedQuestion.getId());
        assertFalse(deleted.isPresent());
    }

    /**
     * Test case TC036: Xóa câu hỏi không tồn tại
     * Mục đích: Kiểm tra service xử lý đúng khi xóa
     * câu hỏi không tồn tại
     */
    @Test
    public void testDelete_WithNonExistingQuestion() {
        // Khi
        questionService.delete(999L);

        // Thì
        Optional<Question> deleted = questionRepository.findById(999L);
        assertFalse(deleted.isPresent());
    }

    /**
     * Test case TC037: Xóa câu hỏi với ID null
     * Mục đích: Kiểm tra service xử lý đúng khi xóa
     * câu hỏi với ID null
     */
    @Test
    public void testDelete_WithNullId() {
        // Khi
        questionService.delete(null);

        // Thì
        // Không có exception nào được ném ra
    }

    /**
     * Test case TC040: Xóa câu hỏi với ID âm
     * Mục đích: Kiểm tra service xử lý đúng khi xóa
     * câu hỏi với ID âm
     */
    @Test
    public void testDelete_WithNegativeId() {
        // Khi
        questionService.delete(-1L);

        // Thì
        Optional<Question> deleted = questionRepository.findById(-1L);
        assertFalse(deleted.isPresent());
    }

    /**
     * Test case TC038: Tìm câu hỏi theo ID âm
     * Mục đích: Kiểm tra service xử lý đúng khi tìm kiếm
     * câu hỏi với ID âm
     */
    @Test
    public void testGetQuestionById_WithNegativeId() {
        // Khi
        Optional<Question> result = questionService.getQuestionById(-1L);

        // Thì
        assertFalse(result.isPresent());
    }

    /**
     * Test case TC039: Tìm nội dung câu hỏi theo ID âm
     * Mục đích: Kiểm tra service xử lý đúng khi tìm kiếm
     * nội dung câu hỏi với ID âm
     */
    @Test
    public void testFindQuestionTextById_WithNegativeId() {
        // Khi
        String questionText = questionService.findQuestionTextById(-1L);

        // Thì
        assertNull(questionText);
    }

    /**
     * Phương thức hỗ trợ tạo câu hỏi test
     *
     * @param difficultyLevel Mức độ khó của câu hỏi
     * @return Câu hỏi test
     */
    private Question createTestQuestion(DifficultyLevel difficultyLevel) {

        List<Choice>choices = new ArrayList<>();
        Choice choice1 = new Choice();
        choice1.setChoiceText("True");
        choice1.setIsCorrected(1);
        choices.add(choice1);

        Choice choice2 = new Choice();
        choice2.setChoiceText("False");
        choice2.setIsCorrected(0);
        choices.add(choice2);

        Question question = new Question();
        question.setQuestionText("Test Question");
        question.setDifficultyLevel(difficultyLevel);
        question.setQuestionType(questionType);
        question.setPart(part);
        question.setChoices(choices);
        question.setDeleted(false);
        question.setCreatedBy(testUser);
        return question;
    }
} 