package nahye.sejali.repository;


import nahye.sejali.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByUserId(String userId);

    void deleteByUserId(String userId);

    Optional<RefreshToken> findByToken(String token);
}
