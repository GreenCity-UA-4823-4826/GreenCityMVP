package greencity.filters;

import greencity.annotations.RatingCalculationEnum;
import greencity.entity.RatingStatistics;
import greencity.entity.RatingStatistics_;
import greencity.entity.User;
import greencity.entity.User_;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;


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
    private Predicate likePredicate;

    @Mock
    private Predicate dataRangePredicate;

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
    private Path<ZonedDateTime> dateRangePath;

    @Mock
    private Path<String> userMailPath;

    @Mock
    private Path<RatingCalculationEnum> enumPath;

    @Mock
    private Join<RatingStatistics, User> userJoin;

    @Mock
    private Predicate enumOrPredicate;

    @Mock
    private Predicate enumEqualPredicate;

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

    @Test
    void toPredicateShouldCreateLikePredicateForUserMail(){
        SearchCriteria criteria = SearchCriteria.builder()
                .key("userMail")
                .type("userMail")
                .value("gmail")
                .build();

        RatingStatisticsSpecification ratingStatisticsSpecification =
                new RatingStatisticsSpecification(List.of(criteria));

        when(criteriaBuilder.conjunction()).thenReturn(startPredicate);
        when(root.join(RatingStatistics_.user)).thenReturn(userJoin);
        when(userJoin.get(User_.email)).thenReturn(userMailPath);
        when(criteriaBuilder.like(userMailPath, "%gmail%" )).thenReturn(likePredicate);
        when(criteriaBuilder.and(startPredicate, likePredicate)).thenReturn(finalPredicate);


        Predicate result= ratingStatisticsSpecification.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertSame(finalPredicate, result);

        verify(criteriaBuilder).conjunction();
        verify(root).join(RatingStatistics_.user);
        verify(userJoin).get(User_.email);
        verify(criteriaBuilder).like(userMailPath, "%gmail%");
        verify(criteriaBuilder).and(startPredicate, likePredicate);
    }

    @Test
    void toPredicateShouldCreateEnumPredicate(){

        SearchCriteria criteria = SearchCriteria.builder()
                .key("ratingCalculationEnum")
                .type("enum")
                .value("ADD_ECO_NEWS")
                .build();

        RatingStatisticsSpecification ratingStatisticsSpecification =
                new RatingStatisticsSpecification(List.of(criteria));

        when(criteriaBuilder.conjunction()).thenReturn(startPredicate);
        when(criteriaBuilder.disjunction()).thenReturn(enumOrPredicate);
        when(root.<RatingCalculationEnum>get("ratingCalculationEnum")).thenReturn(enumPath);
        when(criteriaBuilder.equal(enumPath, RatingCalculationEnum.ADD_ECO_NEWS)).thenReturn(enumEqualPredicate);
        when(criteriaBuilder.or(enumOrPredicate, enumEqualPredicate)).thenReturn(enumOrPredicate);
        when(criteriaBuilder.and(startPredicate, enumOrPredicate)).thenReturn(finalPredicate);

        Predicate result= ratingStatisticsSpecification.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertSame(finalPredicate, result);

        verify(criteriaBuilder).conjunction();
        verify(criteriaBuilder).disjunction();
        verify(root).get("ratingCalculationEnum");
        verify(criteriaBuilder).equal(enumPath, RatingCalculationEnum.ADD_ECO_NEWS);
        verify(criteriaBuilder).or(enumOrPredicate, enumEqualPredicate);
        verify(criteriaBuilder).and(startPredicate, enumOrPredicate);
    }

    @Test
    void toPredicateShouldCreateDataRangePredicate(){
        String[] dateRange={"2026-06-16","2026-06-17"};

        SearchCriteria criteria = SearchCriteria.builder()
                .key("dateRange")
                .type("dateRange")
                .value(dateRange)
                .build();

        RatingStatisticsSpecification ratingStatisticsSpecification =
                new RatingStatisticsSpecification(List.of(criteria));

        ZonedDateTime start = LocalDate.parse(dateRange[0]).atStartOfDay(ZoneOffset.UTC);
        ZonedDateTime end = LocalDate.parse(dateRange[1]).atStartOfDay(ZoneOffset.UTC);

        when(criteriaBuilder.conjunction()).thenReturn(startPredicate);
        when(root.<ZonedDateTime>get("dateRange")).thenReturn(dateRangePath);
        when(criteriaBuilder.between(dateRangePath, start, end)).thenReturn(dataRangePredicate);
        when(criteriaBuilder.and(startPredicate, dataRangePredicate)).thenReturn(finalPredicate);

        Predicate result= ratingStatisticsSpecification.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertSame(finalPredicate, result);

        verify(criteriaBuilder).conjunction();
        verify(root).<ZonedDateTime>get("dateRange");
        verify(criteriaBuilder).between(dateRangePath, start, end);
        verify(criteriaBuilder).and(startPredicate, dataRangePredicate);
    }


}
