package nahye.sejali.dto.room;

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
