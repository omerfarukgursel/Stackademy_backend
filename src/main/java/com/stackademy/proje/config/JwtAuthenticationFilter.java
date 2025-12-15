package com.stackademy.proje.config;

import com.stackademy.proje.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // Manuel Constructor (Lombok kullanmadığımız için)
    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Header kontrolü: Token var mı ve "Bearer " ile başlıyor mu?
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Token'ı ayıkla ("Bearer " kısmını at)
        jwt = authHeader.substring(7);

        // 3. Token içinden emaili çıkar - Süresi dolmuş token için hata yakalama
        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (ExpiredJwtException e) {
            // Token süresi dolmuş - devam et, public endpoint'ler için sorun yok
            System.out.println("JWT token süresi dolmuş: " + e.getMessage());
            filterChain.doFilter(request, response);
            return;
        } catch (Exception e) {
            // Diğer JWT hataları - devam et
            System.out.println("JWT token hatası: " + e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // 4. Email varsa ve kullanıcı henüz sisteme giriş yapmamışsa (Authentication
        // boşsa)
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Veritabanından kullanıcıyı bul
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 5. Token geçerli mi diye sor
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // GEÇERLİ! O zaman sisteme giriş yaptır (SecurityContext oluştur)
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Sisteme kaydet: "Bu istek şu kullanıcıya ait"
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Zincire devam et (Sonraki filtreye veya Controller'a geç)
        filterChain.doFilter(request, response);
    }
}
