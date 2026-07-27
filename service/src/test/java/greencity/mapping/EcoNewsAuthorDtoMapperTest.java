package greencity.mapping;

import greencity.dto.user.EcoNewsAuthorDto;
import greencity.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EcoNewsAuthorDtoMapperTest {
    private final EcoNewsAuthorDtoMapper mapper = new EcoNewsAuthorDtoMapper();

    @Test
    void convert() {
        User user = User.builder().id(1L).name("Author").build();
        EcoNewsAuthorDto expected = new EcoNewsAuthorDto(1L, "Author");
        assertEquals(expected, mapper.convert(user));
    }

    @Test
    void convertWithNullFields() {
        User user = User.builder().id(null).name(null).build();
        EcoNewsAuthorDto expected = new EcoNewsAuthorDto(null, null);
        assertEquals(expected, mapper.convert(user));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSource() {
        assertThrows(NullPointerException.class, () -> mapper.convert((User) null));
    }
}
