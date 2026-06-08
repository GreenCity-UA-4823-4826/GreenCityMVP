package greencity.mapping;

import greencity.dto.language.LanguageTranslationDTO;
import greencity.entity.HabitFactTranslation;
import greencity.entity.Language;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
class LanguageTranslationDtoMapperTest {

    @InjectMocks
    private LanguageTranslationDtoMapper mapper;

    @Test
    void convertTest() {
        Language language = Language.builder().id(1L).code("en").build();

        HabitFactTranslation factTranslation = HabitFactTranslation.builder()
            .id(1L)
            .content("Fact content")
            .language(language)
            .build();

        LanguageTranslationDTO result = mapper.convert(factTranslation);

        assertEquals("Fact content", result.getContent());
        assertEquals(1L, result.getLanguage().getId());
        assertEquals("en", result.getLanguage().getCode());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSourceTest() {
        assertThrows(NullPointerException.class, () -> mapper.convert((HabitFactTranslation) null));
    }
}
