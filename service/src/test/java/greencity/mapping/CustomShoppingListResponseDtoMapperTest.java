package greencity.mapping;

import greencity.dto.shoppinglistitem.CustomShoppingListItemResponseDto;
import greencity.entity.CustomShoppingListItem;
import greencity.enums.ShoppingListItemStatus;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomShoppingListResponseDtoMapperTest {
    private final CustomShoppingListResponseDtoMapper mapper = new CustomShoppingListResponseDtoMapper();

    @Test
    void convert() {
        CustomShoppingListItem item = CustomShoppingListItem.builder()
            .id(1L).text("Item").status(ShoppingListItemStatus.INPROGRESS).build();
        CustomShoppingListItemResponseDto result = mapper.convert(item);
        assertEquals(1L, result.getId());
        assertEquals("Item", result.getText());
        assertEquals(ShoppingListItemStatus.INPROGRESS, result.getStatus());
    }

    @Test
    void convertWithNullFields() {
        CustomShoppingListItem item = CustomShoppingListItem.builder()
            .id(null).text(null).status(null).build();
        CustomShoppingListItemResponseDto result = mapper.convert(item);
        assertNull(result.getId());
        assertNull(result.getText());
        assertNull(result.getStatus());
    }

    @Test
    void mapAllToList() {
        List<CustomShoppingListItem> items = Arrays.asList(
            CustomShoppingListItem.builder().id(1L).text("A").status(ShoppingListItemStatus.ACTIVE).build(),
            CustomShoppingListItem.builder().id(2L).text("B").status(ShoppingListItemStatus.DONE).build());
        List<CustomShoppingListItemResponseDto> result = mapper.mapAllToList(items);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("A", result.get(0).getText());
        assertEquals(2L, result.get(1).getId());
        assertEquals("B", result.get(1).getText());
    }

    @Test
    void mapAllToListEmpty() {
        assertTrue(mapper.mapAllToList(Collections.emptyList()).isEmpty());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSource() {
        assertThrows(NullPointerException.class, () -> mapper.convert((CustomShoppingListItem) null));
    }
}
