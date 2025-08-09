package nahye.sejali.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nahye.sejali.dto.reservation.ReservationCreatedResponse;
import nahye.sejali.dto.reservation.ReservationRequest;
import nahye.sejali.dto.reservation.ReservationResponse;
import nahye.sejali.dto.reservation.ReservationsByRoomNameResponse;
import nahye.sejali.dto.room.RoomNameRequest;
import nahye.sejali.entity.Reservation;
import nahye.sejali.entity.Room;
import nahye.sejali.entity.User;
import nahye.sejali.enums.AuthLevel;
import nahye.sejali.repository.ReservationRepository;
import nahye.sejali.repository.RoomRepository;
import nahye.sejali.repository.UserRepository;
import org.apache.http.HttpEntity;
import org.hibernate.service.NullServiceException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public ReservationsByRoomNameResponse getAllReservationsByRoomName(String roomName) {
        Room room = roomRepository.findByRoomName(roomName);
        System.out.println(roomName);
        if(room == null){
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
    public ReservationCreatedResponse createReservation( String userId, ReservationRequest request) {

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + userId));

        if (user.getStudentNum() != request.getStudentNum()) {
            throw new IllegalArgumentException("유저의 학번이 일치하지 않습니다.");
        }
        Room room = roomRepository.findByRoomName(request.getRoomName());

        if(room == null){
            throw new IllegalArgumentException("해당 실습실이 없습니다.");
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
        try{
            return startTime.plusMinutes(duration.getMinute())
                    .plusHours(duration.getHour());
        } catch (Exception e){
            throw new IllegalArgumentException("끝 시간 변환에 실패하였습니다.");
        }

    }

    public boolean deleteReservation(String userId, Long reservationId) {
        Optional<Reservation> optionalReservation = reservationRepository.findById(reservationId);

        if(optionalReservation.isEmpty()){
            throw new IllegalArgumentException("해당 예약이 존재하지 않습니다.");
        }

        Reservation reservation = optionalReservation.get();

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + userId));

        if(user.getAuthLevel() != AuthLevel.ADMIN && !Objects.equals(user.getId(), reservation.getUser().getId())){
            throw new IllegalArgumentException("관리자가 아니거나 예약한 사용자가 아닙니다.");
        }

        reservationRepository.delete(reservation);

        return true;
    }
}
