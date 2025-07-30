package nahye.sejali.dto.room;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
public class ReservationCreatedResponse {
    private Long id;
    private String roomName;
    private int studentNum;
    private String username;
    private int seatNum;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalTime duration;
}
