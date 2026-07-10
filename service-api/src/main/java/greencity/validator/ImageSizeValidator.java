package greencity.validator;

import greencity.annotations.ImageSizeValidation;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ImageSizeValidator implements ConstraintValidator<ImageSizeValidation, MultipartFile> {
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10 MB

    @Override
    public boolean isValid(MultipartFile image, ConstraintValidatorContext context) {
        if (image == null || image.isEmpty()) {
            return true;
        }
        return image.getSize() <= MAX_IMAGE_SIZE;
    }
}