package org.example.github2.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.github2.Services.DB.UserService;
import org.example.github2.Services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    final UserService userService;
    final JwtService jwtService;

    @Autowired
    public JwtRequestFilter(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException {
        String tokenFromCookie = jwtService.resolveToken(request);
        String tokenFromHeader = request.getHeader("auth");
        String token = (tokenFromCookie!=null)?tokenFromCookie:tokenFromHeader;
        if (jwtService.isTokenValid(token)) {
            SecurityContextHolder.getContext().setAuthentication(jwtService.getAuthentication(token));
        } else {
        SecurityContextHolder.clearContext();
    }
    try {
        filterChain.doFilter(request, response);
    } catch (ServletException | IOException e) {
        e.printStackTrace();
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
    }
}
}