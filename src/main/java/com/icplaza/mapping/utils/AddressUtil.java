package com.icplaza.mapping.utils;

import org.bouncycastle.util.encoders.Hex;
import org.tron.trident.utils.Base58Check;

public class AddressUtil {
    public static String zero() {
        return "icplaza1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq72p6qn";
    }
    /**
     * icplaza类型地址转为bsc|eth地址
     */
    public static String convertICPlazaToEth(String from) throws Exception {
        Bech32.Bech32Data data = Bech32.decode(from);
        byte[] dec = data.getData();
        dec = ConvertBits.convertBits(dec, 0, dec.length, 5, 8, false);
        return "0x" + Hex.toHexString(dec);
    }

    /**
     * 从以太坊地址生成cosmos地址
     */
    public static String convertEthAddressToCosmos(String ethAddress, String prefix) throws Exception {
        if (ethAddress.startsWith("0x")) {
            ethAddress = ethAddress.substring(2);
        }
        byte[] bytes = Hex.decode(ethAddress);

        byte[] addr = ConvertBits.convertBits(bytes, 0, bytes.length, 8, 5, true);
        return Bech32.encode(prefix, addr);
    }

    /**
     * 检查波场地址
     */
    public static boolean validTRONAddress(String address) {
        if (!address.startsWith("T")) {
            return false;
        }
        try {
            TronTool.parseAddress(address);
            return true;
        } catch (Exception e) {
            // ignore
        }
        return false;
    }

    /**
     * 检查bsc地址
     */
    public static boolean validBSCAddress(String address) {
        if (address.startsWith("0x")) {
            address = address.substring(2);
        }
        //长度
        if (address.length() != 40) {
            return false;
        }
        //16进制
        try {
            Hex.decode(address);
            return true;
        } catch (Exception e) {
            // ignore
        }
        return false;
    }
    /**
     * 0x to tron地址
     * */
    public static String from0xToTron(String addr) {
        addr = addr.startsWith("0x") ? addr.substring(2) : addr;
        addr = "41" + addr;
        byte[] bytes = Hex.decode(addr);
        return Base58Check.bytesToBase58(bytes);
    }
    /**
     * tron to 0x
     * */
    public static String fromTronTo0x(String addr) {
        byte[] rawAddr = Base58Check.base58ToBytes(addr);
        return "0x"+Hex.toHexString(rawAddr).substring(2);
    }
}
