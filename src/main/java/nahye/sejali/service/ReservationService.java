package nahye.sejali.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nahye.sejali.dto.reservation.ReservationCreatedResponse;
import nahye.sejali.dto.reservation.ReservationRequest;
import nahye.sejali.dto.reservation.ReservationResponse;
import nahye.sejali.dto.reservation.ReservationsByRoomNameResponse;
import nahye.sejali.entity.Reservation;
import nahye.sejali.entity.Room;
import nahye.sejali.entity.User;
import nahye.sejali.repository.ReservationRepository;
import nahye.sejali.repository.RoomRepository;
import nahye.sejali.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public ReservationsByRoomNameResponse getAllReservationsByRoomName(String roomName) {
        System.out.println("roomName: " + roomName);

        Room room = roomRepository.findByRoomNameIgnoreCase(roomName);
        if (room == null) {
            throw new IllegalArgumentException("해당 실습실을 찾을 수 없습니다.");
        }

        List<ReservationResponse> reservationDto = room.getReservations().stream()
                .map(reservation -> {
                    User user = reservation.getUser();
                    return new ReservationResponse(
                            reservation.getSeatNum(),
                            user.getStudentNum(),
                            user.getUsername()
                    );
                })
                .collect(Collectors.toList());

        if(reservationDto.isEmpty()){
            return null;
        }

        return new ReservationsByRoomNameResponse(
                room.getRoomName(),
                room.getRemainingSeats(),
                reservationDto
        );
    }


    @Transactional // 트랜잭션 관리
    public ReservationCreatedResponse createReservation(String roomName, String userId, ReservationRequest request) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + userId));

        if (user.getStudentNum() != request.getStudentNum()) {
            throw new IllegalArgumentException("유저의 학번이 일치하지 않습니다.");
        }

        Room room = roomRepository.findByRoomNameIgnoreCase(roomName);

        if(room == null){
            throw new IllegalArgumentException("존재하지 않는 방 이름입니다: " + roomName);
        }

        LocalDateTime endTime = calculateEndTime(request.getStartTime(), request.getDuration());

        Reservation reservation = Reservation.builder()
                .seatNum(request.getSeatNum())
                .startTime(request.getStartTime())
                .endTime(endTime)
                .duration(request.getDuration())
                .user(user) // User 엔티티 연결
                .room(room) // Room 엔티티 연결
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        return new ReservationCreatedResponse(
                savedReservation.getId(),
                savedReservation.getRoom().getRoomName(),
                savedReservation.getUser().getStudentNum(),
                savedReservation.getUser().getUsername(),
                savedReservation.getSeatNum(),
                savedReservation.getStartTime(),
                savedReservation.getEndTime(),
                savedReservation.getDuration()
        );
    }

    private LocalDateTime calculateEndTime(LocalDateTime startTime, LocalTime duration){
        return startTime.plusMinutes(duration.getMinute())
                .plusHours(duration.getHour());
    }
}
