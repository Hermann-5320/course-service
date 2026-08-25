package course_service.service;

import course_service.dto.SuggestionAdresseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GeocodageService {

    @Value("${app.serpapi.key}")
    private String serpApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String CENTRE_YAOUNDE = "@3.848,11.502,13z";

    public List<SuggestionAdresseDTO> rechercherAdresses(String recherche) {

        String url = UriComponentsBuilder
                .fromUriString("https://serpapi.com/search.json")
                .queryParam("engine", "google_maps_autocomplete")
                .queryParam("q", recherche)
                .queryParam("ll", CENTRE_YAOUNDE)
                .queryParam("api_key", serpApiKey)
                .toUriString();

        Map response = restTemplate.getForObject(url, Map.class);

        List<SuggestionAdresseDTO> resultats = new ArrayList<>();

        if (response != null && response.containsKey("suggestions")) {
            List<Map<String, Object>> suggestions = (List<Map<String, Object>>) response.get("suggestions");

            for (Map<String, Object> suggestion : suggestions) {
                if ("place".equals(suggestion.get("type"))) {
                    resultats.add(new SuggestionAdresseDTO(
                            (String) suggestion.get("value"),
                            (String) suggestion.getOrDefault("subtext", ""),
                            suggestion.get("latitude") != null ? ((Number) suggestion.get("latitude")).doubleValue() : null,
                            suggestion.get("longitude") != null ? ((Number) suggestion.get("longitude")).doubleValue() : null
                    ));
                }
            }
        }

        return resultats;
    }
}