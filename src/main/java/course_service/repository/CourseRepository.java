package course_service.repository;

import course_service.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // Courses d'un passager
    List<Course> findByPassagerIdOrderByCreatedAtDesc(Long passagerId);

    // Courses d'un chauffeur
    List<Course> findByChauffeurIdOrderByCreatedAtDesc(Long chauffeurId);

    // Courses en attente dans une ville
    List<Course> findByStatutAndVilleId(String statut, Long villeId);

    // Courses en cours d'un chauffeur
    List<Course> findByChauffeurIdAndStatutIn(Long chauffeurId, List<String> statuts);

    // Stats chauffeur — nombre de courses terminées
    Long countByChauffeurIdAndStatut(Long chauffeurId, String statut);

    // Toutes les courses pour admin
    List<Course> findAllByOrderByCreatedAtDesc();

    // Courses en cours pour admin
    List<Course> findByStatutIn(List<String> statuts);

    // Courses d'un passager par statut
    List<Course> findByPassagerIdAndStatut(Long passagerId, String statut);
}