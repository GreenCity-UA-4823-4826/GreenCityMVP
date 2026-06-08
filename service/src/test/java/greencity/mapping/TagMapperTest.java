package greencity.mapping;

import greencity.dto.language.LanguageVO;
import greencity.dto.tag.TagTranslationVO;
import greencity.dto.tag.TagVO;
import greencity.entity.Tag;
import greencity.enums.TagType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
class TagMapperTest {

    @InjectMocks
    private TagMapper mapper;

    @Test
    void convertTest() {
        TagTranslationVO translationVO = TagTranslationVO.builder()
            .id(1L)
            .name("News")
            .languageVO(LanguageVO.builder().id(1L).code("en").build())
            .build();

        TagVO tagVO = TagVO.builder()
            .id(1L)
            .type(TagType.ECO_NEWS)
            .tagTranslations(Collections.singletonList(translationVO))
            .build();

        Tag result = mapper.convert(tagVO);

        assertEquals(1L, result.getId());
        assertEquals(TagType.ECO_NEWS, result.getType());
        assertEquals(1, result.getTagTranslations().size());
        assertEquals("News", result.getTagTranslations().getFirst().getName());
        assertEquals(1L, result.getTagTranslations().getFirst().getId());
        assertEquals("en", result.getTagTranslations().getFirst().getLanguage().getCode());
        assertEquals(1L, result.getTagTranslations().getFirst().getLanguage().getId());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSourceTest() {
        assertThrows(NullPointerException.class, () -> mapper.convert((TagVO) null));
    }
}
