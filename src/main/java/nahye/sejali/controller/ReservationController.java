package nahye.sejali.controller;

import lombok.RequiredArgsConstructor;
import nahye.sejali.dto.reservation.ReservationCreatedResponse;
import nahye.sejali.dto.reservation.ReservationRequest;
import nahye.sejali.dto.reservation.ReservationsByRoomNameResponse;
import nahye.sejali.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservation")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping
    public ResponseEntity<?> getAllReservationsByRoomName(@RequestBody String roomName){
        try{
            if(roomName == null || roomName.isEmpty()){
                return ResponseEntity.status(500).body("입력 오류");
            }
            ReservationsByRoomNameResponse response = reservationService.getAllReservationsByRoomName(roomName);
            return ResponseEntity.ok(response);
        } catch (Exception e){
            logger.error("서버 오류 발생 : ", e);
            return ResponseEntity.status(500).body("서버 오류 발생 : " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody String roomName, Authentication authentication, @RequestBody ReservationRequest request){
        try{
            String userId = authentication.getName();
            ReservationCreatedResponse response = reservationService.createReservation(roomName, userId, request);
            return ResponseEntity.status(201).body(response);
        } catch(Exception e){
            logger.error("서버 오류 발생 : ", e);
            return ResponseEntity.status(500).body("서버 오류 발생" + e.getMessage());
        }

    }
}
