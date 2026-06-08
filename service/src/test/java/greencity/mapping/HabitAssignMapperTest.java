package greencity.mapping;

import greencity.dto.habit.HabitAssignDto;
import greencity.dto.habit.HabitDto;
import greencity.dto.user.UserShoppingListItemAdvanceDto;
import greencity.entity.HabitAssign;
import greencity.enums.HabitAssignStatus;
import greencity.enums.ShoppingListItemStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class HabitAssignMapperTest {

    @InjectMocks
    private HabitAssignMapper mapper;

    @Test
    void convertTest() {
        ZonedDateTime now = ZonedDateTime.now();

        UserShoppingListItemAdvanceDto item1 = UserShoppingListItemAdvanceDto.builder()
            .id(1L)
            .shoppingListItemId(100L)
            .status(ShoppingListItemStatus.INPROGRESS)
            .dateCompleted(null)
            .build();

        UserShoppingListItemAdvanceDto item2 = UserShoppingListItemAdvanceDto.builder()
            .id(2L)
            .shoppingListItemId(200L)
            .status(ShoppingListItemStatus.DONE)
            .dateCompleted(java.time.LocalDateTime.now())
            .build();

        HabitAssignDto dto = HabitAssignDto.builder()
            .id(1L)
            .duration(30)
            .habitStreak(5)
            .createDateTime(now)
            .status(HabitAssignStatus.INPROGRESS)
            .workingDays(3)
            .lastEnrollmentDate(now)
            .habit(HabitDto.builder()
                .id(10L)
                .complexity(2)
                .build())
            .userShoppingListItems(Arrays.asList(item1, item2))
            .build();

        HabitAssign result = mapper.convert(dto);

        assertEquals(1L, result.getId());
        assertEquals(30, result.getDuration());
        assertEquals(5, result.getHabitStreak());
        assertEquals(now, result.getCreateDate());
        assertEquals(HabitAssignStatus.INPROGRESS, result.getStatus());
        assertEquals(3, result.getWorkingDays());
        assertEquals(now, result.getLastEnrollmentDate());
        assertEquals(10L, result.getHabit().getId());
        assertEquals(2, result.getHabit().getComplexity());
        assertEquals(30, result.getHabit().getDefaultDuration());
        assertEquals(1, result.getUserShoppingListItems().size());
        assertEquals(1L, result.getUserShoppingListItems().getFirst().getId());
        assertEquals(100L, result.getUserShoppingListItems().getFirst().getShoppingListItem().getId());
        assertEquals(ShoppingListItemStatus.INPROGRESS, result.getUserShoppingListItems().getFirst().getStatus());
    }

    @Test
    void convertWithNoInProgressItemsTest() {
        UserShoppingListItemAdvanceDto item = UserShoppingListItemAdvanceDto.builder()
            .id(1L)
            .shoppingListItemId(100L)
            .status(ShoppingListItemStatus.DONE)
            .build();

        HabitAssignDto dto = HabitAssignDto.builder()
            .id(1L)
            .duration(30)
            .habitStreak(5)
            .createDateTime(ZonedDateTime.now())
            .status(HabitAssignStatus.INPROGRESS)
            .workingDays(3)
            .habit(HabitDto.builder().id(10L).complexity(2).build())
            .userShoppingListItems(Collections.singletonList(item))
            .build();

        HabitAssign result = mapper.convert(dto);

        assertTrue(result.getUserShoppingListItems().isEmpty());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSourceTest() {
        assertThrows(NullPointerException.class, () -> mapper.convert((HabitAssignDto) null));
    }
}
