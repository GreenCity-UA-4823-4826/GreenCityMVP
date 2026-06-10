package greencity.filters;

import greencity.entity.RatingStatistics;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class RatingStatisticsSpecificationTest {

    @Mock
    private Root<RatingStatistics> root;

    @Mock
    private CriteriaQuery<?> criteriaQuery;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Predicate startPredicate;

    @Mock
    private Predicate numericPredicade;

    @Mock
    private Predicate finalPredicade;

    @Mock
    private Path<Object> idPath;

    @Test
    void toPredicateShouldCreateNumericPredicadeteForId(){
        SearchCriteria searchCriteria = SearchCriteria.builder()
                .key("id")
                .type("id")
                .value(10L)
                .build();

        RatingStatisticsSpecification ratingStatisticsSpecification =
                new RatingStatisticsSpecification(List.of(searchCriteria));

        when(criteriaBuilder.conjunction()).thenReturn(startPredicate);
        when(root.get("id")).thenReturn(idPath);
        when(criteriaBuilder.equal(idPath, 10L)).thenReturn(numericPredicade);
        when(criteriaBuilder.and(startPredicate, numericPredicade)).thenReturn(finalPredicade);

        Predicate result= ratingStatisticsSpecification.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertSame(finalPredicade, result);

        verify(criteriaBuilder).conjunction();
        verify(root).get("id");
        verify(criteriaBuilder).equal(idPath, 10L);
        verify(criteriaBuilder).and(startPredicate, numericPredicade);

    }


}
