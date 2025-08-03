package nahye.sejali.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReservationsByRoomNameResponse {
    private String roomName;
    private int remainingSeats;
    private List<ReservationResponse> reservationDto;
}
