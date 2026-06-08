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

class CustomShoppingListMapperTest {
    private final CustomShoppingListMapper mapper = new CustomShoppingListMapper();

    @Test
    void convert() {
        CustomShoppingListItemResponseDto dto = CustomShoppingListItemResponseDto.builder()
            .id(1L).text("Item").status(ShoppingListItemStatus.INPROGRESS).build();
        CustomShoppingListItem result = mapper.convert(dto);
        assertEquals(1L, result.getId());
        assertEquals("Item", result.getText());
        assertEquals(ShoppingListItemStatus.INPROGRESS, result.getStatus());
    }

    @Test
    void convertWithNullFields() {
        CustomShoppingListItemResponseDto dto = CustomShoppingListItemResponseDto.builder()
            .id(null).text(null).status(null).build();
        CustomShoppingListItem result = mapper.convert(dto);
        assertNull(result.getId());
        assertNull(result.getText());
        assertNull(result.getStatus());
    }

    @Test
    void mapAllToList() {
        List<CustomShoppingListItemResponseDto> dtos = Arrays.asList(
            CustomShoppingListItemResponseDto.builder().id(1L).text("A").status(ShoppingListItemStatus.ACTIVE).build(),
            CustomShoppingListItemResponseDto.builder().id(2L).text("B").status(ShoppingListItemStatus.DONE).build());
        List<CustomShoppingListItem> result = mapper.mapAllToList(dtos);
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
        assertThrows(NullPointerException.class, () -> mapper.convert((CustomShoppingListItemResponseDto) null));
    }
}
