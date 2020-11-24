package com.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Enumeration;

/**
 * CryptoUtils 提供了一个安全算法类,其中包括对称密码算法和散列算法
 */
public final class CryptoUtils
{
	static
	{
		Security.addProvider(new BouncyCastleProvider());
	}

	/**
	 * DES对称加密方法
	 * @param byteSource 需要加密的数据
	 * @return 经过加密的数据
	 * @throws Exception
	 */
	public static byte[] symmetricEncrypto(byte[] keyData, byte[] byteSource) throws Exception
	{
		//检测系统是否已加载此Provider的方法
		if(null==Security.getProvider("BC"))
		{
			//加载Provider
			Security.addProvider(new BouncyCastleProvider());
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int mode = Cipher.ENCRYPT_MODE;
		try
		{
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES", "BC");
			DESKeySpec keySpec = new DESKeySpec(keyData);
			Key key = keyFactory.generateSecret(keySpec);
			Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding", "BC");
			cipher.init(mode, key);

			//必须用下面Stream实现，用原cipher.update和doFinal方法加密还是会在后面Padding内容。
			CipherOutputStream cOut = new CipherOutputStream(baos, cipher);
			cOut.write(byteSource);
			cOut.close();
			return baos.toByteArray();
		}
		catch(Exception e)
		{
			throw e;
		}
		finally
		{
			baos.close();
		}
	}

	/**
	 * DES对称解密方法
	 * @param byteSource 需要解密的数据
	 * @return 经过解密的数据
	 * @throws Exception
	 */
	public static byte[] symmetricDecrypto(byte[] keyData, byte[] byteSource) throws Exception
	{
		//检测系统是否已加载此Provider的方法
		if(null==Security.getProvider("BC"))
		{
			//加载Provider
			Security.addProvider(new BouncyCastleProvider());
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int mode = Cipher.DECRYPT_MODE;
		try
		{
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES", "BC");
			DESKeySpec keySpec = new DESKeySpec(keyData);
			Key key = keyFactory.generateSecret(keySpec);
			Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding", "BC");
			cipher.init(mode, key);

			CipherOutputStream cOut = new CipherOutputStream(baos, cipher);
			cOut.write(byteSource);
			cOut.close();
			return baos.toByteArray();
		}
		catch(Exception e)
		{
			throw e;
		}
		finally
		{
			baos.close();
		}
	}

	/**
	 * 散列算法
	 * @param strAlgorithm 算法，"SHA-1"或"MD5"
	 * @param byteSource 需要散列计算的数据
	 * @return 经过散列计算的数据
	 * @throws NoSuchAlgorithmException
	 * @throws Exception
	 */
	public static byte[] doHash(String strAlgorithm, byte[] byteSource) throws NoSuchAlgorithmException
	{
		MessageDigest currentAlgorithm = MessageDigest.getInstance(strAlgorithm);
		//currentAlgorithm.reset();
		currentAlgorithm.update(byteSource);
		return currentAlgorithm.digest();
	}

	/**
	 * 散列算法
	 * @param strAlgorithm 算法，"SHA-1"或"MD5"
	 * @param byteSource 需要散列计算的数据
	 * @return 经过散列计算的数据
	 * @throws NoSuchAlgorithmException
	 * @throws Exception
	 */
	public static String doHash(String strAlgorithm, String strSource) throws NoSuchAlgorithmException
	{
		MessageDigest currentAlgorithm = MessageDigest.getInstance(strAlgorithm);
		//currentAlgorithm.reset();
		currentAlgorithm.update(strSource.getBytes());
		return byte2hex(currentAlgorithm.digest());
	}

	/**
	 * 校验散列值
	 * @param strAlgorithm 算法，"SHA-1"或"MD5"
	 * @param byteSource 需要散列计算的数据
	 * @param byteHash 需要验证的散列数据
	 * @return 校验结果true或false
	 * @throws NoSuchAlgorithmException
	 * @throws Exception
	 */
	public static boolean verifyHash(String strAlgorithm, byte[] byteSource, byte[] byteHash) throws NoSuchAlgorithmException
	{
		MessageDigest alga= MessageDigest.getInstance(strAlgorithm);
		alga.update(byteSource);
		byte[] digesta=alga.digest();
		if( MessageDigest.isEqual(digesta, byteHash) )
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * 校验散列值
	 * @param strAlgorithm 算法，"SHA-1"或"MD5"
	 * @param strSource 需要散列计算的文本数据
	 * @param strHexHash 需要验证的散列数据，16进制字串表示
	 * @return 校验结果true或false
	 * @throws NoSuchAlgorithmException
	 * @throws Exception
	 */
	public static boolean verifyHash(String strAlgorithm, String strSource, String strHexHash) throws NoSuchAlgorithmException
	{
		MessageDigest alga= MessageDigest.getInstance(strAlgorithm);
		alga.update(strSource.getBytes());
		byte[] digesta=alga.digest();
		if( MessageDigest.isEqual(digesta, hex2byte(strHexHash)) )
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * 签名
	 * @param strKeyStorePath keystore文件路径
	 * @param strKeyStorePass keystore密码
	 * @param strPKIAlias 密钥对的别名
	 * @param strPKIPass 提取私钥的密码
	 * @param byteSource 需要被签名的数据
	 * @param algorithm 签名算法
	 * @return 经过签名计算的数据
	 * @throws Exception
	 */
	public static byte[] signByKeyStore(String strKeyStorePath, String strKeyStorePass, String strPKIAlias, String strPKIPass, byte[] byteSource, String algorithm) throws Exception
	{
		//检测系统是否已加载此Provider的方法
		if(null==Security.getProvider("BC"))
		{
			//加载Provider
			Security.addProvider(new BouncyCastleProvider());
		}

		try
		{
			//从密钥库中直接读取证书
			FileInputStream in=new FileInputStream(strKeyStorePath);
			KeyStore ks=KeyStore.getInstance("JKS");
			ks.load(in,strKeyStorePass.toCharArray());
			//从密钥库中读取CA的私钥
			PrivateKey myprikey=(PrivateKey)ks.getKey(strPKIAlias,strPKIPass.toCharArray());
			//用他私人密钥(prikey)对他所确认的信息(info)进行数字签名产生一个签名数组
			//初始一个Signature对象,并用私钥对信息签名
			Signature signet= Signature.getInstance(algorithm,"BC");
			signet.initSign(myprikey);
			signet.update(byteSource);
			return signet.sign();
		}
		catch(Exception e)
		{
			throw e;
		}
	}

	/**
	 * 校验签名
	 * @param strKeyStorePath keystore文件路径
	 * @param strKeyStorePass keystore密码
	 * @param strPKIAlias 密钥对的别名
	 * @param byteSource 被签名的数据
	 * @param byteSigned 需要被校验的签名
	 * @param algorithm 签名算法
	 * @return 验证签名的结果
	 * @throws Exception
	 */
	public static boolean verifyByKeyStore(String strKeyStorePath, String strKeyStorePass, String strPKIAlias, byte[] byteSource, byte[] byteSigned, String algorithm) throws Exception
	{
		//检测系统是否已加载此Provider的方法
		if(null==Security.getProvider("BC"))
		{
			//加载Provider
			Security.addProvider(new BouncyCastleProvider());
		}

		try
		{
			//从密钥库中直接读取证书
			FileInputStream in=new FileInputStream(strKeyStorePath);
			KeyStore ks=KeyStore.getInstance("JKS");
			ks.load(in,strKeyStorePass.toCharArray());
			//从密钥库中读取证书
			java.security.cert.Certificate c1=ks.getCertificate(strPKIAlias);
			//初始一个Signature对象,并用证书对签名作校验
			Signature signet= Signature.getInstance(algorithm,"BC");
			signet.initVerify(c1);
			signet.update(byteSource);
			return signet.verify(byteSigned);
		}
		catch(Exception e)
		{
			throw e;
		}
	}

	/**
	 * 校验签名
	 * @param byteSrc asc码数组
	 * @return bcd码编码到的byte数组
	 */
	public static byte[] asc2bcd(byte[] byteSrc, int nLen)
	{
		return null;
	}

	/**
	 * 校验签名
	 * @param byteSrc bcd码数组
	 * @return 解码出来的asc码byte数组
	 */
	public static byte[] bcd2asc(byte[] byteSrc, int nLen)
	{
		return null;
	}

	/**
	 * 功能：byte数值数组转化成16进制字符字串
	 * @param b byte数组数据
	 * @return byte数值数组转化成16进制字符的字串
	 */
	public static String byte2hex(byte[] b)
	{
		String hs = "";
		String stmp = "";
		for (int i = 0; i < b.length; i++)
		{
			stmp = Integer.toHexString(b[i] & 0xFF);
			if (stmp.length() == 1)
			{
				hs += "0" + stmp;
			}
			else
			{
				hs += stmp;
			}
		}
		return hs.toUpperCase();
	}

	/**
	 * 功能：16进制字符字串转化成byte数值数组
	 * @param hex 16进制字符字串
	 * @return 16进制字符字串转化成byte数值的数组
	 */
	public static byte[] hex2byte(String hex) throws IllegalArgumentException
	{
		if (hex.length() % 2 != 0)
		{
			throw new IllegalArgumentException();
		}
		char[] arr = hex.toCharArray();
		byte[] b = new byte[hex.length() / 2];
		for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++)
		{
			String swap = "" + arr[i++] + arr[i];
			int byteint = Integer.parseInt(swap, 16) & 0xFF;
			b[j] = new Integer(byteint).byteValue();
		}
		return b;
	}

	//===============RSA密钥对生成和签名校验==================
	/*
	 * 功能：生成证书私钥der编码数据，此2数据可以导出被存为文件
	 * */
	public static KeyPair generateKeyPair(String strAlgorithm, Provider provider, int nKeySize) throws NoSuchAlgorithmException
	{
		/****生成证书，导出文件****/
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(strAlgorithm, provider);
		//final int KEY_SIZE = 1024;//没什么好说的了，这个值关系到块加密的大小，可以更改，但是不要太大，否则效率会低
		keyPairGen.initialize(nKeySize, new SecureRandom());
		return keyPairGen.genKeyPair();
	}

	/*
	 * 功能：从文件中取得公钥der编码数据，生成公钥对象
	 * */
	public static PublicKey getFilePublicKey(String strAlgorithm, String strFilePath) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException
	{
		KeyFactory kf = KeyFactory.getInstance(strAlgorithm);
		File file = new File(strFilePath);
		FileInputStream fis = new FileInputStream(file);
		byte bPublicKey[] = new byte[(int)file.length()];
		BufferedInputStream bis = new BufferedInputStream(fis);
		if (bis.available() > 0) {
			bis.read(bPublicKey);
		}
		bis.close();
		X509EncodedKeySpec keySpecPublic = new X509EncodedKeySpec(bPublicKey);
		return kf.generatePublic(keySpecPublic);
	}

	/*
	 * 功能：从文件中取得私钥der编码数据，生成私钥对象
	 * */
	public static PrivateKey getFilePrivateKey(String strAlgorithm, String strFilePath) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException
	{
		KeyFactory kf = KeyFactory.getInstance(strAlgorithm);
		File file = new File(strFilePath);
		FileInputStream fis = new FileInputStream(file);
		byte bPrivateKey[] = new byte[(int)file.length()];
		BufferedInputStream bis = new BufferedInputStream(fis);
		if (bis.available() > 0) {
			bis.read(bPrivateKey);
		}
		bis.close();
		PKCS8EncodedKeySpec keySpecPrivate = new PKCS8EncodedKeySpec(bPrivateKey);
		return kf.generatePrivate(keySpecPrivate);
	}

	/*
	 * 功能：用私钥对信息签名
	 * */
	public static byte[] sign(String strAlgorithm, Provider provider, PrivateKey privateKey, byte[] btSignedData) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException
	{
		//初始一个Signature对象,并用私钥对信息签名
		Signature signet= Signature.getInstance(strAlgorithm, provider);

		signet.initSign(privateKey);
		signet.update(btSignedData);
		return signet.sign();
	}

	/*
	 * 功能：用私钥对信息签名
	 * */
	public static String sign(String strAlgorithm, Provider provider, PrivateKey privateKey, String strSignedData) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException
	{
		//初始一个Signature对象,并用私钥对信息签名
		Signature signet= Signature.getInstance(strAlgorithm, provider);

		signet.initSign(privateKey);
		signet.update(strSignedData.getBytes());
		return byte2hex(signet.sign());
	}

	/*
	 * 功能：用公钥对签名信息进行校验
	 * */
	public static boolean verifySign(String strAlgorithm, Provider provider, PublicKey publicKey, byte[] btSignedData, byte[] btDigest) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException
	{
		//初始一个Signature对象,并用私钥对信息签名
		Signature signet= Signature.getInstance(strAlgorithm, provider);

		signet.initVerify(publicKey);
		signet.update(btSignedData);
		return signet.verify(btDigest);
	}

	/*
	 * 功能：用公钥对签名信息进行校验
	 * */
	public static boolean verifySign(String strAlgorithm, Provider provider, PublicKey publicKey, String strSignedData, String strDigest) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException
	{
		//初始一个Signature对象,并用私钥对信息签名
		Signature signet= Signature.getInstance(strAlgorithm, provider);

		signet.initVerify(publicKey);
		signet.update(strSignedData.getBytes());
		return signet.verify(hex2byte(strDigest));
	}

	/*
	 * 功能：用PKCS12证书私钥对信息签名
	 * */
	public static String signByPKCS12(String strAlgorithm, byte[] privateKey, String password, String strSignedData)
	{
		String tAlias;
		try {
			tAlias = new String();
			char[] carrayPwd = password.toCharArray();

			KeyStore tKeystore = KeyStore.getInstance("PKCS12");
			tKeystore.load(new ByteArrayInputStream(privateKey), carrayPwd);

			Enumeration<?> e = tKeystore.aliases();
			if (e.hasMoreElements()) tAlias = (String) e.nextElement();
			//System.out.println("tAlias:"+tAlias);

			PrivateKey tPrivateKey = (PrivateKey) tKeystore.getKey(tAlias, carrayPwd);

			//初始一个Signature对象,并用私钥对信息签名
			Signature tSign = Signature.getInstance(strAlgorithm);//, provider
			tSign.initSign(tPrivateKey);

			tSign.update(strSignedData.getBytes());
			byte[] tSignedText = tSign.sign();

			return byte2hex(tSignedText);
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}

	/*
	 * 功能：用PKCS12证书私钥对信息签名
	 * */
	public static String signByPKCS12(String strAlgorithm, String privateKeyFile, String password, String strSignedData)
	{
		String tAlias;
		try {
			tAlias = new String();
			char[] carrayPwd = password.toCharArray();

			KeyStore tKeystore = KeyStore.getInstance("PKCS12");
			FileInputStream input = new FileInputStream(privateKeyFile);
			tKeystore.load(input, carrayPwd);

			Enumeration<?> e = tKeystore.aliases();
			if (e.hasMoreElements()) tAlias = (String) e.nextElement();
			//System.out.println("tAlias:"+tAlias);

			PrivateKey tPrivateKey = (PrivateKey) tKeystore.getKey(tAlias, carrayPwd);

			//初始一个Signature对象,并用私钥对信息签名
			Signature tSign = Signature.getInstance(strAlgorithm);//, provider
			tSign.initSign(tPrivateKey);

			tSign.update(strSignedData.getBytes());
			byte[] tSignedText = tSign.sign();

			return byte2hex(tSignedText);
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}

	/*
	 * 功能：用Cer证书公钥对签名信息进行校验
	 * */
	public static boolean verifySignByCer(String strAlgorithm, byte[] publicKey, String strSignedData, String strDigest)
	{
		boolean tResult = false;
		try {
			CertificateFactory tCertFactory = CertificateFactory.getInstance("X.509");

			java.security.cert.Certificate tCertificate = tCertFactory.generateCertificate(new ByteArrayInputStream(publicKey));

			Signature tSign = Signature.getInstance(strAlgorithm);//, "BC"
			tSign.initVerify(tCertificate);

			tSign.update(strSignedData.getBytes());
			tResult = tSign.verify(hex2byte(strDigest));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return tResult;
	}

	/*
	 * 功能：用Cer证书公钥对签名信息进行校验
	 * */
	public static boolean verifySignByCer(String strAlgorithm, String publicKeyFile, String strSignedData, String strDigest)
	{
		boolean tResult = false;
		try {
			InputStream inStream = new FileInputStream(publicKeyFile);
			CertificateFactory tCertFactory = CertificateFactory.getInstance("X.509");

			java.security.cert.Certificate tCertificate = tCertFactory.generateCertificate(inStream);

			Signature tSign = Signature.getInstance(strAlgorithm);//, "BC"
			tSign.initVerify(tCertificate);

			tSign.update(strSignedData.getBytes());
			tResult = tSign.verify(hex2byte(strDigest));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return tResult;
	}


	/**
	 * 功能：10进制串转为BCD码
	 *
	 * @param str 待转的字符串
	 * @return bcd码编码到的byte数组
	 */
	public static byte[] str2bcd(String str)
	{
		if(str.length()%2!=0) str="0"+str;

		StringBuffer sb = new StringBuffer(str);
		ByteBuffer   bb = ByteBuffer.allocate(str.length()/2);

		int i=0;
		while(i<str.length())
		{
			bb.put((byte)((Integer.parseInt(sb.substring(i,i+1)))<<4|Integer.parseInt(sb.substring(i+1,i+2))));
			i=i+2;
		}
		return bb.array();
	}

	/**
	 * ANSIX9.8格式
	 * @param strPassword
	 * @param strCardNo
	 * @return
	 */
	public static byte[] pinBlock(String strPassword, String strCardNo)
	{
		//PIN BLOCK - 8位
		byte[] bytesPin = new byte[]{(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF};
		bytesPin[0] = (byte)strPassword.length();
		byte[] bcdPwd = str2bcd(strPassword);
		System.arraycopy(bcdPwd, 0, bytesPin, 1, bcdPwd.length);
		//PAN  - 这里没算了前面的0，是6位
		int nLength = strCardNo.length();
		String strCardNo12 = strCardNo.substring(nLength-13, nLength-1);
		byte[] bcdPAN = str2bcd(strCardNo12);
		//异或
		byte[] bytesPinBlock = new byte[8];
		bytesPinBlock[0] = bytesPin[0];
		bytesPinBlock[1] = bytesPin[1];
		for(int i=2;i<8;i++)
		{
			bytesPinBlock[i] = (byte)(bytesPin[i]^bcdPAN[i-2]);
		}
		return bytesPinBlock;
	}

	/*******
	 * 功能：商赢系统编码
	 * @throws NoSuchAlgorithmException
	 * */
	public static String sanwingEncode(long lngID, String strPassword) throws NoSuchAlgorithmException
	{
		StringBuilder sb = new StringBuilder();
		//1)添加[ID值*2+119]
		//2)添加密码
		//3)添加[金莲花开]
		//4)进行MD5算法，得到签名串
		sb.append(lngID*2+119).append(strPassword).append("金莲花开");
		String strContent = sb.toString();
		String strMD5 = CryptoUtils.doHash("MD5", strContent);
		//System.out.println("第1次MD5的内容："+strContent);
		//System.out.println("第1次MD5的结果："+strMD5);
		//5)反转签名串
		//6)添加[万里江山万里晴，一缕尘心一缕烟。]
		//7)再做MD5算法
		sb.setLength(0);
		sb.append(strMD5);
		sb.reverse();
		sb.append("万里江山万里晴，一缕尘心一缕烟。");
		strContent = sb.toString();
		strMD5 = CryptoUtils.doHash("MD5", strContent);
		//System.out.println("第2次MD5的内容："+strContent);
		//System.out.println("第2次MD5的结果："+strMD5);
		//8)将上次的MD5值分成两份
		//9)再分别做MD5算法
		//10)合并两份的值
		String strContent1 = strMD5.substring(0, 5);
		String strContent2 = strMD5.substring(5);
		strContent = strContent1;
		strMD5 = CryptoUtils.doHash("MD5", strContent);
		sb.setLength(0);
		sb.append(strMD5);
		//System.out.println("第3次MD5的内容："+strContent);
		//System.out.println("第3次MD5的结果："+strMD5);
		strContent = strContent2;
		strMD5 = CryptoUtils.doHash("MD5", strContent);
		sb.append(strMD5);
		//System.out.println("第4次MD5的内容："+strContent);
		//System.out.println("第4次MD5的结果："+strMD5);
		//11)对所得的值再做一次MD5算法
		strContent = sb.toString();
		strMD5 = CryptoUtils.doHash("MD5", strContent);
		//System.out.println("第5次MD5的内容："+strContent);
		//System.out.println("第5次MD5的结果："+strMD5);

		return strMD5;
	}

	public static String decrypt3DES(String data, String key) throws Exception{
		if(key.length() != 32){
			throw new InvalidKeyException();
		}

		String firstKey = key.substring(0, 16);
		String secondKey = key.substring(16, 32);
		String result = null;
		result = decryptDES(data, firstKey);
		result = encryptDES(result, secondKey);
		result = decryptDES(result, firstKey);

		return result;
	}
	public static String decryptDES(String data, String key) throws Exception {
		SecretKeySpec secretKey = new SecretKeySpec(hex2byte(key), "DES");
		Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte decryptedData[] = cipher.doFinal(hex2byte(data));
		return byte2hex(decryptedData);
	}
	public static String encryptDES(String data, String key) throws Exception {
		SecretKeySpec secretKey = new SecretKeySpec(hex2byte(key), "DES");
		Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] encryptedData = cipher.doFinal(hex2byte(data));
		return byte2hex(encryptedData);
	}


	/**
	 * 十六进制串转化为byte数组
	 *
	 * @return the array of byte
	 */
	public static byte[] hexToByte(String hex)
			throws IllegalArgumentException {
		if (hex.length() % 2 != 0) {
			throw new IllegalArgumentException();
		}
		char[] arr = hex.toCharArray();
		byte[] b = new byte[hex.length() / 2];
		for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++) {
			String swap = "" + arr[i++] + arr[i];
			int byteint = Integer.parseInt(swap, 16) & 0xFF;
			b[j] = new Integer(byteint).byteValue();
		}
		return b;
	}

} 
