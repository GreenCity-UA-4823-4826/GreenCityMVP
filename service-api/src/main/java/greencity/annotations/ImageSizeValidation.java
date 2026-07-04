package greencity.annotations;

import greencity.validator.ImageSizeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ImageSizeValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ImageSizeValidation {
    String message() default "Incorrect image size. Maximum allowed size is 10 MB";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
