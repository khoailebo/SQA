package com.thanhtam.backend;

import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.repository.CourseRepository;
import com.thanhtam.backend.service.CourseServiceImpl;
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

@RunWith(SpringRunner.class) // Sử dụng SpringRunner để tích hợp với Spring Test
@SpringBootTest // Tải toàn bộ context của Spring Boot để kiểm thử
@Transactional // Đảm bảo mỗi test sẽ rollback sau khi chạy, tránh ảnh hưởng dữ liệu
public class CourseServiceImplTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseServiceImpl courseService;

    @Autowired
    private EntityManager entityManager;

    private Course course1;
    private Course course2;

    @Before
    public void setUp() {
        // Xóa tất cả dữ liệu trước mỗi test để đảm bảo tính độc lập
        courseRepository.deleteAll();
        entityManager.flush();

        // Khởi tạo và lưu Course 1
        course1 = new Course();
        course1.setName("Test Course 1");
        course1.setCourseCode("TEST001");
        course1.setImgUrl("test1.jpg");
        courseRepository.save(course1);

        // Khởi tạo và lưu Course 2
        course2 = new Course();
        course2.setName("Test Course 2");
        course2.setCourseCode("TEST002");
        course2.setImgUrl("test2.jpg");
        courseRepository.save(course2);

        entityManager.flush();
    }

    @Test
    public void getCourseById_ShouldReturnCourse_WhenIdExists() {
        // Kiểm tra trả về course khi ID tồn tại
        Optional<Course> result = courseService.getCourseById(course1.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Test Course 1");
        assertThat(result.get().getCourseCode()).isEqualTo("TEST001");
        assertThat(result.get().getImgUrl()).isEqualTo("test1.jpg");
    }

    @Test
    public void getCourseById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        // Kiểm tra trả về empty khi ID không tồn tại
        Optional<Course> result = courseService.getCourseById(99999L);
        assertThat(result).isEmpty();
    }

    @Test
    public void getCourseById_ShouldReturnEmpty_WhenIdIsNull() {
        // Kiểm tra trả về empty khi ID null
        Optional<Course> result = courseService.getCourseById(null);
        assertThat(result).isEmpty();
    }

    @Test
    public void getCourseById_ShouldReturnEmpty_WhenIdIsNegative() {
        // Kiểm tra trả về empty khi ID âm
        Optional<Course> result = courseService.getCourseById(-1L);
        assertThat(result).isEmpty();
    }

    @Test
    public void getCourseById_ShouldReturnEmpty_WhenIdIsZero() {
        // Kiểm tra trả về empty khi ID = 0
        Optional<Course> result = courseService.getCourseById(0L);
        assertThat(result).isEmpty();
    }

    @Test
    public void getCourseList_ShouldReturnAllCourses() {
        // Kiểm tra lấy tất cả các khóa học
        List<Course> result = courseService.getCourseList();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Course::getName)
                .containsExactlyInAnyOrder("Test Course 1", "Test Course 2");
        assertThat(result).extracting(Course::getCourseCode)
                .containsExactlyInAnyOrder("TEST001", "TEST002");
    }

    @Test
    public void getCourseListByPage_ShouldReturnPaginatedCourses() {
        // Kiểm tra phân trang khóa học
        Page<Course> result = courseService.getCourseListByPage(PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(Course::getName)
                .containsExactlyInAnyOrder("Test Course 1", "Test Course 2");
    }

    @Test
    public void saveCourse_ShouldSaveCourseSuccessfully() {
        // Kiểm tra thêm mới khóa học
        Course newCourse = new Course();
        newCourse.setName("New Course");
        newCourse.setCourseCode("NEW001");
        newCourse.setImgUrl("new.jpg");

        courseService.saveCourse(newCourse);
        entityManager.flush();

        Optional<Course> savedCourse = courseRepository.findById(newCourse.getId());
        assertThat(savedCourse).isPresent();
        assertThat(savedCourse.get().getName()).isEqualTo("New Course");
        assertThat(savedCourse.get().getCourseCode()).isEqualTo("NEW001");
        assertThat(savedCourse.get().getImgUrl()).isEqualTo("new.jpg");
    }

    @Test
    public void delete_ShouldDeleteCourseSuccessfully() {
        // Kiểm tra xóa khóa học
        courseService.delete(course1.getId());
        entityManager.flush();

        Optional<Course> deletedCourse = courseRepository.findById(course1.getId());
        assertThat(deletedCourse).isEmpty();
    }

    @Test
    public void existsByCode_ShouldReturnTrue_WhenCodeExists() {
        // Kiểm tra tồn tại theo mã khóa học
        boolean result = courseService.existsByCode("TEST001");
        assertThat(result).isTrue();
    }

    @Test
    public void existsByCode_ShouldReturnFalse_WhenCodeDoesNotExist() {
        // Kiểm tra mã không tồn tại
        boolean result = courseService.existsByCode("NONEXISTENT");
        assertThat(result).isFalse();
    }

    @Test
    public void existsById_ShouldReturnTrue_WhenIdExists() {
        // Kiểm tra ID tồn tại
        boolean result = courseService.existsById(course1.getId());
        assertThat(result).isTrue();
    }

    @Test
    public void existsById_ShouldReturnFalse_WhenIdDoesNotExist() {
        // Kiểm tra ID không tồn tại
        boolean result = courseService.existsById(99999L);
        assertThat(result).isFalse();
    }

    @Test
    public void findAllByIntakeId_ShouldReturnCoursesForIntake() {
        // Kiểm tra tìm khóa học theo intakeId
        List<Course> result = courseService.findAllByIntakeId(1L);
        assertThat(result).isNotNull();
    }

    @Test
    public void findCourseByPartId_ShouldReturnCourse() {
        // Kiểm tra tìm khóa học theo partId
        Course result = courseService.findCourseByPartId(1L);
        assertThat(result).isNotNull();
    }
}
