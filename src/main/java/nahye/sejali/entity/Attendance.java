package nahye.sejali.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "attendances")
@EntityListeners(AuditingEntityListener.class)
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attendance_img")
    private String attendanceImg;

    @Column(name = "is_verified")
    private boolean isVerified;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id") // 외래 키 컬럼 지정
    private Reservation reservation;

    @Column(name = "created_at")
    @CreatedDate
    private LocalDateTime createdAt;
}
