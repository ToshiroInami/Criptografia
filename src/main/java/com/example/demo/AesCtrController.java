package com.example.demo;

import org.springframework.web.bind.annotation.*;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AesCtrController {

    private static SecretKey secretKey;
    private static final List<String> encryptedMessages = new ArrayList<>(); // Lista para mensajes cifrados
    private static final List<String> decryptedMessages = new ArrayList<>(); // Lista para mensajes descifrados

    static {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            secretKey = keyGen.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/encrypt")
    public String encrypt(@RequestBody String message) throws Exception {
        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        IvParameterSpec ivParams = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParams);
        byte[] encryptedMessage = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));

        // Combinar el IV y el mensaje cifrado
        byte[] combined = new byte[iv.length + encryptedMessage.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedMessage, 0, combined, iv.length, encryptedMessage.length);

        String hexMessage = bytesToHex(combined);
        encryptedMessages.add(hexMessage); // Guardar mensaje cifrado

        return hexMessage;
    }

    @PostMapping("/decrypt")
    public String decrypt(@RequestBody String hexMessage) throws Exception {
        byte[] combined = hexToBytes(hexMessage);
        byte[] iv = Arrays.copyOfRange(combined, 0, 16);
        byte[] encryptedMessage = Arrays.copyOfRange(combined, 16, combined.length);

        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        IvParameterSpec ivParams = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams);
        byte[] decryptedMessage = cipher.doFinal(encryptedMessage);

        String decryptedText = new String(decryptedMessage, StandardCharsets.UTF_8);
        decryptedMessages.add(decryptedText); // Guardar mensaje descifrado

        return decryptedText;
    }

    @GetMapping("/messages")
    public List<String> getEncryptedMessages() {
        return new ArrayList<>(encryptedMessages); // Copia de la lista de mensajes cifrados
    }

    @GetMapping("/decryptedMessages")
    public List<String> getDecryptedMessages() {
        return new ArrayList<>(decryptedMessages); // Copia de la lista de mensajes descifrados
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
