package nahye.sejali.repository;

import nahye.sejali.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String userId);

    Optional<User> findByStudentNum(int studentNum);

//    @Query("SELECT r.user FROM reservation r WHERE r.id = :reservationId")
//    User findByReservationId(@Param("reservationId") Long reservationId);
}