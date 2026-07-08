package greencity.service;

import greencity.dto.user.UserGoogleRegistrationDto;
import greencity.dto.user.UserVO;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

//TODO use VO instead of entity.User

@Service
@RequiredArgsConstructor
public class GoogleUserServiceImpl implements GoogleUserService {
    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserVO registerGoogleUser(UserGoogleRegistrationDto dto) {
        // Check if user already exists with this email
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("User with this email already exists");
        }

        // Create a new user with Google authentication
        UserVO user = new UserVO();
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