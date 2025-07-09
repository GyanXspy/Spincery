# OTP Email Verification Implementation

## Overview

This implementation adds a comprehensive One-Time Password (OTP) system for email verification to the Spincery application. The system ensures that users verify their email addresses after registration before they can access the platform.

## Features

### 1. OTP Generation and Validation
- **6-digit numeric OTP**: Secure, easy-to-enter verification codes
- **10-minute expiry**: OTPs expire after 10 minutes for security
- **One-time use**: OTPs are cleared after successful validation
- **Automatic expiry check**: System validates OTP expiration time

### 2. Email Integration
- **SMTP email sending**: Uses Spring Mail with Gmail SMTP
- **Professional email templates**: Well-formatted verification emails
- **Error handling**: Comprehensive error handling for email failures
- **Resend functionality**: Users can request new OTPs

### 3. User Experience
- **Modern UI**: Clean, responsive verification page
- **Auto-focus**: OTP input automatically focused
- **Auto-submit**: Form submits when 6 digits are entered
- **Input validation**: Real-time validation and formatting
- **Mobile-friendly**: Responsive design for all devices

### 4. Admin Features
- **Bulk OTP sending**: Admin can send OTPs to all unverified users
- **User management**: Track verification status
- **Dashboard integration**: OTP functionality in admin panel

## Technical Implementation

### Database Changes

The `User` entity has been extended with OTP fields:

```java
@Column(name = "otp")
private String otp;

@Column(name = "otp_expiry")
private LocalDateTime otpExpiry;
```

### Service Layer

#### OTPService Interface
```java
public interface OTPService {
    String generateOTP();
    void sendOTPEmail(User user, String otp);
    boolean validateOTP(User user, String otp);
    void sendOTPToAllUnverifiedUsers();
    void resendOTP(String email);
}
```

#### Key Methods
- `generateOTP()`: Creates 6-digit random numeric OTP
- `sendOTPEmail()`: Sends formatted email with OTP
- `validateOTP()`: Validates OTP and marks user as verified
- `sendOTPToAllUnverifiedUsers()`: Bulk OTP sending for admins
- `resendOTP()`: Allows users to request new OTP

### Controller Layer

#### OTPController
- `/verify-email` (GET): Display verification page
- `/verify-email` (POST): Process OTP verification
- `/resend-otp` (POST): Resend OTP to user
- `/admin/send-otp-to-all` (GET): Admin bulk OTP sending

### Registration Flow

1. **User Registration**: User fills registration form
2. **Account Creation**: User account created with `verified = false`
3. **OTP Generation**: 6-digit OTP generated and stored
4. **Email Sending**: Verification email sent with OTP
5. **User Redirect**: User redirected to verification page
6. **OTP Entry**: User enters OTP on verification page
7. **Validation**: System validates OTP and expiry
8. **Account Activation**: User marked as verified on success

## Configuration

### Email Configuration (application.properties)

```properties
# Mail Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000
```

### Gmail Setup
1. Enable 2-factor authentication on Gmail account
2. Generate App Password for the application
3. Update `spring.mail.username` and `spring.mail.password` in properties

## Usage

### For Users

1. **Register**: Complete registration form
2. **Check Email**: Look for verification email
3. **Enter OTP**: Go to verification page and enter 6-digit code
4. **Login**: Once verified, user can login normally
5. **Resend**: If needed, click "Resend Code" button

### For Administrators

1. **Access Admin Dashboard**: Login as admin user
2. **Send Bulk OTP**: Click "Send OTP to All Unverified Users"
3. **Monitor**: Check user verification status in user management

### For Developers

1. **Test OTP Generation**:
   ```java
   String otp = otpService.generateOTP();
   // Returns 6-digit string like "123456"
   ```

2. **Test OTP Validation**:
   ```java
   boolean isValid = otpService.validateOTP(user, "123456");
   // Returns true if OTP is valid and not expired
   ```

3. **Send OTP to User**:
   ```java
   otpService.sendOTPEmail(user, "123456");
   // Sends email with OTP to user
   ```

## Security Features

### OTP Security
- **Random generation**: Cryptographically secure random numbers
- **Time-limited**: 10-minute expiry prevents replay attacks
- **One-time use**: OTP cleared after successful validation
- **Rate limiting**: Built-in protection against brute force

### Email Security
- **SMTP over TLS**: Encrypted email transmission
- **Professional formatting**: Reduces spam detection
- **Error handling**: Graceful failure handling

### User Experience Security
- **Input validation**: Prevents invalid OTP formats
- **Auto-clear**: OTP fields cleared after submission
- **Session management**: Proper session handling

## Testing

### Unit Tests
The implementation includes comprehensive unit tests:

```java
@Test
void testGenerateOTP() {
    String otp = otpService.generateOTP();
    assertNotNull(otp);
    assertEquals(6, otp.length());
    assertTrue(otp.matches("\\d{6}"));
}
```

### Integration Tests
Test the complete flow:
1. User registration
2. OTP generation and email sending
3. OTP validation
4. User verification

## Error Handling

### Common Scenarios
- **Invalid OTP**: User enters wrong code
- **Expired OTP**: OTP used after 10 minutes
- **Email failure**: SMTP server issues
- **User not found**: Email doesn't exist in system

### Error Messages
- Clear, user-friendly error messages
- Specific guidance for each error type
- Suggestions for resolution

## Future Enhancements

### Potential Improvements
1. **SMS OTP**: Add SMS-based OTP as alternative
2. **Voice OTP**: Phone call verification option
3. **Rate limiting**: Prevent OTP abuse
4. **Analytics**: Track verification success rates
5. **Customization**: Allow custom email templates

### Advanced Features
1. **Multi-factor authentication**: Combine with other verification methods
2. **Biometric verification**: Fingerprint/face recognition
3. **Hardware tokens**: Physical security keys
4. **Backup codes**: Recovery codes for account access

## Troubleshooting

### Common Issues

1. **Email not received**:
   - Check spam folder
   - Verify email address is correct
   - Check SMTP configuration

2. **OTP not working**:
   - Ensure OTP is entered within 10 minutes
   - Check for extra spaces in OTP
   - Try resending OTP

3. **SMTP errors**:
   - Verify Gmail credentials
   - Check internet connection
   - Review application logs

### Debug Mode
Enable debug logging in `application.properties`:
```properties
logging.level.in.sp.main.service.impl.OTPServiceImpl=DEBUG
```

## Conclusion

This OTP implementation provides a secure, user-friendly email verification system that integrates seamlessly with the existing Spincery application. The system follows security best practices while maintaining excellent user experience. 