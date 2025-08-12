package nahye.sejali.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nahye.sejali.dto.reservation.*;
import nahye.sejali.entity.Reservation;
import nahye.sejali.entity.Room;
import nahye.sejali.entity.User;
import nahye.sejali.enums.AuthLevel;
import nahye.sejali.repository.ReservationRepository;
import nahye.sejali.repository.RoomRepository;
import nahye.sejali.repository.UserRepository;
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
        if(room == null){
            throw new IllegalArgumentException("해당 실습실을 찾을 수 없습니다.");
        }

        List<ReservationResponse> reservationDto = room.getReservations().stream()
                .map(reservation -> {
                    User user = reservation.getUser();
                    return new ReservationResponse(
                            reservation.getSeatNum(),
                            user.getStudentNum(),
                            user.getUsername(),
                            reservation.getHeadcount()
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

    public ReservationsByUserResponse getAllReservationsByUser(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(()-> new IllegalArgumentException("유저가 없습니다."));

        if(user.getAuthLevel() == AuthLevel.ADMIN){
            throw new IllegalArgumentException("관리자는 접근 권한이 없습니다.");
        }

        Reservation reservation = reservationRepository.findByUser(user);

        if(reservation == null){
            return null;
        }

        return new ReservationsByUserResponse(
                reservation.getRoom().getRoomName(),
                reservation.getSeatNum(),
                reservation.getStartTime(),
                reservation.getDuration(),
                reservation.getEndTime(),
                reservation.getHeadcount()
        );
    }

    public ReservationsByUserResponse getUsersReservations(Long userId, String adminId) {
        userRepository.findByUserId(adminId)
                .orElseThrow(()-> new IllegalArgumentException("유저 없음"));

        Optional<User> user = userRepository.findById(userId);

        if(user.isEmpty()){
            throw new IllegalArgumentException("해당 유저가 존재하지 않습니다.");
        }

        Reservation reservation = reservationRepository.findByUser(user.get());

        if(reservation == null){
            return null;
        }

        return new ReservationsByUserResponse(
                reservation.getRoom().getRoomName(),
                reservation.getSeatNum(),
                reservation.getStartTime(),
                reservation.getDuration(),
                reservation.getEndTime(),
                reservation.getHeadcount()
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

        possibleReservationException(request.getSeatNum(), request.getRoomName(), null, request.getDuration(), endTime, request.getStartTime());

        Reservation reservation = Reservation.builder()
                .seatNum(request.getSeatNum())
                .startTime(request.getStartTime())
                .endTime(endTime)
                .duration(request.getDuration())
                .headcount(request.getHeadCount())
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
                savedReservation.getDuration(),
                savedReservation.getHeadcount()
        );
    }



    public boolean deleteReservation(String userId, Long reservationId) {
        Optional<Reservation> optionalReservation = reservationRepository.findById(reservationId);

        if(optionalReservation.isEmpty()){
            throw new IllegalArgumentException("해당 예약이 존재하지 않습니다.");
        }

        Reservation reservation = optionalReservation.get();

        isAdminOrUserException(userId, reservation.getUser().getId());

        reservationRepository.delete(reservation);

        return true;
    }


    @Transactional
    public ReservationCreatedResponse updateReservation(String userId, Long reservationId, ReservationUpdateRequest request) {
        Optional<Reservation> optionalReservation = reservationRepository.findById(reservationId);

        if(optionalReservation.isEmpty()){
            throw new IllegalArgumentException("해당 예약이 존재하지 않습니다.");
        }

        Reservation reservation = optionalReservation.get();

        isAdminOrUserException(userId, reservation.getUser().getId());

        Room room = roomRepository.findByRoomName(request.getNewRoomName());
        if(room == null){
            throw new IllegalArgumentException("해당 실습실을 찾을 수 없습니다.");
        }

        LocalDateTime newEndTime = calculateEndTime(request.getNewStartTime(),request.getNewDuration());

        possibleReservationException(request.getNewSeatNum(), request.getNewRoomName(), reservation.getId(), request.getNewDuration(), newEndTime, request.getNewStartTime());

        reservation.setRoom(room);
        reservation.setSeatNum(request.getNewSeatNum());
        reservation.setStartTime(request.getNewStartTime());
        reservation.setDuration(request.getNewDuration());
        reservation.setEndTime(newEndTime);

        Reservation updatedReservation = reservationRepository.save(reservation);
        return new ReservationCreatedResponse(
                updatedReservation.getId(),
                updatedReservation.getRoom().getRoomName(),
                updatedReservation.getUser().getStudentNum(),
                updatedReservation.getUser().getUsername(),
                updatedReservation.getSeatNum(),
                updatedReservation.getStartTime(),
                updatedReservation.getEndTime(),
                updatedReservation.getDuration(),
                updatedReservation.getHeadcount()
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

    private void possibleReservationException(Integer seatNum, String roomName, Long reservationId, LocalTime duration, LocalDateTime endTime, LocalDateTime startTime){
        List<Reservation> existingReservations = reservationRepository.findBySeatNumAndRoom_RoomName(seatNum, roomName);

        int changedDuration = duration.getMinute();
        if (changedDuration % 30 != 0 || duration.getHour() * 60 + duration.getMinute() > 180) {
            throw new IllegalArgumentException("이용 시간은 30분 단위, 최대 3시간입니다.");
        }

        if(!existingReservations.isEmpty()){
            for (Reservation existingReservation : existingReservations) {
                // 현재 변경하려는 예약 자신은 제외
                if (existingReservation.getId().equals(reservationId)){
                    continue;
                }

                // 1. 기존 예약 시작 시간 <= 새로운 예약 시작 시간 < 기존 예약 종료 시간
                // 2. 새로운 예약 시작 시간 <= 기존 예약 시작 시간 < 새로운 예약 종료 시간
                if (!(endTime.isBefore(existingReservation.getStartTime()) || startTime.isAfter(existingReservation.getEndTime()))) {
                    throw new IllegalArgumentException("해당 시간에는 예약할 수 없습니다.");
                }
            }
        }
    }

    private void isAdminOrUserException(String userId, Long reservationUserId){
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음 : " +userId));

        if(user.getAuthLevel() != AuthLevel.ADMIN && !Objects.equals(user.getId(), reservationUserId)){
            throw new IllegalArgumentException("관리자가 아니거나 예약한 사용자가 아닙니다.");
        }
    }


}
