package nahye.sejali.controller;

import lombok.RequiredArgsConstructor;
import nahye.sejali.dto.room.RoomCreatedResponse;
import nahye.sejali.dto.room.RoomRequest;
import nahye.sejali.dto.room.RoomResponse;
import nahye.sejali.dto.user.AdminUserResponse;
import nahye.sejali.service.RoomService;
import nahye.sejali.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;
    private final RoomService roomService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/all-users")
    //ADMIN 인 경우에만 요청 허용
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<AdminUserResponse> getAllUsers(){
        return userService.getAllUsers();
    }

    @PostMapping("/add-room")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> createRoom(@RequestBody RoomRequest request, Authentication authentication) {
        try{
            String userId = authentication.getName();
            RoomCreatedResponse response = roomService.createRoom(request, userId);
            return ResponseEntity.status(201).body(response);
        } catch(Exception e){
            logger.error("서버 오류 발생 : ", e);
            return ResponseEntity.status(500).body("서버 오류 발생 : " + e.getMessage());
        }

    }

    @DeleteMapping("/delete-room/{roomId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteRoom(@PathVariable Long roomId, Authentication authentication){
        try{
            String userId = authentication.getName();
            boolean isDeleted = roomService.deleteRoom(roomId, userId);

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

    @PutMapping("/udpate-room/{roomId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> updateRoom(@PathVariable Long roomId, Authentication authentication, RoomRequest request){
        try{
            String userId = authentication.getName();
            RoomCreatedResponse response = roomService.updateRoom(userId, roomId, request);

            return ResponseEntity.status(200).body(response);
        } catch (Exception e){
            logger.error("서버 오류 발생 : ", e);
            return ResponseEntity.status(500).body("서버 오류 발생 : " + e.getMessage());
        }
    }
}
