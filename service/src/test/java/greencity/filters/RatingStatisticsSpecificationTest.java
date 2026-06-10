package greencity.filters;

import greencity.entity.RatingStatistics;
import greencity.entity.RatingStatistics_;
import greencity.entity.User;
import greencity.entity.User_;
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
    private Predicate numericPredicate;

    @Mock
    private Predicate finalPredicate;

    @Mock
    private Path<Object> idPath;

    @Mock
    private Path<Object> pointsChangedPath;

    @Mock
    private Path<Object> currentRatingPath;

    @Mock
    private Path<Long> userIdPath;

    @Mock
    private Join<RatingStatistics, User> userJoin;

    @Test
    void toPredicateShouldCreateNumericPredicateForId(){
        SearchCriteria searchCriteria = SearchCriteria.builder()
                .key("id")
                .type("id")
                .value(10L)
                .build();

        RatingStatisticsSpecification ratingStatisticsSpecification =
                new RatingStatisticsSpecification(List.of(searchCriteria));

        when(criteriaBuilder.conjunction()).thenReturn(startPredicate);
        when(root.get("id")).thenReturn(idPath);
        when(criteriaBuilder.equal(idPath, 10L)).thenReturn(numericPredicate);
        when(criteriaBuilder.and(startPredicate, numericPredicate)).thenReturn(finalPredicate);

        Predicate result= ratingStatisticsSpecification.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertSame(finalPredicate, result);

        verify(criteriaBuilder).conjunction();
        verify(root).get("id");
        verify(criteriaBuilder).equal(idPath, 10L);
        verify(criteriaBuilder).and(startPredicate, numericPredicate);
    }

    @Test
    void toPredicateShouldCreateNumericPredicateForPointsChanged(){
        SearchCriteria searchCriteria = SearchCriteria.builder()
                .key("pointsChanged")
                .type("pointsChanged")
                .value(10)
                .build();

        RatingStatisticsSpecification ratingStatisticsSpecification =
                new RatingStatisticsSpecification(List.of(searchCriteria));

        when(criteriaBuilder.conjunction()).thenReturn(startPredicate);
        when(root.get("pointsChanged")).thenReturn(pointsChangedPath);
        when(criteriaBuilder.equal(pointsChangedPath, 10)).thenReturn(numericPredicate);
        when(criteriaBuilder.and(startPredicate, numericPredicate)).thenReturn(finalPredicate);

        Predicate result= ratingStatisticsSpecification.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertSame(finalPredicate, result);

        verify(criteriaBuilder).conjunction();
        verify(root).get("pointsChanged");
        verify(criteriaBuilder).equal(pointsChangedPath, 10);
        verify(criteriaBuilder).and(startPredicate, numericPredicate);
    }

    @Test
    void toPredicateShouldCreateNumericPredicateForCurrentRating(){
        SearchCriteria criteria = SearchCriteria.builder()
                .key("rating")
                .type("currentRating")
                .value(22)
                .build();

        RatingStatisticsSpecification ratingStatisticsSpecification =
                new RatingStatisticsSpecification(List.of(criteria));

        when(criteriaBuilder.conjunction()).thenReturn(startPredicate);
        when(root.get("rating")).thenReturn(currentRatingPath);
        when(criteriaBuilder.equal(currentRatingPath, 22)).thenReturn(numericPredicate);
        when(criteriaBuilder.and(startPredicate, numericPredicate)).thenReturn(finalPredicate);

        Predicate result= ratingStatisticsSpecification.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertSame(finalPredicate, result);

        verify(criteriaBuilder).conjunction();
        verify(root).get("rating");
        verify(criteriaBuilder).equal(currentRatingPath,22);
        verify(criteriaBuilder).and(startPredicate, numericPredicate);
    }

    @Test
    void toPredicateShouldCreateNumericPredicateForUserId(){
        SearchCriteria criteria = SearchCriteria.builder()
                .key("userId")
                .type("userId")
                .value(5)
                .build();

        RatingStatisticsSpecification ratingStatisticsSpecification =
                new RatingStatisticsSpecification(List.of(criteria));

        when(criteriaBuilder.conjunction()).thenReturn(startPredicate);
        when(root.join(RatingStatistics_.user)).thenReturn(userJoin);
        when(userJoin.get(User_.id)).thenReturn(userIdPath);
        when(criteriaBuilder.equal(userIdPath, 5)).thenReturn(numericPredicate);
        when(criteriaBuilder.and(startPredicate, numericPredicate)).thenReturn(finalPredicate);

        Predicate result= ratingStatisticsSpecification.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertSame(finalPredicate, result);

        verify(criteriaBuilder).conjunction();
        verify(root).join(RatingStatistics_.user);
        verify(userJoin).get(User_.id);
        verify(criteriaBuilder).equal(userIdPath,5);
        verify(criteriaBuilder).and(startPredicate, numericPredicate);
    }
}
