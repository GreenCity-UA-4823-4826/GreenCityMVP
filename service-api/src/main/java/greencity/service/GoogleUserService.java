package greencity.service;

import greencity.dto.user.UserGoogleRegistrationDto;
import greencity.dto.user.UserVO;

public interface GoogleUserService {

    //TODO javaDoc

    /**
     * Method to
     *
     * @param
     * @return {}.
     */
    UserVO registerGoogleUser(UserGoogleRegistrationDto dto);

}
