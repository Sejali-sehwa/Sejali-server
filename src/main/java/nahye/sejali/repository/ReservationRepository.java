package nahye.sejali.repository;


import nahye.sejali.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
//    List<Reservation> findByRoom_RoomName(String roomName);
}
