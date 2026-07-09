package greencity.service;

import greencity.dto.user.UserGoogleRegistrationDto;
import greencity.dto.user.UserVO;
import greencity.entity.User;
import greencity.enums.EmailNotification;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.repository.UserRepo;
import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoogleUserServiceImplTest {
    private static final String EMAIL = "google.user@gmail.com";
    private static final String PASSWORD = "password";
    private static final String ENCODED_PASSWORD = "encoded-password";
    private static final String GOOGLE_ID = "google-sub-123";

    @Test
    void registerGoogleUserCreatesUserEntity() {
        AtomicReference<User> savedUser = new AtomicReference<>();
        GoogleUserServiceImpl googleUserService = new GoogleUserServiceImpl(
            userRepo(Optional.empty(), savedUser), passwordEncoder(), new ModelMapper());
        UserGoogleRegistrationDto dto = new UserGoogleRegistrationDto(EMAIL, PASSWORD, GOOGLE_ID);

        UserVO actual = googleUserService.registerGoogleUser(dto);
        User user = savedUser.get();

        assertEquals(EMAIL, actual.getEmail());
        assertEquals(Role.ROLE_USER, actual.getRole());
        assertNotNull(user);
        assertEquals(EMAIL, user.getEmail());
        assertEquals("google.user", user.getName());
        assertEquals(Role.ROLE_USER, user.getRole());
        assertEquals(UserStatus.ACTIVATED, user.getUserStatus());
        assertEquals(EmailNotification.DISABLED, user.getEmailNotification());
        assertEquals(GOOGLE_ID, user.getGoogleId());
        assertTrue(user.getIsGoogleAuth());
        assertNotNull(user.getDateOfRegistration());
        assertNotNull(user.getRefreshTokenKey());
        assertNotNull(user.getOwnSecurity());
        assertEquals(user, user.getOwnSecurity().getUser());
        assertEquals(ENCODED_PASSWORD, user.getOwnSecurity().getPassword());
        assertNotNull(user.getVerifyEmail());
        assertEquals(user, user.getVerifyEmail().getUser());
    }

    @Test
    void registerGoogleUserThrowsWhenEmailAlreadyExists() {
        AtomicReference<User> savedUser = new AtomicReference<>();
        GoogleUserServiceImpl googleUserService = new GoogleUserServiceImpl(
            userRepo(Optional.of(new User()), savedUser), passwordEncoder(), new ModelMapper());
        UserGoogleRegistrationDto dto = new UserGoogleRegistrationDto(EMAIL, PASSWORD, GOOGLE_ID);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> googleUserService.registerGoogleUser(dto));

        assertEquals("User with this email already exists", exception.getMessage());
        assertEquals(null, savedUser.get());
    }

    private UserRepo userRepo(Optional<User> existingUser, AtomicReference<User> savedUser) {
        return (UserRepo) Proxy.newProxyInstance(
            UserRepo.class.getClassLoader(),
            new Class<?>[] {UserRepo.class},
            (proxy, method, args) -> switch (method.getName()) {
                case "findByEmail" -> existingUser;
                case "save" -> {
                    savedUser.set((User) args[0]);
                    yield args[0];
                }
                case "toString" -> "UserRepo test proxy";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException(method.getName());
            });
    }

    private PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return ENCODED_PASSWORD;
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return ENCODED_PASSWORD.equals(encodedPassword);
            }
        };
    }
}
