package com.CRM.Config;

import com.CRM.Util.Principal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Collections;

@RequiredArgsConstructor
@Slf4j
public  class JWTFilter extends OncePerRequestFilter {
    private final JwksProvider jwksProvider;
    @Value("${authifyer.issuer}")
    private String authifyerIssuer;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);
            String[] chunks = token.split("\\.");

            String headerJson =
                    new String(Base64.getUrlDecoder().decode(chunks[0]));

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(headerJson);
            String kid = node.get("kid").asText();

            String authifyerId = node.get("sub").asText();
            PublicKey publicKey =
                    null;
            try {
                publicKey = jwksProvider.getPublicKey(kid);
            } catch (InterruptedException | InvalidKeySpecException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

                Claims claims = Jwts.parser()
                        .verifyWith(publicKey)
                        .requireIssuer(authifyerIssuer)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

            log.info("Setting security context for Authifyer Id : " + claims.getSubject());

                Principal principal = Principal.builder()
                        .authifyerId(claims.getSubject())
                        .build();


                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                Collections.emptyList()
                        );


                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
                filterChain.doFilter(request, response);
            }
        }
    }

