package com.example.parkingreservation.config;

import com.example.parkingreservation.model.User;
import com.example.parkingreservation.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/register", "/login", "/css/**", "/slots", "/access-denied", "/admin/login").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().hasRole("USER")
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/slots", true)
                        .failureHandler((request, response, exception) -> {
                            String errorMsg = "Invalid email or password.";
                            String email = request.getParameter("username");
                            if (email != null) {
                                java.util.Optional<User> uOpt = userRepository.findByEmail(email.trim());
                                if (uOpt.isPresent() && "ROLE_ADMIN".equals(uOpt.get().getRole())) {
                                    errorMsg = "Please use the Admin Login page.";
                                }
                            }
                            request.getSession().setAttribute("loginErrorMessage", errorMsg);
                            response.sendRedirect("/login?error=true");
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(request -> "GET".equalsIgnoreCase(request.getMethod()) && "/logout".equals(request.getServletPath()))
                        .logoutSuccessUrl("/?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/access-denied")
                );

        return http.build();
    }
}