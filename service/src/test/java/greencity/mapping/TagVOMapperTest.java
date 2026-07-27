package greencity.mapping;

import greencity.dto.tag.TagVO;
import greencity.entity.Tag;
import greencity.entity.localization.TagTranslation;
import greencity.entity.Language;
import greencity.enums.TagType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TagVOMapperTest {

    @InjectMocks
    private TagVOMapper mapper;

    @Test
    void convertTest() {
        TagTranslation translation = TagTranslation.builder()
            .id(1L)
            .name("News")
            .language(Language.builder().id(1L).code("en").build())
            .build();

        Tag tag = Tag.builder()
            .id(1L)
            .type(TagType.ECO_NEWS)
            .tagTranslations(Collections.singletonList(translation))
            .build();

        TagVO result = mapper.convert(tag);

        assertEquals(1L, result.getId());
        assertEquals(TagType.ECO_NEWS, result.getType());
        assertEquals(1, result.getTagTranslations().size());
        assertEquals("News", result.getTagTranslations().getFirst().getName());
        assertEquals(1L, result.getTagTranslations().getFirst().getId());
        assertEquals("en", result.getTagTranslations().getFirst().getLanguageVO().getCode());
        assertEquals(1L, result.getTagTranslations().getFirst().getLanguageVO().getId());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSourceTest() {
        assertThrows(NullPointerException.class, () -> mapper.convert((Tag) null));
    }
}
