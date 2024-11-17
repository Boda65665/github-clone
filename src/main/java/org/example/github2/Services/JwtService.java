package org.example.github2.Services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.example.github2.Model.Role;
import org.example.github2.Services.DB.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.expiration}")
    private Duration validityInMilliseconds;
    private final UserService userService;

    public JwtService(UserService userService) {
        this.userService = userService;
    }

    public Authentication getAuthentication(String token){
        String email = extractEmail(token);
        Set<SimpleGrantedAuthority> authorities;
        authorities=getRole(token).getAuthorities();
        return new UsernamePasswordAuthenticationToken(
                email,
                null,
                authorities

        );    }

    public String extractEmail(HttpServletRequest request) {
        return extractEmail(resolveToken(request));
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        if (claims==null)return null;
        return claimsResolvers.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser().setSigningKey(getSigningKey()).build().parseClaimsJws(token)
                    .getBody();
        }
        catch (Exception | Error err){
            return null;
        }
    }

    public Role getRole(String token){
        return userService.findUserDtoByEmail(extractEmail(token)).getRole();
    }


    public String generateToken(String email) {
        Date issuedDate = new Date();
        Date expiredDate = new Date(issuedDate.getTime() + validityInMilliseconds.toMillis());
        return Jwts.builder().setSubject(email)
                .setIssuedAt(issuedDate)
                .setExpiration(expiredDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenValid(String token) {
        try {
            final String email = extractEmail(token);
            return (email!=null && userService.findUserDtoByEmail(email) != null) && !isTokenExpired(token);
        }
        catch (Exception | Error e){
            e.printStackTrace();
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String resolveToken(HttpServletRequest request) {
        return getTokenFomCookie(request.getCookies());
    }

    private String getTokenFomCookie(Cookie[] cookies) {
        if (cookies==null)return null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("JWT")) return cookie.getValue();
        }
        return null;
    }
}