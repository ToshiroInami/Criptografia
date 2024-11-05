package pe.edu.vallegrande.ctr.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import java.util.logging.Logger;

@RestController
@RequestMapping("/api")
public class AesCtrController {

    private static final Logger logger = Logger.getLogger(AesCtrController.class.getName());
    private static SecretKey secretKey;
    private static final List<String> encryptedClickRates = new ArrayList<>();
    private static final List<String> decryptedClickRates = new ArrayList<>();

    // Generación de clave secreta
    static {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            secretKey = keyGen.generateKey();
            logger.info("Clave secreta generada exitosamente.");
        } catch (Exception e) {
            logger.severe("Error al generar la clave secreta: " + e.getMessage());
        }
    }

    // Cifrar tasa de clics
    @PostMapping("/encryptClickRate")
    public ResponseEntity<?> encryptClickRate(@RequestBody String clickRate) {
        if (clickRate == null || clickRate.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La tasa de clics a cifrar no puede estar vacía.");
        }

        try {
            byte[] iv = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParams);

            byte[] encryptedData = cipher.doFinal(clickRate.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);

            String hexMessage = bytesToHex(combined);
            encryptedClickRates.add(hexMessage);
            logger.info("Tasa de clics cifrada exitosamente.");

            return ResponseEntity.ok(hexMessage);
        } catch (Exception e) {
            logger.severe("Error al cifrar la tasa de clics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cifrar la tasa de clics.");
        }
    }

    // Descifrar tasa de clics
    @PostMapping("/decryptClickRate")
    public ResponseEntity<?> decryptClickRate(@RequestBody String hexMessage) {
        if (hexMessage == null || hexMessage.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El mensaje cifrado no puede estar vacío.");
        }

        try {
            byte[] combined = hexToBytes(hexMessage);
            byte[] iv = Arrays.copyOfRange(combined, 0, 16);
            byte[] encryptedData = Arrays.copyOfRange(combined, 16, combined.length);

            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams);

            byte[] decryptedData = cipher.doFinal(encryptedData);
            String decryptedText = new String(decryptedData, StandardCharsets.UTF_8);
            decryptedClickRates.add(decryptedText);
            logger.info("Tasa de clics descifrada exitosamente.");

            return ResponseEntity.ok(decryptedText);
        } catch (Exception e) {
            logger.severe("Error al descifrar la tasa de clics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al descifrar la tasa de clics.");
        }
    }

    // Obtener tasas de clics cifradas
    @GetMapping("/encryptedClickRates")
    public ResponseEntity<List<String>> getEncryptedClickRates() {
        logger.info("Obteniendo lista de tasas de clics cifradas.");
        return ResponseEntity.ok(new ArrayList<>(encryptedClickRates));
    }

    // Obtener tasas de clics descifradas
    @GetMapping("/decryptedClickRates")
    public ResponseEntity<List<String>> getDecryptedClickRates() {
        logger.info("Obteniendo lista de tasas de clics descifradas.");
        return ResponseEntity.ok(new ArrayList<>(decryptedClickRates));
    }

    // Métodos auxiliares para conversión
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
