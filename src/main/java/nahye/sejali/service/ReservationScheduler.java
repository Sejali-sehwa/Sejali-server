package nahye.sejali.service;

import lombok.RequiredArgsConstructor;
import nahye.sejali.entity.Reservation;
import nahye.sejali.repository.ReservationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;

    @Scheduled(cron = "0 * * * * *") // 매분 0초에 실행
    public void deleteExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        // 종료 시간이 현재 시간보다 이전이거나 같은 모든 예약을 조회
        List<Reservation> expiredReservations = reservationRepository.findByEndTimeLessThanEqual(now);

        if (!expiredReservations.isEmpty()) {
            reservationRepository.deleteAll(expiredReservations);
        }
    }
}
