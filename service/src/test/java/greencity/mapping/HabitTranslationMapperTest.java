package greencity.mapping;

import greencity.dto.habittranslation.HabitTranslationDto;
import greencity.entity.HabitTranslation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class HabitTranslationMapperTest {

    @InjectMocks
    private HabitTranslationMapper mapper;

    @Test
    void convertTest() {
        HabitTranslationDto dto = HabitTranslationDto.builder()
            .name("Test Habit")
            .description("Test description")
            .habitItem("Test item")
            .languageCode("en")
            .build();

        HabitTranslation result = mapper.convert(dto);

        assertEquals("Test description", result.getDescription());
        assertEquals("Test item", result.getHabitItem());
        assertEquals("Test Habit", result.getName());
    }

    @Test
    void convertWithNullFieldsTest() {
        HabitTranslationDto dto = HabitTranslationDto.builder()
            .name(null)
            .description(null)
            .habitItem(null)
            .languageCode(null)
            .build();

        HabitTranslation result = mapper.convert(dto);

        assertNull(result.getName());
        assertNull(result.getDescription());
        assertNull(result.getHabitItem());
    }

    @Test
    void mapAllToListTest() {
        HabitTranslationDto dto1 = HabitTranslationDto.builder()
            .name("Habit1").description("Desc1").habitItem("Item1").languageCode("en").build();
        HabitTranslationDto dto2 = HabitTranslationDto.builder()
            .name("Habit2").description("Desc2").habitItem("Item2").languageCode("ua").build();

        List<HabitTranslation> result = mapper.mapAllToList(Arrays.asList(dto1, dto2));

        assertEquals(2, result.size());
        assertEquals("Habit1", result.get(0).getName());
        assertEquals("Habit2", result.get(1).getName());
    }

    @Test
    void mapAllToListWithEmptyListTest() {
        List<HabitTranslation> result = mapper.mapAllToList(Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSourceTest() {
        assertThrows(NullPointerException.class, () -> mapper.convert((HabitTranslationDto) null));
    }
}
