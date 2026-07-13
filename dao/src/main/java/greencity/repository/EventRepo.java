package greencity.repository;

import greencity.dto.event.EventSearchSuggestionResponseDto;
import greencity.entity.event.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepo extends JpaRepository<Event, Long> {
    List<Event> findByOrganizerId(Long organizerId);

    @Query("SELECT e FROM Event e " +
            "WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "ORDER BY CASE " +
            "WHEN LOWER(e.title) = LOWER(:query) THEN 1 " +
            "WHEN LOWER(e.title) LIKE LOWER(CONCAT(:query, '%')) THEN 2 " +
            "ELSE 3 END")
    List<Event> searchByTitle(@Param("query") String query);

    @Query("SELECT new greencity.dto.event.EventSearchSuggestionResponseDto(e.id, e.title) FROM Event e " +
            "WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "ORDER BY CASE " +
            "WHEN LOWER(e.title) = LOWER(:query) THEN 1 " +
            "WHEN LOWER(e.title) LIKE LOWER(CONCAT(:query, '%')) THEN 2 " +
            "ELSE 3 END")
    List<EventSearchSuggestionResponseDto> findTitleSuggestions(@Param("query") String query);
}