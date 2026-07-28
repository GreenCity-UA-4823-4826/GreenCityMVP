package greencity.validator;

import greencity.annotations.ValidLanguage;
import greencity.service.LanguageService;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LanguageValidatorTest {

    @Mock
    private LanguageService languageService;

    @Mock
    private ValidLanguage validLanguage;

    @Mock
    private ConstraintValidatorContext context;

    @InjectMocks
    private LanguageValidator languageValidator;

    @BeforeEach
    void setUp() {
        when(languageService.findAllLanguageCodes()).thenReturn(List.of("en", "ua"));

        languageValidator.initialize(validLanguage);
    }

    @Test
    void initialize_ValidAnnotation_FetchesLanguageCodes() {

        verify(languageService).findAllLanguageCodes();
    }

    @Test
    void isValid_SupportedLanguage_ReturnsTrue() {

        Locale locale = new Locale("en");

        boolean result = languageValidator.isValid(locale, context);

        assertTrue(result);
    }

    @Test
    void isValid_UnsupportedLanguage_ReturnsFalse() {

        Locale locale = new Locale("fr");

        boolean result = languageValidator.isValid(locale, context);

        assertFalse(result);
    }
}