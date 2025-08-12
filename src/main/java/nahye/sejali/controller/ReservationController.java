package nahye.sejali.controller;

import lombok.RequiredArgsConstructor;
import nahye.sejali.dto.reservation.*;
import nahye.sejali.dto.room.RoomNameRequest;
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
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/by-room")
    public ResponseEntity<?> getAllReservationsByRoomName(@RequestBody RoomNameRequest request){
        try{
            if(request == null){
                return ResponseEntity.status(400).body("입력 오류");
            }

            String roomName = request.getRoomName();

            ReservationsByRoomNameResponse response = reservationService.getAllReservationsByRoomName(roomName);
            return ResponseEntity.ok(response);
        } catch (Exception e){
            logger.error("서버 오류 발생 : ", e);
            return ResponseEntity.status(500).body("서버 오류 발생 : " + e.getMessage());
        }
    }

    @GetMapping("/by-user")
    public ResponseEntity<?> getAllReservationsByUser(Authentication authentication){
        try{
            String userId = authentication.getName();
            ReservationsByUserResponse response = reservationService.getAllReservationsByUser(userId);

            return ResponseEntity.status(200).body(response);
        } catch (Exception e){
            logger.error("서버 오류 발생 : ", e);
            return ResponseEntity.status(500).body("서버 오류 발생 : " + e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createReservation(Authentication authentication, @RequestBody ReservationRequest request){
        try{
            String userId = authentication.getName();

            if(userId == null){
                return ResponseEntity.status(403).body("로그인해주세요.");
            }
            ReservationCreatedResponse response = reservationService.createReservation(userId, request);
            return ResponseEntity.status(201).body(response);
        } catch(Exception e){
            logger.error("서버 오류 발생 : ", e);
            return ResponseEntity.status(500).body("서버 오류 발생" + e.getMessage());
        }

    }

    @DeleteMapping("/delete/{reservationId}")
    public ResponseEntity<?> deleteReservation(Authentication authentication, @PathVariable Long reservationId){
        try{
            String userId = authentication.getName();

            if(userId == null){
                return ResponseEntity.status(403).body("로그인해주세요.");
            }

            boolean isDeleted = reservationService.deleteReservation(userId, reservationId);
            if(isDeleted){
                return ResponseEntity.status(200).body("삭제되었습니다.");
            } else{
                return ResponseEntity.status(500).body("삭제 실패하였습니다.");
            }
        } catch (Exception e){
            logger.error("서버 내부 오류 발생 : ",e);
            return ResponseEntity.status(500).body("서버 오류 발생" + e.getMessage());
        }

    }


    @PutMapping("/update/{reservationId}")
    public ResponseEntity<?> updateReservation(Authentication authentication, @PathVariable Long reservationId, @RequestBody ReservationUpdateRequest request){
        try{
            String userId = authentication.getName();

            if(userId == null){
                return ResponseEntity.status(401).body("로그인해주세요.");
            }

            ReservationCreatedResponse response = reservationService.updateReservation(userId, reservationId, request);
            return ResponseEntity.status(200).body(response);
        } catch (Exception e){
            logger.error("서버 내부 오류 발생 : ", e);
            return ResponseEntity.status(500).body("서버 오류 발생" + e.getMessage());
        }
    }
}
