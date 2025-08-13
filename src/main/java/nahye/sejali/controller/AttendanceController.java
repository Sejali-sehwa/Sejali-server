package nahye.sejali.controller;

import lombok.RequiredArgsConstructor;
import nahye.sejali.dto.attendance.AttendanceCreateRequest;
import nahye.sejali.dto.attendance.AttendanceCreatedResponse;
import nahye.sejali.service.AttendanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/upload/{reservationId}")
    public ResponseEntity<?> uploadAttendance(@RequestBody AttendanceCreateRequest request, Authentication authentication, @PathVariable Long reservationId){
        try{
            String userId = authentication.getName();

            if(userId == null){
                return ResponseEntity.status(401).body("로그인 되지 않았습니다.");
            }

            if(request == null){
                return ResponseEntity.status(400).body("입력 오류 : request가 null입니다.");
            }

            AttendanceCreatedResponse response = attendanceService.uploadAttendance(userId, reservationId, request);
            return ResponseEntity.status(201).body(response);
        } catch (Exception e){
            logger.error("서버 오류 발생 : ", e);
            return ResponseEntity.status(500).body("서버 오류 발생 : "+e.getMessage());
        }
    }
}
