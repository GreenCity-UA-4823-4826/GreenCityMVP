package greencity.mapping;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MultipartBase64ImageMapperTest {
    private final MultipartBase64ImageMapper mapper = new MultipartBase64ImageMapper();

    @Test
    void convertWithInvalidBase64Test() {
        String invalidImage = "data:image/png;base64,invalidBase64Data!";
        assertThrows(IllegalStateException.class, () -> mapper.convert(invalidImage));
    }
}
