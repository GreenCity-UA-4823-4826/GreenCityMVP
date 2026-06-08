package greencity.mapping;

import greencity.dto.shoppinglistitem.ShoppingListItemRequestDto;
import greencity.entity.UserShoppingListItem;
import greencity.enums.ShoppingListItemStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
class ShoppingListItemRequestDtoMapperTest {

    @InjectMocks
    private ShoppingListItemRequestDtoMapper mapper;

    @Test
    void convertTest() {
        ShoppingListItemRequestDto dto = ShoppingListItemRequestDto.builder()
            .id(1L)
            .build();

        UserShoppingListItem result = mapper.convert(dto);

        assertEquals(1L, result.getShoppingListItem().getId());
        assertEquals(ShoppingListItemStatus.ACTIVE, result.getStatus());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSourceTest() {
        assertThrows(NullPointerException.class, () -> mapper.convert((ShoppingListItemRequestDto) null));
    }
}
