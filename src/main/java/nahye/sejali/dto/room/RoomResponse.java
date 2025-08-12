package nahye.sejali.dto.room;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nahye.sejali.entity.Reservation;
import nahye.sejali.entity.Room;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoomResponse {
    private Long id;
    private String roomName;
    private int seats;
    private int remainingSeats;
    private List<Reservation> reservations;

}
