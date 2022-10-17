package demo.utils;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
public class JwtUtil {

    @Value("${jwt.expire.in}")
    private Duration expireIn;

    @Value("${jwt.static.key}")
    private String secret;

    public String generate(String username) {
        //Create the TOKEN
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(username)
                .issuer("cyper.run")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(expireIn))
                .build();
        var encoder = jwtEncoder();
        JwsHeader jwsHeader = JwsHeader.with(() -> "HS256").build();
        String token = encoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
        return token;
    }

    public boolean validate(String token) {
        try {
            var decoder = jwtDecoder();
            decoder.decode(token);
            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public String getUsername(String token) {
        Map<String, Object> claims = getClaims(token);
        return (String) claims.get("sub");
    }

    private Map<String, Object> getClaims(String token) {
        var decoder = jwtDecoder();
        Jwt jwt = decoder.decode(token);
        return jwt.getClaims();
    }

    public JwtEncoder jwtEncoder() {
        SecretKey key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        JWKSource<SecurityContext> immutableSecret = new ImmutableSecret<SecurityContext>(key);
        return new NimbusJwtEncoder(immutableSecret);
    }

    public JwtDecoder jwtDecoder() {
        SecretKey originalKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(originalKey).build();
        return jwtDecoder;
    }

}
