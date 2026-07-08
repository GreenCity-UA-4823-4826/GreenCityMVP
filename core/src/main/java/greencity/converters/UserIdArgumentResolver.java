package greencity.converters;

import greencity.annotations.CurrentUserId;
import greencity.service.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import java.security.Principal;

@Component
@RequiredArgsConstructor
public class UserIdArgumentResolver implements HandlerMethodArgumentResolver {
    private final UserService userService;

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return parameter.getParameterAnnotation(CurrentUserId.class) != null
            && parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        @NonNull NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory) {
        Principal principal = webRequest.getUserPrincipal();
        if (principal == null) {
            return null;
        }
        return userService.findByEmail(principal.getName()).getId();
    }
}
