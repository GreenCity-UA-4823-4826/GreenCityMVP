package greencity.mapping;

import greencity.dto.tag.NewTagDto;
import greencity.entity.Tag;
import greencity.entity.localization.TagTranslation;
import greencity.entity.Language;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class NewTagDtoMapperTest {

    @InjectMocks
    private NewTagDtoMapper mapper;

    @Test
    void convertTest() {
        TagTranslation enTranslation = TagTranslation.builder()
            .id(1L).name("News").language(Language.builder().id(1L).code("en").build()).build();
        TagTranslation uaTranslation = TagTranslation.builder()
            .id(2L).name("Новини").language(Language.builder().id(2L).code("ua").build()).build();

        Tag tag = Tag.builder()
            .id(1L)
            .tagTranslations(Arrays.asList(enTranslation, uaTranslation))
            .build();

        NewTagDto result = mapper.convert(tag);

        assertEquals(1L, result.getId());
        assertEquals("News", result.getName());
        assertEquals("Новини", result.getNameUa());
    }

    @Test
    void convertWithMissingEnTranslationTest() {
        TagTranslation uaTranslation = TagTranslation.builder()
            .id(1L).name("Новини").language(Language.builder().id(2L).code("ua").build()).build();

        Tag tag = Tag.builder()
            .id(1L)
            .tagTranslations(Collections.singletonList(uaTranslation))
            .build();

        NewTagDto result = mapper.convert(tag);

        assertEquals(1L, result.getId());
        assertNull(result.getName());
        assertEquals("Новини", result.getNameUa());
    }

    @Test
    void convertWithMissingUaTranslationTest() {
        TagTranslation enTranslation = TagTranslation.builder()
            .id(1L).name("News").language(Language.builder().id(1L).code("en").build()).build();

        Tag tag = Tag.builder()
            .id(1L)
            .tagTranslations(Collections.singletonList(enTranslation))
            .build();

        NewTagDto result = mapper.convert(tag);

        assertEquals(1L, result.getId());
        assertEquals("News", result.getName());
        assertNull(result.getNameUa());
    }

    @Test
    void convertWithNoTranslationsTest() {
        Tag tag = Tag.builder()
            .id(1L)
            .tagTranslations(Collections.emptyList())
            .build();

        NewTagDto result = mapper.convert(tag);

        assertEquals(1L, result.getId());
        assertNull(result.getName());
        assertNull(result.getNameUa());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSourceTest() {
        assertThrows(NullPointerException.class, () -> mapper.convert((Tag) null));
    }
}
