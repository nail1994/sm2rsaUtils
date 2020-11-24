package com.service;

import com.util.CryptoUtils;
import com.util.HSMDataPackage;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class RsaMessageService {


    /****
     * sm2解密方法。
     * @param value 需要解密的密文
     * @param privateKeySM2 sm2私钥
     * @return
     * @throws IOException
     */
    public String sm2Decode(String value, String privateKeySM2) throws IOException {
        HSMDataPackage hsmDataPackage = new HSMDataPackage();
        Vector<String> sm2Str = hsmDataPackage.makeRequestKF(privateKeySM2, value);
        String result = sm2Str.get(1);
        return new String(CryptoUtils.hex2byte(sm2Str.get(2)), "GBK");
    }

    /***
     * 公钥拆分
     * @param publicKeySM2
     * @return
     */
    public Map<String, String> getPublicKeyXY(String publicKeySM2) {
        int len = publicKeySM2.length();
        String publicKeyX = publicKeySM2.substring(0, len / 2);
        String publicKeyY = publicKeySM2.substring(len / 2);
        Map<String, String> resultMap = new HashMap<>(8);
        resultMap.put("publicKeyX", publicKeyX);
        resultMap.put("publicKeyY", publicKeyY);
        return resultMap;
    }

    /****
     * 加密
     * @param publicKeyX x公钥
     * @param publicKeyY y公钥
     * @param value 需要sm2加密的参数
     * @return 返回密文
     * @throws IOException
     */
    public String sm2Encrypt(String publicKeyX, String publicKeyY, String value) throws IOException {
        HSMDataPackage hsmDataPackage = new HSMDataPackage();
        Vector<String> sm2Str = hsmDataPackage.makeRequestKE(publicKeyX, publicKeyY, CryptoUtils.byte2hex(value.getBytes()));
        String result = sm2Str.get(1);
        value = sm2Str.get(2);
        return value;
    }

    /****
     * RSA解密方法。
     * @param value 需要解密的密文
     * @param privateKeySM2 sm2私钥
     * @return
     * @throws IOException
     */
    public String rsaDecode(String value, String privateKeySM2) throws IOException {
        HSMDataPackage hsmDataPackage = new HSMDataPackage();
        String payPwdHex = new String(Hex.encodeHex(org.apache.commons.codec.binary.Base64.decodeBase64(value.getBytes())));
        Vector<String> strings2 = hsmDataPackage.makeRequest33(privateKeySM2, payPwdHex);
        return new String(CryptoUtils.hexToByte(strings2.get(2)));
    }

    public String encryptHex(String publicKeyString, String data) {
        return org.apache.commons.codec.binary.Base64.encodeBase64String(encrypt("RSA", publicKeyString, data));
    }

    public byte[] encrypt(String algorithm, String publicKeyString, String data) {
        try {
            PublicKey publicKey = getPubKey(publicKeyString);
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(data.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new IllegalArgumentException("encrypt error with algorithm:" + algorithm + "publicKeyString:" + publicKeyString + "data:" + data, e);
        }
    }

    private PublicKey getPubKey(String publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec pubX509 = new X509EncodedKeySpec(CryptoUtils.hex2byte(publicKey));
            return keyFactory.generatePublic(pubX509);
        } catch (Exception e) {
            throw new IllegalArgumentException("公钥配置不正确,无法生成公钥", e);
        }
    }

}
