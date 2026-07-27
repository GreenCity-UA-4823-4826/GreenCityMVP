package greencity.mapping;

import greencity.dto.habit.HabitAssignVO;
import greencity.dto.habitstatuscalendar.HabitStatusCalendarVO;
import greencity.entity.HabitStatusCalendar;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class HabitStatusCalendarMapperTest {

    @InjectMocks
    private HabitStatusCalendarMapper mapper;

    @Test
    void convertTest() {
        LocalDate enrollDate = LocalDate.now();
        HabitStatusCalendarVO vo = HabitStatusCalendarVO.builder()
            .id(1L)
            .enrollDate(enrollDate)
            .habitAssignVO(HabitAssignVO.builder().id(5L).build())
            .build();

        HabitStatusCalendar result = mapper.convert(vo);

        assertEquals(1L, result.getId());
        assertEquals(enrollDate, result.getEnrollDate());
        assertEquals(5L, result.getHabitAssign().getId());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSourceTest() {
        assertThrows(NullPointerException.class, () -> mapper.convert((HabitStatusCalendarVO) null));
    }
}
