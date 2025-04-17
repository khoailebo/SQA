// Khai báo package
package com.thanhtam.backend;

// Import các lớp cần thiết
import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.repository.RoleRepository;
import com.thanhtam.backend.service.RoleServiceImpl;
import com.thanhtam.backend.ultilities.ERole;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

// Sử dụng SpringRunner để chạy các test tích hợp với Spring context
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional // Đảm bảo rằng mỗi test sẽ chạy trong một transaction và rollback sau khi kết thúc
public class RoleServiceImplTest {

    // Tự động inject repository và service cần test
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleServiceImpl roleService;

    @Autowired
    private EntityManager entityManager;

    // Khai báo các biến dùng chung cho các test case
    private Role adminRole;
    private Role lecturerRole;
    private Role studentRole;

    // Hàm setup dữ liệu mẫu chạy trước mỗi test
    @Before
    public void setUp() {
        // Xoá toàn bộ dữ liệu trong bảng Role
        roleRepository.deleteAll();
        entityManager.flush(); // Đẩy dữ liệu xuống database

        // Tạo và lưu role admin
        adminRole = new Role();
        adminRole.setName(ERole.ROLE_ADMIN);
        roleRepository.save(adminRole);

        // Tạo và lưu role giảng viên
        lecturerRole = new Role();
        lecturerRole.setName(ERole.ROLE_LECTURER);
        roleRepository.save(lecturerRole);

        // Tạo và lưu role sinh viên
        studentRole = new Role();
        studentRole.setName(ERole.ROLE_STUDENT);
        roleRepository.save(studentRole);

        // Flush để đảm bảo dữ liệu được lưu
        entityManager.flush();
    }

    // Kiểm tra nếu tìm kiếm theo ROLE_ADMIN thì trả về đúng đối tượng
    @Test
    public void findByName_ShouldReturnAdminRole_WhenNameIsAdmin() {
        Optional<Role> result = roleService.findByName(ERole.ROLE_ADMIN);
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(ERole.ROLE_ADMIN);
    }

    // Kiểm tra nếu tìm kiếm theo ROLE_LECTURER thì trả về đúng đối tượng
    @Test
    public void findByName_ShouldReturnLecturerRole_WhenNameIsLecturer() {
        Optional<Role> result = roleService.findByName(ERole.ROLE_LECTURER);
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(ERole.ROLE_LECTURER);
    }

    // Kiểm tra nếu tìm kiếm theo ROLE_STUDENT thì trả về đúng đối tượng
    @Test
    public void findByName_ShouldReturnStudentRole_WhenNameIsStudent() {
        Optional<Role> result = roleService.findByName(ERole.ROLE_STUDENT);
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(ERole.ROLE_STUDENT);
    }

    // Kiểm tra nếu role không tồn tại thì phải trả về Optional rỗng
    @Test
    public void findByName_ShouldReturnEmpty_WhenRoleDoesNotExist() {
        Optional<Role> result = roleService.findByName(ERole.valueOf("ROLE_NONEXISTENT"));
        assertThat(result).isEmpty();
    }
}
