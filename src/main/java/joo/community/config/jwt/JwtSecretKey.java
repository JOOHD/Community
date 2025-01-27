package joo.community.config.jwt;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.util.Base64;

@Slf4j
public class JwtSecretKey {

    public JwtSecretKey() {
        // HS512에 적합한 안전한 크기의 키 생성
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

        // 키 확인
        System.out.println("Generated Key: " + key);
    }
}
