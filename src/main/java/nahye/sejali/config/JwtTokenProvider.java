package nahye.sejali.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private final Key key; // 시크릿 키

    // JWT 시크릿 키를 application.properties에서 주입
    // HMAC SHA 키를 생성
    public JwtTokenProvider(@Value("${spring.jwt.secret}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // 토큰 생성 :
    // .signWith(key, SignatureAlgorithm.HS256)를 사용하여 JWT에 서명
    // HS256 : HMAC SHA256 알고리즘

    // --- Access Token 생성 ---
    // Access Token은 짧은 만료 시간을 가지며, 'tokenVersion' 클레임을 포함합니다.
    public String createAccessToken(String userId, long accessTokenValidity, Integer tokenVersion) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidity);

        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenVersion", tokenVersion); // tokenVersion 클레임 추가

        return Jwts.builder()
                .setClaims(claims) // 클레임 설정
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // --- Refresh Token 생성 ---
    // Refresh Token은 긴 만료 시간을 가지며, 'tokenVersion' 클레임을 포함합니다.
    public String createRefreshToken(String userId, long refreshTokenValidity, Integer tokenVersion) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenValidity);

        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenVersion", tokenVersion); // tokenVersion 클레임 추가

        return Jwts.builder()
                .setClaims(claims) // 클레임 설정
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // --- 토큰 유효성 검증 ---
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // 토큰 파싱 실패 (예: 서명 불일치, 만료된 토큰 등)
            // System.err.println("Token validation error: " + e.getMessage()); // 디버깅용
            return false;
        }
    }

    // --- 토큰에서 사용자 ID 추출 ---
    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // --- 모든 클레임 추출 (내부 사용) ---
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody(); // `key`로 통일
    }

    // --- 토큰에서 tokenVersion 추출 ---
    // 이 메서드는 Access Token과 Refresh Token 모두에서 tokenVersion을 추출할 때 사용됩니다.
    public Integer getTokenVersion(String token) {
        try {
            Claims claims = extractAllClaims(token);
            // Integer 타입으로 캐스팅하여 반환
            return claims.get("tokenVersion", Integer.class);
        } catch (Exception e) {
            // 토큰 파싱 실패 또는 tokenVersion 클레임이 없는 경우
            // System.err.println("Error extracting token version: " + e.getMessage()); // 디버깅용
            return null; // 클레임이 없거나 오류 발생 시 null 반환
        }
    }

    // --- 토큰에서 만료 시간 추출 ---
    public Date getExpirationDateFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }

}