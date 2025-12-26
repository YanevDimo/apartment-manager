package apartmentsmanager.apartmentsmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    private final UserDetailsService userDetailsService;
    
    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/", "/home", "/public/**", "/properties", "/properties/**", 
                               "/about", "/contact", "/auth/register", "/auth/login", "/login", "/register",
                               "/api/properties/**", "/static/**", "/css/**", "/js/**", "/images/**").permitAll()
                // Dashboard - requires authentication (any role)
                .requestMatchers("/index", "/dashboard", "/api/statistics").authenticated()
                // User endpoints
                .requestMatchers("/user/**", "/profile", "/inquiries/my").hasAnyRole("USER", "AGENT", "ADMIN")
                // Agent endpoints
                .requestMatchers("/agent/**", "/properties/create", "/properties/edit/**", 
                               "/properties/delete/**", "/inquiries/agent/**").hasAnyRole("AGENT", "ADMIN")
                // Admin endpoints
                .requestMatchers("/admin/**", "/users/**", "/system/**").hasRole("ADMIN")
                // Legacy endpoints (keep for backward compatibility) - ADMIN only
                .requestMatchers("/apartments/**", "/clients/**", "/payments/**", 
                               "/excel/**", "/contract/**", "/add-apartment", "/add-building", 
                               "/add-client", "/buildings/**").hasAnyRole("USER", "AGENT", "ADMIN")
                // All other requests require authentication
                .anyRequest().authenticated()
            )
                   .formLogin(form -> form
                       .loginPage("/auth/login")
                       .loginProcessingUrl("/auth/login")
                       .defaultSuccessUrl("/index", true)
                       .failureUrl("/auth/login?error=true")
                       .permitAll()
                   )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .userDetailsService(userDetailsService);
        
        return http.build();
    }
}

