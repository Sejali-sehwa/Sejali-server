package nahye.sejali.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nahye.sejali.dto.attendance.AttendanceCreateRequest;
import nahye.sejali.dto.attendance.AttendanceCreatedResponse;
import nahye.sejali.entity.Attendance;
import nahye.sejali.entity.Reservation;
import nahye.sejali.entity.User;
import nahye.sejali.repository.AttendanceRepository;
import nahye.sejali.repository.ReservationRepository;
import nahye.sejali.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final ReservationRepository reservationRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;



    @Transactional
    public AttendanceCreatedResponse uploadAttendance(String userId, Long reservationId, AttendanceCreateRequest request) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약이 존재하지 않습니다."));

        if(user != reservation.getUser()){
            throw new IllegalArgumentException("로그인 한 유저와 예약한 유저가 일치하지 않습니다.");
        }

        if(!LocalDateTime.now().isAfter(reservation.getStartTime())){
            throw new IllegalArgumentException("실습실 예약 시간 이후에 작성하실 수 있습니다.");
        }

        Attendance attendance;

        // Reservation에 Attendance가 아직 없는 경우 (null)
        if (reservation.getAttendance() == null) {
            attendance = Attendance.builder()
                    .attendanceImg(request.getAttendanceImg())
                    .isVerified(false)
                    .reservation(reservation) // Attendance는 Reservation을 참조 (소유자)
                    .build();

            reservation.setAttendance(attendance);

            attendanceRepository.save(attendance);
        }
        // Reservation에 Attendance가 이미 존재하는 경우
        else {
            attendance = reservation.getAttendance();
            attendance.setAttendanceImg(request.getAttendanceImg());

            attendanceRepository.save(attendance);
        }

        // 4. 최종 Attendance 정보로 DTO 생성 및 반환
        return new AttendanceCreatedResponse(
                attendance.getId(),
                attendance.getAttendanceImg(),
                attendance.isVerified(),
                attendance.getCreatedAt()
        );
    }


    @Transactional
    public AttendanceCreatedResponse verifyAttendance(String userId, Long attendanceId) {
        userRepository.findByUserId(userId)
                .orElseThrow(()-> new IllegalArgumentException("유저가 존재하지 않습니다."));

        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(()-> new IllegalArgumentException("해당 출석은 업로드되지 않았습니다."));

        attendance.setVerified(true);
        attendanceRepository.save(attendance);

        return new AttendanceCreatedResponse(
                attendance.getId(),
                attendance.getAttendanceImg(),
                attendance.isVerified(),
                attendance.getCreatedAt()
        );
    }
}
