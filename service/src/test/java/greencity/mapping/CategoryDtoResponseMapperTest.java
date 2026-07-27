package greencity.mapping;

import greencity.dto.category.CategoryDtoResponse;
import greencity.entity.Category;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CategoryDtoResponseMapperTest {
    private final CategoryDtoResponseMapper mapper = new CategoryDtoResponseMapper();

    @Test
    void convert() {
        Category category = Category.builder().id(1L).name("Test").build();
        CategoryDtoResponse result = mapper.convert(category);
        assertEquals(1L, result.getId());
        assertEquals("Test", result.getName());
    }

    @Test
    void convertWithNullId() {
        Category category = Category.builder().id(null).name("Test").build();
        CategoryDtoResponse result = mapper.convert(category);
        assertNull(result.getId());
        assertEquals("Test", result.getName());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSource() {
        assertThrows(NullPointerException.class, () -> mapper.convert((Category) null));
    }
}
