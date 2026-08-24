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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CourseController {

    private final CourseService courseService;
    private final JwtService jwtService;
    private final AuthServiceClient authServiceClient;

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
        return ResponseEntity.ok(courseService.noterChauffeur(id, dto, passagerId));
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
        return ResponseEntity.ok(courseService.mettreAJourStatut(id, dto.getStatut(), chauffeurId));
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
}