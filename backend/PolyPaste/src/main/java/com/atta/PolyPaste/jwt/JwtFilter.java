package com.atta.PolyPaste.jwt;

import com.atta.PolyPaste.dto.UserPrincipalDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = jwtProvider.extractJwt(request);

        if (jwt != null && jwtProvider.validateToken(jwt)) {
            // Твоя логика аутентификации...
            UserPrincipalDto principal = new UserPrincipalDto(
                    jwtProvider.extractVkUserId(jwt),
                    jwtProvider.extractFirstName(jwt),
                    jwtProvider.extractLastName(jwt)
            );

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );

            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/auth/");
    }
}
