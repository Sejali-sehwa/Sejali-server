package nahye.sejali.dto.attendance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCreatedResponse {
    private Long id;
    private String attendanceImg;
    private boolean isVerified;
    private LocalDateTime createdAt;
}
