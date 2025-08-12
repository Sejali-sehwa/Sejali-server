package nahye.sejali.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReservationResponse {
    private Integer seatNum;
    private Integer studentNum;
    private String username;
    private Integer headCount;
}
