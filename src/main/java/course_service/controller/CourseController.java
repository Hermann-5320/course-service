package course_service.controller;

import course_service.client.AuthServiceClient;
import course_service.dto.*;
import course_service.entity.Course;
import course_service.entity.Notation;
import course_service.security.JwtService;
import course_service.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import course_service.dto.SuggestionAdresseDTO;
import course_service.service.GeocodageService;
import course_service.client.PaiementServiceClient;
import course_service.entity.Paiement;
import course_service.dto.DeclarerPaiementDTO;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CourseController {

    private final CourseService courseService;
    private final JwtService jwtService;
    private final AuthServiceClient authServiceClient;
    private final GeocodageService geocodageService;
    private final PaiementServiceClient paiementServiceClient;

    // ── PASSAGER ──────────────────────────────────────────

    // Créer une course
    @PostMapping
    @PreAuthorize("hasRole('PASSAGER')")
    public ResponseEntity<Course> creerCourse(
            @Valid @RequestBody CreerCourseDTO dto,
            @RequestHeader("Authorization") String token) {
        Long utilisateurId = jwtService.extraireUserId(token.substring(7));
        Long passagerId = authServiceClient.getPassagerId(utilisateurId, token);
        return ResponseEntity.ok(courseService.creerCourse(dto, passagerId));
    }

    // Historique passager
    @GetMapping("/historique")
    @PreAuthorize("hasRole('PASSAGER')")
    public ResponseEntity<List<Course>> historiquePassager(
            @RequestHeader("Authorization") String token) {
        Long utilisateurId = jwtService.extraireUserId(token.substring(7));
        Long passagerId = authServiceClient.getPassagerId(utilisateurId, token);
        return ResponseEntity.ok(courseService.getHistoriquePassager(passagerId));
    }

    // Annuler une course (passager)
    @PutMapping("/{id}/annuler")
    @PreAuthorize("hasRole('PASSAGER')")
    public ResponseEntity<Course> annulerCourse(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        Long utilisateurId = jwtService.extraireUserId(token.substring(7));
        Long passagerId = authServiceClient.getPassagerId(utilisateurId, token);
        return ResponseEntity.ok(courseService.annulerCourse(id, passagerId, "PASSAGER"));
    }

    // Noter le chauffeur
    @PostMapping("/{id}/noter")
    @PreAuthorize("hasRole('PASSAGER')")
    public ResponseEntity<Notation> noterChauffeur(
            @PathVariable Long id,
            @Valid @RequestBody NotationDTO dto,
            @RequestHeader("Authorization") String token) {
        Long utilisateurId = jwtService.extraireUserId(token.substring(7));
        Long passagerId = authServiceClient.getPassagerId(utilisateurId, token);
        Notation notation = courseService.noterChauffeur(id, dto, passagerId);

        // Recalculer et mettre à jour la note moyenne du chauffeur
        Double nouvelleMoyenne = courseService.calculerNoteMoyenneChauffeur(notation.getChauffeurId());
        authServiceClient.mettreAJourStatsChauffeur(notation.getChauffeurId(), nouvelleMoyenne, token);

        return ResponseEntity.ok(notation);
    }

    // ── CHAUFFEUR ─────────────────────────────────────────

    // Accepter une course
    @PutMapping("/{id}/accepter")
    @PreAuthorize("hasRole('CHAUFFEUR')")
    public ResponseEntity<Course> accepterCourse(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        Long utilisateurId = jwtService.extraireUserId(token.substring(7));
        Long chauffeurId = authServiceClient.getChauffeurId(utilisateurId, token);

        if (!paiementServiceClient.soldeSuffisant(chauffeurId, token)) {
            throw new RuntimeException("Solde insuffisant. Veuillez recharger votre compte pour continuer à accepter des courses.");
        }

        return ResponseEntity.ok(courseService.accepterCourse(id, chauffeurId));
    }

    // Mettre à jour le statut
    @PutMapping("/{id}/statut")
    @PreAuthorize("hasRole('CHAUFFEUR')")
    public ResponseEntity<Course> mettreAJourStatut(
            @PathVariable Long id,
            @Valid @RequestBody StatutCourseDTO dto,
            @RequestHeader("Authorization") String token) {
        Long utilisateurId = jwtService.extraireUserId(token.substring(7));
        Long chauffeurId = authServiceClient.getChauffeurId(utilisateurId, token);
        Course course = courseService.mettreAJourStatut(id, dto.getStatut(), chauffeurId);
        if (dto.getStatut().equals("TERMINEE")) {
            authServiceClient.incrementerCoursesChauffeur(chauffeurId, token);
            if (course.getDistanceKm() != null) {
                authServiceClient.ajouterKilometres(chauffeurId, course.getDistanceKm(), token);
            }
        }
        return ResponseEntity.ok(course);
    }
    // Annuler une course (chauffeur)
    @PutMapping("/{id}/annuler-chauffeur")
    @PreAuthorize("hasRole('CHAUFFEUR')")
    public ResponseEntity<Course> annulerCourseChauffeur(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        Long utilisateurId = jwtService.extraireUserId(token.substring(7));
        Long chauffeurId = authServiceClient.getChauffeurId(utilisateurId, token);
        return ResponseEntity.ok(courseService.annulerCourse(id, chauffeurId, "CHAUFFEUR"));
    }

    // Historique chauffeur
    @GetMapping("/chauffeur/historique")
    @PreAuthorize("hasRole('CHAUFFEUR')")
    public ResponseEntity<List<Course>> historiqueChauffeur(
            @RequestHeader("Authorization") String token) {
        Long utilisateurId = jwtService.extraireUserId(token.substring(7));
        Long chauffeurId = authServiceClient.getChauffeurId(utilisateurId, token);
        return ResponseEntity.ok(courseService.getHistoriqueChauffeur(chauffeurId));
    }

    // Stats chauffeur
    @GetMapping("/chauffeur/stats")
    @PreAuthorize("hasRole('CHAUFFEUR')")
    public ResponseEntity<StatsDTO> statsChauffeur(
            @RequestHeader("Authorization") String token) {
        Long utilisateurId = jwtService.extraireUserId(token.substring(7));
        Long chauffeurId = authServiceClient.getChauffeurId(utilisateurId, token);
        return ResponseEntity.ok(courseService.getStatsChauffeur(chauffeurId));
    }

    // ── ADMIN ─────────────────────────────────────────────

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Course>> toutesCourses() {
        return ResponseEntity.ok(courseService.getToutesCourses());
    }

    @GetMapping("/admin/en-cours")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Course>> coursesEnCours() {
        return ResponseEntity.ok(courseService.getCoursesEnCours());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PASSAGER','CHAUFFEUR','ADMIN')")
    public ResponseEntity<Course> detailCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @PutMapping("/admin/{id}/annuler")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Course> annulerCourseAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.annulerCourse(id, null, "ADMIN"));
    }
    // Voir les chauffeurs disponibles avant de créer une course
    @GetMapping("/chauffeurs-disponibles")
    @PreAuthorize("hasRole('PASSAGER')")
    public ResponseEntity<List<Map<String, Object>>> chauffeursDisponibles(
            @RequestParam Long villeId,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(authServiceClient.getChauffeursDisponibles(villeId, token));
    }
    // Recherche d'adresse avec autocomplétion (proxy vers SerpApi)
    @GetMapping("/rechercher-adresse")
    @PreAuthorize("hasAnyRole('PASSAGER','CHAUFFEUR')")
    public ResponseEntity<List<SuggestionAdresseDTO>> rechercherAdresse(
            @RequestParam String q) {
        return ResponseEntity.ok(geocodageService.rechercherAdresses(q));
    }
    // ajouter l endpoint de declaration de paiement (passager)
    @PostMapping("/{id}/paiement")
    @PreAuthorize("hasRole('PASSAGER')")
    public ResponseEntity<Paiement> declarerPaiement(
            @PathVariable Long id,
            @Valid @RequestBody DeclarerPaiementDTO dto,
            @RequestHeader("Authorization") String token) {
        Long utilisateurId = jwtService.extraireUserId(token.substring(7));
        Long passagerId = authServiceClient.getPassagerId(utilisateurId, token);
        return ResponseEntity.ok(courseService.declarerPaiement(id, dto, passagerId));
    }
    // ajouter l endpoint de confirmation de paiement
    @PutMapping("/{id}/paiement/confirmer")
    @PreAuthorize("hasRole('CHAUFFEUR')")
    public ResponseEntity<Paiement> confirmerPaiement(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        Long utilisateurId = jwtService.extraireUserId(token.substring(7));
        Long chauffeurId = authServiceClient.getChauffeurId(utilisateurId, token);

        Paiement paiement = courseService.confirmerPaiement(id, chauffeurId);

        // Maintenant qu'on a confirmé, on déduit la commission
        paiementServiceClient.deduireCommission(chauffeurId, paiement.getMontant(), id);

        return ResponseEntity.ok(paiement);
    }
}