package course_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StatutCourseDTO {

    @NotBlank(message = "Statut obligatoire")
    private String statut;
}