Based on this
tutorial [Spring Security: Authentication Architecture Explained in Depth](https://backendstory.com/spring-security-authentication-architecture-explained-in-depth/)

But I changed a lot of code to reflect latest spring security changes.

In this demo, I use HS256 alg, in future demos, I will use RS256.

## Environment

- Java 17
- Spring Boot 2.7.4
- Nimbus Jose Jwt(HS256)

## Checkpoint

- [x] Use nimbus-jose to generate jwt - (shared) static key
- [x] custom authentication provider
- [x] multiple custom authentication providers

![](./doc/images/multi-provider.png)

## Notes

We will use the following:

1. ~~built-in~~ Custom AuthenticationManager
2. Custom AuthenticationProvider
3. custom UserDetailsService
4. Define our own JwtTokenFilter, add it before UsernamePasswordAuthenticationFilter

config/SecurityConfig.java 核心代码:

```java

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtTokenFilter jwtTokenFilter;
    private final JwtAuthenticationProvider jwtAuthenticationProvider;
    private final GoogleCloudAuthenticationProvider googleCloudAuthenticationProvider;

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
```

Authentication Provider核心代码

```java

@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // In LoginController.login() method, we call authenticationManager.authenticate(token)
        // Then, Authentication Manager calls AuthenticationProvider's authenticate method.
        // Since JwtAuthenticationProvider is our custom authentication provider,
        // this method will be executed.
        String username = authentication.getName();
        String password = String.valueOf(authentication.getCredentials());

        // Fetching user as wrapped with UserDetails object
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // If user is not null, then we check if password matches
        if (userDetails != null) {
            if (passwordEncoder.matches(password, userDetails.getPassword())) {
                // if it matches, then we can initialize UsernamePasswordAuthenticationToken.
                // Attention! We used its 3 parameters constructor.
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(username, password, userDetails.getAuthorities());
                return authenticationToken;
            }
        }
        throw new BadCredentialsException("Error!!");
    }

    // Authentication Manager checks if the token is supported by this filter
    // to avoid unnecessary checks.
    @Override
    public boolean supports(Class<?> authenticationType) {
        return UsernamePasswordAuthenticationToken.class.equals(authenticationType);
    }
}

```

## Test

```shell
$ curl -H "content-type:application/json" \
-d '{"username":"cyper","password":"123"}' \
http://localhost:8080/login

$ curl -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJjeXBlci5ydW4iLCJzdWIiOiJjeXBlciIsImV4cCI6MTY2NjAyNTY1MCwiaWF0IjoxNjY2MDI1MzUwfQ.WAJQsj47_w4LAF5OibYrS53i9hB7dqW1TkXNLhjdF1U' \
http://localhost:8080/hello && echo


$ curl -H "content-type:application/json" \
-d '{"username":"david","password":"123"}' \
http://localhost:8080/login


$ curl -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJjeXBlci5ydW4iLCJzdWIiOiJkYXZpZCIsImV4cCI6MTY2NjAyNjIzOCwiaWF0IjoxNjY2MDI1OTM4fQ.gd0p0bIQCeVCgQbzn25LbDq9tV35Duvv2RidemoSJuo' \
http://localhost:8080/hello && echo
```