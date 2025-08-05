package nahye.sejali.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReservationResponse {
    private int seatNum;
    private int studentNum;
    private String username;
}
