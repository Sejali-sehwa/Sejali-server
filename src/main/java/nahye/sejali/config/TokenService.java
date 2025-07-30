package nahye.sejali.config;

import lombok.RequiredArgsConstructor;
import nahye.sejali.dto.user.TokenResponse;
import nahye.sejali.entity.RefreshToken;
import nahye.sejali.entity.User;
import nahye.sejali.repository.RefreshTokenRepository;
import nahye.sejali.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private static final long ACCESS_TOKEN_VALIDITY = 1000L * 60 * 60;         // 60분
    private static final long REFRESH_TOKEN_VALIDITY = 1000L * 60 * 60 * 24 * 7; // 7일


    // --- 1. 성공적인 로그인 후 토큰 발급 ---
    @Transactional
    public TokenResponse issueTokens(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // 사용자의 tokenVersion을 증가시킵니다. (로그인 시 이전 토큰들을 무효화하는 데 중요)
        user.incrementTokenVersion(); // User 엔티티에 tokenVersion 필드 및 증가 메서드 가정
        userRepository.save(user); // 업데이트된 tokenVersion을 DB에 저장

        // 새로운 Access Token 생성 (tokenVersion 포함)
        String accessToken = jwtTokenProvider.createAccessToken(userId, ACCESS_TOKEN_VALIDITY, user.getTokenVersion());

        // 새로운 Refresh Token 생성
        String newRefreshTokenValue = jwtTokenProvider.createRefreshToken(userId, REFRESH_TOKEN_VALIDITY, user.getTokenVersion());
        LocalDateTime newRefreshTokenExpiry = LocalDateTime.now().plusSeconds(REFRESH_TOKEN_VALIDITY);

        // DB에 Refresh Token 저장 또는 업데이트 (Rotation)
        Optional<RefreshToken> existingRefreshToken = refreshTokenRepository.findByUserId(user.getUserId());
        if (existingRefreshToken.isPresent()) {
            // 기존 Refresh Token이 있으면 업데이트 (갱신)
            RefreshToken refreshToken = existingRefreshToken.get();
            refreshTokenRepository.save(refreshToken);
        } else {
            // 없으면 새로운 Refresh Token 생성
            RefreshToken newRefreshToken = RefreshToken.builder()
                    .userId(user.getUserId())
                    .token(newRefreshTokenValue)
                    .expiryDate(newRefreshTokenExpiry)
                    .build();
            refreshTokenRepository.save(newRefreshToken);
        }

        return new TokenResponse(accessToken, newRefreshTokenValue);
    }

    // --- 2. 토큰 갱신 (Refresh) ---
    @Transactional
    public TokenResponse refreshTokens(String refreshToken) {
        // 1. 기존 Refresh Token의 형식/서명 유효성 검사 (JWT 기반이라면)
        // Refresh Token이 단순히 UUID라면 이 JWT 유효성 검사는 건너뜁니다.
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 Refresh Token 형식입니다.");
        }

        // 2. 데이터베이스에서 Refresh Token 조회
        RefreshToken storedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh Token을 찾을 수 없거나 이미 무효화되었습니다."));

        // 3. DB에 저장된 Refresh Token의 만료 여부 확인
        if (storedRefreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(storedRefreshToken); // 만료된 토큰 삭제 (무효화)
            throw new IllegalArgumentException("Refresh Token이 만료되었습니다.");
        }

        // 4. Refresh Token과 연관된 사용자 정보 조회
        User user = userRepository.findByUserId(storedRefreshToken.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("사용자가 없음"));

         Integer tokenVersion = jwtTokenProvider.getTokenVersion(refreshToken); // Refresh Token이 JWT인 경우
         if (!user.getTokenVersion().equals(tokenVersion)) {
            throw new IllegalArgumentException("토큰 버전 불일치. 다시 로그인해주세요.");
         }


        // 6. 새로운 Access Token 생성 (현재 tokenVersion 포함)
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getUserId(), ACCESS_TOKEN_VALIDITY, user.getTokenVersion());

        refreshTokenRepository.save(storedRefreshToken);

        return new TokenResponse(newAccessToken, refreshToken);
    }

    // --- 3. 토큰 무효화 (로그아웃) ---
    @Transactional
    public void invalidateTokens(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // DB에서 해당 사용자의 Refresh Token 삭제하여 무효화
        refreshTokenRepository.deleteByUserId(user.getUserId());

        user.incrementTokenVersion();
        userRepository.save(user);
    }

    // 토큰 버전이 일치하는지 확인
    public boolean isTokenVersionValid(String userId, Integer tokenVersionToValidate) {
        // 1. 사용자 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        // 2. DB에 저장된 사용자의 현재 토큰 버전 가져오기
        Integer currentUserTokenVersion = user.getTokenVersion();

        return tokenVersionToValidate != null && tokenVersionToValidate.equals(currentUserTokenVersion);
    }

}