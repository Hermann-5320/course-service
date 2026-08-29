package course_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeclarerPaiementDTO {

    @NotBlank(message = "Le mode de paiement est obligatoire")
    private String mode; // LIQUIDE, MTN, ORANGE
}