package greencity.service.impl;

import greencity.dto.user.GoogleUserRegistrationDto;
import greencity.entity.User;
import greencity.entity.VerifyEmail;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleUserService {
    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerGoogleUser(GoogleUserRegistrationDto dto) {
        // Check if user already exists with this email
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("User with this email already exists");
        }

        // Create a new user with Google authentication
        User user = new User();
        user.setEmail(dto.getEmail());
        user.getOwnSecurity().setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.ROLE_USER);
        user.setUserStatus(UserStatus.ACTIVATED);
        user.setVerifyEmail(
                VerifyEmail.builder()
                        .user(user)
                        .token(null)// TODO should I mention token and expiryDate?
                        .expiryDate(null)
                        .build());

        return userRepository.save(user);
    }
}