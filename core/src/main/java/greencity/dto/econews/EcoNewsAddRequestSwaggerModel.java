package greencity.dto.econews;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/**
 * Swagger-only model for POST /econews. Represents the documented shape of the
 * request body without exposing internal fields (image, shortInfo) that belong
 * to the real DTO but are not part of the public API contract. This class is
 * NOT used for actual request binding/validation.
 */
@Data
@Schema(name = "EcoNewsAddRequestSwaggerModel")
public class EcoNewsAddRequestSwaggerModel {
    private List<String> tags;
    private String text;
    private String title;
    private String source;
}
