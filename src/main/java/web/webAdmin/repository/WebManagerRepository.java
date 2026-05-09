package web.webAdmin.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.webAdmin.entity.WebManagerEntity;

public interface WebManagerRepository extends JpaRepository<WebManagerEntity, Integer> {

    @Query("select w from WebManagerEntity w where w.wId = :wId")
    Optional<WebManagerEntity> findByWId(@Param("wId") String wId);
}
