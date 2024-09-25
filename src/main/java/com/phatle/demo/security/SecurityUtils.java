package com.phatle.demo.security;

import java.util.Arrays;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phatle.demo.entity.User;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Component
public class SecurityUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_PREFIX = "Bearer ";
    private static final int SIX_HOURS_MILLISECOND = 1000 * 3600 * 6;

    private static final String USER_CLAIM = "user";
    private static final String ISSUER = "auth0";

    private static String SECRET_KEY;
    private static Algorithm ALGORITHM;

    // It's meant to be called only by system to assign env var to static field
    @Value("${jwt-key}")
    public void setStaticJwtKey(String jwtKey) {
        SECRET_KEY = jwtKey;
        ALGORITHM = Algorithm.HMAC256(SECRET_KEY);
    }

    @SneakyThrows
    public static String createToken(JwtTokenVo jwtTokenVo) {
        var builder = JWT.create();
        var tokenJson = OBJECT_MAPPER.writeValueAsString(jwtTokenVo);
        builder.withClaim(USER_CLAIM, tokenJson);
        return builder
                .withIssuedAt(new Date())
                .withIssuer(ISSUER)
                .withExpiresAt(new Date(System.currentTimeMillis() + SIX_HOURS_MILLISECOND))
                .sign(ALGORITHM);
    }

    public static JwtTokenVo buildJwtTokenFromUser(User user) {
        return JwtTokenVo.builder()
                .id(user.getId())
                .roles(Arrays.asList(user.getUserRole()))
                .build();
    }

    public static void setJwtToClient(JwtTokenVo vo) {
        var token = createToken(vo);
        var attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var response = attributes.getResponse();

        // Set the token in the X-Access-Token header
        response.setHeader("X-Access-Token", AUTHORIZATION_PREFIX + token);
        response.setHeader("Access-Control-Expose-Headers", "X-Access-Token"); // If using CORS, expose the header
    }

    @SneakyThrows
    public static DecodedJWT validate(String token) {
        var verifier = JWT.require(ALGORITHM)
                .withIssuer(ISSUER)
                .build();
        return verifier.verify(token);
    }

    @SneakyThrows
    public static JwtTokenVo getValueObject(DecodedJWT decodedJWT) {
        var userClaim = decodedJWT.getClaims().get(USER_CLAIM).asString();
        return OBJECT_MAPPER.readValue(userClaim, JwtTokenVo.class);
    }

    public static String getToken(HttpServletRequest req) {
        String authorizationHeader = req.getHeader(AUTHORIZATION_HEADER);
        Assert.notNull(authorizationHeader, "Authorization header is missing.");

        Assert.isTrue(authorizationHeader.startsWith(AUTHORIZATION_PREFIX),
                "Authorization header must start with '" + AUTHORIZATION_PREFIX + "'.");

        String jwtToken = authorizationHeader.substring(AUTHORIZATION_PREFIX.length()).trim();
        return jwtToken;
    }

    public static JwtTokenVo getSession() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("Not authorized.");
        }
        return (JwtTokenVo) authentication.getPrincipal();
    }
}