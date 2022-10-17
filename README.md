Based on this tutorial [Spring Security: Authentication Architecture Explained in Depth](https://youtu.be/ElY3rjtukig)

But I changed a lot of code to reflect latest spring security changes.

## Environment

- Java 17
- Spring Boot 2.7.4
- Nimbus JOSE

## Checkpoint

- [x] Use nimbus-jose to generate jwt - (shared) static key
- [x] custom authentication provider
- [ ] multiple custom authentication provider

## Checkpoint #1 Generate jwt token

We will use the following:

1. ~~built-in~~ Custom AuthenticationManager
2. Custom AuthenticationProvider
3. custom UserDetailsService
4. Define our own JwtTokenFilter, add it before UsernamePasswordAuthenticationFilter

config/SecurityConfig.java 核心代码:

```java

```

## Test

```shell
$ curl -H "content-type:application/json" \
-d '{"username":"cyper","password":"123"}' \
http://localhost:8080/login

$ curl -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJjeXBlci5ydW4iLCJzdWIiOiJjeXBlciIsImV4cCI6MTY2NjAyNTY1MCwiaWF0IjoxNjY2MDI1MzUwfQ.WAJQsj47_w4LAF5OibYrS53i9hB7dqW1TkXNLhjdF1U' \
http://localhost:8080/hello && echo
```