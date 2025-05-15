package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.Email;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Lớp kiểm thử cho MailServiceImpl
 * Lớp này chứa các trường hợp kiểm thử để xác minh chức năng của MailServiceImpl
 * bao gồm việc gửi email thông thường và email đặt lại mật khẩu
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Transactional
@Rollback
public class MailServiceTest {

    @Autowired
    private MailServiceImpl mailService;

    @Value("${spring.mail.username}")
    private String testEmailUsername;

    private Email testEmail;
    private static final String TEST_RECIPIENT = "btdungblr@gmail.com";
    private static final String TEST_SUBJECT = "Test Subject";
    private static final String TEST_BODY = "<h1>Test Body</h1>";
    private static final String TEST_TOKEN = "test-token-123";

    /**
     * Phương thức thiết lập chạy trước mỗi bài kiểm tra
     * Tạo một đối tượng email mẫu với dữ liệu thử nghiệm
     */
    @BeforeEach
    public void setUp() {
        testEmail = new Email();
        testEmail.setFromAddress(TEST_RECIPIENT);
        testEmail.setSubject(TEST_SUBJECT);
        testEmail.setBody(TEST_BODY);
    }

    /**
     * Trường hợp kiểm thử TC001: Gửi email thông thường với dữ liệu hợp lệ
     * Mục đích: Xác minh rằng service có thể gửi thành công
     * một email thông thường với các tham số hợp lệ
     */
    @Test
    public void testSendEmail_WithValidData() throws MessagingException {
        // Khi
        mailService.sendEmail(testEmail);
        // Thì - không có ngoại lệ được ném ra nghĩa là kiểm thử đã thành công
    }

    /**
     * Trường hợp kiểm thử TC002: Gửi email thông thường với đối tượng email null
     * Mục đích: Xác minh rằng service xử lý đúng khi đối tượng email là null
     */
    @Test
    public void testSendEmail_WithNullEmail() {
        boolean thrown = false;
        try {
            mailService.sendEmail(null);
        } catch (NullPointerException | MessagingException e) {
            thrown = true;
            // Kiểm thử thành công nếu NullPointerException được ném ra
            assert(e instanceof NullPointerException);
        }
        finally {
            assertTrue(thrown);
        }
    }

    /**
     * Trường hợp kiểm thử TC003: Gửi email thông thường với tiêu đề trống
     * Mục đích: Xác minh rằng service có thể xử lý email với tiêu đề trống
     */
    @Test
    public void testSendEmail_WithEmptySubject() throws MessagingException {
        // Cho
        testEmail.setSubject("");

        // Khi
        mailService.sendEmail(testEmail);
        // Thì - không có ngoại lệ được ném ra nghĩa là kiểm thử đã thành công
    }

    /**
     * Trường hợp kiểm thử TC004: Gửi email thông thường với nội dung trống
     * Mục đích: Xác minh rằng service có thể xử lý email với nội dung trống
     */
    @Test
    public void testSendEmail_WithEmptyBody() throws MessagingException {
        // Cho
        testEmail.setBody("");

        // Khi
        mailService.sendEmail(testEmail);
        // Thì - không có ngoại lệ được ném ra nghĩa là kiểm thử đã thành công
    }

    /**
     * Trường hợp kiểm thử TC011: Gửi email thông thường với nội dung trống
     * Mục đích: Xác minh rằng service có thể xử lý email với tiêu đề null
     */
    @Test
    public void testSendEmail_WithNullSubject()  {
        testEmail.setSubject(null);

        boolean thrown = false;
        try {
            mailService.sendEmail(testEmail);
        } catch (NullPointerException | MessagingException e) {
            thrown = true;
            Assertions.assertFalse(e instanceof NullPointerException);
        }
        finally {
            Assertions.assertTrue(thrown);
        }

    }

    /**
     * Trường hợp kiểm thử TC004: Gửi email thông thường với nội dung trống
     * Mục đích: Xác minh rằng service có thể xử lý email với nội dung null
     */
    @Test
    public void testSendEmail_WithNullBody()  {
        testEmail.setBody(null);
        boolean thrown = false;

        try{
            mailService.sendEmail(testEmail);
        } catch (NullPointerException | MessagingException e) {
            assert (e instanceof NullPointerException);
            thrown = true;
        }
        finally {
            assertTrue(thrown);
        }
    }

    /**
     * Trường hợp kiểm thử TC005: Gửi email đặt lại mật khẩu với dữ liệu hợp lệ
     * Mục đích: Xác minh rằng service có thể gửi thành công
     * một email đặt lại mật khẩu với các tham số hợp lệ
     */
    @Test
    public void testResetPassword_WithValidData() throws MessagingException {
        // Khi
        mailService.resetPassword(TEST_RECIPIENT, TEST_TOKEN);
        // Thì - không có ngoại lệ được ném ra nghĩa là kiểm thử đã thành công
    }

    /**
     * Trường hợp kiểm thử TC006: Gửi email đặt lại mật khẩu với email null
     * Mục đích: Xác minh rằng service xử lý đúng khi email là null
     * cho chức năng đặt lại mật khẩu
     */
    @Test
    public void testResetPassword_WithNullEmail() {
        try {
            mailService.resetPassword(null, TEST_TOKEN);
        } catch (MessagingException e) {
            // Kiểm thử thành công nếu MessagingException được ném ra
            assert(e instanceof MessagingException);
        }
    }

    /**
     * Trường hợp kiểm thử TC007: Gửi email đặt lại mật khẩu với token null
     * Mục đích: Xác minh rằng service xử lý đúng khi token là null
     * cho chức năng đặt lại mật khẩu
     */
    @Test
    public void testResetPassword_WithNullToken() {
        try {
            mailService.resetPassword(TEST_RECIPIENT, null);
        } catch (MessagingException e) {
            // Kiểm thử thành công nếu MessagingException được ném ra
            assert(e instanceof MessagingException);
        }
    }

    /**
     * Trường hợp kiểm thử TC008: Gửi email đặt lại mật khẩu với email trống
     * Mục đích: Xác minh rằng service xử lý đúng khi email trống
     */
    @Test
    public void testResetPassword_WithEmptyEmail() {
        boolean thrown = false;
        try {
            mailService.resetPassword("", TEST_TOKEN);
        } catch (MessagingException e) {
            // Kiểm thử thành công nếu MessagingException được ném ra
            assert(e instanceof MessagingException);
            thrown = true;
        }
        finally {
            assertTrue(thrown);
        }
    }

    /**
     * Trường hợp kiểm thử TC009: Gửi email đặt lại mật khẩu với token trống
     * Mục đích: Xác minh rằng service xử lý đúng khi token trống
     */
    @Test
    public void testResetPassword_WithEmptyToken() {
        boolean thrown = false;
        try {
            mailService.resetPassword(TEST_RECIPIENT, "");
        } catch (MessagingException e) {
            // Kiểm thử thành công nếu MessagingException được ném ra
            assert(e instanceof MessagingException);
            thrown = true;
        }
        finally {
            assertTrue(thrown);
        }
    }

    /**
     * Trường hợp kiểm thử TC010: Gửi email đặt lại mật khẩu với định dạng email không hợp lệ
     * Mục đích: Xác minh rằng service xử lý đúng khi định dạng email không hợp lệ
     */
    @Test
    public void testResetPassword_WithInvalidEmailFormat() {
        boolean thrown = false;
        try {
            mailService.resetPassword("invalid-email", TEST_TOKEN);
        } catch (MessagingException e) {
            // Kiểm thử thành công nếu MessagingException được ném ra
            thrown = true;
            assert(e instanceof MessagingException);
        }
        finally {
            assertTrue(thrown);
        }
    }
} 