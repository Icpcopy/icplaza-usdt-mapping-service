package com.icplaza.mapping.utils;

import com.icplaza.mapping.common.Constant;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class RSATool {
    /**
     * 本地公钥-用于加密波场私钥、加密返回的随机公钥
     */
    private static PublicKey LOCAL_PUBLIC_KEY = null;
    /**
     * 随机私钥
     */
    private static PrivateKey PRIVATEKEY = null;
    /**
     * 随机公钥
     */
    private static Map<String, String> PUBLIC_KEY_MAP = null;
    /**
     * 波场转账私钥
     */
    private static String SK;
    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;

    public static PublicKey getLocalPublicKey() {
        if (LOCAL_PUBLIC_KEY == null) {
            String pubKey = Constant.PUBLIC_KEY;
            try {
                X509EncodedKeySpec bobPubKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(pubKey));
                // RSA对称加密算法
                KeyFactory keyFactory;
                keyFactory = KeyFactory.getInstance("RSA");
                // 取公钥匙对象
                return keyFactory.generatePublic(bobPubKeySpec);
            } catch (Exception e) {
                return null;
            }
        }
        return LOCAL_PUBLIC_KEY;
    }

    /**
     * 公钥加密
     */
    public static byte[] encrypt(byte[] content, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");//java默认"RSA"="RSA/ECB/PKCS1Padding"
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(content);
    }

    /**
     * 公钥加密
     */
    public static String encryptStr(String content) throws Exception {
        PublicKey publicKey = getLocalPublicKey();
        if (publicKey == null) {
            return null;
        }
        try {
            byte[] data = encrypt(content.getBytes(), publicKey);
            return Base64.getEncoder().encodeToString(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 返回公钥用于加密
     */
    public static Map<String, String> getPublicKeyMap() {
        if (PUBLIC_KEY_MAP == null) {
            try {
                KeyPair keyPair = genKeyPair(1024);
                String PKCS1 = new String(Base64.getEncoder().encode(formatPublicKeyPKCS8ToPKCS1(keyPair.getPublic().getEncoded())));
                String PKCS8 = new String(Base64.getEncoder().encode(keyPair.getPublic().getEncoded()));
                PUBLIC_KEY_MAP = new HashMap<>();
                PUBLIC_KEY_MAP.put("PKCS1", PKCS1);
                PUBLIC_KEY_MAP.put("PKCS8", PKCS8);
                PRIVATEKEY = keyPair.getPrivate();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return PUBLIC_KEY_MAP;
    }

    public static void setPK(String pkString) throws Exception {
        SK = decrypt(pkString);
    }

    public static String getSK() {
        return SK;
    }

    /**
     * 使用私钥解密公钥加密的数据
     */
    public static String decrypt(String content) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, PRIVATEKEY);
        return new String(cipher.doFinal(Base64.getDecoder().decode(content)));
    }

    /**
     * 使用私钥解密公钥加密的数据
     */
    public static String decryptByPrivateKey(String content, String privateKeyStr) {
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyStr));
            // RSA对称加密算法
            KeyFactory keyFactory;
            keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(content)));
        } catch (Exception e) {
            return null;
        }
    }

    //生成密钥对
    public static KeyPair genKeyPair(int keyLength) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        return keyPairGenerator.generateKeyPair();
    }

    public static byte[] formatPublicKeyPKCS8ToPKCS1(byte[] pkcs8PublicKeyByte) {

        ASN1Sequence publicKeyASN1Object = ASN1Sequence.getInstance(pkcs8PublicKeyByte);

        ASN1Encodable derBitStringASN1Encodable = publicKeyASN1Object.getObjectAt(1);

        DERBitString derBitStringObject = DERBitString.getInstance(derBitStringASN1Encodable);

        return derBitStringObject.getBytes();

    }

    /**
     * 使用公钥加密
     *
     * @param data 源数据
     */
    public static String encryptByPublicKey(String data) {
        // 加密
        String str = "";
        try {
            PublicKey rsaPubKey = getLocalPublicKey();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, rsaPubKey);
            int inputLen = data.getBytes().length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0;
            byte[] cache;
            int i = 0;
            while (inputLen - offSet > 0) {
                //MAX_ENCRYPT_BLOCK 117
                if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                    cache = cipher.doFinal(data.getBytes(), offSet, MAX_ENCRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(data.getBytes(), offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * MAX_ENCRYPT_BLOCK;
            }
            byte[] encryptedData = out.toByteArray();
            out.close();

            str = new String(Base64.getEncoder().encode(encryptedData));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }
}
