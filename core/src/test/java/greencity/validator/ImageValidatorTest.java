package greencity.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageValidatorTest {

    @InjectMocks
    private ImageValidator imageValidator;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @BeforeEach
    void setUp() {
        imageValidator.initialize(null);
    }

    @Test
    void isValid_ImageIsNull_ReturnsTrue() {
        boolean result = imageValidator.isValid(null, constraintValidatorContext);

        assertTrue(result);
    }

    @Test
    void isValid_ContentTypeIsJpeg_ReturnsTrue() {
        when(multipartFile.getContentType()).thenReturn("image/jpeg");

        boolean result = imageValidator.isValid(multipartFile, constraintValidatorContext);

        assertTrue(result);
    }

    @Test
    void isValid_ContentTypeIsJpg_ReturnsTrue() {
        when(multipartFile.getContentType()).thenReturn("image/jpg");

        boolean result = imageValidator.isValid(multipartFile, constraintValidatorContext);

        assertTrue(result);
    }

    @Test
    void isValid_ContentTypeIsPng_ReturnsTrue() {
        when(multipartFile.getContentType()).thenReturn("image/png");

        boolean result = imageValidator.isValid(multipartFile, constraintValidatorContext);

        assertTrue(result);
    }

    @Test
    void isValid_ContentTypeIsGif_ReturnsFalse() {
        when(multipartFile.getContentType()).thenReturn("image/gif");

        boolean result = imageValidator.isValid(multipartFile, constraintValidatorContext);

        assertFalse(result);
    }

    @Test
    void isValid_ContentTypeIsPdf_ReturnsFalse() {
        when(multipartFile.getContentType()).thenReturn("application/pdf");

        boolean result = imageValidator.isValid(multipartFile, constraintValidatorContext);

        assertFalse(result);
    }

    @Test
    void isValid_ContentTypeIsNull_ReturnsFalse() {
        when(multipartFile.getContentType()).thenReturn(null);

        boolean result = imageValidator.isValid(multipartFile, constraintValidatorContext);

        assertFalse(result);
    }
}