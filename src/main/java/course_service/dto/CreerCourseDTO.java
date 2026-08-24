package course_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreerCourseDTO {

    @NotBlank(message = "Type de véhicule obligatoire")
    private String typeVehicule;

    @NotBlank(message = "Adresse de départ obligatoire")
    private String departAdresse;

    private BigDecimal departLat;
    private BigDecimal departLng;

    @NotBlank(message = "Adresse d'arrivée obligatoire")
    private String arriveeAdresse;

    private BigDecimal arriveeLat;
    private BigDecimal arriveeLng;

    @NotNull(message = "Ville obligatoire")
    private Long villeId;
}