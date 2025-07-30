package nahye.sejali.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    // Refresh Token 관련 처리를 위한 서비스 (예: TokenService)를 주입받을 수 있습니다.
     private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Access Token 추출
        String accessToken = extractAccessToken(request);

        if (accessToken != null) {
            // 2. Access Token 유효성 검증 및 인증
            try {
                if (tokenProvider.validateToken(accessToken)) {
                    String userId = tokenProvider.getUsername(accessToken);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userId);


                     Integer tokenVersion = tokenProvider.getTokenVersion(accessToken);
                     if (!tokenService.isTokenVersionValid(userId, tokenVersion)) {
                        throw new IllegalArgumentException("토큰 버전이 일치하지 않습니다.");
                     }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {

                 response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                 return;
            }
        }

        // 다음 필터 체인으로 진행
        filterChain.doFilter(request, response);
    }

    private String extractAccessToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}