package com.hms.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = request.getHeader("x-auth-token");
        if (token != null && jwtUtils.validateToken(token)) {
            try {
                String email = jwtUtils.getEmailFromToken(token);
                String role = (String) Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(jwtUtils.getKeyBytes()))
                        .build().parseClaimsJws(token).getBody().get("role");

                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(email, null,
                        Collections.singletonList(authority));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                // Ignore invalid tokens or parsing errors
            }
        }
        filterChain.doFilter(request, response);
    }
}
