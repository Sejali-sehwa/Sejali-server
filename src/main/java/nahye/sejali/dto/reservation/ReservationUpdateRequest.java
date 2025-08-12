package nahye.sejali.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationUpdateRequest {
    private String newRoomName;
    private Integer newSeatNum;
    private LocalDateTime newStartTime;
    private LocalTime newDuration;
}
