package nahye.sejali.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import nahye.sejali.enums.AuthLevel;

@Getter
@Setter
@AllArgsConstructor
public class SignRequest {
    private int studentNum;
    private String username;
    private String userId;
    private String password;
}
