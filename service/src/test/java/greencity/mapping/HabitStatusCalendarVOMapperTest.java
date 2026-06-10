package greencity.mapping;

import greencity.dto.habitstatuscalendar.HabitStatusCalendarVO;
import greencity.entity.HabitAssign;
import greencity.entity.HabitStatusCalendar;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class HabitStatusCalendarVOMapperTest {

    @InjectMocks
    private HabitStatusCalendarVOMapper mapper;

    @Test
    void convertTest() {
        LocalDate enrollDate = LocalDate.now();
        HabitStatusCalendar calendar = HabitStatusCalendar.builder()
            .id(1L)
            .enrollDate(enrollDate)
            .habitAssign(HabitAssign.builder().id(5L).build())
            .build();

        HabitStatusCalendarVO result = mapper.convert(calendar);

        assertEquals(1L, result.getId());
        assertEquals(enrollDate, result.getEnrollDate());
        assertEquals(5L, result.getHabitAssignVO().getId());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSourceTest() {
        assertThrows(NullPointerException.class, () -> mapper.convert((HabitStatusCalendar) null));
    }
}
