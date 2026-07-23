package greencity.entity.event;

import greencity.entity.User;

import greencity.enums.event.EventType;
import greencity.enums.event.EventVisibility;
import greencity.enums.event.InitiativeType;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "events")
@Getter
@Setter
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 70)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @ElementCollection(targetClass = EventType.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "event_types", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "event_type")
    private Set<EventType> eventTypes = new HashSet<>();

    @ElementCollection(targetClass = InitiativeType.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "event_initiative_types", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "initiative_type")
    private Set<InitiativeType> initiativeTypes = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventVisibility visibility = EventVisibility.OPEN;

    private String location;
    private Double latitude;
    private Double longitude;

    private String onlineLink;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventDate> dates = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventImage> images = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}