package greencity.mapping;

import greencity.dto.tag.TagDto;
import greencity.entity.Tag;
import greencity.entity.localization.TagTranslation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TagDtoMapperTest {

    @InjectMocks
    private TagDtoMapper mapper;

    @Test
    void convertTest() {
        Tag tag = Tag.builder().id(1L).build();

        TagTranslation translation = TagTranslation.builder()
            .id(2L)
            .name("News")
            .tag(tag)
            .build();

        TagDto result = mapper.convert(translation);

        assertEquals(1L, result.getId());
        assertEquals("News", result.getName());
    }

    @Test
    void convertWithNullFieldsTest() {
        Tag tag = Tag.builder().id(null).build();

        TagTranslation translation = TagTranslation.builder()
            .id(1L)
            .name(null)
            .tag(tag)
            .build();

        TagDto result = mapper.convert(translation);

        assertNull(result.getId());
        assertNull(result.getName());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSourceTest() {
        assertThrows(NullPointerException.class, () -> mapper.convert((TagTranslation) null));
    }
}
