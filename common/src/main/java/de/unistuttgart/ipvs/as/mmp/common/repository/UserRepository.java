package de.unistuttgart.ipvs.as.mmp.common.repository;

import de.unistuttgart.ipvs.as.mmp.common.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByName(String name);
}