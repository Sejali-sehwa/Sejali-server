package nahye.sejali.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import nahye.sejali.enums.AuthLevel;

@Getter
@Setter
@AllArgsConstructor
public class UserProfileResponse {
    private AuthLevel authLevel;
    private int studentNum;
    private String username;
    private String userId;
}
