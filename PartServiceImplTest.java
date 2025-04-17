package com.thanhtam.backend;

import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.entity.Part;
import com.thanhtam.backend.repository.CourseRepository;
import com.thanhtam.backend.repository.PartRepository;
import com.thanhtam.backend.service.CourseServiceImpl;
import com.thanhtam.backend.service.PartServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class PartServiceImplTest {

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private PartServiceImpl partService;

    @Autowired
    private CourseServiceImpl courseService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EntityManager entityManager;

    private Course course;
    private Part part1;
    private Part part2;

    @Before
    public void setUp() {
        // Xóa tất cả dữ liệu trong bảng Part và Course trước khi chạy test
        partRepository.deleteAll();
        entityManager.flush();
        courseRepository.deleteAll();
        entityManager.flush();

        // Tạo và lưu một Course mẫu
        course = new Course();
        course.setName("Test Course");
        course.setCourseCode("TEST001");
        course.setImgUrl("test.jpg");
        courseService.saveCourse(course);
        entityManager.flush();

        // Tạo và lưu Part thứ nhất
        part1 = new Part();
        part1.setName("Test Part 1");
        part1.setCourse(course);
        partRepository.save(part1);

        // Tạo và lưu Part thứ hai
        part2 = new Part();
        part2.setName("Test Part 2");
        part2.setCourse(course);
        partRepository.save(part2);

        entityManager.flush();
    }

    @Test
    public void findPartById_ShouldReturnPart_WhenIdExists() {
        // Kiểm tra khi tìm part theo id tồn tại
        Optional<Part> result = partService.findPartById(part1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Test Part 1");
        assertThat(result.get().getCourse()).isEqualTo(course);
    }

    @Test
    public void findPartById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        // Kiểm tra khi tìm part theo id không tồn tại
        Optional<Part> result = partService.findPartById(99999L);

        assertThat(result).isEmpty();
    }

    @Test
    public void findPartById_ShouldReturnEmpty_WhenIdIsNull() {
        // Kiểm tra khi truyền id null
        Optional<Part> result = partService.findPartById(null);

        assertThat(result).isEmpty();
    }

    @Test
    public void findPartById_ShouldReturnEmpty_WhenIdIsNegative() {
        // Kiểm tra khi truyền id âm
        Optional<Part> result = partService.findPartById(-1L);

        assertThat(result).isEmpty();
    }

    @Test
    public void findPartById_ShouldReturnEmpty_WhenIdIsZero() {
        // Kiểm tra khi truyền id bằng 0
        Optional<Part> result = partService.findPartById(0L);

        assertThat(result).isEmpty();
    }

    @Test
    public void getPartListByCourse_ShouldReturnAllPartsForCourse() {
        // Kiểm tra lấy tất cả phần học thuộc course
        List<Part> result = partService.getPartListByCourse(course);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Part::getName)
                .containsExactlyInAnyOrder("Test Part 1", "Test Part 2");
    }

    @Test
    public void getPartLisByCourse_ShouldReturnPaginatedParts() {
        // Kiểm tra phân trang danh sách phần học theo course
        Page<Part> result = partService.getPartLisByCourse(PageRequest.of(0, 10), course.getId());

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(Part::getName)
                .containsExactlyInAnyOrder("Test Part 1", "Test Part 2");
    }

    @Test
    public void savePart_ShouldSavePartSuccessfully() {
        // Kiểm tra lưu một phần học mới
        Part newPart = new Part();
        newPart.setName("New Part");
        newPart.setCourse(course);

        partService.savePart(newPart);
        entityManager.flush();

        Optional<Part> savedPart = partRepository.findById(newPart.getId());
        assertThat(savedPart).isPresent();
        assertThat(savedPart.get().getName()).isEqualTo("New Part");
        assertThat(savedPart.get().getCourse()).isEqualTo(course);
    }

    @Test
    public void existsById_ShouldReturnTrue_WhenIdExists() {
        // Kiểm tra tồn tại của part theo id hợp lệ
        boolean result = partService.existsById(part1.getId());

        assertThat(result).isTrue();
    }

    @Test
    public void existsById_ShouldReturnFalse_WhenIdDoesNotExist() {
        // Kiểm tra tồn tại của part theo id không hợp lệ
        boolean result = partService.existsById(99999L);

        assertThat(result).isFalse();
    }
}
