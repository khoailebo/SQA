package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.entity.Part;
import com.thanhtam.backend.repository.CourseRepository;
import com.thanhtam.backend.repository.PartRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lớp test cho PartServiceImpl
 * Lớp này chứa các test case để kiểm tra chức năng của PartServiceImpl
 * bao gồm việc quản lý phần thi (thêm, tìm kiếm, kiểm tra tồn tại)
 */
@RunWith(org.springframework.test.context.junit4.SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback
public class PartServiceTest {
    static boolean initail = false;

    @Autowired
    private PartRepository partRepository;
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PartServiceImpl partService;

    static Part savedPart;
    static Course course;

    /**
     * Phương thức setup chạy trước mỗi test
     * Tạo và lưu các đối tượng cần thiết cho test:
     * - Khóa học Face
     * - Phần thi
     */
    @Before
    public void setUp() {
        // Tạo khóa học Face
        if (!initail) {
            initail = true;
            course = new Course();
            course.setName("Face Course");
            course.setCourseCode("FACE001");
            course.setImgUrl("https://example.com/face-course.jpg");
            course = courseRepository.save(course);
            // Tạo và lưu phần thi
            Part part = new Part();
            part.setName("Face Part 1");
            part.setCourse(course);
            savedPart = partRepository.save(part);
        }

    }


    /**
     * Test case TC001: Lưu phần thi mới
     * Mục đích: Kiểm tra service có thể lưu thành công
     * một phần thi mới
     */
    @Test
    public void testSavePart() {
        // Cho
        Part newPart = new Part();
        newPart.setName("Face Part 2");
        newPart.setCourse(course);

        // Khi
        partService.savePart(newPart);

        // Thì
        Optional<Part> saved = partRepository.findById(newPart.getId());
        assertTrue(saved.isPresent());
        assertEquals("Face Part 2", saved.get().getName());
        assertEquals(course.getId(), saved.get().getCourse().getId());
    }

    /**
     * Test case TC002: Lấy danh sách phần thi theo khóa học với phân trang
     * Mục đích: Kiểm tra service có thể lấy danh sách phần thi
     * thuộc một khóa học với phân trang
     */
    @Test
    public void testGetPartListByCourseWithPagination() {
        // Cho
        Pageable pageable = PageRequest.of(0, 10);

        // Khi
        Page<Part> parts = partService.getPartLisByCourse(pageable, course.getId());

        // Thì
        assertTrue(parts.hasContent());
        assertEquals(savedPart.getId(), parts.getContent().get(0).getId());
        assertEquals(course.getId(), parts.getContent().get(0).getCourse().getId());
    }

    /**
     * Test case TC003: Lấy danh sách phần thi theo khóa học
     * Mục đích: Kiểm tra service có thể lấy danh sách phần thi
     * thuộc một khóa học
     */
    @Test
    public void testGetPartListByCourse_WithExistingCourse() {
        // Khi
        List<Part> parts = partService.getPartListByCourse(course);

        // Thì
        assertFalse(parts.isEmpty());
        assertEquals(savedPart.getId(), parts.get(0).getId());
        assertEquals(course.getId(), parts.get(0).getCourse().getId());
    }

    /**
     * Test case TC004: Tìm phần thi theo ID
     * Mục đích: Kiểm tra service có thể tìm phần thi
     * theo ID
     */
    @Test
    public void testFindPartById() {
        // Khi
        Optional<Part> found = partService.findPartById(savedPart.getId());

        // Thì
        assertTrue(found.isPresent());
        assertEquals("Face Part 1", found.get().getName());
        assertEquals(course.getId(), found.get().getCourse().getId());
    }

    /**
     * Test case TC005: Kiểm tra sự tồn tại của phần thi
     * Mục đích: Kiểm tra service có thể xác định chính xác
     * sự tồn tại của phần thi theo ID
     */
    @Test
    public void testExistsById() {
        // Thì
        assertTrue(partService.existsById(savedPart.getId()));
        assertFalse(partService.existsById(999L));
    }

    /**
     * Test case TC006: Tìm phần thi theo ID không tồn tại
     * Mục đích: Kiểm tra service xử lý đúng khi tìm kiếm
     * phần thi với ID không tồn tại
     */
    @Test
    public void testFindPartByIdNotFound() {
        // Khi
        Optional<Part> found = partService.findPartById(999L);

        // Thì
        assertFalse(found.isPresent());
    }

    /**
     * Test case TC007: Lưu phần thi với tên trùng lặp
     * Mục đích: Kiểm tra service có thể lưu phần thi
     * với tên trùng lặp trong cùng một khóa học
     */
    @Test
    public void testSavePartWithDuplicateName() {
        // Cho
        Part duplicatePart = new Part();
        duplicatePart.setName("Face Part 1"); // Tên trùng với phần thi đã tồn tại
        duplicatePart.setCourse(course);

        // Khi
        partService.savePart(duplicatePart);

        // Thì
        Optional<Part> saved = partRepository.findById(duplicatePart.getId());
        assertTrue(saved.isPresent());
        assertEquals("Face Part 1", saved.get().getName());
    }

    /**
     * Test case TC008: Lấy danh sách phần thi với phân trang và kiểm tra thứ tự
     * Mục đích: Kiểm tra service có thể lấy danh sách phần thi
     * với phân trang và đảm bảo thứ tự đúng
     */
    @Test
    public void testGetPartListByCourseWithPaginationAndOrder() {
        // Cho
        // Tạo thêm một phần thi
        Part secondPart = new Part();
        secondPart.setName("Face Part 2");
        secondPart.setCourse(course);
        partRepository.save(secondPart);

        Pageable pageable = PageRequest.of(0, 10);

        // Khi
        Page<Part> parts = partService.getPartLisByCourse(pageable, course.getId());

        // Thì
        assertTrue(parts.hasContent());
        assertEquals(2, parts.getContent().size());
    }

    /**
     * Test case TC009: Lấy danh sách phần thi với khóa học không tồn tại
     * Mục đích: Kiểm tra service xử lý đúng khi lấy danh sách phần thi
     * của một khóa học không tồn tại
     */
    @Test
    public void testGetPartListByNonExistingCourse() {
        // Khi
        List<Part> parts = partService.getPartListByCourse(new Course());

        // Thì
        assertTrue(parts.isEmpty());
    }

    /**
     * Test case TC010: Lấy danh sách phần thi với phân trang và khóa học không tồn tại
     * Mục đích: Kiểm tra service xử lý đúng khi lấy danh sách phần thi
     * với phân trang của một khóa học không tồn tại
     */
    @Test
    public void testGetPartListByCourseWithPaginationAndNonExistingCourse() {
        // Cho
        Pageable pageable = PageRequest.of(0, 10);

        // Khi
        Page<Part> parts = partService.getPartLisByCourse(pageable, 999L);

        // Thì
        assertFalse(parts.hasContent());
    }

    /**
     * Test case TC011: Lưu phần thi với tên rỗng
     * Mục đích: Kiểm tra service xử lý đúng khi lưu
     * phần thi với tên rỗng
     */
    @Test
    public void testSavePartWithEmptyName() {
        // Cho
        Part emptyNamePart = new Part();
        emptyNamePart.setName("");
        emptyNamePart.setCourse(course);

        // Khi
        partService.savePart(emptyNamePart);

        // Thì
        Optional<Part> saved = partRepository.findById(emptyNamePart.getId());
        assertTrue(saved.isPresent());
        assertEquals("", saved.get().getName());
    }

    /**
     * Test case TC012: Lưu phần thi với tên null
     * Mục đích: Kiểm tra service xử lý đúng khi lưu
     * phần thi với tên null
     */
    @Test
    public void testSavePartWithNullName() {
        // Cho
        Part nullNamePart = new Part();
        nullNamePart.setName(null);
        nullNamePart.setCourse(course);

        // Khi
        partService.savePart(nullNamePart);

        // Thì
        Optional<Part> saved = partRepository.findById(nullNamePart.getId());
        assertTrue(saved.isPresent());
        assertNull(saved.get().getName());
    }

    /**
     * Test case TC013: Lưu phần thi với khóa học null
     * Mục đích: Kiểm tra service xử lý đúng khi lưu
     * phần thi với khóa học null
     */
    @Test
    public void testSavePartWithNullCourse() {
        // Cho
        Part nullCoursePart = new Part();
        nullCoursePart.setName("Test Part");
        nullCoursePart.setCourse(null);

        // Khi
        partService.savePart(nullCoursePart);

        // Thì
        Optional<Part> saved = partRepository.findById(nullCoursePart.getId());
        assertTrue(saved.isPresent());
        assertNull(saved.get().getCourse());
    }

    /**
     * Test case TC014: Lấy danh sách phần thi với phân trang và trang không hợp lệ
     * Mục đích: Kiểm tra service xử lý đúng khi lấy danh sách phần thi
     * với số trang không hợp lệ
     */
    @Test
    public void testGetPartListByCourseWithInvalidPage() {
        // Cho
        Pageable pageable = PageRequest.of(-1, 10);

        // Khi
        Page<Part> parts = partService.getPartLisByCourse(pageable, course.getId());

        // Thì
        assertFalse(parts.hasContent());
    }

    /**
     * Test case TC015: Lấy danh sách phần thi với phân trang và kích thước trang không hợp lệ
     * Mục đích: Kiểm tra service xử lý đúng khi lấy danh sách phần thi
     * với kích thước trang không hợp lệ
     */
    @Test
    public void testGetPartListByCourseWithInvalidPageSize() {
        // Cho
        Pageable pageable = PageRequest.of(0, 0);

        // Khi
        Page<Part> parts = partService.getPartLisByCourse(pageable, course.getId());

        // Thì
        assertFalse(parts.hasContent());
    }
} 