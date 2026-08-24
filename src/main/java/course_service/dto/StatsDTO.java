package course_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class StatsDTO {
    private Long nbCoursesTotal;
    private Long nbCoursesTerminees;
    private Long nbCoursesAnnulees;
    private Double noteMoyenne;
}