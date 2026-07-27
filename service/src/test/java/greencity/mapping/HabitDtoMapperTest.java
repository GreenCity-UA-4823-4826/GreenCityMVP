package greencity.mapping;

import greencity.dto.habit.HabitDto;
import greencity.entity.*;
import greencity.entity.localization.ShoppingListItemTranslation;
import greencity.entity.localization.TagTranslation;
import greencity.enums.ShoppingListItemStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class HabitDtoMapperTest {

    @InjectMocks
    private HabitDtoMapper mapper;

    @Test
    void convertTest() {
        Language language = Language.builder().id(1L).code("en").build();

        TagTranslation tagTranslation = TagTranslation.builder()
            .id(1L).name("Reusable").language(language).build();

        Tag tag = Tag.builder()
            .id(1L)
            .tagTranslations(Collections.singletonList(tagTranslation))
            .build();

        ShoppingListItemTranslation itemTranslation = ShoppingListItemTranslation.builder()
            .id(1L).content("Buy bamboo").language(language).build();

        ShoppingListItem shoppingListItem = ShoppingListItem.builder()
            .id(1L)
            .translations(Collections.singletonList(itemTranslation))
            .build();

        Habit habit = Habit.builder()
            .id(1L)
            .image("image.jpg")
            .defaultDuration(30)
            .complexity(2)
            .tags(Collections.singleton(tag))
            .shoppingListItems(Collections.singleton(shoppingListItem))
            .build();

        HabitTranslation translation = HabitTranslation.builder()
            .id(1L)
            .name("Test Habit")
            .description("Test description")
            .habitItem("Test item")
            .language(language)
            .habit(habit)
            .build();

        HabitDto result = mapper.convert(translation);

        assertEquals(habit.getId(), result.getId());
        assertEquals(habit.getImage(), result.getImage());
        assertEquals(habit.getDefaultDuration(), result.getDefaultDuration());
        assertEquals(habit.getComplexity(), result.getComplexity());
        assertNotNull(result.getHabitTranslation());
        assertEquals("Test Habit", result.getHabitTranslation().getName());
        assertEquals("Test description", result.getHabitTranslation().getDescription());
        assertEquals("Test item", result.getHabitTranslation().getHabitItem());
        assertEquals("en", result.getHabitTranslation().getLanguageCode());
        assertEquals(Collections.singletonList("Reusable"), result.getTags());
        assertEquals(1, result.getShoppingListItems().size());
        assertEquals("Buy bamboo", result.getShoppingListItems().getFirst().getText());
        assertEquals(ShoppingListItemStatus.ACTIVE.toString(), result.getShoppingListItems().getFirst().getStatus());
    }

    @Test
    void convertWithNullShoppingListTest() {
        Language language = Language.builder().id(1L).code("en").build();

        TagTranslation tagTranslation = TagTranslation.builder()
            .id(1L).name("Tag1").language(language).build();

        Tag tag = Tag.builder()
            .id(1L)
            .tagTranslations(Collections.singletonList(tagTranslation))
            .build();

        Habit habit = Habit.builder()
            .id(1L).image("img.jpg").defaultDuration(30).complexity(2)
            .tags(Collections.singleton(tag))
            .shoppingListItems(null)
            .build();

        HabitTranslation translation = HabitTranslation.builder()
            .id(1L).name("Habit").description("Desc").habitItem("Item")
            .language(language).habit(habit)
            .build();

        HabitDto result = mapper.convert(translation);

        assertTrue(result.getShoppingListItems().isEmpty());
    }

    @Test
    void convertWithFilteredTagsAndTranslationsTest() {
        Language enLang = Language.builder().id(1L).code("en").build();
        Language uaLang = Language.builder().id(2L).code("ua").build();

        TagTranslation tagEn = TagTranslation.builder().id(1L).name("News").language(enLang).build();
        TagTranslation tagUa = TagTranslation.builder().id(2L).name("Новини").language(uaLang).build();

        Tag tag = Tag.builder()
            .id(1L)
            .tagTranslations(Arrays.asList(tagEn, tagUa))
            .build();

        ShoppingListItemTranslation itemEn = ShoppingListItemTranslation.builder()
            .id(1L).content("Recycle").language(enLang).build();
        ShoppingListItemTranslation itemUa = ShoppingListItemTranslation.builder()
            .id(2L).content("Переробляти").language(uaLang).build();

        ShoppingListItem item = ShoppingListItem.builder()
            .id(1L)
            .translations(Arrays.asList(itemEn, itemUa))
            .build();

        Habit habit = Habit.builder()
            .id(1L).image("img.jpg").defaultDuration(30).complexity(2)
            .tags(Collections.singleton(tag))
            .shoppingListItems(Collections.singleton(item))
            .build();

        HabitTranslation translation = HabitTranslation.builder()
            .id(1L).name("Habit").description("Desc").habitItem("Item")
            .language(enLang).habit(habit)
            .build();

        HabitDto result = mapper.convert(translation);

        assertEquals(Collections.singletonList("News"), result.getTags());
        assertEquals("Recycle", result.getShoppingListItems().getFirst().getText());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSourceTest() {
        assertThrows(NullPointerException.class, () -> mapper.convert((HabitTranslation) null));
    }
}
