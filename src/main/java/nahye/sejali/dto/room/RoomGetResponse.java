package nahye.sejali.dto.room;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RoomGetResponse {

    private String roomName;
    private int seats;
    private int remainingSeats;

}
