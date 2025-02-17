package az.example.online.shopping.infrastructure.web.service.concretes;

import az.example.online.shopping.infrastructure.dataaccess.entity.UserEntity;
import az.example.online.shopping.infrastructure.dataaccess.repository.UserRepository;
import az.example.online.shopping.infrastructure.web.dto.response.AuthResponseModel;
import az.example.online.shopping.infrastructure.web.service.abstracts.AbstractJwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService implements AbstractJwtService {
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration.access}")
    private long accessTokenExpiration;

    @Value("${jwt.expiration.refresh}")
    private long refreshTokenExpiration;

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Map<String, Object> claims, String subject, boolean isAccessToken) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        + (isAccessToken ? accessTokenExpiration : refreshTokenExpiration)))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateAccessToken(String subject) {
        return generateToken(Map.of("type", "ACCESS"), subject, true);
    }

    public String generateRefreshToken(String subject) {
        return generateToken(Map.of("type", "REFRESH"), subject, false);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUser(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public UserDetails extractUser(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        String userName = extractUser(authHeader.substring(7));
        UserDetails user = userDetailsService.loadUserByUsername(userName);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user;
    }

    @Override
    public UserEntity extractUserEntity(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        String userName = extractUser(authHeader.substring(7));
        return userRepository.findByPhoneNumber(userName).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token, String username) {
        return extractUser(token).equals(username) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public AuthResponseModel validateRefreshTokenAndGenerateAccessToken(HttpServletRequest request,
                                                                        @NonNull HttpServletResponse response) {
        try {
            final String authHeader = request.getHeader("Authorization");
            final String jwt;
            final String username;


            jwt = authHeader.substring(7);
            username = extractUser(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var userDetails = userDetailsService.loadUserByUsername(username);

                if (isTokenValid(jwt, userDetails.getUsername())) {
                    var authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }


            Claims claims = extractAllClaims(jwt);
            String tokenType = claims.get("type", String.class);

            if ("REFRESH".equals(tokenType) && !isTokenExpired(jwt)) {
                String phoneNumber = extractUser(jwt);
                return AuthResponseModel.builder()
                        .accessToken(generateAccessToken(phoneNumber))
                        .refreshToken(jwt)
                        .build();
            } else {
                throw new IllegalArgumentException("Invalid or expired refresh token");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid refresh token", e);
        }
    }
}
