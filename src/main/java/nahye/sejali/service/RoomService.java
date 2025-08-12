package nahye.sejali.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nahye.sejali.dto.reservation.ReservationCreatedResponse;
import nahye.sejali.dto.reservation.ReservationUpdateRequest;
import nahye.sejali.dto.room.*;
import nahye.sejali.entity.Reservation;
import nahye.sejali.entity.Room;
import nahye.sejali.entity.User;
import nahye.sejali.enums.AuthLevel;
import nahye.sejali.repository.RoomRepository;
import nahye.sejali.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public List<RoomGetResponse> getAllRooms() {
        List<Room> rooms = roomRepository.findAll();

        return rooms.stream()
                .map(room -> new RoomGetResponse(
                        room.getRoomName(),
                        room.getSeats(),
                        room.getRemainingSeats()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomCreatedResponse createRoom(RoomRequest request, String userId) {

        userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        Room isExisting = roomRepository.findByRoomName(request.getRoomName());

        if(isExisting != null){
            throw new IllegalArgumentException("이미 존재하는 실습실입니다.");
        }

        Room room = Room.builder()
                .roomName(request.getRoomName())
                .seats(request.getSeats())
                .remainingSeats(request.getSeats())
                .build();

        Room newRoom = roomRepository.save(room);
        return new RoomCreatedResponse(
                newRoom.getId(),
                newRoom.getRoomName(),
                newRoom.getSeats()
        );
    }

    public List<RoomNameGetResponse> getAllRoomNames(){
        List<Room> rooms = roomRepository.findAll();

        return rooms.stream()
                .map(room -> new RoomNameGetResponse(
                        room.getId(),
                        room.getRoomName()
                ))
                .collect(Collectors.toList());
    }

    public boolean deleteRoom(Long roomId, String userId) {
        userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        Optional<Room> isExisting = roomRepository.findById(roomId);

        if(isExisting.isEmpty()){
            throw new IllegalArgumentException("존재하지 않는 실습실입니다.");
        }

        Room room = isExisting.get();

        roomRepository.delete(room);

        return true;
    }

    @Transactional
    public RoomCreatedResponse updateRoom(String userId, Long roomId, RoomRequest request) {
        Optional<Room> optionalRoom = roomRepository.findById(roomId);

        if(optionalRoom.isEmpty()){
            throw new IllegalArgumentException("해당 실습실이 존재하지 않습니다.");
        }

        Room room = optionalRoom.get();

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음 : " +userId));

        if(user.getAuthLevel() != AuthLevel.ADMIN){
            throw new IllegalArgumentException("관리자가 아닙니다.");
        }

        room.setRoomName(request.getRoomName());
        room.setSeats(request.getSeats());

        Room newRoom = roomRepository.save(room);
        return new RoomCreatedResponse(
                newRoom.getId(),
                newRoom.getRoomName(),
                newRoom.getSeats()
        );
    }

}
