package greencity.mapping;

import greencity.dto.habittranslation.HabitTranslationDto;
import greencity.entity.HabitTranslation;
import greencity.entity.Language;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class HabitTranslationDtoMapperTest {

    @InjectMocks
    private HabitTranslationDtoMapper mapper;

    @Test
    void convertTest() {
        Language language = Language.builder().id(1L).code("en").build();

        HabitTranslation translation = HabitTranslation.builder()
            .id(1L)
            .name("Test Habit")
            .description("Test description")
            .habitItem("Test item")
            .language(language)
            .build();

        HabitTranslationDto result = mapper.convert(translation);

        assertEquals("Test description", result.getDescription());
        assertEquals("Test item", result.getHabitItem());
        assertEquals("Test Habit", result.getName());
        assertEquals("en", result.getLanguageCode());
    }

    @Test
    void convertWithNullFieldsTest() {
        HabitTranslation translation = HabitTranslation.builder()
            .id(1L)
            .name(null)
            .description(null)
            .habitItem(null)
            .language(null)
            .build();

        assertThrows(NullPointerException.class, () -> mapper.convert(translation));
    }

    @Test
    void mapAllToListTest() {
        Language en = Language.builder().id(1L).code("en").build();
        Language ua = Language.builder().id(2L).code("ua").build();

        HabitTranslation t1 = HabitTranslation.builder()
            .id(1L).name("Habit1").description("Desc1").habitItem("Item1").language(en).build();
        HabitTranslation t2 = HabitTranslation.builder()
            .id(2L).name("Habit2").description("Desc2").habitItem("Item2").language(ua).build();

        List<HabitTranslationDto> result = mapper.mapAllToList(Arrays.asList(t1, t2));

        assertEquals(2, result.size());
        assertEquals("Habit1", result.get(0).getName());
        assertEquals("en", result.get(0).getLanguageCode());
        assertEquals("Habit2", result.get(1).getName());
        assertEquals("ua", result.get(1).getLanguageCode());
    }

    @Test
    void mapAllToListWithEmptyListTest() {
        List<HabitTranslationDto> result = mapper.mapAllToList(Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSourceTest() {
        assertThrows(NullPointerException.class, () -> mapper.convert((HabitTranslation) null));
    }
}
