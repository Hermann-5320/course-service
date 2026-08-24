package course_service.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CourseResponseDTO {
    private Long id;
    private Long passagerId;
    private Long chauffeurId;
    private String typeVehicule;
    private String departAdresse;
    private String arriveeAdresse;
    private BigDecimal distanceKm;
    private Integer dureeMin;
    private BigDecimal prixEstime;
    private BigDecimal prixFinal;
    private String statut;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
}