package greencity.service;

import greencity.dto.user.UserGoogleRegistrationDto;
import greencity.dto.user.UserVO;
import greencity.entity.OwnSecurity;
import greencity.entity.User;
import greencity.entity.VerifyEmail;
import greencity.enums.EmailNotification;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.repository.UserRepo;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleUserServiceImpl implements GoogleUserService {
    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public UserVO registerGoogleUser(UserGoogleRegistrationDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("User with this email already exists");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setName(dto.getEmail().substring(0, dto.getEmail().indexOf('@')));
        user.setRole(Role.ROLE_USER);
        user.setUserStatus(UserStatus.ACTIVATED);
        user.setDateOfRegistration(LocalDateTime.now());
        user.setRefreshTokenKey(UUID.randomUUID().toString());
        user.setEmailNotification(EmailNotification.DISABLED);
        user.setIsGoogleAuth(true);
        user.setGoogleId(dto.getGoogleId());

        OwnSecurity ownSecurity = OwnSecurity.builder()
            .user(user)
            .password(passwordEncoder.encode(dto.getPassword()))
            .build();
        user.setOwnSecurity(ownSecurity);

        VerifyEmail verifyEmail = VerifyEmail.builder()
            .user(user)
            .build();
        user.setVerifyEmail(verifyEmail);

        return modelMapper.map(userRepository.save(user), UserVO.class);
    }
}
