package course_service.service;

import course_service.repository.ParametreSystemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ParametreService {

    private final ParametreSystemeRepository parametreRepository;

    public BigDecimal getValeurDecimal(String cle, String valeurParDefaut) {
        return parametreRepository.findById(cle)
                .map(p -> new BigDecimal(p.getValeur()))
                .orElse(new BigDecimal(valeurParDefaut));
    }
}