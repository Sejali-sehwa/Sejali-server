package nahye.sejali.service;

import lombok.RequiredArgsConstructor;
import nahye.sejali.dto.room.RoomGetResponse;
import nahye.sejali.dto.room.RoomNameGetResponse;
import nahye.sejali.dto.room.RoomRequest;
import nahye.sejali.dto.room.RoomResponse;
import nahye.sejali.entity.Room;
import nahye.sejali.repository.RoomRepository;
import nahye.sejali.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
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
                        room.getRemainingSeats(),
                        room.getRoomImg()
                ))
                .collect(Collectors.toList());
    }

    public RoomResponse createRoom(RoomRequest request, String userId) {

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
                .roomImg(request.getRoomImg())
                .build();

        Room newRoom = roomRepository.save(room);
        return new RoomResponse(newRoom);
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
}
