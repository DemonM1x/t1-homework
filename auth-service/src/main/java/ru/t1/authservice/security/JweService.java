package ru.t1.authservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class JweService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    @Value("${security.jwt.encryption.key:}")
    private String encryptionKeyBase64;

    private final SecretKey encryptionKey;

    public JweService() throws Exception {
        if (encryptionKeyBase64 == null || encryptionKeyBase64.isEmpty()) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(256);
            this.encryptionKey = keyGenerator.generateKey();
        } else {
            byte[] keyBytes = Base64.getDecoder().decode(encryptionKeyBase64);
            this.encryptionKey = new SecretKeySpec(keyBytes, ALGORITHM);
        }
    }

    public String encryptToken(String jwtToken) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, parameterSpec);

            byte[] encryptedData = cipher.doFinal(jwtToken.getBytes(StandardCharsets.UTF_8));

            byte[] encryptedWithIv = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
            System.arraycopy(encryptedData, 0, encryptedWithIv, iv.length, encryptedData.length);

            return Base64.getEncoder().encodeToString(encryptedWithIv);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при шифровании токена", e);
        }
    }

    public String decryptToken(String encryptedToken) {
        try {
            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedToken);

            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(encryptedWithIv, 0, iv, 0, iv.length);

            byte[] encryptedData = new byte[encryptedWithIv.length - GCM_IV_LENGTH];
            System.arraycopy(encryptedWithIv, GCM_IV_LENGTH, encryptedData, 0, encryptedData.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, parameterSpec);

            byte[] decryptedData = cipher.doFinal(encryptedData);

            return new String(decryptedData, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при дешифровании токена", e);
        }
    }
}