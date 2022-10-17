package demo.config;

import demo.security.GoogleCloudAuthenticationProvider;
import demo.security.JwtAuthenticationProvider;
import demo.utils.JwtTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtTokenFilter jwtTokenFilter;
    private final JwtAuthenticationProvider jwtAuthenticationProvider;
    private final GoogleCloudAuthenticationProvider googleCloudAuthenticationProvider;

    public SecurityConfig(JwtTokenFilter jwtTokenFilter, JwtAuthenticationProvider jwtAuthenticationProvider, GoogleCloudAuthenticationProvider googleCloudAuthenticationProvider) {
        this.jwtTokenFilter = jwtTokenFilter;
        this.jwtAuthenticationProvider = jwtAuthenticationProvider;
        this.googleCloudAuthenticationProvider = googleCloudAuthenticationProvider;
    }


    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(jwtAuthenticationProvider, googleCloudAuthenticationProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeRequests(auth -> auth
                        .antMatchers("/login").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

