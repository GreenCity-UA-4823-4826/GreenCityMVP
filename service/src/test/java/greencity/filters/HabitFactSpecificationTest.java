package greencity.filters;

import greencity.entity.Habit;
import greencity.entity.HabitFact;
import greencity.entity.HabitFactTranslation;
import greencity.entity.HabitFact_;
import greencity.entity.Habit_;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.SingularAttribute;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HabitFactSpecificationTest {

    @Mock
    private Root<HabitFact> root;

    @Mock
    private CriteriaQuery<?> criteriaQuery;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Predicate initialPredicate;

    @Mock
    private Predicate resultPredicate;

    @Mock
    private Predicate idPredicate;

    @Mock
    private Predicate habitIdPredicate;

    @Mock
    private Predicate contentPredicate;

    @Mock
    private Predicate likePredicate;

    @Mock
    private Predicate equalPredicate;

    @Mock
    private Path<Object> habitFactIdPathByString;

    @Mock
    private Path<Long> habitFactIdPath;

    @Mock
    private Join<HabitFact, Habit> habitJoin;

    @Mock
    private Path<Long> habitIdPath;

    @Mock
    private Root<HabitFactTranslation> habitFactTranslationRoot;

    @Mock
    private Path<String> contentPath;

    @Mock
    private Path<HabitFact> habitFactTranslationHabitFactPath;

    @Mock
    private Path<Long> habitFactTranslationHabitFactIdPath;

    @Mock
    private Predicate emptyContentPredicate;

    @Mock
    private Predicate firstCombinedPredicate;

    @Mock
    private Predicate secondCombinedPredicate;

    @Test
    void toPredicate_ShouldReturnPredicate_WhenSearchById() {
        SearchCriteria searchCriteria = SearchCriteria.builder()
            .type("id")
            .key("id")
            .value(1L)
            .build();

        HabitFactSpecification specification =
            new HabitFactSpecification(List.of(searchCriteria));

        when(criteriaBuilder.conjunction()).thenReturn(initialPredicate);
        when(root.get("id")).thenReturn(habitFactIdPathByString);
        when(criteriaBuilder.equal(habitFactIdPathByString, 1L)).thenReturn(idPredicate);
        when(criteriaBuilder.and(initialPredicate, idPredicate)).thenReturn(resultPredicate);

        Predicate actual = specification.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertSame(resultPredicate, actual);

        verify(criteriaBuilder).conjunction();
        verify(root).get("id");
        verify(criteriaBuilder).equal(habitFactIdPathByString, 1L);
        verify(criteriaBuilder).and(initialPredicate, idPredicate);
    }

    @Test
    void toPredicate_ShouldReturnPredicate_WhenSearchByHabitId() {
        SearchCriteria searchCriteria = SearchCriteria.builder()
            .type("habitId")
            .key("habitId")
            .value(10L)
            .build();

        HabitFactSpecification specification =
            new HabitFactSpecification(List.of(searchCriteria));

        when(criteriaBuilder.conjunction()).thenReturn(initialPredicate);
        when(root.join(HabitFact_.habit)).thenReturn(habitJoin);
        when(habitJoin.get(Habit_.id)).thenReturn(habitIdPath);
        when(criteriaBuilder.equal(habitIdPath, 10L)).thenReturn(habitIdPredicate);
        when(criteriaBuilder.and(initialPredicate, habitIdPredicate)).thenReturn(resultPredicate);

        Predicate actual = specification.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertSame(resultPredicate, actual);

        verify(criteriaBuilder).conjunction();
        verify(root).join(HabitFact_.habit);
        verify(habitJoin).get(Habit_.id);
        verify(criteriaBuilder).equal(habitIdPath, 10L);
        verify(criteriaBuilder).and(initialPredicate, habitIdPredicate);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void toPredicate_ShouldReturnPredicate_WhenSearchByContent() {
        SearchCriteria searchCriteria = SearchCriteria.builder()
            .type("content")
            .key("content")
            .value("eco")
            .build();

        HabitFactSpecification specification =
            new HabitFactSpecification(List.of(searchCriteria));

        when(criteriaBuilder.conjunction()).thenReturn(initialPredicate);

        when(criteriaQuery.from(HabitFactTranslation.class))
            .thenReturn(habitFactTranslationRoot);
        // Static metamodel fields can be null in isolated unit tests without JPA
        // context.
        doReturn(contentPath, habitFactTranslationHabitFactPath)
            .when(habitFactTranslationRoot)
            .get((SingularAttribute) isNull());

        when(criteriaBuilder.like(contentPath, "%eco%")).thenReturn(likePredicate);

        doReturn(habitFactTranslationHabitFactIdPath)
            .when(habitFactTranslationHabitFactPath)
            .get((SingularAttribute) isNull());

        doReturn(habitFactIdPath)
            .when(root)
            .get((SingularAttribute) isNull());

        when(criteriaBuilder.equal(habitFactTranslationHabitFactIdPath, habitFactIdPath))
            .thenReturn(equalPredicate);

        when(criteriaBuilder.and(likePredicate, equalPredicate))
            .thenReturn(contentPredicate);
        when(criteriaBuilder.and(initialPredicate, contentPredicate))
            .thenReturn(resultPredicate);

        Predicate actual = specification.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertSame(resultPredicate, actual);

        verify(criteriaBuilder).conjunction();
        verify(criteriaQuery).from(HabitFactTranslation.class);
        verify(habitFactTranslationRoot, times(2)).get((SingularAttribute) isNull());
        verify(criteriaBuilder).like(contentPath, "%eco%");
        verify(habitFactTranslationHabitFactPath).get((SingularAttribute) isNull());
        verify(root).get((SingularAttribute) isNull());
        verify(criteriaBuilder).equal(habitFactTranslationHabitFactIdPath, habitFactIdPath);
        verify(criteriaBuilder).and(likePredicate, equalPredicate);
        verify(criteriaBuilder).and(initialPredicate, contentPredicate);
    }

    @Test
    void toPredicate_ShouldNotAddAdditionalPredicate_WhenContentIsEmptyString() {
        SearchCriteria searchCriteria = SearchCriteria.builder()
            .type("content")
            .key("content")
            .value("")
            .build();

        HabitFactSpecification specification =
            new HabitFactSpecification(List.of(searchCriteria));

        when(criteriaBuilder.conjunction())
            .thenReturn(initialPredicate)
            .thenReturn(emptyContentPredicate);
        when(criteriaQuery.from(HabitFactTranslation.class))
            .thenReturn(habitFactTranslationRoot);
        when(criteriaBuilder.and(initialPredicate, emptyContentPredicate))
            .thenReturn(resultPredicate);

        Predicate actual = specification.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertSame(resultPredicate, actual);

        verify(criteriaBuilder, times(2)).conjunction();
        verify(criteriaQuery).from(HabitFactTranslation.class);
        verify(criteriaBuilder).and(initialPredicate, emptyContentPredicate);
        verify(criteriaBuilder, never()).like(contentPath, "%%");
    }

    @Test
    void toPredicate_ShouldBuildCompoundPredicate_WhenMultipleSearchCriteriaProvided() {
        SearchCriteria idCriteria = SearchCriteria.builder()
            .type("id")
            .key("id")
            .value(1L)
            .build();

        SearchCriteria habitIdCriteria = SearchCriteria.builder()
            .type("habitId")
            .key("habitId")
            .value(10L)
            .build();

        HabitFactSpecification specification =
            new HabitFactSpecification(List.of(idCriteria, habitIdCriteria));

        when(criteriaBuilder.conjunction()).thenReturn(initialPredicate);

        when(root.get("id")).thenReturn(habitFactIdPathByString);
        when(criteriaBuilder.equal(habitFactIdPathByString, 1L)).thenReturn(idPredicate);
        when(criteriaBuilder.and(initialPredicate, idPredicate)).thenReturn(firstCombinedPredicate);

        when(root.join(HabitFact_.habit)).thenReturn(habitJoin);
        when(habitJoin.get(Habit_.id)).thenReturn(habitIdPath);
        when(criteriaBuilder.equal(habitIdPath, 10L)).thenReturn(habitIdPredicate);
        when(criteriaBuilder.and(firstCombinedPredicate, habitIdPredicate)).thenReturn(secondCombinedPredicate);

        Predicate actual = specification.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertSame(secondCombinedPredicate, actual);

        verify(criteriaBuilder).conjunction();

        verify(root).get("id");
        verify(criteriaBuilder).equal(habitFactIdPathByString, 1L);
        verify(criteriaBuilder).and(initialPredicate, idPredicate);

        verify(root).join(HabitFact_.habit);
        verify(habitJoin).get(Habit_.id);
        verify(criteriaBuilder).equal(habitIdPath, 10L);
        verify(criteriaBuilder).and(firstCombinedPredicate, habitIdPredicate);
    }

    @Test
    void toPredicate_ShouldThrowNullPointerException_WhenContentValueIsNull() {
        SearchCriteria searchCriteria = SearchCriteria.builder()
            .type("content")
            .key("content")
            .value(null)
            .build();

        HabitFactSpecification specification =
            new HabitFactSpecification(List.of(searchCriteria));

        when(criteriaBuilder.conjunction()).thenReturn(initialPredicate);
        when(criteriaQuery.from(HabitFactTranslation.class))
            .thenReturn(habitFactTranslationRoot);

        assertThrows(NullPointerException.class,
            () -> specification.toPredicate(root, criteriaQuery, criteriaBuilder));

        verify(criteriaBuilder).conjunction();
        verify(criteriaQuery).from(HabitFactTranslation.class);
    }

    @Test
    void toPredicate_ShouldReturnConjunction_WhenSearchCriteriaListIsEmpty() {
        HabitFactSpecification specification =
            new HabitFactSpecification(List.of());

        when(criteriaBuilder.conjunction()).thenReturn(initialPredicate);

        Predicate actual = specification.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertSame(initialPredicate, actual);

        verify(criteriaBuilder).conjunction();
    }

    @Test
    void toPredicate_ShouldReturnConjunction_WhenSearchCriteriaTypeIsUnknown() {
        SearchCriteria searchCriteria = SearchCriteria.builder()
            .type("unknown")
            .key("unknown")
            .value("some value")
            .build();

        HabitFactSpecification specification =
            new HabitFactSpecification(List.of(searchCriteria));

        when(criteriaBuilder.conjunction()).thenReturn(initialPredicate);

        Predicate actual = specification.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertSame(initialPredicate, actual);

        verify(criteriaBuilder).conjunction();
        verify(root, never()).get("id");
        verify(root, never()).join(HabitFact_.habit);
        verify(criteriaQuery, never()).from(HabitFactTranslation.class);
    }
}