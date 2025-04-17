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
        assertEquals("True/False", result.get().getDescription());
    }

    /**
     * Test case TC002: Tìm loại câu hỏi theo mã loại
     * Mục đích: Kiểm tra service có thể lấy đúng thông tin loại câu hỏi
     *          khi tìm kiếm bằng mã loại
     */
    @Test
    public void testGetQuestionTypeByCode() {
        // Khi
        Optional<QuestionType> result = questionTypeService.getQuestionTypeByCode(EQTypeCode.TF);

        // Thì
        assertTrue(result.isPresent());
        assertEquals(new Long(1), result.get().getId());
        assertEquals("True/False", result.get().getDescription());
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
        assertEquals(3,questionTypes.size());
    }

    /**
     * Test case TC005: Xóa loại câu hỏi
     * Mục đích: Kiểm tra service có thể xóa thành công
     *          một loại câu hỏi
     */
    @Test
    public void testDeleteQuestionType() {
        // Khi
        questionTypeService.delete(1l);
        
        // Thì
        Optional<QuestionType> deleted = questionTypeRepository.findById(1l);
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
        assertTrue(questionTypeService.existsById(3l));
        assertFalse(questionTypeService.existsById(999L));
    }



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
}
