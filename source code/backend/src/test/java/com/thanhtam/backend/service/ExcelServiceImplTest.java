package com.thanhtam.backend.unittest;

import com.thanhtam.backend.dto.UserExport;
import com.thanhtam.backend.entity.Intake;
import com.thanhtam.backend.entity.Profile;
import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.repository.IntakeRepository;
import com.thanhtam.backend.repository.RoleRepository;
import com.thanhtam.backend.repository.UserRepository;
import com.thanhtam.backend.service.ExcelServiceImpl;
import com.thanhtam.backend.service.FilesStorageService;
import com.thanhtam.backend.ultilities.ERole;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Unit test cho ExcelServiceImpl.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class ExcelServiceImplTest {

    @Autowired
    private ExcelServiceImpl excelService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private IntakeRepository intakeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FilesStorageService filesStorageService;

    private Role studentRole;
    private Role lecturerRole;
    private Role adminRole;
    private Intake testIntake;
    private List<User> testUsers;

    /**
     * Tạo và lưu các role, intake và user test vào cơ sở dữ liệu.
     */
    @Before
    public void setUp() {
        // Clean up any existing test data
        userRepository.deleteAll();
        roleRepository.deleteAll();
        intakeRepository.deleteAll();

        // Create roles
        studentRole = new Role();
        studentRole.setName(ERole.ROLE_STUDENT);
        studentRole = roleRepository.save(studentRole);

        lecturerRole = new Role();
        lecturerRole.setName(ERole.ROLE_LECTURER);
        lecturerRole = roleRepository.save(lecturerRole);

        adminRole = new Role();
        adminRole.setName(ERole.ROLE_ADMIN);
        adminRole = roleRepository.save(adminRole);

        // Create test intake
        testIntake = new Intake();
        testIntake.setName("TEST2024");
        testIntake = intakeRepository.save(testIntake);

        // Create test users
        testUsers = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            User user = new User();
            user.setUsername("testuser_" + i + "_" + System.currentTimeMillis());
            user.setEmail("test_" + i + "_" + System.currentTimeMillis() + "@example.com");
            user.setPassword(passwordEncoder.encode("password"));
            
            Profile profile = new Profile();
            profile.setFirstName("John" + i);
            profile.setLastName("Doe" + i);
            user.setProfile(profile);
            
            Set<Role> roles = new HashSet<>();
            roles.add(studentRole);
            user.setRoles(roles);
            
            user.setIntake(testIntake);
            user = userRepository.save(user);
            testUsers.add(user);
        }
    }

    /**
     * Test đọc user từ file Excel.
     * Expected output: Danh sách user được đọc chính xác từ file Excel
     */
    @Test
    public void testReadUserFromExcelFile() throws IOException {
        // Create test Excel file
        String testFilePath = "test_users.xlsx";
        createTestExcelFile(testFilePath);

        // Execute
        List<User> users = excelService.readUserFromExcelFile(testFilePath);

        // Verify
        Assert.assertNotNull(users);
        Assert.assertFalse(users.isEmpty());
        Assert.assertEquals(1, users.size());

        User user = users.get(0);
        Assert.assertEquals("testuser", user.getUsername());
        Assert.assertEquals("test@example.com", user.getEmail());
        
        Profile profile = user.getProfile();
        Assert.assertNotNull(profile);
        Assert.assertEquals("John", profile.getFirstName());
        Assert.assertEquals("Doe", profile.getLastName());

        // Verify intake and role
        Assert.assertNotNull(user.getIntake());
        Assert.assertEquals("TEST2024", user.getIntake().getName());
        Assert.assertNotNull(user.getRoles());
        Assert.assertTrue(user.getRoles().stream().anyMatch(role -> role.getName() == ERole.ROLE_STUDENT));

        // Cleanup
        new File(testFilePath).delete();
    }

    /**
     * Test đọc file Excel không tồn tại.
     * Expected output: Ném ra IOException
     */
    @Test(expected = IOException.class)
    public void testReadUserFromNonExistentExcelFile() throws IOException {
        excelService.readUserFromExcelFile("non_existent_file.xlsx");
    }

    /**
     * Test ghi user vào file Excel.
     * Expected output: File Excel được tạo với dữ liệu user chính xác
     */
    @Test
    public void testWriteUserToExcelFile() throws IOException {
        // Prepare export data
        ArrayList<UserExport> userExports = new ArrayList<>();
        for (User user : testUsers) {
            UserExport userExport = new UserExport(
                user.getUsername(),
                user.getProfile().getFirstName(),
                user.getProfile().getLastName(),
                user.getEmail()
            );
            userExports.add(userExport);
        }

        // Execute
        excelService.writeUserToExcelFile(userExports);

        // Verify file was created
        File outputFile = new File("users.xlsx");
        Assert.assertTrue(outputFile.exists());
        
        // Cleanup
        outputFile.delete();
    }

    /**
     * Test ghi danh sách user rỗng vào file Excel.
     * Expected output: File Excel được tạo với chỉ có header
     */
    @Test
    public void testWriteEmptyUserListToExcelFile() throws IOException {
        // Prepare empty export data
        ArrayList<UserExport> emptyUserExports = new ArrayList<>();

        // Execute
        excelService.writeUserToExcelFile(emptyUserExports);

        // Verify file was created
        File outputFile = new File("users.xlsx");
        Assert.assertTrue(outputFile.exists());
        
        // Cleanup
        outputFile.delete();
    }

    /**
     * Test thêm user mới vào database.
     * Expected output: User mới được thêm thành công vào database
     */
    @Test
    public void testInsertUserToDB_NewUsers() {
        // Prepare test data
        List<User> users = new ArrayList<>();
        User user = new User();
        user.setUsername("newuser_" + System.currentTimeMillis());
        user.setEmail("new_" + System.currentTimeMillis() + "@example.com");
        user.setPassword(passwordEncoder.encode("password"));
        
        Profile profile = new Profile();
        profile.setFirstName("New");
        profile.setLastName("User");
        user.setProfile(profile);
        
        Set<Role> roles = new HashSet<>();
        roles.add(studentRole);
        user.setRoles(roles);
        
        user.setIntake(testIntake);
        users.add(user);

        // Execute
        excelService.InsertUserToDB(users);

        // Verify
        Optional<User> savedUser = userRepository.findByUsername(user.getUsername());
        Assert.assertTrue(savedUser.isPresent());
        Assert.assertEquals(user.getEmail(), savedUser.get().getEmail());
        Assert.assertEquals("New", savedUser.get().getProfile().getFirstName());
        Assert.assertEquals("User", savedUser.get().getProfile().getLastName());
    }

    /**
     * Test thêm user đã tồn tại vào database.
     * Expected output: User đã tồn tại không được thêm lại
     */
    @Test
    public void testInsertUserToDB_ExistingUsers() {
        // First create a user
        User existingUser = testUsers.get(0);

        // Try to insert the same user again
        List<User> users = new ArrayList<>();
        User user = new User();
        user.setUsername(existingUser.getUsername());
        user.setEmail(existingUser.getEmail());
        users.add(user);

        // Execute
        excelService.InsertUserToDB(users);

        // Verify only one user exists with this username
        List<User> allUsers = userRepository.findAll();
        long count = allUsers.stream()
            .filter(u -> u.getUsername().equals(existingUser.getUsername()))
            .count();
        Assert.assertEquals(1, count);
    }

    /**
     * Helper method để tạo file Excel test.
     */
    private void createTestExcelFile(String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Users");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Username");
            headerRow.createCell(1).setCellValue("Email");
            headerRow.createCell(2).setCellValue("FirstName");
            headerRow.createCell(3).setCellValue("LastName");
            headerRow.createCell(4).setCellValue("Intake");
            headerRow.createCell(5).setCellValue("Role");

            // Create data row
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("testuser");
            dataRow.createCell(1).setCellValue("test@example.com");
            dataRow.createCell(2).setCellValue("John");
            dataRow.createCell(3).setCellValue("Doe");
            dataRow.createCell(4).setCellValue("TEST2024");
            dataRow.createCell(5).setCellValue("STUDENT");

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
        }
    }
} 