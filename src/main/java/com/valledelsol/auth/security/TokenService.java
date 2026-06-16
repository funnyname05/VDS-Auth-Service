package com.valledelsol.auth.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.valledelsol.auth.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Migrado desde ValleDelSol.demo.infra.security.TokenService.
 * Cambios: solo el package y la importación de User.
 */
@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String apiSecret;

    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(apiSecret);
            return JWT.create()
                    .withIssuer("valle_del_sol")
                    .withSubject(user.getEmail())
                    .withClaim("id", user.getId())
                    .withClaim("role", "ROLE_" + user.getRol().name())  // ← agregar esta línea
                    .withExpiresAt(generateExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error al generar el token jwt", exception);
        }
    }

    /**
     * Valida el token y retorna el subject (email).
     * Retorna null si el token es inválido — el BFF interpretará eso como 401.
     */
    public String getSubject(String token) {
        if (token == null) return null;
        try {
            Algorithm algorithm = Algorithm.HMAC256(apiSecret);
            DecodedJWT verifier = JWT.require(algorithm)
                    .withIssuer("valle_del_sol")
                    .build()
                    .verify(token);
            return verifier.getSubject();
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    private Instant generateExpirationDate() {
        return LocalDateTime.now().plusHours(24).toInstant(ZoneOffset.of("-03:00"));
    }
}
