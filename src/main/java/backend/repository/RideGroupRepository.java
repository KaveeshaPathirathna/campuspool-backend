package backend.repository;

import backend.entity.RideGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RideGroupRepository extends JpaRepository<RideGroup, Long> {
}