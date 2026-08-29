package course_service.service;

import course_service.dto.*;
import course_service.entity.Course;
import course_service.entity.Notation;
import course_service.repository.CourseRepository;
import course_service.repository.NotationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import course_service.entity.Paiement;
import course_service.repository.PaiementRepository;
import course_service.dto.DeclarerPaiementDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final NotationRepository notationRepository;
    private final ParametreService parametreService;
    private final PaiementRepository paiementRepository;


    // ── CREER UNE COURSE ──────────────────────────────────
    @Transactional
    public Course creerCourse(CreerCourseDTO dto, Long passagerId) {

        BigDecimal distanceKm = null;
        BigDecimal prixEstime;

        if (dto.getDepartLat() != null && dto.getDepartLng() != null
                && dto.getArriveeLat() != null && dto.getArriveeLng() != null) {

            double distance = calculerDistanceKm(
                    dto.getDepartLat().doubleValue(), dto.getDepartLng().doubleValue(),
                    dto.getArriveeLat().doubleValue(), dto.getArriveeLng().doubleValue()
            );
            distanceKm = BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP);
        }

        prixEstime = calculerPrix(dto.getTypeVehicule(), dto.getDepartLat(),
                dto.getDepartLng(), dto.getArriveeLat(), dto.getArriveeLng());

        Course course = new Course();
        course.setPassagerId(passagerId);
        course.setVilleId(dto.getVilleId());
        course.setTypeVehicule(dto.getTypeVehicule().toUpperCase());
        course.setDepartAdresse(dto.getDepartAdresse());
        course.setDepartLat(dto.getDepartLat());
        course.setDepartLng(dto.getDepartLng());
        course.setArriveeAdresse(dto.getArriveeAdresse());
        course.setArriveeLat(dto.getArriveeLat());
        course.setArriveeLng(dto.getArriveeLng());
        course.setDistanceKm(distanceKm);
        course.setPrixEstime(prixEstime);
        course.setStatut("EN_ATTENTE");

        return courseRepository.save(course);
    }

    // ── ACCEPTER UNE COURSE ───────────────────────────────
    @Transactional
    public Course accepterCourse(Long courseId, Long chauffeurId) {
        Course course = getCourseById(courseId);

        if (!course.getStatut().equals("EN_ATTENTE")) {
            throw new RuntimeException("Cette course n'est plus disponible");
        }

        List<String> statutsActifs = List.of("ACCEPTEE", "EN_ROUTE", "ARRIVEE", "DEMARREE");
        List<Course> coursesActives = courseRepository.findByChauffeurIdAndStatutIn(chauffeurId, statutsActifs);

        if (!coursesActives.isEmpty()) {
            throw new RuntimeException("Vous avez déjà une course en cours. Terminez-la avant d'en accepter une nouvelle.");
        }

        List<Course> coursesTerminees = courseRepository.findByChauffeurIdAndStatutIn(chauffeurId, List.of("TERMINEE"));
        for (Course c : coursesTerminees) {
            Optional<Paiement> paiement = paiementRepository.findByCourseId(c.getId());
            if (paiement.isEmpty() || paiement.get().getStatut().equals("EN_ATTENTE")) {
                throw new RuntimeException("Vous devez confirmer le paiement de votre course précédente (#" + c.getId() + ") avant d'en accepter une nouvelle.");
            }
        }

        course.setChauffeurId(chauffeurId);
        course.setStatut("ACCEPTEE");
        return courseRepository.save(course);
    }

    // ── METTRE A JOUR LE STATUT ───────────────────────────
    @Transactional
    public Course mettreAJourStatut(Long courseId, String nouveauStatut, Long chauffeurId) {
        Course course = getCourseById(courseId);

        if (!course.getChauffeurId().equals(chauffeurId)) {
            throw new RuntimeException("Vous n'êtes pas le chauffeur de cette course");
        }

        validerTransitionStatut(course.getStatut(), nouveauStatut);

        course.setStatut(nouveauStatut);

        if (nouveauStatut.equals("DEMARREE")) {
            course.setStartedAt(LocalDateTime.now());
        }

        if (nouveauStatut.equals("TERMINEE")) {
            course.setEndedAt(LocalDateTime.now());
            // Calculer prix final et durée
            if (course.getStartedAt() != null) {
                long dureeMin = Duration.between(course.getStartedAt(), course.getEndedAt()).toMinutes();
                course.setDureeMin((int) dureeMin);
            }
            // Prix final = prix estimé pour le MVP
            course.setPrixFinal(course.getPrixEstime());
        }

        return courseRepository.save(course);
    }

    // ── ANNULER UNE COURSE ────────────────────────────────
    @Transactional
    public Course annulerCourse(Long courseId, Long userId, String annuleePar) {
        Course course = getCourseById(courseId);

        List<String> statutsAnnulables = List.of("EN_ATTENTE", "ACCEPTEE", "EN_ROUTE", "ARRIVEE");
        if (!statutsAnnulables.contains(course.getStatut())) {
            throw new RuntimeException("Cette course ne peut plus être annulée");
        }

        course.setStatut("ANNULEE");
        course.setAnnuleePar(annuleePar);
        course.setPrixFinal(null);
        return courseRepository.save(course);
    }

    // ── NOTER UN CHAUFFEUR ────────────────────────────────
    @Transactional
    public Notation noterChauffeur(Long courseId, NotationDTO dto, Long passagerId) {
        Course course = getCourseById(courseId);

        if (!course.getPassagerId().equals(passagerId)) {
            throw new RuntimeException("Vous n'êtes pas le passager de cette course");
        }

        if (!course.getStatut().equals("TERMINEE")) {
            throw new RuntimeException("La course doit être terminée pour être notée");
        }

        if (notationRepository.existsByCourseId(courseId)) {
            throw new RuntimeException("Cette course a déjà été notée");
        }

        Notation notation = new Notation();
        notation.setCourseId(courseId);
        notation.setPassagerId(passagerId);
        notation.setChauffeurId(course.getChauffeurId());
        notation.setNote(dto.getNote());
        notation.setCommentaire(dto.getCommentaire());
        notation.setSecuriteOk(dto.getSecuriteOk());
        notation.setTags(dto.getTags());

        return notationRepository.save(notation);
    }

    // ── HISTORIQUE PASSAGER ───────────────────────────────
    public List<Course> getHistoriquePassager(Long passagerId) {
        return courseRepository.findByPassagerIdOrderByCreatedAtDesc(passagerId);
    }

    // ── HISTORIQUE CHAUFFEUR ──────────────────────────────
    public List<Course> getHistoriqueChauffeur(Long chauffeurId) {
        return courseRepository.findByChauffeurIdOrderByCreatedAtDesc(chauffeurId);
    }

    // ── STATS CHAUFFEUR ───────────────────────────────────
    public StatsDTO getStatsChauffeur(Long chauffeurId) {
        Long total = courseRepository.countByChauffeurIdAndStatut(chauffeurId, "TERMINEE")
                + courseRepository.countByChauffeurIdAndStatut(chauffeurId, "ANNULEE");
        Long terminees = courseRepository.countByChauffeurIdAndStatut(chauffeurId, "TERMINEE");
        Long annulees = courseRepository.countByChauffeurIdAndStatut(chauffeurId, "ANNULEE");
        Double noteMoyenne = notationRepository.calculerNoteMoyenne(chauffeurId);

        return new StatsDTO(total, terminees, annulees, noteMoyenne);
    }

    // ── TOUTES LES COURSES (ADMIN) ────────────────────────
    public List<Course> getToutesCourses() {
        return courseRepository.findAllByOrderByCreatedAtDesc();
    }

    // ── COURSES EN COURS (ADMIN) ──────────────────────────
    public List<Course> getCoursesEnCours() {
        return courseRepository.findByStatutIn(
                List.of("EN_ATTENTE", "ACCEPTEE", "EN_ROUTE", "ARRIVEE", "DEMARREE")
        );
    }

    // ── DETAIL D'UNE COURSE ───────────────────────────────
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course introuvable"));
    }
    // ── CALCUL DU PRIX ────────────────────────────────────
    private BigDecimal calculerPrix(String typeVehicule, BigDecimal lat1, BigDecimal lng1,
                                    BigDecimal lat2, BigDecimal lng2) {

        BigDecimal prixMinimum = parametreService.getValeurDecimal("PRIX_MINIMUM", "500");

        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) {
            return prixMinimum;
        }

        double distanceKm = calculerDistanceKm(
                lat1.doubleValue(), lng1.doubleValue(),
                lat2.doubleValue(), lng2.doubleValue()
        );

        BigDecimal tarif = typeVehicule.equalsIgnoreCase("MOTO")
                ? parametreService.getValeurDecimal("TARIF_MOTO_KM", "300")
                : parametreService.getValeurDecimal("TARIF_VOITURE_KM", "500");

        BigDecimal prix = tarif.multiply(new BigDecimal(distanceKm));

        return prix.max(prixMinimum).setScale(0, RoundingMode.HALF_UP);
    }

    // Formule Haversine pour calculer la distance entre 2 points GPS
    private double calculerDistanceKm(double lat1, double lng1, double lat2, double lng2) {
        final int RAYON_TERRE = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RAYON_TERRE * c;
    }

    // Valider la transition de statut
    private void validerTransitionStatut(String actuel, String nouveau) {
        boolean valide = switch (actuel) {
            case "ACCEPTEE" -> nouveau.equals("EN_ROUTE");
            case "EN_ROUTE" -> nouveau.equals("ARRIVEE");
            case "ARRIVEE"  -> nouveau.equals("DEMARREE");
            case "DEMARREE" -> nouveau.equals("TERMINEE");
            default -> false;
        };
        if (!valide) {
            throw new RuntimeException("Transition de statut invalide : " + actuel + " → " + nouveau);
        }
    }
    public Double calculerNoteMoyenneChauffeur(Long chauffeurId) {
        return notationRepository.calculerNoteMoyenne(chauffeurId);
    }
    // ── DECLARER LE PAIEMENT (PASSAGER) ───────────────────

    // ── CONFIRMER LE PAIEMENT (CHAUFFEUR) ─────────────────
    @Transactional
    public Paiement confirmerPaiement(Long courseId, Long chauffeurId) {
        Course course = getCourseById(courseId);

        if (!course.getChauffeurId().equals(chauffeurId)) {
            throw new RuntimeException("Vous n'êtes pas le chauffeur de cette course");
        }

        Paiement paiement = paiementRepository.findByCourseId(courseId)
                .orElseThrow(() -> new RuntimeException("Aucun paiement déclaré pour cette course"));

        if (paiement.getStatut().equals("CONFIRME")) {
            throw new RuntimeException("Ce paiement a déjà été confirmé");
        }

        paiement.setStatut("CONFIRME");
        paiement.setConfirmeAt(java.time.LocalDateTime.now());
        return paiementRepository.save(paiement);
    }
    // ── DECLARER LE PAIEMENT (PASSAGER) ───────────────────
    @Transactional
    public Paiement declarerPaiement(Long courseId, DeclarerPaiementDTO dto, Long passagerId) {
        Course course = getCourseById(courseId);

        if (!course.getPassagerId().equals(passagerId)) {
            throw new RuntimeException("Vous n'êtes pas le passager de cette course");
        }

        if (!course.getStatut().equals("TERMINEE")) {
            throw new RuntimeException("La course doit être terminée pour déclarer un paiement");
        }

        if (paiementRepository.findByCourseId(courseId).isPresent()) {
            throw new RuntimeException("Un paiement a déjà été déclaré pour cette course");
        }

        BigDecimal montant = course.getPrixFinal();
        BigDecimal tauxCommission = parametreService.getValeurDecimal("COMMISSION_POURCENTAGE", "15");
        BigDecimal commission = montant.multiply(tauxCommission)
                .divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
        BigDecimal gainChauffeur = montant.subtract(commission);

        Paiement paiement = new Paiement();
        paiement.setCourseId(courseId);
        paiement.setMode(dto.getMode().toUpperCase());
        paiement.setMontant(montant);
        paiement.setCommission(commission);
        paiement.setGainChauffeur(gainChauffeur);
        paiement.setStatut("EN_ATTENTE");

        return paiementRepository.save(paiement);
    }
}