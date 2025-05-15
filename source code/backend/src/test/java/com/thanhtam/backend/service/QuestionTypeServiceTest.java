package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.QuestionType;
import com.thanhtam.backend.repository.QuestionTypeRepository;
import com.thanhtam.backend.ultilities.EQTypeCode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lớp test cho QuestionTypeServiceImpl
 * Lớp này chứa các test case để kiểm tra chức năng của QuestionTypeServiceImpl
 * bao gồm việc quản lý các loại câu hỏi (thêm, sửa, xóa, tìm kiếm)
 */
@RunWith(org.springframework.test.context.junit4.SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback
public class QuestionTypeServiceTest {

    @Autowired
    private QuestionTypeRepository questionTypeRepository;

    @Autowired
    private QuestionTypeServiceImpl questionTypeService;

    private QuestionType savedQuestionType;

    /**
     * Phương thức setup chạy trước mỗi test
     * Tạo và lưu một loại câu hỏi test với mã TF (True/False)
     */
    @Before
    public void setUp() {
        // Tạo và lưu một loại câu hỏi test
        QuestionType questionType = new QuestionType();
        questionType.setTypeCode(EQTypeCode.TF);
        questionType.setDescription("True/False Question");
        savedQuestionType = questionTypeRepository.save(questionType);
    }

    /**
     * Test case TC001: Tìm loại câu hỏi theo ID với loại câu hỏi tồn tại
     * Mục đích: Kiểm tra service có thể lấy đúng thông tin loại câu hỏi
     *          khi tìm kiếm bằng ID tồn tại
     */
    @Test
    public void testGetQuestionTypeById() {
        // Khi
        Optional<QuestionType> result = questionTypeService.getQuestionTypeById(1l);

        // Thì
        assertTrue(result.isPresent());
        assertEquals(EQTypeCode.TF, result.get().getTypeCode());
//        assertEquals("True/False Question", result.get().getDescription());
    }

    /**
     * Test case TC002: Tìm loại câu hỏi theo mã loại
     * Mục đích: Kiểm tra service có thể lấy đúng thông tin loại câu hỏi
     *          khi tìm kiếm bằng mã loại
     */
    @Test
    public void testGetQuestionTypeByCode() {
        // Khi
        Optional<QuestionType> result = questionTypeService.getQuestionTypeByCode(EQTypeCode.MC);

        // Thì
        assertTrue(result.isPresent());
//        assertEquals(savedQuestionType.getId(), result.get().getId());
//        assertEquals("True/False Question", result.get().getDescription());
    }

    /**
     * Test case TC003: Lấy danh sách tất cả loại câu hỏi
     * Mục đích: Kiểm tra service có thể lấy được danh sách đầy đủ
     *          các loại câu hỏi
     */
    @Test
    public void testGetQuestionTypeList() {
        // Khi
        List<QuestionType> questionTypes = questionTypeService.getQuestionTypeList();
        
        // Thì
        assertFalse(questionTypes.isEmpty());
        assertTrue(questionTypes.stream().anyMatch(qt -> qt.getId().equals(savedQuestionType.getId())));
    }

    /**
     * Test case TC004: Lưu loại câu hỏi mới
     * Mục đích: Kiểm tra service có thể lưu thành công
     *          một loại câu hỏi mới
     */
    @Test
    public void testSaveQuestionType() {
        // Cho
        QuestionType newQuestionType = new QuestionType();
        newQuestionType.setTypeCode(EQTypeCode.MC);
        newQuestionType.setDescription("Multiple Choice Question");

        // Khi
        questionTypeService.saveQuestionType(newQuestionType);

        // Thì
        Optional<QuestionType> saved = questionTypeRepository.findById(newQuestionType.getId());
        assertTrue(saved.isPresent());
        assertEquals(EQTypeCode.MC, saved.get().getTypeCode());
        assertEquals("Multiple Choice Question", saved.get().getDescription());
    }

    /**
     * Test case TC005: Xóa loại câu hỏi
     * Mục đích: Kiểm tra service có thể xóa thành công
     *          một loại câu hỏi
     */
    @Test
    public void testDeleteQuestionType() {
        // Khi
        questionTypeService.delete(savedQuestionType.getId());
        
        // Thì
        Optional<QuestionType> deleted = questionTypeRepository.findById(savedQuestionType.getId());
        assertFalse(deleted.isPresent());
    }

    /**
     * Test case TC006: Kiểm tra sự tồn tại của loại câu hỏi
     * Mục đích: Kiểm tra service có thể xác định chính xác
     *          sự tồn tại của loại câu hỏi theo ID
     */
    @Test
    public void testExistsById() {
        // Thì
        assertTrue(questionTypeService.existsById(savedQuestionType.getId()));
        assertFalse(questionTypeService.existsById(999L));
    }

    /**
     * Test case TC007: Cập nhật loại câu hỏi
     * Mục đích: Kiểm tra service có thể cập nhật thành công
     *          thông tin của một loại câu hỏi
     */
//    @Test
//    public void testUpdateQuestionType() {
//        // Cho
//        savedQuestionType.setDescription("Updated True/False Question");
//
//        // Khi
//        questionTypeService.(savedQuestionType);
//
//        // Thì
//        Optional<QuestionType> updated = questionTypeRepository.findById(savedQuestionType.getId());
//        assertTrue(updated.isPresent());
//        assertEquals("Updated True/False Question", updated.get().getDescription());
//    }

    /**
     * Test case TC008: Tìm loại câu hỏi theo ID không tồn tại
     * Mục đích: Kiểm tra service xử lý đúng khi tìm kiếm
     *          loại câu hỏi với ID không tồn tại
     */
    @Test
    public void testGetQuestionTypeByIdNotFound() {
        // Khi
        Optional<QuestionType> result = questionTypeService.getQuestionTypeById(999L);

        // Thì
        assertFalse(result.isPresent());
    }

    /**
     * Test case TC009: Tìm loại câu hỏi theo mã loại không tồn tại
     * Mục đích: Kiểm tra service xử lý đúng khi tìm kiếm
     *          loại câu hỏi với mã loại không tồn tại
     */
//    @Test
//    public void testGetQuestionTypeByCodeNotFound() {
//        // Khi
//        Optional<QuestionType> result = questionTypeService.getQuestionTypeByCode(EQTypeCode.ES);
//
//        // Thì
//        assertFalse(result.isPresent());
//    }

    /**
     * Test case TC010: Lưu loại câu hỏi với mã loại trùng lặp
     * Mục đích: Kiểm tra service xử lý đúng khi lưu
     *          loại câu hỏi với mã loại đã tồn tại
     */
    @Test
    public void testSaveQuestionTypeWithDuplicateCode() {
        // Cho
        QuestionType duplicateType = new QuestionType();
        duplicateType.setTypeCode(EQTypeCode.TF);
        duplicateType.setDescription("Another True/False Question");

        // Khi
        questionTypeService.saveQuestionType(duplicateType);

        // Thì
        Optional<QuestionType> saved = questionTypeRepository.findById(duplicateType.getId());
        assertFalse(saved.isPresent());
//        assertEquals(EQTypeCode.TF, saved.get().getTypeCode());
    }

    /**
     * Test case TC011: Lưu loại câu hỏi với mô tả rỗng
     * Mục đích: Kiểm tra service xử lý đúng khi lưu
     *          loại câu hỏi với mô tả rỗng
     */
    @Test
    public void testSaveQuestionTypeWithEmptyDescription() {
        // Cho
        QuestionType emptyDescType = new QuestionType();
        emptyDescType.setTypeCode(EQTypeCode.MC);
        emptyDescType.setDescription("");

        // Khi
        questionTypeService.saveQuestionType(emptyDescType);

        // Thì
        Optional<QuestionType> saved = questionTypeRepository.findById(emptyDescType.getId());
        assertTrue(saved.isPresent());
        assertEquals("", saved.get().getDescription());
    }

    /**
     * Test case TC012: Lưu loại câu hỏi với mô tả null
     * Mục đích: Kiểm tra service xử lý đúng khi lưu
     *          loại câu hỏi với mô tả null
     */
    @Test
    public void testSaveQuestionTypeWithNullDescription() {
        // Cho
        QuestionType nullDescType = new QuestionType();
        nullDescType.setTypeCode(EQTypeCode.MC);
        nullDescType.setDescription(null);

        // Khi
        questionTypeService.saveQuestionType(nullDescType);

        // Thì
        Optional<QuestionType> saved = questionTypeRepository.findById(nullDescType.getId());
        assertTrue(saved.isPresent());
        assertNull(saved.get().getDescription());
    }

    /**
     * Test case TC013: Tìm loại câu hỏi theo ID âm
     * Mục đích: Kiểm tra service xử lý đúng khi tìm kiếm
     *          loại câu hỏi với ID âm
     */
    @Test
    public void testGetQuestionTypeById_WithNegativeId() {
        // Khi
        Optional<QuestionType> result = questionTypeService.getQuestionTypeById(-1L);

        // Thì
        assertFalse(result.isPresent());
    }

    /**
     * Test case TC014: Kiểm tra sự tồn tại của loại câu hỏi với ID âm
     * Mục đích: Kiểm tra service xử lý đúng khi kiểm tra
     *          sự tồn tại của loại câu hỏi với ID âm
     */
    @Test
    public void testExistsById_WithNegativeId() {
        // Thì
        assertFalse(questionTypeService.existsById(-1L));
    }
}
