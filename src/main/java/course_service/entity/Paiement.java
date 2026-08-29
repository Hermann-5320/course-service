package course_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "paiements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", unique = true, nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private String mode; // LIQUIDE, MTN, ORANGE

    @Column(nullable = false)
    private BigDecimal montant;

    private BigDecimal frais = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal commission;

    @Column(name = "gain_chauffeur", nullable = false)
    private BigDecimal gainChauffeur;

    @Column(nullable = false)
    private String statut = "EN_ATTENTE";

    @Column(name = "confirme_at")
    private LocalDateTime confirmeAt;
}