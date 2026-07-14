package greencity.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "news_subscribers")
public class NewsSubscriber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email_address", nullable = false, unique = true)
    private String email;

    @Column(name = "unsubscribe_token", nullable = false, unique = true)
    private String unsubscribeToken;

}
