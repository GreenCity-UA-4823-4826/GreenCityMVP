package greencity.validator;

import greencity.dto.econews.AddEcoNewsDtoRequest;
import greencity.exception.exceptions.InvalidURLException;
import greencity.exception.exceptions.WrongCountOfTagsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static greencity.ModelUtils.getAddEcoNewsDtoRequest;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EcoNewsDtoRequestValidatorTest {
    private EcoNewsDtoRequestValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EcoNewsDtoRequestValidator();
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

    @Test
    void isValidWithManyTagsThrowsException() {
        AddEcoNewsDtoRequest request = getAddEcoNewsDtoRequest();
        request.setTags(List.of("One", "Two", "Three", "Four"));

        assertThrows(WrongCountOfTagsException.class, () -> validator.isValid(request, null));
    }

    @Test
    void isValidWithEmptyTagsThrowsException() {
        AddEcoNewsDtoRequest request = getAddEcoNewsDtoRequest();
        request.setTags(List.of());

        assertThrows(WrongCountOfTagsException.class, () -> validator.isValid(request, null));
    }

    @Test
    void isValidWithInvalidSourceThrowsException() {
        AddEcoNewsDtoRequest request = getAddEcoNewsDtoRequest();
        request.setSource("invalid-url");

        assertThrows(InvalidURLException.class, () -> validator.isValid(request, null));
    }

    @Test
    void isValidWithMaximumAmountOfTagsReturnsTrue() {
        AddEcoNewsDtoRequest request = getAddEcoNewsDtoRequest();
        request.setTags(List.of("One", "Two", "Three"));

        assertTrue(validator.isValid(request, null));
    }

    @Test
    void initializeDoesNotThrowException() {
        assertDoesNotThrow(() -> validator.initialize(null));
    }
}
