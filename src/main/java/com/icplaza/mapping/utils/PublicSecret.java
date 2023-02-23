package com.icplaza.mapping.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

/**
 * 非对称加密-公钥加密
 * 公钥加密-私钥解密
 * 私钥加密-公钥解密
 */
public class PublicSecret {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        publicEncrypt();
        privatecEncrypt();

    }

    /**
     * 公钥加密
     *
     * @throws Exception
     */
    private static void publicEncrypt() throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        //产生钥匙对
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        //前面做加密时都是用默认的编码，如果加密的数据是中文会出现乱码，现在改成中文的数据进行测试以下
        byte[] results = cipher.doFinal("我是要被加密的数据！".getBytes("UTF-8"));

        //保存密钥,由于是公钥加密，解密时得用私钥，所以这里要对产生的私钥进行存盘
        saveKey(privateKey, "key_public.key");
        saveData(results, "key_pubData.data");
        System.out.println("加密后的数据：" + new String(results));


    }

    /**
     * 私钥解密
     *
     * @throws Exception
     */
    private static void privatecEncrypt() throws Exception {
        //读取key与data
        Key privateKey = readKey("key_public.key");
        byte[] results = readData("key_pubData.data");

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        //不要忘记了加编码格式，不然乱码
        System.out.println("解密后的数据：" + new String(cipher.doFinal(results), "UTF-8"));

        //===================读数据的另一种写法，如以下========================

        FileInputStream fis = new FileInputStream("key_pubData.data");
        CipherInputStream cis = new CipherInputStream(fis, cipher);
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        int len = 0;
        byte[] data = new byte[1024];
        while ((len = cis.read(data)) != -1) {
            arrayOutputStream.write(data, 0, len);
        }
        byte[] result2 = arrayOutputStream.toByteArray();
        arrayOutputStream.close();
        cis.close();
        System.out.println("解密后的数据(读取数据的另一种方式)：" + new String(result2, "UTF-8"));

    }

    /**
     * 保存数据的方法
     *
     * @param results
     * @param dataName
     * @throws Exception
     */
    public static void saveData(byte[] results, String dataName) throws Exception {
        FileOutputStream fosData = new FileOutputStream(dataName);
        fosData.write(results);
        fosData.close();
    }

    /**
     * 读取数据的方法
     *
     * @param dataName
     * @return byte[]
     * @throws Exception
     */
    public static byte[] readData(String dataName) throws Exception {
        FileInputStream fisDat = new FileInputStream(dataName);

        //读二进制数据
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        int len = 0;
        byte[] data = new byte[1024];
        while ((len = fisDat.read(data)) != -1) {
            arrayOutputStream.write(data, 0, len);
        }
        byte[] result = arrayOutputStream.toByteArray();
        arrayOutputStream.close();

        fisDat.close();
        return result;

    }


    /**
     * 保存密钥的方法
     *
     * @param key
     * @param keyName
     * @throws Exception
     */
    public static void saveKey(Key key, String keyName) throws Exception {
        FileOutputStream foskey = new FileOutputStream(keyName);
        ObjectOutputStream oos = new ObjectOutputStream(foskey);
        oos.writeObject(key);
        oos.close();
        foskey.close();
    }

    /**
     * 读取密钥的方法
     *
     * @param keyName
     * @return Key
     * @throws Exception
     */
    public static Key readKey(String keyName) throws Exception {
        FileInputStream fiskey = new FileInputStream(keyName);
        ObjectInputStream oiskey = new ObjectInputStream(fiskey);
        Key key = (Key) oiskey.readObject();
        oiskey.close();
        fiskey.close();
        return key;
    }
}

