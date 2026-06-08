package greencity.mapping;

import greencity.dto.habit.HabitManagementDto;
import greencity.entity.Habit;
import greencity.entity.HabitTranslation;
import greencity.entity.Language;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
class HabitManagementDtoMapperTest {

    @InjectMocks
    private HabitManagementDtoMapper mapper;

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

        Habit habit = Habit.builder()
            .id(1L)
            .image("image.jpg")
            .complexity(2)
            .defaultDuration(30)
            .habitTranslations(Collections.singletonList(translation))
            .build();

        HabitManagementDto result = mapper.convert(habit);

        assertEquals(1L, result.getId());
        assertEquals("image.jpg", result.getImage());
        assertEquals(2, result.getComplexity());
        assertEquals(30, result.getDefaultDuration());
        assertEquals(1, result.getHabitTranslations().size());
        assertEquals(1L, result.getHabitTranslations().getFirst().getId());
        assertEquals("Test Habit", result.getHabitTranslations().getFirst().getName());
        assertEquals("Test description", result.getHabitTranslations().getFirst().getDescription());
        assertEquals("Test item", result.getHabitTranslations().getFirst().getHabitItem());
        assertEquals("en", result.getHabitTranslations().getFirst().getLanguageCode());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSourceTest() {
        assertThrows(NullPointerException.class, () -> mapper.convert((Habit) null));
    }
}
