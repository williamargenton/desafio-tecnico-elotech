package br.com.elotech.config.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private final SecretKey key;
	private final long expiration;

	public JwtService(
		@Value("${app.jwt.secret}") String secret,
		@Value("${app.jwt.expiracao}") long expiration
	) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expiration = expiration;
	}

	public String gerarToken(UsuarioAutenticado usuario) {
		Date now = new Date();
		return Jwts
				.builder()
					.subject(usuario.nomeDeUsuario())
					.claim("usuarioId", usuario.id())
					.claim("perfil", usuario.perfil().name())
				.issuedAt(now)
				.expiration(
						new Date(
						now.getTime() + expiration))
				.signWith(key)
				.compact();
	}

	public String extrairEmail(String token) {
		return Jwts
				.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.getSubject();
	}

	public boolean valido(String token, UserDetails usuario) {
		try {
			return this.extrairEmail(token).equals(usuario.getUsername());
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	public long expiracao() {
		return expiration;
	}
}