package greencity.mapping;

import greencity.dto.habit.HabitAssignUserDurationDto;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.User;
import greencity.enums.HabitAssignStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
class HabitAssignUserDurationDtoMapperTest {

    @InjectMocks
    private HabitAssignUserDurationDtoMapper mapper;

    @Test
    void convertTest() {
        HabitAssign habitAssign = HabitAssign.builder()
            .id(1L)
            .status(HabitAssignStatus.INPROGRESS)
            .workingDays(5)
            .duration(30)
            .user(User.builder().id(42L).build())
            .habit(Habit.builder().id(10L).build())
            .build();

        HabitAssignUserDurationDto result = mapper.convert(habitAssign);

        assertEquals(1L, result.getHabitAssignId());
        assertEquals(42L, result.getUserId());
        assertEquals(10L, result.getHabitId());
        assertEquals(HabitAssignStatus.INPROGRESS, result.getStatus());
        assertEquals(5, result.getWorkingDays());
        assertEquals(30, result.getDuration());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSourceTest() {
        assertThrows(NullPointerException.class, () -> mapper.convert((HabitAssign) null));
    }
}
