package com.example.post.config;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;


    private String extractUserIdFromToken(String token) {
        try {
            // Supprimer le préfixe "Bearer " si nécessaire
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // Décodez le token JWT
            Algorithm algorithm = Algorithm.HMAC256("your-secret-key"); // Remplacez par votre clé secrète
            DecodedJWT decodedJWT = JWT.require(algorithm).build().verify(token);

            // Extraire l'identifiant utilisateur (par exemple, le champ "sub")
            return decodedJWT.getSubject();

        } catch (Exception ex) {
            throw new IllegalArgumentException("Token invalide ou expiré");
        }
    }
}
//    public String extractUserIdFromToken(String token) {
//        try {
//            // Parse the token
//            Claims claims = Jwts.parser()
//                    .setSigningKey(jwtSecret.getBytes())
//                    .parseClaimsJws(token)
//                    .getBody();
//
//            // Extract the "userId" from claims (or any other field you use)
//            return claims.getSubject(); // Assumes "sub" contains the userId
//        } catch (Exception e) {
//            throw new IllegalArgumentException("Invalid JWT token", e);
//        }
//    }