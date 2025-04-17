package com.thanhtam.backend;

import com.thanhtam.backend.entity.Exam;
import com.thanhtam.backend.entity.ExamUser;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.repository.ExamRepository;
import com.thanhtam.backend.repository.ExamUserRepository;
import com.thanhtam.backend.service.ExamUserServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class ExamUserServiceImplTest {

    @Autowired
    private ExamUserRepository examUserRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ExamUserServiceImpl examUserService;

    @Autowired
    private EntityManager entityManager;

    private Exam exam;
    private User user1;
    private User user2;
    private List<User> userList;

    @Before
    public void setUp() {
        // Xoá toàn bộ dữ liệu ExamUser hiện có để đảm bảo môi trường sạch
        examUserRepository.deleteAll();
        entityManager.flush();

        // Tạo một kỳ thi mới
        exam = new Exam();
        exam.setDurationExam(60); // 60 phút
        exam.setCanceled(false);
        examRepository.save(exam);

        // Tạo người dùng thứ nhất
        user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("password1");
        entityManager.persist(user1);

        // Tạo người dùng thứ hai
        user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("password2");
        entityManager.persist(user2);

        // Thêm 2 người dùng vào danh sách userList
        userList = new ArrayList<>();
        userList.add(user1);
        userList.add(user2);

        // Đẩy toàn bộ dữ liệu vừa tạo xuống DB
        entityManager.flush();
    }

    @Test
    public void create_ShouldCreateExamUsersSuccessfully() {
        // Kiểm tra tạo mới danh sách ExamUser thành công
        examUserService.create(exam, userList);
        entityManager.flush();

        List<ExamUser> examUsers = examUserRepository.findAll();
        assertThat(examUsers).hasSize(2); // Có 2 người dùng đã được gán vào kỳ thi

        // Kiểm tra từng thuộc tính đã lưu đúng chưa
        assertThat(examUsers).extracting(ExamUser::getUser)
                .containsExactlyInAnyOrder(user1, user2);
        assertThat(examUsers).extracting(ExamUser::getExam)
                .containsOnly(exam);
        assertThat(examUsers).extracting(ExamUser::getRemainingTime)
                .containsOnly(3600); // 60 phút * 60 giây
        assertThat(examUsers).extracting(ExamUser::getTotalPoint)
                .containsOnly(-1.0); // Điểm mặc định
    }

    @Test
    public void getExamListByUsername_ShouldReturnExamsForUser() {
        // Kiểm tra lấy danh sách kỳ thi theo tên người dùng
        examUserService.create(exam, userList);
        entityManager.flush();

        List<ExamUser> result = examUserService.getExamListByUsername("user1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getUsername()).isEqualTo("user1");
        assertThat(result.get(0).getExam()).isEqualTo(exam);
    }

    @Test
    public void findByExamAndUser_ShouldReturnExamUser() {
        // Tìm ExamUser theo examId và username
        examUserService.create(exam, userList);
        entityManager.flush();

        ExamUser result = examUserService.findByExamAndUser(exam.getId(), "user1");

        assertThat(result).isNotNull();
        assertThat(result.getUser().getUsername()).isEqualTo("user1");
        assertThat(result.getExam()).isEqualTo(exam);
    }

    @Test
    public void update_ShouldUpdateExamUserSuccessfully() {
        // Cập nhật điểm số cho một ExamUser
        examUserService.create(exam, userList);
        entityManager.flush();

        ExamUser examUser = examUserService.findByExamAndUser(exam.getId(), "user1");
        examUser.setTotalPoint(8.5);

        examUserService.update(examUser);
        entityManager.flush();

        ExamUser updatedExamUser = examUserService.findByExamAndUser(exam.getId(), "user1");
        assertThat(updatedExamUser.getTotalPoint()).isEqualTo(8.5);
    }

    @Test
    public void findExamUserById_ShouldReturnExamUser_WhenIdExists() {
        // Tìm kiếm ExamUser theo ID - Trường hợp có tồn tại
        examUserService.create(exam, userList);
        entityManager.flush();

        ExamUser examUser = examUserService.findByExamAndUser(exam.getId(), "user1");
        Optional<ExamUser> result = examUserService.findExamUserById(examUser.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getUser().getUsername()).isEqualTo("user1");
        assertThat(result.get().getExam()).isEqualTo(exam);
    }

    @Test
    public void findExamUserById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        // Trường hợp không tìm thấy ExamUser với ID không tồn tại
        Optional<ExamUser> result = examUserService.findExamUserById(99999L);
        assertThat(result).isEmpty();
    }

    @Test
    public void getCompleteExams_ShouldReturnCompletedExams() {
        // Lấy danh sách các kỳ thi đã hoàn thành (có điểm)
        examUserService.create(exam, userList);
        entityManager.flush();

        ExamUser examUser = examUserService.findByExamAndUser(exam.getId(), "user1");
        examUser.setTotalPoint(8.5);
        examUserService.update(examUser);
        entityManager.flush();

        List<ExamUser> result = examUserService.getCompleteExams(exam.getPart().getCourse().getId(), "user1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTotalPoint()).isEqualTo(8.5);
    }

    @Test
    public void findAllByExam_Id_ShouldReturnAllExamUsersForExam() {
        // Lấy tất cả người dùng đã tham gia một kỳ thi cụ thể
        examUserService.create(exam, userList);
        entityManager.flush();

        List<ExamUser> result = examUserService.findAllByExam_Id(exam.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ExamUser::getUser)
                .containsExactlyInAnyOrder(user1, user2);
    }

    @Test
    public void findExamUsersByIsFinishedIsTrueAndExam_Id_ShouldReturnFinishedExams() {
        // Lấy các ExamUser đã hoàn thành kỳ thi (isFinished = true)
        examUserService.create(exam, userList);
        entityManager.flush();

        ExamUser examUser = examUserService.findByExamAndUser(exam.getId(), "user1");
        examUser.setIsFinished(true);
        examUserService.update(examUser);
        entityManager.flush();

        List<ExamUser> result = examUserService.findExamUsersByIsFinishedIsTrueAndExam_Id(exam.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getUsername()).isEqualTo("user1");
        assertThat(result.get(0).getIsFinished()).isTrue();
    }
}
}