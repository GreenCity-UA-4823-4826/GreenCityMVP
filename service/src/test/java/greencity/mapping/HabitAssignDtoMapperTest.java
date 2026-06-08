package greencity.mapping;

import greencity.dto.habit.HabitAssignDto;
import greencity.entity.HabitAssign;
import greencity.entity.HabitStatusCalendar;
import greencity.entity.User;
import greencity.enums.HabitAssignStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.ZonedDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class HabitAssignDtoMapperTest {

    @InjectMocks
    private HabitAssignDtoMapper mapper;

    @Test
    void convertTest() {
        ZonedDateTime now = ZonedDateTime.now();

        HabitStatusCalendar calendar = HabitStatusCalendar.builder()
            .id(10L)
            .enrollDate(java.time.LocalDate.now())
            .build();

        HabitAssign habitAssign = HabitAssign.builder()
            .id(1L)
            .status(HabitAssignStatus.INPROGRESS)
            .createDate(now)
            .duration(30)
            .habitStreak(5)
            .workingDays(3)
            .lastEnrollmentDate(now)
            .user(User.builder().id(42L).build())
            .habitStatusCalendars(Collections.singletonList(calendar))
            .build();

        HabitAssignDto result = mapper.convert(habitAssign);

        assertEquals(1L, result.getId());
        assertEquals(HabitAssignStatus.INPROGRESS, result.getStatus());
        assertEquals(now, result.getCreateDateTime());
        assertEquals(42L, result.getUserId());
        assertEquals(30, result.getDuration());
        assertEquals(5, result.getHabitStreak());
        assertEquals(3, result.getWorkingDays());
        assertEquals(now, result.getLastEnrollmentDate());
        assertEquals(1, result.getHabitStatusCalendarDtoList().size());
        assertEquals(10L, result.getHabitStatusCalendarDtoList().getFirst().getId());
    }

    @Test
    void convertWithEmptyCalendarsTest() {
        HabitAssign habitAssign = HabitAssign.builder()
            .id(1L)
            .status(HabitAssignStatus.ACQUIRED)
            .createDate(ZonedDateTime.now())
            .user(User.builder().id(1L).build())
            .habitStatusCalendars(Collections.emptyList())
            .build();

        HabitAssignDto result = mapper.convert(habitAssign);

        assertTrue(result.getHabitStatusCalendarDtoList().isEmpty());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSourceTest() {
        assertThrows(NullPointerException.class, () -> mapper.convert((HabitAssign) null));
    }
}
