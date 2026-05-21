package app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import app.entity.WaitingListEntity;

public interface WaitingListRepository extends JpaRepository<WaitingListEntity, Integer> {
}
