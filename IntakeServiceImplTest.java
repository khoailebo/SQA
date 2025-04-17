package com.thanhtam.backend;

import com.thanhtam.backend.entity.Intake;
import com.thanhtam.backend.repository.IntakeRepository;
import com.thanhtam.backend.service.IntakeServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

// Cấu hình để chạy Spring test
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional // Sau mỗi test, dữ liệu sẽ được rollback lại
public class IntakeServiceImplTest {

    @Autowired
    private IntakeRepository intakeRepository;

    @Autowired
    private IntakeServiceImpl intakeService;

    private Intake intake;

    // Thiết lập dữ liệu mẫu trước mỗi test
    @Before
    public void setUp() {
        intakeRepository.deleteAll(); // Xóa hết dữ liệu để đảm bảo môi trường sạch
        intake = new Intake();        // Tạo đối tượng Intake mẫu
        intake.setName("Test Intake");
        intake.setIntakeCode("TEST001");
        intakeRepository.save(intake); // Lưu vào database
    }

    // Kiểm tra khi tìm bằng code hợp lệ
    @Test
    public void findByCode_ShouldReturnIntake_WhenCodeExists() {
        Intake result = intakeService.findByCode("TEST001");

        assertThat(result).isNotNull(); // Phải tìm thấy Intake
        assertThat(result.getName()).isEqualTo("Test Intake");
        assertThat(result.getIntakeCode()).isEqualTo("TEST001");
    }

    // Kiểm tra khi code không tồn tại
    @Test
    public void findByCode_ShouldReturnNull_WhenCodeDoesNotExist() {
        Intake result = intakeService.findByCode("NONEXISTENT");

        assertThat(result).isNull(); // Không tìm thấy
    }

    // Kiểm tra khi code là chuỗi rỗng
    @Test
    public void findByCode_ShouldReturnNull_WhenCodeIsEmpty() {
        Intake result = intakeService.findByCode("");

        assertThat(result).isNull(); // Không tìm thấy
    }

    // Kiểm tra khi code là null
    @Test
    public void findByCode_ShouldReturnNull_WhenCodeIsNull() {
        Intake result = intakeService.findByCode(null);

        assertThat(result).isNull(); // Không tìm thấy
    }

    // Kiểm tra khi tìm bằng ID hợp lệ
    @Test
    public void findById_ShouldReturnIntake_WhenIdExists() {
        Optional<Intake> result = intakeService.findById(intake.getId());

        assertThat(result).isPresent(); // Phải tìm thấy
        assertThat(result.get().getName()).isEqualTo("Test Intake");
        assertThat(result.get().getIntakeCode()).isEqualTo("TEST001");
    }

    // Kiểm tra khi ID không tồn tại
    @Test
    public void findById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        Optional<Intake> result = intakeService.findById(999999L);

        assertThat(result).isEmpty(); // Không tìm thấy
    }

    // Kiểm tra khi ID là null
    @Test
    public void findById_ShouldReturnEmpty_WhenIdIsNull() {
        Optional<Intake> result = intakeService.findById(null);

        assertThat(result).isEmpty(); // Không tìm thấy
    }

    // Kiểm tra khi ID là số âm
    @Test
    public void findById_ShouldReturnEmpty_WhenIdIsNegative() {
        Optional<Intake> result = intakeService.findById(-1L);

        assertThat(result).isEmpty(); // Không tìm thấy
    }

    // Kiểm tra khi ID là 0
    @Test
    public void findById_ShouldReturnEmpty_WhenIdIsZero() {
        Optional<Intake> result = intakeService.findById(0L);

        assertThat(result).isEmpty(); // Không tìm thấy
    }

    // Kiểm tra phương thức findAll trả về danh sách đúng
    @Test
    public void findAll_ShouldReturnAllIntakes() {
        // Tạo thêm 2 Intake mới
        Intake intake1 = new Intake();
        intake1.setName("Test Intake 1");
        intake1.setIntakeCode("TEST001");
        intakeRepository.save(intake1);

        Intake intake2 = new Intake();
        intake2.setName("Test Intake 2");
        intake2.setIntakeCode("TEST002");
        intakeRepository.save(intake2);

        // Gọi service để lấy danh sách
        List<Intake> result = intakeService.findAll();

        // Kiểm tra kết quả
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Intake::getName)
                .containsExactlyInAnyOrder("Test Intake 1", "Test Intake 2");
        assertThat(result).extracting(Intake::getIntakeCode)
                .containsExactlyInAnyOrder("TEST001", "TEST002");
    }

    // Kiểm tra khi không có Intake nào trong DB
    @Test
    public void findAll_ShouldReturnEmptyList_WhenNoIntakesExist() {
        intakeRepository.deleteAll(); // Xóa hết dữ liệu

        List<Intake> result = intakeService.findAll();

        assertThat(result).isEmpty(); // Danh sách phải rỗng
    }
}
