package course_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class PaiementServiceClient {

    @Value("${app.paiement-service.url}")
    private String paiementServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void deduireCommission(Long chauffeurId, BigDecimal montantCourse, Long courseId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "chauffeurId", chauffeurId,
                "montantCourse", montantCourse,
                "courseId", courseId
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        restTemplate.postForObject(
                paiementServiceUrl + "/api/paiements/interne/commission",
                entity,
                String.class
        );
    }
}