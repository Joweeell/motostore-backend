package cl.duoc.motostore.security;

import cl.duoc.motostore.model.Usuario;

import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;

    public JwtService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generarToken(Usuario usuario) {

        Instant ahora = Instant.now();

        String rol = usuario.getRol()
                .getNombreRol()
                .toUpperCase(Locale.ROOT);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("motorstore-api")
                .subject(usuario.getIdUsuario().toString())
                .issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(3600))
                .claim("email", usuario.getEmail())
                .claim("roles", List.of(rol))
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }
}