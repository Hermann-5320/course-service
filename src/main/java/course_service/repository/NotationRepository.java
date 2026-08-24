package course_service.repository;

import course_service.entity.Notation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface NotationRepository extends JpaRepository<Notation, Long> {

    Optional<Notation> findByCourseId(Long courseId);

    // Note moyenne d'un chauffeur
    @Query("SELECT AVG(n.note) FROM Notation n WHERE n.chauffeurId = :chauffeurId")
    Double calculerNoteMoyenne(Long chauffeurId);

    // Nombre de notations d'un chauffeur
    Long countByChauffeurId(Long chauffeurId);

    boolean existsByCourseId(Long courseId);
}