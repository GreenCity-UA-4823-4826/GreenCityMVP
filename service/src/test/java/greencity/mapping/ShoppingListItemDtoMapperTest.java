package greencity.mapping;

import greencity.dto.shoppinglistitem.ShoppingListItemDto;
import greencity.entity.ShoppingListItem;
import greencity.entity.localization.ShoppingListItemTranslation;
import greencity.entity.Language;
import greencity.enums.ShoppingListItemStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ShoppingListItemDtoMapperTest {

    @InjectMocks
    private ShoppingListItemDtoMapper mapper;

    @Test
    void convertTest() {
        ShoppingListItem item = ShoppingListItem.builder().id(1L).build();

        ShoppingListItemTranslation translation = ShoppingListItemTranslation.builder()
            .id(2L)
            .content("Buy bamboo toothbrush")
            .shoppingListItem(item)
            .language(Language.builder().id(1L).code("en").build())
            .build();

        ShoppingListItemDto result = mapper.convert(translation);

        assertEquals(1L, result.getId());
        assertEquals("Buy bamboo toothbrush", result.getText());
        assertEquals(ShoppingListItemStatus.ACTIVE.toString(), result.getStatus());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSourceTest() {
        assertThrows(NullPointerException.class, () -> mapper.convert((ShoppingListItemTranslation) null));
    }
}
