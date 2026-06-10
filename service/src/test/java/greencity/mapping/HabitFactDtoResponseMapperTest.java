package greencity.mapping;

import greencity.dto.habit.HabitVO;
import greencity.dto.habitfact.HabitFactDtoResponse;
import greencity.dto.habitfact.HabitFactTranslationVO;
import greencity.dto.habitfact.HabitFactVO;
import greencity.dto.language.LanguageVO;
import greencity.enums.FactOfDayStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class HabitFactDtoResponseMapperTest {

    @InjectMocks
    private HabitFactDtoResponseMapper mapper;

    @Test
    void convertTest() {
        HabitVO habitVO = HabitVO.builder().id(1L).image("img.jpg").complexity(2).build();

        LanguageVO languageVO = LanguageVO.builder().id(1L).code("en").build();

        HabitFactTranslationVO translationVO = HabitFactTranslationVO.builder()
            .id(1L)
            .content("Fact content")
            .factOfDayStatus(FactOfDayStatus.CURRENT)
            .language(languageVO)
            .build();

        HabitFactVO factVO = HabitFactVO.builder()
            .id(1L)
            .habit(habitVO)
            .translations(Collections.singletonList(translationVO))
            .build();

        HabitFactDtoResponse result = mapper.convert(factVO);

        assertEquals(1L, result.getId());
        assertNotNull(result.getHabit());
        assertEquals(1L, result.getHabit().getId());
        assertEquals(1, result.getTranslations().size());
        assertEquals(1L, result.getTranslations().getFirst().getId());
        assertEquals("Fact content", result.getTranslations().getFirst().getContent());
        assertEquals(FactOfDayStatus.CURRENT, result.getTranslations().getFirst().getFactOfDayStatus());
        assertEquals(1L, result.getTranslations().getFirst().getLanguage().getId());
        assertEquals("en", result.getTranslations().getFirst().getLanguage().getCode());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSourceTest() {
        assertThrows(NullPointerException.class, () -> mapper.convert((HabitFactVO) null));
    }
}
