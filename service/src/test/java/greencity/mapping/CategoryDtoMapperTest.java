package greencity.mapping;

import greencity.dto.category.CategoryDto;
import greencity.entity.Category;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CategoryDtoMapperTest {
    private final CategoryDtoMapper mapper = new CategoryDtoMapper();

    @Test
    void convert() {
        CategoryDto dto = CategoryDto.builder().name("Test Category").build();
        Category result = mapper.convert(dto);
        assertEquals("Test Category", result.getName());
    }

    @Test
    void convertWithNullName() {
        CategoryDto dto = CategoryDto.builder().name(null).build();
        Category result = mapper.convert(dto);
        assertNull(result.getName());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSource() {
        assertThrows(NullPointerException.class, () -> mapper.convert((CategoryDto) null));
    }
}
