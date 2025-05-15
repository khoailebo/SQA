package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.Choice;
import com.thanhtam.backend.repository.ChoiceRepository;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Lớp test cho ChoiceServiceImpl
 * Lớp này chứa các test case để kiểm tra chức năng của ChoiceServiceImpl
 * bao gồm việc tìm kiếm tính đúng/sai của lựa chọn và nội dung lựa chọn theo ID
 */
//@RunWith(org.springframework.test.context.junit4.SpringRunner.class)
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Transactional
@Rollback
public class ChoiceServiceTest {

    @Autowired
    private ChoiceRepository choiceRepository;

    @Autowired
    private ChoiceServiceImpl choiceService;

    private Choice savedChoice;

    /**
     * Phương thức setup chạy trước mỗi test
     * Tạo và lưu một lựa chọn test với đáp án đúng (isCorrected = 1)
     */
    @BeforeEach
    public void setUp() {
        // Tạo và lưu một lựa chọn test
        Choice choice = new Choice();
        choice.setChoiceText("Test Choice");
        choice.setIsCorrected(1); // 1 đại diện cho đáp án đúng
        savedChoice = choiceRepository.save(choice);
    }

    /**
     * Test case TC001: Tìm isCorrected theo ID với lựa chọn tồn tại
     * Mục đích: Kiểm tra service có thể lấy đúng giá trị isCorrected
     *          cho một lựa chọn tồn tại với đáp án đúng (1)
     */
    @Test
    public void testFindIsCorrectedById_WithExistingChoice() {
        // Khi
        Integer isCorrected = choiceService.findIsCorrectedById(savedChoice.getId());

        // Thì
        assertNotNull(isCorrected);
        assertEquals(new Integer(1), isCorrected);
    }

    /**
     * Test case TC002: Tìm isCorrected theo ID với lựa chọn không tồn tại
     * Mục đích: Kiểm tra service trả về null khi tìm kiếm
     *          giá trị isCorrected cho một ID không tồn tại
     */
    @Test
    public void testFindIsCorrectedById_WithNonExistingChoice() {
        // Khi
        Integer isCorrected = choiceService.findIsCorrectedById(999L);

        // Thì
        assertNull(isCorrected);
    }

    /**
     * Test case TC003: Tìm nội dung lựa chọn theo ID với lựa chọn tồn tại
     * Mục đích: Kiểm tra service có thể lấy đúng nội dung lựa chọn
     *          cho một lựa chọn tồn tại
     */
    @Test
    public void testFindChoiceTextById_WithExistingChoice() {
        // Khi
        String choiceText = choiceService.findChoiceTextById(savedChoice.getId());

        // Thì
        assertNotNull(choiceText);
        assertEquals("Test Choice", choiceText);
    }

    /**
     * Test case TC004: Tìm nội dung lựa chọn theo ID với lựa chọn không tồn tại
     * Mục đích: Kiểm tra service trả về null khi tìm kiếm
     *          nội dung lựa chọn cho một ID không tồn tại
     */
    @Test
    public void testFindChoiceTextById_WithNonExistingChoice() {
        // Khi
        String choiceText = choiceService.findChoiceTextById(999L);

        // Thì
        assertNull(choiceText);
    }

    /**
     * Test case TC005: Tìm isCorrected theo ID với đáp án sai
     * Mục đích: Kiểm tra service có thể lấy đúng giá trị isCorrected
     *          cho một lựa chọn với đáp án sai (0)
     */
    @Test
    public void testFindIsCorrectedById_WithIncorrectAnswer() {
        // Cho
        Choice incorrectChoice = new Choice();
        incorrectChoice.setChoiceText("Incorrect Choice");
        incorrectChoice.setIsCorrected(0); // 0 đại diện cho đáp án sai
        Choice savedIncorrectChoice = choiceRepository.save(incorrectChoice);

        // Khi
        Integer isCorrected = choiceService.findIsCorrectedById(savedIncorrectChoice.getId());

        // Thì
        assertNotNull(isCorrected);
        assertEquals(new Integer(0), isCorrected);
    }

    /**
     * Test case TC006: Tìm nội dung lựa chọn theo ID với nội dung rỗng
     * Mục đích: Kiểm tra service có thể xử lý và trả về nội dung rỗng
     *          cho một lựa chọn có nội dung rỗng
     */
    @Test
    public void testFindChoiceTextById_WithEmptyChoiceText() {
        // Cho
        Choice emptyChoice = new Choice();
        emptyChoice.setChoiceText("");
        emptyChoice.setIsCorrected(1);
        Choice savedEmptyChoice = choiceRepository.save(emptyChoice);

        // Khi
        String choiceText = choiceService.findChoiceTextById(savedEmptyChoice.getId());

        // Thì
        assertNotNull(choiceText);
        assertEquals("", choiceText);
    }

    /**
     * Test case TC007: Tìm isCorrected theo ID với ID null
     * Mục đích: Kiểm tra service có thể xử lý đầu vào ID null
     *          và trả về null như mong đợi
     */
    @Test
    public void testFindIsCorrectedById_WithNullId() {
        // Khi
        Integer isCorrected = choiceService.findIsCorrectedById(null);

        // Thì
        assertNull(isCorrected);
    }

    /**
     * Test case TC008: Tìm nội dung lựa chọn theo ID với ID null
     * Mục đích: Kiểm tra service có thể xử lý đầu vào ID null
     *          và trả về null như mong đợi
     */
    @Test
    public void testFindChoiceTextById_WithNullId() {
        // Khi
        String choiceText = choiceService.findChoiceTextById(null);

        // Thì
        assertNull(choiceText);
    }
} 