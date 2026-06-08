package greencity.mapping;

import greencity.dto.habitstatistic.HabitStatisticDto;
import greencity.entity.HabitAssign;
import greencity.entity.HabitStatistic;
import greencity.enums.HabitRate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
class HabitStatisticDtoMapperTest {

    @InjectMocks
    private HabitStatisticDtoMapper mapper;

    @Test
    void convertTest() {
        ZonedDateTime now = ZonedDateTime.now();

        HabitStatistic statistic = HabitStatistic.builder()
            .id(1L)
            .amountOfItems(10)
            .createDate(now)
            .habitRate(HabitRate.GOOD)
            .habitAssign(HabitAssign.builder().id(5L).build())
            .build();

        HabitStatisticDto result = mapper.convert(statistic);

        assertEquals(1L, result.getId());
        assertEquals(10, result.getAmountOfItems());
        assertEquals(now, result.getCreateDate());
        assertEquals(HabitRate.GOOD, result.getHabitRate());
        assertEquals(5L, result.getHabitAssignId());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSourceTest() {
        assertThrows(NullPointerException.class, () -> mapper.convert((HabitStatistic) null));
    }
}
