package nahye.sejali.entity;

import jakarta.persistence.*;
import lombok.*;
import nahye.sejali.enums.AuthLevel;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 5, name = "student_num")
    private int studentNum;

    @Column(nullable = false, name = "user_name")
    private String username;

    @Column(nullable = false, unique = true, name = "user_id")
    private String userId;

    @Column(nullable = false)
    private String password;

    @Column(name="auth_level", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthLevel authLevel;

    @Builder.Default
    @Column(name="token_version", nullable = false)
    private Integer tokenVersion=0;

    @Column(name = "created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<Reservation> reservations = new ArrayList<>();


    public void incrementTokenVersion() {
        this.tokenVersion++;
    }
}
