package nahye.sejali.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "room_name")
    private String roomName;

    @Column(nullable = false)
    private int seats;

    @Column(nullable = false, name="remaining_seats")
    private int remainingSeats;

    @Builder.Default
    @OneToMany(mappedBy = "room")
    private List<Reservation> reservations = new ArrayList<>();
}
