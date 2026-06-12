package greencity.filters;

import greencity.entity.ShoppingListItem;
import greencity.entity.localization.ShoppingListItemTranslation;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.SingularAttribute;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShoppingListItemSpecificationTest {

    @Mock
    private Root<ShoppingListItem> root;

    @Mock
    private CriteriaQuery<?> criteriaQuery;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Predicate contentPredicate;

    @Mock
    private Predicate likePredicate;

    @Mock
    private Predicate equalPredicate;

    @Mock
    private Path<Object> idPath;

    @Mock
    private Predicate resultPredicate;

    @Test
    void toPredicate_ShouldReturnPredicate_WhenSearchById() {
        SearchCriteria searchCriteria = SearchCriteria.builder()
            .type("id")
            .key("id")
            .value(1L)
            .build();

        ShoppingListItemSpecification specification =
            new ShoppingListItemSpecification(List.of(searchCriteria));

        when(criteriaBuilder.conjunction()).thenReturn(contentPredicate);
        when(root.get("id")).thenReturn(idPath);
        when(criteriaBuilder.equal(idPath, 1L)).thenReturn(equalPredicate);
        when(criteriaBuilder.and(contentPredicate, equalPredicate)).thenReturn(resultPredicate);

        Predicate actual = specification.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertSame(resultPredicate, actual);

        verify(criteriaBuilder).conjunction();
        verify(root).get("id");
        verify(criteriaBuilder).equal(idPath, 1L);
        verify(criteriaBuilder).and(contentPredicate, equalPredicate);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void toPredicate_ShouldReturnPredicate_WhenSearchByContent() {
        SearchCriteria searchCriteria = SearchCriteria.builder()
            .type("content")
            .key("content")
            .value("eco")
            .build();

        ShoppingListItemSpecification specification =
            new ShoppingListItemSpecification(List.of(searchCriteria));

        Root<ShoppingListItemTranslation> translationRoot = mock(Root.class);
        Path<String> contentPath = mock(Path.class);
        Path<Object> itemPath = mock(Path.class);
        Path<Object> translationItemIdPath = mock(Path.class);
        Path<Object> shoppingListItemIdPath = mock(Path.class);

        when(criteriaBuilder.conjunction()).thenReturn(contentPredicate);
        when(criteriaQuery.from(ShoppingListItemTranslation.class)).thenReturn(translationRoot);

        doReturn(contentPath, itemPath)
            .when(translationRoot)
            .get((SingularAttribute) isNull());

        when(criteriaBuilder.like(contentPath, "%eco%")).thenReturn(likePredicate);

        doReturn(translationItemIdPath)
            .when(itemPath)
            .get((SingularAttribute) isNull());

        doReturn(shoppingListItemIdPath)
            .when(root)
            .get((SingularAttribute) isNull());

        when(criteriaBuilder.equal(translationItemIdPath, shoppingListItemIdPath))
            .thenReturn(equalPredicate);

        when(criteriaBuilder.and(likePredicate, equalPredicate))
            .thenReturn(contentPredicate);

        when(criteriaBuilder.and(contentPredicate, contentPredicate))
            .thenReturn(resultPredicate);

        Predicate actual = specification.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertSame(resultPredicate, actual);

        verify(criteriaQuery).from(ShoppingListItemTranslation.class);
        verify(criteriaBuilder).like(contentPath, "%eco%");
        verify(criteriaBuilder).equal(translationItemIdPath, shoppingListItemIdPath);
    }

    @Test
    void toPredicate_ShouldSkipFiltering_WhenContentIsBlank() {
        SearchCriteria searchCriteria = SearchCriteria.builder()
            .type("content")
            .key("content")
            .value(" ")
            .build();

        ShoppingListItemSpecification specification =
            new ShoppingListItemSpecification(List.of(searchCriteria));

        when(criteriaBuilder.conjunction())
            .thenReturn(contentPredicate)
            .thenReturn(equalPredicate);

        when(criteriaQuery.from(ShoppingListItemTranslation.class))
            .thenReturn(mock(Root.class));

        when(criteriaBuilder.and(contentPredicate, equalPredicate))
            .thenReturn(resultPredicate);

        Predicate actual = specification.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertSame(resultPredicate, actual);

        verify(criteriaQuery).from(ShoppingListItemTranslation.class);
        verify(criteriaBuilder, never()).like(
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.anyString());
    }

}
