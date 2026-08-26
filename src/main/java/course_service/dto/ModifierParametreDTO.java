package course_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ModifierParametreDTO {
    @NotBlank(message = "La valeur est obligatoire")
    private String valeur;
}