package nahye.sejali.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ReservationRequest {
    private String roomName;
    private int studentNum;
    private String username;
    private int seatNum;
    private LocalDateTime startTime;
    private LocalTime duration;
}
