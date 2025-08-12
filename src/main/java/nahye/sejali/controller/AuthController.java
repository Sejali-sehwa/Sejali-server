package nahye.sejali.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import nahye.sejali.dto.auth.*;
import nahye.sejali.dto.user.*;
import nahye.sejali.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignRequest request){
        try {
            if (request == null) {
                logger.error("회원가입 실패: request가 null입니다. 요청: {}", request);
                return ResponseEntity.status(400).body("회원가입 실패: 입력 오류");
            }

            UserResponse response = authService.signup(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("회원가입 중 예외 발생", e);
            return ResponseEntity.status(500).body("회원가입 실패: " +e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request){
        System.out.println("컨트롤러 로직 들어옴");
        try{
            if (request == null) {
                return ResponseEntity.status(400).body("로그인 실패: 입력 오류");
            }

            TokenResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("로그인 중 예외 발생", e);
            return ResponseEntity.status(500).body("로그인 실패: " +e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request){
        try{
            if(request == null){
                return ResponseEntity.status(400).body("엑세스 토큰 발급 실패: 입력 오류");
            }

            TokenResponse tokenResponse = authService.getRefresh(request);

            return ResponseEntity.ok(tokenResponse);
        } catch (Exception e){
            logger.error("오류 : ",e);
            return ResponseEntity.status(500).body("엑세스 토큰 발급 실패"+e.getMessage());
        }

    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest accessRequest, @RequestBody RefreshRequest refreshRequest){
        String accessToken = accessRequest.getHeader("Authorization");
        String refreshToken = refreshRequest.getRefreshToken();

        if(accessToken == null || !accessToken.startsWith("Bearer ")){
            return ResponseEntity.status(401).body("Access Token이 누락되었거나 유효하지 않습니다.");
        }

        accessToken = accessToken.substring(7);

        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(400).body("Refresh Token이 누락되었습니다.");
        }

        try {
            authService.logout(accessToken, refreshToken);
            return ResponseEntity.status(200).body("로그아웃 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("서버 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/validate-passwd")
    public ResponseEntity<?> validatePasswd(@RequestBody PasswdRequest passwd, Authentication authentication){
        try{
            String userId = authentication.getName();
            boolean isValidated = authService.validatePasswd(userId, passwd);

            if(!isValidated){
                return ResponseEntity.status(200).body(false);
            }
            return ResponseEntity.status(200).body(true);
        } catch (Exception e){
            logger.error("서버 오류 발생 : ", e);
            return ResponseEntity.status(500).body("서버 오류 발생 : " + e.getMessage());
        }
    }
}
