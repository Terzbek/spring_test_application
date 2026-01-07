package ru.codekitchen.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import ru.codekitchen.entity.User;
import ru.codekitchen.entity.UserRole;
import ru.codekitchen.repository.UserRepository;

import java.util.Collections;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final UserRepository userRepository;

    @Autowired
    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable()
            )
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/registration", "/error", "/css/**").permitAll()
                    .requestMatchers("/account/**").hasAnyRole(UserRole.USER.name(), UserRole.ADMIN.name())
                    .requestMatchers("/admin/**").hasAnyRole(UserRole.ADMIN.name())
                    .anyRequest().authenticated()
            )
            .formLogin(form -> form
                    .loginPage("/login")
                    .usernameParameter("email")
                    .permitAll()
                    .defaultSuccessUrl("/account")
            )
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .permitAll()
            );

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new  BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                User user = userRepository
                        .findByEmailIgnoreCase(username)
                        .orElseThrow(() -> new UsernameNotFoundException("Username with email " + username + " not found!"));
                Set<SimpleGrantedAuthority> roles = Collections.singleton((user.getRole().toAuthority()));
                return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), roles);
            }
        };
    }
}
