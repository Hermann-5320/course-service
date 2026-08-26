package course_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "parametres_systeme")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParametreSysteme {

    @Id
    private String cle;

    @Column(nullable = false)
    private String valeur;

    private String description;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}