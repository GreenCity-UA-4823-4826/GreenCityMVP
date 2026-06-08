package greencity.mapping;

import greencity.dto.habitstatuscalendar.HabitStatusCalendarVO;
import greencity.entity.HabitAssign;
import greencity.entity.HabitStatusCalendar;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
class HabitStatusCalendarVOMapperTest {

    @InjectMocks
    private HabitStatusCalendarVOMapper mapper;

    @Test
    void convertTest() {
        HabitStatusCalendar calendar = HabitStatusCalendar.builder()
            .id(1L)
            .enrollDate(LocalDate.now())
            .habitAssign(HabitAssign.builder().id(5L).build())
            .build();

        HabitStatusCalendarVO result = mapper.convert(calendar);

        assertEquals(1L, result.getId());
        assertEquals(LocalDate.now(), result.getEnrollDate());
        assertEquals(5L, result.getHabitAssignVO().getId());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSourceTest() {
        assertThrows(NullPointerException.class, () -> mapper.convert((HabitStatusCalendar) null));
    }
}
