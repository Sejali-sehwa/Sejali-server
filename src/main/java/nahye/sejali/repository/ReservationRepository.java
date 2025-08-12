package nahye.sejali.repository;


import nahye.sejali.entity.Reservation;
import nahye.sejali.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findBySeatNumAndRoom_RoomName(Integer seatNum, String roomName);

    List<Reservation> findByEndTimeLessThanEqual(LocalDateTime now);

    Reservation findByUser(User user);
}
