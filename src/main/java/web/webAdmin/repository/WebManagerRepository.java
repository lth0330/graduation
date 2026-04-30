package web.webAdmin.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import web.webAdmin.entity.WebManagerEntity;

public interface WebManagerRepository extends JpaRepository<WebManagerEntity, Integer> {

    Optional<WebManagerEntity> findByLoginId(String loginId);
}
