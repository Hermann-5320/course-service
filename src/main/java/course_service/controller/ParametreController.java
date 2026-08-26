package course_service.controller;

import course_service.dto.ModifierParametreDTO;
import course_service.entity.ParametreSysteme;
import course_service.repository.ParametreSystemeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/parametres")
@RequiredArgsConstructor
public class ParametreController {

    private final ParametreSystemeRepository parametreRepository;

    // Voir tous les paramètres (Admin)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ParametreSysteme>> getTousParametres() {
        return ResponseEntity.ok(parametreRepository.findAll());
    }

    // Modifier un paramètre (Admin)
    @PutMapping("/{cle}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> modifierParametre(
            @PathVariable String cle,
            @Valid @RequestBody ModifierParametreDTO dto) {

        ParametreSysteme parametre = parametreRepository.findById(cle)
                .orElseThrow(() -> new RuntimeException("Paramètre introuvable"));

        parametre.setValeur(dto.getValeur());
        parametre.setUpdatedAt(LocalDateTime.now());
        parametreRepository.save(parametre);

        return ResponseEntity.ok("Paramètre mis à jour avec succès");
    }
}