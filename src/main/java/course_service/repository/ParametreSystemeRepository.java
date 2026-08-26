package course_service.repository;

import course_service.entity.ParametreSysteme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParametreSystemeRepository extends JpaRepository<ParametreSysteme, String> {
}