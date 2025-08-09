package nahye.sejali.dto.reservation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank(message = "실습실 이름은 필수 항목입니다.")
    private String roomName;
    @NotNull(message = "학번은 필수 항목입니다.")
    private Integer studentNum;
    @NotBlank(message = "유저 이름은 필수 항목입니다.")
    private String username;
    @NotNull(message = "자리 번호는 필수 항목입니다.")
    private Integer seatNum;
    @NotNull(message = "시작 시간은 필수 항목입니다.")
    private LocalDateTime startTime;
    @NotNull(message = "시간은 필수 항목입니다.")
    private LocalTime duration;
}
