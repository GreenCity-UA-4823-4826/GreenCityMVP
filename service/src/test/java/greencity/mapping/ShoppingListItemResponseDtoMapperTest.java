package greencity.mapping;

import greencity.dto.shoppinglistitem.ShoppingListItemResponseDto;
import greencity.entity.ShoppingListItem;
import greencity.entity.localization.ShoppingListItemTranslation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
class ShoppingListItemResponseDtoMapperTest {

    @InjectMocks
    private ShoppingListItemResponseDtoMapper mapper;

    @Test
    void convertTest() {
        ShoppingListItemTranslation translation = ShoppingListItemTranslation.builder()
            .id(2L)
            .content("Buy bamboo toothbrush")
            .build();

        ShoppingListItem item = ShoppingListItem.builder()
            .id(1L)
            .translations(Collections.singletonList(translation))
            .build();

        ShoppingListItemResponseDto result = mapper.convert(item);

        assertEquals(1L, result.getId());
        assertEquals(1, result.getTranslations().size());
        assertEquals(2L, result.getTranslations().getFirst().getId());
        assertEquals("Buy bamboo toothbrush", result.getTranslations().getFirst().getContent());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSourceTest() {
        assertThrows(NullPointerException.class, () -> mapper.convert((ShoppingListItem) null));
    }
}
