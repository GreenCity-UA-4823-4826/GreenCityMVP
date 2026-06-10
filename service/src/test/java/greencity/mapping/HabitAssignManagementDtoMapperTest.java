package greencity.mapping;

import greencity.dto.habit.HabitAssignManagementDto;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.User;
import greencity.enums.HabitAssignStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class HabitAssignManagementDtoMapperTest {

    @InjectMocks
    private HabitAssignManagementDtoMapper mapper;

    @Test
    void convertTest() {
        ZonedDateTime now = ZonedDateTime.now();

        HabitAssign habitAssign = HabitAssign.builder()
            .id(1L)
            .status(HabitAssignStatus.INPROGRESS)
            .createDate(now)
            .duration(30)
            .habitStreak(5)
            .workingDays(3)
            .lastEnrollmentDate(now)
            .user(User.builder().id(42L).build())
            .habit(Habit.builder().id(10L).build())
            .build();

        HabitAssignManagementDto result = mapper.convert(habitAssign);

        assertEquals(1L, result.getId());
        assertEquals(HabitAssignStatus.INPROGRESS, result.getStatus());
        assertEquals(now, result.getCreateDateTime());
        assertEquals(42L, result.getUserId());
        assertEquals(10L, result.getHabitId());
        assertEquals(30, result.getDuration());
        assertEquals(5, result.getHabitStreak());
        assertEquals(3, result.getWorkingDays());
        assertEquals(now, result.getLastEnrollment());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSourceTest() {
        assertThrows(NullPointerException.class, () -> mapper.convert((HabitAssign) null));
    }
}
