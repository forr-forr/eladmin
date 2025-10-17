package com.itheima.utlis;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESUtil {

    // 使用固定的密钥进行加密解密，密钥必须保持安全
    private static final String SECRET_KEY = "1234567890123456";  // 16字节的密钥，长度必须为 16, 24, 或 32 字节

    // 加密
    public static String encrypt(String data) throws Exception {
        // 创建 AES 密钥
        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");

        // 创建 Cipher 对象，设置为 AES 加密模式
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        // 执行加密
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());

        // 返回 Base64 编码后的密文
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // 解密
    public static String decrypt(String encryptedData) throws Exception {
        // 创建 AES 密钥
        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        // 创建 Cipher 对象，设置为 AES 解密模式
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        // 进行 Base64 解码，得到加密后的字节数组
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        // 执行解密
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);

        // 返回解密后的字符串
        return new String(decryptedBytes);
    }

    // 用于生成一个随机的 AES 密钥，密钥长度可以是 16, 24, 32
    public static String generateSecretKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);  // AES 密钥长度（128、192、256 位）

        SecretKey secretKey = keyGenerator.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    public static void main(String[] args) {
        try {
            String password = "123456";

            // 加密
            String encryptedPassword = AESUtil.encrypt(password);
            System.out.println("Encrypted: " + encryptedPassword);

            // 解密
            String decryptedPassword = AESUtil.decrypt(encryptedPassword);
            System.out.println("Decrypted: " + decryptedPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
