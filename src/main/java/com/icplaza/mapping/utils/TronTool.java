package com.icplaza.mapping.utils;


import com.google.protobuf.ByteString;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;
import org.tron.tronj.client.TronClient;
import org.tron.tronj.crypto.SECP256K1;
import org.tron.tronj.utils.Base58Check;

import java.util.HashMap;
import java.util.Map;


public class TronTool {
    /**
     * 创建地址
     * */
    public static Map<String, String> createAddress() throws Exception {
        Map<String, String> result = new HashMap<>();
        SECP256K1.KeyPair kp = SECP256K1.KeyPair.generate();
        SECP256K1.PublicKey pubKey = kp.getPublicKey();
        Keccak.Digest256 digest = new Keccak.Digest256();
        digest.update(pubKey.getEncoded(), 0, 64);
        byte[] raw = digest.digest();
        byte[] rawAddr = new byte[21];
        rawAddr[0] = 0x41;
        System.arraycopy(raw, 12, rawAddr, 1, 20);
        String privateKey = Hex.toHexString(kp.getPrivateKey().getEncoded());
        String adress = Base58Check.bytesToBase58(parseAddress(toHex(rawAddr)).toByteArray());
        result.put("address", adress);
        result.put("privateKey", privateKey);
        return result;
    }
    public static ByteString parseAddress(String address) {
        byte[] raw;
        if (address.startsWith("T")) {
            raw = Base58Check.base58ToBytes(address);
        } else if (address.startsWith("41")) {
            raw = Hex.decode(address);
        } else if (address.startsWith("0x")) {
            raw = Hex.decode(address.substring(2));
        } else {
            try {
                raw = Hex.decode(address);
            } catch (Exception var3) {
                throw new IllegalArgumentException("Invalid address: " + address);
            }
        }

        return ByteString.copyFrom(raw);
    }
    public static String toHex(byte[] raw) {
        return Hex.toHexString(raw);
    }
    /**
     * 根据私钥获取地址
     *
     * @param privateKey
     * @return
     */
    public static String getAddressByPrivateKey(String privateKey) {
        SECP256K1.PrivateKey privkey = SECP256K1.PrivateKey.create(privateKey);
        SECP256K1.KeyPair kp = SECP256K1.KeyPair.create(privkey);
        SECP256K1.PublicKey pubKey = kp.getPublicKey();
        Keccak.Digest256 digest = new Keccak.Digest256();
        digest.update(pubKey.getEncoded(), 0, 64);
        byte[] raw = digest.digest();
        byte[] rawAddr = new byte[21];
        rawAddr[0] = 0x41;
        System.arraycopy(raw, 12, rawAddr, 1, 20);
        String address = Base58Check.bytesToBase58(parseAddress(toHex(rawAddr)).toByteArray());
        return address;
    }
    /**
     *
     * */

}
