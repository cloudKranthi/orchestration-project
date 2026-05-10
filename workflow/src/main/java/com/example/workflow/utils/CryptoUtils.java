package com.example.workflow.utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import lombok.RequiredArgsConstructor;


@Component


@RequiredArgsConstructor
public class CryptoUtils {
    @Value("${app.security.secret}")
    private  String Key;
    private final String ALGORITHM="AES";
    public String encrypt(String text) throws Exception{
        SecretKeySpec keySpec=new SecretKeySpec(Key.getBytes(), ALGORITHM);
        Cipher cipher=Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE,keySpec);
        byte[] encryptbyte=cipher.doFinal(text.getBytes());
        return Base64.getEncoder().encodeToString(encryptbyte);
    } 
    public String decrypt (String encryptedText) throws Exception{
        byte [] encryptBytes= Base64.getDecoder().decode(encryptedText);
        SecretKeySpec keyspec=new SecretKeySpec(Key.getBytes(), ALGORITHM);
        Cipher cipher=Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE,keyspec);
        byte[] decryptBytes=cipher.doFinal(encryptBytes);
        return new String(decryptBytes);
    }
}
