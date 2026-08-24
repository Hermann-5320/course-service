package course_service.client;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class AuthServiceClient {

    @Value("${app.auth-service.url}")
    private String authServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // Récupérer les chauffeurs en ligne dans une ville
    public List<Map> getChauffeursEnLigne(Long villeId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                authServiceUrl + "/api/chauffeurs/en-ligne?villeId=" + villeId,
                HttpMethod.GET,
                entity,
                List.class
        );
        return response.getBody();
    }

    // Mettre à jour les stats du chauffeur après une course
    public void mettreAJourStatsChauffeur(Long chauffeurId, Double noteMoyenne, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        headers.set("Content-Type", "application/json");

        Map<String, Object> body = Map.of(
                "noteMoyenne", noteMoyenne
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        restTemplate.exchange(
                authServiceUrl + "/api/chauffeurs/" + chauffeurId + "/stats",
                HttpMethod.PUT,
                entity,
                Void.class
        );
    }

    // Traduire utilisateurId (du token) → passager.id réel
    public Long getPassagerId(Long utilisateurId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Long> response = restTemplate.exchange(
                authServiceUrl + "/api/passagers/by-utilisateur/" + utilisateurId,
                HttpMethod.GET,
                entity,
                Long.class
        );
        return response.getBody();
    }

    // Traduire utilisateurId (du token) → chauffeur.id réel
    public Long getChauffeurId(Long utilisateurId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Long> response = restTemplate.exchange(
                authServiceUrl + "/api/chauffeurs/by-utilisateur/" + utilisateurId,
                HttpMethod.GET,
                entity,
                Long.class
        );
        return response.getBody();
    }
}