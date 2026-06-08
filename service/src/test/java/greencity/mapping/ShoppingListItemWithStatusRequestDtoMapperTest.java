package greencity.mapping;

import greencity.dto.shoppinglistitem.ShoppingListItemWithStatusRequestDto;
import greencity.entity.UserShoppingListItem;
import greencity.enums.ShoppingListItemStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
class ShoppingListItemWithStatusRequestDtoMapperTest {

    @InjectMocks
    private ShoppingListItemWithStatusRequestDtoMapper mapper;

    @Test
    void convertTest() {
        ShoppingListItemWithStatusRequestDto dto = ShoppingListItemWithStatusRequestDto.builder()
            .id(1L)
            .status(ShoppingListItemStatus.DONE)
            .build();

        UserShoppingListItem result = mapper.convert(dto);

        assertEquals(1L, result.getShoppingListItem().getId());
        assertEquals(ShoppingListItemStatus.DONE, result.getStatus());
    }

    @Test
    void convertWithInProgressStatusTest() {
        ShoppingListItemWithStatusRequestDto dto = ShoppingListItemWithStatusRequestDto.builder()
            .id(2L)
            .status(ShoppingListItemStatus.INPROGRESS)
            .build();

        UserShoppingListItem result = mapper.convert(dto);

        assertEquals(2L, result.getShoppingListItem().getId());
        assertEquals(ShoppingListItemStatus.INPROGRESS, result.getStatus());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSourceTest() {
        assertThrows(NullPointerException.class, () -> mapper.convert((ShoppingListItemWithStatusRequestDto) null));
    }
}
