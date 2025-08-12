package nahye.sejali.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import nahye.sejali.dto.auth.SignRequest;
import nahye.sejali.dto.user.UserProfileResponse;
import nahye.sejali.entity.User;
import nahye.sejali.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @GetMapping
    public ResponseEntity<?> getUser(Authentication authentication){
        try{
            String userId = authentication.getName();
            UserProfileResponse response = userService.getUser(userId);
            return ResponseEntity.status(200).body(response);
        } catch (Exception e){
            logger.error("서버 오류 발생 : ", e);
            return ResponseEntity.status(500).body("서버 오류 발생 : " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(HttpServletRequest accessRequest, @RequestBody SignRequest request){
        try{
            String accessToken = accessRequest.getHeader("Authorization");
            if(accessToken == null || !accessToken.startsWith("Bearer ")){
                return new ResponseEntity<>("Access Token이 누락되었거나 유효하지 않습니다.", HttpStatus.UNAUTHORIZED);
            }

            if(request == null){
                logger.error("요청: {}", request);
                return ResponseEntity.status(400).body("프로필 수정 실패: 입력 오류");
            }

            accessToken = accessToken.substring(7);

            UserProfileResponse response = userService.updateUser(accessToken, request);

            if (response == null) {
                logger.error("결과가 null입니다. 요청: {}", request);
                return new ResponseEntity<>("서버 내부 오류", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e){
            logger.error("오류 : ",e);
            return new ResponseEntity<>("서버 내부 오류", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId, Authentication authentication){
        try{
            String currentUserId = authentication.getName();
            boolean isDeleted = userService.deleteUser(userId, currentUserId);

            if(isDeleted){
                return ResponseEntity.status(200).body("삭제되었습니다.");
            } else{
                return ResponseEntity.status(500).body("삭제 실패.");
            }

        } catch(Exception e){
            logger.error("서버 오류 발생 : ", e);
            return ResponseEntity.status(500).body("서버 오류 발생 : " + e.getMessage());
        }
    }
}
