package greencity.mapping;

import greencity.dto.user.UserFilterDtoResponse;
import greencity.entity.Filter;
import greencity.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilterDtoResponseMapperTest {
    private final FilterDtoResponseMapper mapper = new FilterDtoResponseMapper();

    @Test
    void convertTest() {
        Filter filter = Filter.builder()
            .id(1L)
            .name("Test Filter")
            .type("USERS")
            .values("search;USER;ACTIVATED")
            .user(User.builder().id(1L).build())
            .build();

        UserFilterDtoResponse result = mapper.convert(filter);

        assertEquals(1L, result.getId());
        assertEquals("Test Filter", result.getName());
        assertEquals("search", result.getSearchCriteria());
        assertEquals("USER", result.getUserRole());
        assertEquals("ACTIVATED", result.getUserStatus());
    }

    @Test
    void convertWithMultipleSemicolonsTest() {
        Filter filter = Filter.builder()
            .id(2L)
            .name("Filter 2")
            .type("USERS")
            .values("a;b;c")
            .build();

        UserFilterDtoResponse result = mapper.convert(filter);

        assertEquals("a", result.getSearchCriteria());
        assertEquals("b", result.getUserRole());
        assertEquals("c", result.getUserStatus());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSourceTest() {
        assertThrows(NullPointerException.class, () -> mapper.convert((Filter) null));
    }
}
