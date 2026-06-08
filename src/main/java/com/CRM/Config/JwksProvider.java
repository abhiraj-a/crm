package com.CRM.Config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwksProvider {

    @Value("${authifyer.jwks-url}")
    private  String jwksUrl;

    private final Map<String , PublicKey> cache =new HashMap<>();

    private final long ttl = 3600000;

    private long lastFetchTime=0;

    public PublicKey getPublicKey(String kid) throws IOException, InterruptedException, InvalidKeySpecException, NoSuchAlgorithmException {
        if(cache.containsKey(kid) && System.currentTimeMillis() - lastFetchTime < ttl){
            return cache.get(kid);
        }
        refreshKeys();
        return cache.get(kid);
    }

    private void refreshKeys() throws IOException, InterruptedException, InvalidKeySpecException, NoSuchAlgorithmException {

        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).
                build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(jwksUrl))
                .timeout(Duration.ofSeconds(15))
                .header("Accept" , "application/json")
                .build();

        HttpResponse<String> response = client.send(request ,HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch JWKS: " + response.statusCode());
        }

        ObjectMapper mapper =new ObjectMapper();
        JsonNode node = mapper.readTree(response.body());
        JsonNode keys = node.get("keys");

        if(keys!=null){
            for (var key:keys){
                String kid = key.get("kid").asText();
                String alg = key.get("alg").asText();
                String kty = key.get("kty").asText();
                if("RS256".equals(alg)&& "RSA".equals(kty)){

                    String n = key.get("n").asText();
                    String e = key.get("e").asText();


                    PublicKey publicKey =createPublicKey(n,e);
                    cache.put(kid,publicKey);
                }
            }
        }

        lastFetchTime = System.currentTimeMillis();

    }

    private PublicKey createPublicKey(String n, String e) throws InvalidKeySpecException, NoSuchAlgorithmException {

        byte[] mod = Base64.getUrlDecoder().decode(n);
        byte[] ex = Base64.getUrlDecoder().decode(e);

        BigInteger modulus = new BigInteger(1, mod);
        BigInteger publicExponent = new BigInteger(1, ex);

        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, publicExponent);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }

}
