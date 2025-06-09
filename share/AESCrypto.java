package share;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class AESCrypto {

    private static final String SECRET_KEY = "1234567890abcdef"; // 16 字元 = 128-bit key

    // 加密（傳回 Base64 字串）
    public static String encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes); // 回傳 base64 密文
    }

    // 解密（還原原始字串）
    public static String decrypt(String encryptedText) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    // 測試
    public static void main(String[] args) throws Exception {
        String original = "中文密碼123";
        String encrypted = encrypt(original);
        String decrypted = decrypt(encrypted);

        System.out.println("原始文字: " + original);
        System.out.println("加密後密文: " + encrypted + "（長度: " + encrypted.length() + ")");
        System.out.println("解密後明文: " + decrypted);
    }
}
