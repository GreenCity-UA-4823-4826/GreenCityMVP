package greencity.mapping;

import greencity.dto.habit.HabitAssignVO;
import greencity.dto.habitstatuscalendar.HabitStatusCalendarVO;
import greencity.entity.HabitStatusCalendar;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
class HabitStatusCalendarMapperTest {

    @InjectMocks
    private HabitStatusCalendarMapper mapper;

    @Test
    void convertTest() {
        HabitStatusCalendarVO vo = HabitStatusCalendarVO.builder()
            .id(1L)
            .enrollDate(LocalDate.now())
            .habitAssignVO(HabitAssignVO.builder().id(5L).build())
            .build();

        HabitStatusCalendar result = mapper.convert(vo);

        assertEquals(1L, result.getId());
        assertEquals(LocalDate.now(), result.getEnrollDate());
        assertEquals(5L, result.getHabitAssign().getId());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSourceTest() {
        assertThrows(NullPointerException.class, () -> mapper.convert((HabitStatusCalendarVO) null));
    }
}
