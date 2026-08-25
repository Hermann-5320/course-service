package course_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SuggestionAdresseDTO {
    private String texte;
    private String sousTexte;
    private Double latitude;
    private Double longitude;
}