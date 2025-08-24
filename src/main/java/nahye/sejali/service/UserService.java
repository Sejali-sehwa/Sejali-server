package nahye.sejali.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nahye.sejali.config.JwtTokenProvider;
import nahye.sejali.dto.admin.AdminUserResponse;
import nahye.sejali.dto.auth.SignRequest;
import nahye.sejali.dto.user.UserProfileResponse;
import nahye.sejali.entity.User;
import nahye.sejali.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    // --- 토큰 무효화 (로그아웃) ---
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public UserProfileResponse getUser(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(()-> new IllegalArgumentException("유저 없음"));

        return new UserProfileResponse(
                user.getAuthLevel(),
                user.getStudentNum(),
                user.getUsername(),
                user.getUserId()
        );
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
                updatedUser.getAuthLevel(),
                updatedUser.getStudentNum(),
                updatedUser.getUsername(),
                updatedUser.getUserId()
        );
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

    public boolean deleteUser(Long userId, String currentUserId) {
        userRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        Optional<User> isExisting = userRepository.findById(userId);

        if(isExisting.isEmpty()){
            throw new IllegalArgumentException("해당 유저가 존재하지 않습니다.");
        }

        User user = isExisting.get();

        userRepository.delete(user);
        return true;
    }


}
