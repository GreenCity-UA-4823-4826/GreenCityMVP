package greencity.mapping;

import greencity.dto.habit.AddCustomHabitDtoRequest;
import greencity.entity.Habit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomHabitMapperTest {
    private final CustomHabitMapper mapper = new CustomHabitMapper();

    @Test
    void convert() {
        AddCustomHabitDtoRequest request = AddCustomHabitDtoRequest.builder()
            .image("img.jpg")
            .complexity(2)
            .defaultDuration(30)
            .build();
        Habit result = mapper.convert(request);
        assertEquals("img.jpg", result.getImage());
        assertEquals(2, result.getComplexity());
        assertEquals(30, result.getDefaultDuration());
        assertTrue(result.getIsCustomHabit());
    }

    @Test
    void convertWithNullFields() {
        AddCustomHabitDtoRequest request = AddCustomHabitDtoRequest.builder()
            .image(null).complexity(null).defaultDuration(null).build();
        Habit result = mapper.convert(request);
        assertNull(result.getImage());
        assertNull(result.getComplexity());
        assertNull(result.getDefaultDuration());
        assertTrue(result.getIsCustomHabit());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSource() {
        assertThrows(NullPointerException.class, () -> mapper.convert((AddCustomHabitDtoRequest) null));
    }
}
