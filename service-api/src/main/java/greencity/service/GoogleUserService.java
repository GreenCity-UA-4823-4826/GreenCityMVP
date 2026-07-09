package greencity.service;

import greencity.dto.user.UserGoogleRegistrationDto;
import greencity.dto.user.UserVO;

public interface GoogleUserService {
    /**
     * Registers a user with Google account data.
     *
     * @param dto Google registration data.
     * @return registered user.
     */
    UserVO registerGoogleUser(UserGoogleRegistrationDto dto);
}
