package course_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", unique = true, nullable = false)
    private Long courseId;

    @Column(name = "passager_id", nullable = false)
    private Long passagerId;

    @Column(name = "chauffeur_id", nullable = false)
    private Long chauffeurId;

    @Column(nullable = false)
    private Integer note;

    private String commentaire;

    @Column(name = "securite_ok")
    private Boolean securiteOk;

    private String tags;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}