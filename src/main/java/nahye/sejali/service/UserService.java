package nahye.sejali.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nahye.sejali.config.JwtTokenProvider;
import nahye.sejali.dto.user.*;
import nahye.sejali.entity.RefreshToken;
import nahye.sejali.entity.User;
import nahye.sejali.repository.RefreshTokenRepository;
import nahye.sejali.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;

    private static final long ACCESS_TOKEN_VALIDITY = 1000L * 60 * 60;         // 60분
    private static final long REFRESH_TOKEN_VALIDITY = 1000L * 60 * 60 * 24 * 7; // 7일

    // --- 회원가입 ---
    public UserResponse signup(SignRequest request) {
        if (userRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 사용자명입니다.");
        }

        User user = User.builder()
                .studentNum(request.getStudentNum())
                .username(request.getUsername())
                .userId(request.getUserId())
                .password(passwordEncoder.encode(request.getPassword()))
                .authLevel(request.getAuthLevel())
                .build();

        User saved = userRepository.save(user);

        return new UserResponse(
                saved.getId(),
                saved.getStudentNum(),
                saved.getUsername()
        );
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // Spring Security 인증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUserId(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 기존 세션 무효화를 위해 사용자 tokenVersion 증가
        user.incrementTokenVersion();
        userRepository.save(user); // 업데이트된 tokenVersion을 DB에 저장

        // 새로운 Access Token 생성 (현재 user의 tokenVersion 포함)
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getUserId(), ACCESS_TOKEN_VALIDITY, user.getTokenVersion());

        // 새로운 Refresh Token 생성 (현재 user의 tokenVersion 포함)
        String newRefreshTokenValue = jwtTokenProvider.createRefreshToken(
                user.getUserId(), REFRESH_TOKEN_VALIDITY, user.getTokenVersion());
        LocalDateTime newRefreshTokenExpiry = LocalDateTime.now().plusSeconds(REFRESH_TOKEN_VALIDITY / 1000); // 밀리초를 초로 변환

        refreshTokenRepository.deleteByUserId(user.getUserId());

        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(newRefreshTokenValue)
                .userId(user.getUserId())
                .expiryDate(newRefreshTokenExpiry)
                .build();
        refreshTokenRepository.save(newRefreshToken);

        return new TokenResponse(accessToken, newRefreshTokenValue);
    }

    // --- 토큰 갱신 ---
    @Transactional
    public TokenResponse getRefresh(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        // 1. Refresh Token의 형식/서명 유효성 검사 (JwtTokenProvider 내부에서 수행)
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않거나 변조된 Refresh Token입니다.");
        }

        String userIdFromRT = jwtTokenProvider.getUsername(refreshToken);
        Integer tokenVersionFromRT = jwtTokenProvider.getTokenVersion(refreshToken);

        // 2. Refresh Token과 연관된 사용자 정보 조회
        User user = userRepository.findByUserId(userIdFromRT)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userIdFromRT));

        if (tokenVersionFromRT == null || !user.getTokenVersion().equals(tokenVersionFromRT)) {
            // 버전 불일치: 이미 로그아웃되었거나 다른 기기에서 토큰이 갱신됨 (보안 상 재로그인 필요)
            throw new IllegalArgumentException("토큰 버전이 일치하지 않습니다. 다시 로그인해주세요.");
        }

        // 4. DB에 저장된 Refresh Token 정보 조회 (현재 요청 Refresh Token과 동일한지 확인)
        RefreshToken storedRefreshToken = refreshTokenRepository.findByUserId(user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Refresh Token이 DB에 존재하지 않습니다. 다시 로그인해주세요."));

        // 요청으로 들어온 Refresh Token이 DB에 저장된 현재 유효한 Refresh Token과 동일한지 확인
        if (!storedRefreshToken.getToken().equals(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        // 5. DB에 저장된 Refresh Token의 만료 여부 확인
        if (storedRefreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(storedRefreshToken); // 만료된 토큰 삭제
            throw new IllegalArgumentException("Refresh Token이 만료되었습니다. 다시 로그인해주세요.");
        }

        // 6. 새로운 Access Token 생성 (현재 user의 tokenVersion 포함)
        String newAccessToken = jwtTokenProvider.createAccessToken(
                user.getUserId(), ACCESS_TOKEN_VALIDITY, user.getTokenVersion());
        refreshTokenRepository.save(storedRefreshToken);

        return new TokenResponse(newAccessToken, refreshToken);
    }


    // --- 토큰 무효화 (로그아웃) ---
    @Transactional
    public void logout(String accessToken, String refreshToken) {
        // Access Token이 JWT이고 유효성 검사가 필요하다면 여기서 수행
        // 하지만 여기서는 DB 업데이트를 통한 무효화가 주 목적이므로 토큰 자체의 유효성은 크게 중요하지 않을 수 있음
        // 만약 Access Token에서 userId를 추출해야 한다면 validateToken 및 getUsername 사용

        String userId;
        try {
            userId = jwtTokenProvider.getUsername(accessToken); // Access Token에서 userId 추출
        } catch (Exception e) {
            // Access Token이 유효하지 않더라도, userId가 있다면 로그아웃 진행
            // 또는 유효한 Access Token이 없으면 로그아웃 불가 처리
            throw new IllegalArgumentException("유효하지 않은 Access Token입니다.");
        }

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        // 토큰 버전 증가
        user.incrementTokenVersion();
        userRepository.save(user);

        //Refresh Token 엔티티를 DB에서 삭제
        refreshTokenRepository.deleteByUserId(user.getUserId());
    }

    @Transactional
    public UserProfileResponse updateUser(String accessToken, SignRequest request) {
        String userId = jwtTokenProvider.getUsername(accessToken);
        User existingUser = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("로그인된 사용자를 찾을 수 없습니다."));

        if (String.valueOf(request.getStudentNum()).length() != 5 ||
                isNullOrEmpty(request.getUsername()) ||
                isNullOrEmpty(request.getPassword())) {
            throw new IllegalArgumentException("필수 요청 값이 누락되었습니다. (학번, 사용자 이름, 아이디, 비밀번호)");
        }

        userRepository.findByStudentNum(request.getStudentNum())
                .filter(user -> !user.getId().equals(existingUser.getId()))
                .ifPresent(user -> {
                    throw new IllegalArgumentException("입력하신 학번은 이미 다른 사용자가 사용 중입니다.");
                });

        existingUser.setStudentNum(request.getStudentNum());
        existingUser.setUsername(request.getUsername());
        existingUser.setPassword(passwordEncoder.encode(request.getPassword()));

        User updatedUser = userRepository.save(existingUser);
        return new UserProfileResponse(
                updatedUser.getStudentNum(),
                updatedUser.getUsername(),
                updatedUser.getUserId()
        );
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public List<AdminUserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> new AdminUserResponse(
                        user.getStudentNum(),
                        user.getUsername(),
                        user.getUserId()
                ))
                .collect(Collectors.toList());
    }
}