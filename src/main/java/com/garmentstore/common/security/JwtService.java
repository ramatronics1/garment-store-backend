package com.garmentstore.common.security;
import io.jsonwebtoken.*; import io.jsonwebtoken.security.Keys; import org.springframework.stereotype.Service; import javax.crypto.SecretKey; import java.nio.charset.StandardCharsets; import java.time.Instant; import java.util.*;
@Service public class JwtService { private final JwtProperties properties; private final SecretKey key; public JwtService(JwtProperties properties){this.properties=properties; this.key= Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));}
  public String generateAccessToken(Long userId,String email,List<String> roles){Instant now=Instant.now(); return Jwts.builder().issuer(properties.issuer()).subject(String.valueOf(userId)).issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(properties.accessTokenExpiryMinutes()*60))).claim("email",email==null?"":email).claim("roles",roles).signWith(key).compact();}
  public Claims parseClaims(String token){return Jwts.parser().verifyWith(key).requireIssuer(properties.issuer()).build().parseSignedClaims(token).getPayload();}
}
