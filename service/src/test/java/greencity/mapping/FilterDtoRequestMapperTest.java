package greencity.mapping;

import greencity.dto.user.UserFilterDtoRequest;
import greencity.entity.Filter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class FilterDtoRequestMapperTest {

    @InjectMocks
    private FilterDtoRequestMapper mapper;

    @Test
    void convertTest() {
        UserFilterDtoRequest request = UserFilterDtoRequest.builder()
            .name("Test Filter")
            .searchCriteria("search")
            .userRole("USER")
            .userStatus("ACTIVATED")
            .build();

        Filter result = mapper.convert(request);

        assertEquals("Test Filter", result.getName());
        assertEquals("USERS", result.getType());
        assertEquals("search;USER;ACTIVATED", result.getValues());
    }

    @Test
    void convertWithDifferentValuesTest() {
        UserFilterDtoRequest request = UserFilterDtoRequest.builder()
            .name("Filter")
            .searchCriteria("test-value")
            .userRole("ADMIN")
            .userStatus("DEACTIVATED")
            .build();

        Filter result = mapper.convert(request);

        assertEquals("test-value;ADMIN;DEACTIVATED", result.getValues());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSourceTest() {
        assertThrows(NullPointerException.class, () -> mapper.convert((UserFilterDtoRequest) null));
    }
}
