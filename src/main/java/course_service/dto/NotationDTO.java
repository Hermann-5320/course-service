package course_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotationDTO {

    @NotNull(message = "Note obligatoire")
    @Min(value = 1, message = "Note minimum 1")
    @Max(value = 5, message = "Note maximum 5")
    private Integer note;

    private String commentaire;
    private Boolean securiteOk;
    private String tags;
}