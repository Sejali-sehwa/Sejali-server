package nahye.sejali.dto.reservation;

import jakarta.persistence.Column;
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
public class ReservationsByUserResponse {
    private String roomName;
    private Integer seatNum;
    private LocalDateTime startTime;
    private LocalTime duration;
    private LocalDateTime endTime;
    private Integer headcount;
    private String attendanceImg;
}
