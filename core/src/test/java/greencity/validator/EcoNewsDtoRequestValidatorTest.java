package greencity.validator;

import greencity.dto.econews.AddEcoNewsDtoRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static greencity.ModelUtils.getAddEcoNewsDtoRequest;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class EcoNewsDtoRequestValidatorTest {
    @InjectMocks
    private EcoNewsDtoRequestValidator validator;

    @BeforeEach
    void setup() {
        validator= new EcoNewsDtoRequestValidator();
    }

    @Test
    void isValidTrueTest() {
        AddEcoNewsDtoRequest request = getAddEcoNewsDtoRequest();
        request.setSource("https://eco-lavca.ua/");
        assertTrue(validator.isValid(request, null));
    }

    @Test
    void isValidTrueWhenSourceIsNullTest() {
        AddEcoNewsDtoRequest request = getAddEcoNewsDtoRequest();
        request.setSource(null);
        request.setTags(List.of("News"));
        assertTrue(validator.isValid(request, null));
    }

    @Test
    void isValidTrueWhenSourceIsEmptyTest() {
        AddEcoNewsDtoRequest request = getAddEcoNewsDtoRequest();
        request.setSource("");
        request.setTags(List.of("News"));
        assertTrue(validator.isValid(request, null));
    }


}
