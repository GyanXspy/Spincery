package in.sp.main.service;

import in.sp.main.entity.User;
import in.sp.main.repository.UserRepository;
import in.sp.main.service.impl.OTPServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OTPServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private OTPServiceImpl otpService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPhone("1234567890");
        testUser.setPassword("password");
        testUser.setVerified(false);
    }

    @Test
    void testGenerateOTP() {
        String otp = otpService.generateOTP();
        
        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));
    }

    @Test
    void testValidateOTP_ValidOTP() {
        // Set up test user with OTP
        testUser.setOtp("123456");
        testUser.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        boolean result = otpService.validateOTP(testUser, "123456");
        
        assertTrue(result);
        assertTrue(testUser.isVerified());
        assertNull(testUser.getOtp());
        assertNull(testUser.getOtpExpiry());
        
        verify(userRepository).save(testUser);
    }

    @Test
    void testValidateOTP_InvalidOTP() {
        // Set up test user with OTP
        testUser.setOtp("123456");
        testUser.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        
        boolean result = otpService.validateOTP(testUser, "654321");
        
        assertFalse(result);
        assertFalse(testUser.isVerified());
        assertEquals("123456", testUser.getOtp());
    }

    @Test
    void testValidateOTP_ExpiredOTP() {
        // Set up test user with expired OTP
        testUser.setOtp("123456");
        testUser.setOtpExpiry(LocalDateTime.now().minusMinutes(5));
        
        boolean result = otpService.validateOTP(testUser, "123456");
        
        assertFalse(result);
        assertFalse(testUser.isVerified());
    }

    @Test
    void testValidateOTP_NullOTP() {
        boolean result = otpService.validateOTP(testUser, "123456");
        
        assertFalse(result);
    }

    @Test
    void testResendOTP_UserFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // Mock mail sender to avoid actual email sending
        doNothing().when(mailSender).send(any(org.springframework.mail.SimpleMailMessage.class));
        
        assertDoesNotThrow(() -> otpService.resendOTP("test@example.com"));
        
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
        verify(mailSender).send(any(org.springframework.mail.SimpleMailMessage.class));
    }

    @Test
    void testResendOTP_UserNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> otpService.resendOTP("nonexistent@example.com"));
        
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void testResendOTP_UserAlreadyVerified() {
        testUser.setVerified(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        
        assertThrows(RuntimeException.class, () -> otpService.resendOTP("test@example.com"));
        
        verify(userRepository).findByEmail("test@example.com");
    }
} 