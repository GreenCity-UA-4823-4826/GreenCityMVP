package greencity.filters;

import greencity.entity.Notification;
import greencity.entity.Notification_;
import greencity.entity.User;
import greencity.entity.User_;
import greencity.enums.NotificationType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class NotificationSpecification implements MySpecification<Notification> {
    private final transient List<SearchCriteria> searchCriteriaList;

    @Override
    public Predicate toPredicate(@NotNull Root<Notification> root,
        CriteriaQuery<?> criteriaQuery,
        @NotNull CriteriaBuilder criteriaBuilder) {
        Predicate allPredicates = criteriaBuilder.conjunction();
        for (SearchCriteria searchCriteria : searchCriteriaList) {
            Predicate predicate = switch (searchCriteria.getType()) {
                case Notification_.TARGET_USER -> getTargetUserPredicate(root, criteriaBuilder, searchCriteria);
                case Notification_.PROJECT_NAME -> getEnumPredicate(root, criteriaBuilder, searchCriteria);
                case Notification_.NOTIFICATION_TYPE ->
                    getNotificationTypePredicate(root, criteriaBuilder, searchCriteria);
                case Notification_.VIEWED -> getViewedPredicate(root, criteriaBuilder, searchCriteria);
                default -> criteriaBuilder.conjunction();
            };
            allPredicates = criteriaBuilder.and(allPredicates, predicate);
        }
        criteriaQuery.orderBy(criteriaBuilder.desc(root.get(Notification_.TIME)));
        return allPredicates;
    }

    private Predicate getTargetUserPredicate(Root<Notification> root, CriteriaBuilder criteriaBuilder,
        SearchCriteria searchCriteria) {
        Long targetUserId = (Long) searchCriteria.getValue();
        if (targetUserId == null) {
            return criteriaBuilder.conjunction();
        }
        Join<Notification, User> targetUserJoin = root.join(Notification_.TARGET_USER);
        return criteriaBuilder.equal(targetUserJoin.get(User_.ID), targetUserId);
    }

    private Predicate getNotificationTypePredicate(Root<Notification> root, CriteriaBuilder criteriaBuilder,
        SearchCriteria searchCriteria) {
        NotificationType[] types = (NotificationType[]) searchCriteria.getValue();
        if (types == null || types.length == 0) {
            return criteriaBuilder.conjunction();
        }
        return root.get(Notification_.NOTIFICATION_TYPE).in((Object[]) types);
    }

    private Predicate getViewedPredicate(Root<Notification> root, CriteriaBuilder criteriaBuilder,
        SearchCriteria searchCriteria) {
        String value = searchCriteria.getValue().toString().trim();
        return value.isEmpty() ? criteriaBuilder.conjunction()
            : criteriaBuilder.equal(root.get(Notification_.VIEWED), Boolean.parseBoolean(value));
    }
}
