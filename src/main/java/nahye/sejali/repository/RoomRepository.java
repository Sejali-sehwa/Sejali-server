package nahye.sejali.repository;

import nahye.sejali.entity.Room;
import org.apache.http.HttpEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Room findByRoomName(String roomName);
}
