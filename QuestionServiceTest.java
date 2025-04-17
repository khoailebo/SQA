package com.thanhtam.backend.service;

import com.thanhtam.backend.dto.AnswerSheet;
import com.thanhtam.backend.dto.ExamQuestionPoint;
import com.thanhtam.backend.entity.*;
import com.thanhtam.backend.repository.QuestionRepository;
import com.thanhtam.backend.repository.PartRepository;
import com.thanhtam.backend.repository.QuestionTypeRepository;
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
    public QuestionRepository questionRepository;

    @Autowired
    public PartRepository partRepository;

    @Autowired
    public QuestionTypeRepository questionTypeRepository;

    @Autowired
    public QuestionServiceImpl questionService;

    public Question savedQuestion;
    public QuestionType questionType;
    public Part part;
    public List<Choice> choices;

    /**
     * Phương thức setup chạy trước mỗi test
     * Tạo và lưu các đối tượng cần thiết cho test:
     * - Loại câu hỏi (True/False)
     * - Phần thi
     * - Các lựa chọn (True/False)
     * - Câu hỏi mẫu
     */
    @Before
    public void setUp() {
        // Tạo và lưu loại câu hỏi
        questionType = new QuestionType();
        questionType.setTypeCode(EQTypeCode.TF);
        questionType.setDescription("True/False Question");
        questionType = questionTypeRepository.save(questionType);

        // Tạo và lưu phần thi
        part = new Part();
        part.setName("Test Part");
        part = partRepository.save(part);

        // Tạo các lựa chọn
        choices = new ArrayList<>();
        Choice choice1 = new Choice();
        choice1.setChoiceText("True");
        choice1.setIsCorrected(1);
        choices.add(choice1);

        Choice choice2 = new Choice();
        choice2.setChoiceText("False");
        choice2.setIsCorrected(0);
        choices.add(choice2);

        // Tạo và lưu câu hỏi
        Question q = new Question();
        q.setQuestionText("What is Java?");
        q.setDifficultyLevel(DifficultyLevel.EASY);
        q.setQuestionType(questionType);
        q.setPart(part);
        q.setChoices(choices);
        q.setDeleted(false);
        savedQuestion = questionRepository.save(q);
    }

    /**
     * Test case TC001: Tìm câu hỏi theo ID
     * Mục đích: Kiểm tra service có thể lấy đúng thông tin câu hỏi
     *          khi tìm kiếm bằng ID
     */
    @Test
    public void testGetQuestionById() {
        // Khi
        Optional<Question> result = questionService.getQuestionById(savedQuestion.getId());

        // Thì
        assertTrue(result.isPresent());
        assertEquals("What is Java?", result.get().getQuestionText());
        assertEquals(DifficultyLevel.EASY, result.get().getDifficultyLevel());
        assertEquals(questionType.getTypeCode(), result.get().getQuestionType().getTypeCode());
        assertEquals(part.getName(), result.get().getPart().getName());
        assertEquals(2, result.get().getChoices().size());
    }

    /**
     * Test case TC002: Tìm câu hỏi theo phần thi
     * Mục đích: Kiểm tra service có thể lấy danh sách câu hỏi
     *          thuộc một phần thi cụ thể
     */
    @Test
    public void testGetQuestionByPart() {
        // Khi
        List<Question> questions = questionService.getQuestionByPart(part);

        // Thì
        assertFalse(questions.isEmpty());
        assertEquals(savedQuestion.getId(), questions.get(0).getId());
        assertEquals(part.getId(), questions.get(0).getPart().getId());
    }

    /**
     * Test case TC003: Tìm câu hỏi theo loại câu hỏi
     * Mục đích: Kiểm tra service có thể lấy danh sách câu hỏi
     *          thuộc một loại câu hỏi cụ thể
     */
    @Test
    public void testGetQuestionByQuestionType() {
        // Khi
        List<Question> questions = questionService.getQuestionByQuestionType(questionType);

        // Thì
        assertFalse(questions.isEmpty());
        assertEquals(savedQuestion.getId(), questions.get(0).getId());
        assertEquals(questionType.getTypeCode(), questions.get(0).getQuestionType().getTypeCode());
    }

    /**
     * Test case TC004: Lấy danh sách câu hỏi với điểm số
     * Mục đích: Kiểm tra service có thể lấy danh sách câu hỏi
     *          kèm theo điểm số tương ứng
     */
    @Test
    public void testGetQuestionPointList() {
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
     * Test case TC005: Chuyển đổi danh sách câu hỏi thành phiếu trả lời
     * Mục đích: Kiểm tra service có thể chuyển đổi danh sách câu hỏi
     *          thành định dạng phiếu trả lời
     */
    @Test
    public void testConvertFromQuestionList() {
        // Cho
        List<Question> questionList = new ArrayList<>();
        questionList.add(savedQuestion);

        // Khi
        List<AnswerSheet> answerSheets = questionService.convertFromQuestionList(questionList);

        // Thì
        assertFalse(answerSheets.isEmpty());
        assertEquals(savedQuestion.getId(), answerSheets.get(0).getQuestionId());
        assertEquals(2, answerSheets.get(0).getChoices().size());
        assertEquals(0, answerSheets.get(0).getChoices().get(0).getIsCorrected());
    }

    /**
     * Test case TC006: Lấy danh sách tất cả câu hỏi
     * Mục đích: Kiểm tra service có thể lấy được danh sách đầy đủ
     *          các câu hỏi
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
     * Test case TC007: Tìm câu hỏi theo phần thi với phân trang
     * Mục đích: Kiểm tra service có thể lấy danh sách câu hỏi
     *          thuộc một phần thi với phân trang
     */
    @Test
    public void testFindQuestionsByPart() {
        // Cho
        Pageable pageable = PageRequest.of(0, 10);

        // Khi
        Page<Question> questions = questionService.findQuestionsByPart(pageable, part);

        // Thì
        assertTrue(questions.hasContent());
        assertEquals(savedQuestion.getId(), questions.getContent().get(0).getId());
    }

    /**
     * Test case TC008: Tìm câu hỏi theo phần thi và chưa bị xóa
     * Mục đích: Kiểm tra service có thể lấy danh sách câu hỏi
     *          thuộc một phần thi và chưa bị xóa
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
     * Test case TC009: Tìm tất cả câu hỏi với phân trang
     * Mục đích: Kiểm tra service có thể lấy danh sách tất cả câu hỏi
     *          với phân trang
     */
    @Test
    public void testFindAllQuestions() {
        // Cho
        Pageable pageable = PageRequest.of(0, 10);

        // Khi
        Page<Question> questions = questionService.findAllQuestions(pageable);

        // Thì
        assertTrue(questions.hasContent());
        assertTrue(questions.getContent().size() == 10);
    }

    /**
     * Test case TC010: Tìm nội dung câu hỏi theo ID
     * Mục đích: Kiểm tra service có thể lấy nội dung câu hỏi
     *          theo ID
     */
    @Test
    public void testFindQuestionTextById() {
        // Khi
        String questionText = questionService.findQuestionTextById(savedQuestion.getId());

        // Thì
        assertEquals(savedQuestion.getId().toString(), questionText);
    }

    /**
     * Test case TC011: Lưu câu hỏi với các mức độ khó khác nhau
     * Mục đích: Kiểm tra service có thể lưu câu hỏi
     *          và tự động tính điểm dựa trên mức độ khó
     */
    @Test
    public void testSaveQuestionWithDifferentDifficultyLevels() {
        // Cho và Khi
        // Test câu hỏi dễ
        Question easyQuestion = createTestQuestion(DifficultyLevel.EASY);
        questionService.save(easyQuestion);
        assertEquals(5, easyQuestion.getPoint());

        // Test câu hỏi trung bình
        Question mediumQuestion = createTestQuestion(DifficultyLevel.MEDIUM);
        questionService.save(mediumQuestion);
        assertEquals(10, mediumQuestion.getPoint());

        // Test câu hỏi khó
        Question hardQuestion = createTestQuestion(DifficultyLevel.HARD);
        questionService.save(hardQuestion);
        assertEquals(15, hardQuestion.getPoint());
    }

    /**
     * Test case TC012: Cập nhật câu hỏi
     * Mục đích: Kiểm tra service có thể cập nhật thông tin
     *          của một câu hỏi
     */
    @Test
    public void testUpdateQuestion() {
        // Cho
        savedQuestion.setQuestionText("Updated question text");
        savedQuestion.setDifficultyLevel(DifficultyLevel.MEDIUM);

        // Khi
        questionService.update(savedQuestion);

        // Thì
        Optional<Question> updated = questionService.getQuestionById(savedQuestion.getId());
        assertTrue(updated.isPresent());
        assertEquals("Updated question text", updated.get().getQuestionText());
        assertEquals(DifficultyLevel.MEDIUM, updated.get().getDifficultyLevel());
    }

    /**
     * Test case TC013: Xóa câu hỏi
     * Mục đích: Kiểm tra service có thể xóa một câu hỏi
     */
    @Test
    public void testDeleteQuestion() {
        // Khi
        questionService.delete(savedQuestion.getId());

        // Thì
        Optional<Question> deleted = questionService.getQuestionById(savedQuestion.getId());
        assertFalse(deleted.isPresent());
    }

    /**
     * Phương thức hỗ trợ tạo câu hỏi test
     * @param difficultyLevel Mức độ khó của câu hỏi
     * @return Câu hỏi đã được tạo
     */
    private Question createTestQuestion(DifficultyLevel difficultyLevel) {
        Question question = new Question();
        question.setQuestionText("Test Question");
        question.setDifficultyLevel(difficultyLevel);
        question.setQuestionType(questionType);
        question.setPart(part);
        question.setChoices(choices);
        question.setDeleted(false);
        return question;
    }
} 