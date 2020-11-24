package com.model;

/****
 * @author dwb
 * SM2 加解密 模型类
 */
public class SM2Model
{
    private String privateKey;

    private String publicKey;

    private String value;

    private String printStr;

    public SM2Model(String privateKey, String publicKey, String value) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.value = value;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getPrintStr() {
        return printStr;
    }

    public void setPrintStr(String printStr) {
        this.printStr = printStr;
    }
}
