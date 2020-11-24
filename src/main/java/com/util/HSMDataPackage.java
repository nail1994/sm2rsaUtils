package com.util;


import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Vector;

/**
 * @author fenglinzi
 *
 * 加密机数据操作类�?
 */
public class HSMDataPackage {

		private String _CfgPath = "src/com/util/hsm.property";
	private byte _Version = 1;
	private ByteArrayOutputStream _ByteArrayBuffer = new ByteArrayOutputStream();
	//为了保持长连接的通讯，使用static
	private DataInputStream _InputFromHSM = null;
	private DataOutputStream _OutToHSM = null;
	private static String _HSMHost = "";
	private static int _nPort = 0;
	private static String _FromEncPIK = "";
	public void setFromEncPIK(String fromEncPIK) {
		_FromEncPIK = fromEncPIK;
	}

	private static String _FromTMNKey01 = "";
	private static String _FromTMNKey02 = "";
	public static String getFromTMNKey(String strFromCode)
	{
		switch(NumberUtils.parseAsInt(strFromCode, 0))
		{
			case 1:
				return _FromTMNKey01;
			case 2:
				return _FromTMNKey02;
			default:
				return null;
		}
	}

	private static String _FromEncPIK01 = "";
	private static String _FromEncPIK02 = "";
	public static String getFromEncPIK(String strFromCode)
	{
		switch(NumberUtils.parseAsInt(strFromCode, 0))
		{
			case 1:
				return _FromEncPIK01;
			case 2:
				return _FromEncPIK02;
			default:
				return null;
		}
	}

	private static String _FromMACKey01 = "";
	private static String _FromMACKey02 = "";
	public static String getFromMACKey(String strFromCode)
	{
		switch(NumberUtils.parseAsInt(strFromCode, 0))
		{
			case 1:
				return _FromMACKey01;
			case 2:
				return _FromMACKey02;
			default:
				return null;
		}
	}

	//Local PIK
	//private String _LocalEncPIK = "X8B4ECCAE01B4B17A8B4ECCAE01B4B17A";
	private static String _LocalEncPIK = "";
	public void setLocalEncPIK(String localEncPIK) {
		_LocalEncPIK = localEncPIK;
	}

	//To PIK
	private static String _ToEncPIK = "";

	//public static String _FixCardDataZEK = "X1031CAFD652E389DB2C0636A4578460C";//为加密卡磁道信息�?专用，不能用每次产生的那个ZEK，因为那个是每次变动�?
	//public static String _FixCardDataZAK = "X6BD0404B2683C660CCB63483635814B3";//为加密卡磁道信息�?专用，不能用每次产生的那个ZEK，因为那个是每次变动�?
	public static String _FixCardDataZAK = "";

	public static String _MakeCardZMK = "";
	public static String _TMNZEK = "";

	/**
	 * @return the _TMNZEK
	 */
	public static String getTMNZEK() {
		return _TMNZEK;
	}

	/**
	 * @param _TMNZEK the _TMNZEK to set
	 */
	public static void setTMNZEK(String _TMNZEK) {
		HSMDataPackage._TMNZEK = _TMNZEK;
	}

	public HSMDataPackage() {
		super();
		// TODO Auto-generated constructor stub
		if(_HSMHost.equals(""))
		{
			java.util.Properties properties = new java.util.Properties();
			FileInputStream input;
			try {
				input = new FileInputStream(_CfgPath);
				properties.load(input);
				_HSMHost = properties.getProperty("host");
				_nPort = Integer.parseInt(properties.getProperty("port"));
				_FromTMNKey01 = properties.getProperty("from_TMK01");
				_FromTMNKey02 = properties.getProperty("from_TMK02");
				_FromEncPIK01 = properties.getProperty("from_PIK01");
				_FromEncPIK02 = properties.getProperty("from_PIK02");
				_FromMACKey01 = properties.getProperty("from_MAK01");
				_FromMACKey02 = properties.getProperty("from_MAK02");
				_FromEncPIK = properties.getProperty("from_PIK");
				_LocalEncPIK = properties.getProperty("local_PIK");
				//_ToEncPIK
				_FixCardDataZAK = properties.getProperty("fix_carddata_ZAK");
				_MakeCardZMK = properties.getProperty("make_card_ZMK");
				_TMNZEK = properties.getProperty("tmn_ZEK");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/*功能：接收和解析数据*/
	public byte[] receive(DataInputStream input) throws IOException
	{
		int nLen = 0;
		byte[] bBufferHeader = new byte[2];
		nLen = input.read(bBufferHeader);
		if(nLen<=0) return null;
//		System.out.println("bBufferHeader[0]:"+bBufferHeader[0]);
//		System.out.println("bBufferHeader[1]:"+bBufferHeader[1]);

		_ByteArrayBuffer.reset();
		//nLen = bBufferHeader[0]*0x100+bBufferHeader[1];//error
		nLen = (bBufferHeader[0]<<8)|(0xFF&bBufferHeader[1]);//必须用位运算0xFF来过滤掉字节前面附加的符号位，否则二进制表达时前面有1的就当作负数来处理了�?+或|操作的时候都会出�?
		//System.out.println("receive Data nLen:"+nLen);

		byte[] bBufferData = new byte[nLen];
		int nReadTotal = 0;
		int nReadLen = 0;
		while( nReadTotal<nLen && 0<(nReadLen=input.read(bBufferData)) )
		{
			_ByteArrayBuffer.write(bBufferData, 0, nLen);
			nReadTotal += nReadLen;
		}

		return _ByteArrayBuffer.toByteArray();
	}

	/*功能：不打包直接输出数据*/
	public void send(DataOutputStream output, byte[] bBuffer, int nLength) throws IOException
	{
		output.flush();
		output.write(bBuffer, 0, nLength);
		output.flush();
	}

	/*功能：制作请求数据包，把各域的字符串拼接起来*/
	public static byte[] makeRequestData(Vector vecData) throws IOException
	{
		ByteArrayOutputStream byteArrayRetData = new ByteArrayOutputStream();
		Iterator ite = vecData.iterator();
		while(ite.hasNext())
		{
			String strFieldValue = (String)ite.next();
			byteArrayRetData.write(strFieldValue.getBytes());
		}

		return byteArrayRetData.toByteArray();
	}

	/*功能：添加空域到vecData*/
	public static void appendEmptyTradeField(Vector vecData, int nStart, int nEnd)
	{
		for(int i=nStart; i<=nEnd; ++i)
		{
			vecData.add("");
		}
	}

	/*功能：制作数据请求的包头--包头长度请求*/
	/**
	 * @param byteData 待发送的数据包体
	 * @return
	 * @throws IOException
	 */
	public static byte[] makeRequestHeader(byte[] byteData) throws IOException
	{
		int nSize = byteData.length;
		byte[] bRet = new byte[2];
		bRet[0] = (byte)(0xFF&(nSize>>8));
		bRet[1] = (byte)(0xFF&nSize);

		return bRet;
	}

	/*功能：制作数据请求的包头--包头长度请求*/
	/**
	 * @param byteData 待发送的数据包体
	 * @return
	 * @throws IOException
	 */
	public static byte[] makeHexRequestHeader(byte[] byteData) throws IOException
	{
		int nSize = byteData.length/2;
		byte[] bRet = new byte[2];
		bRet[0] = (byte)(0xFF&(nSize>>8));
		bRet[1] = (byte)(0xFF&nSize);

		return bRet;
	}

	/*功能：制作请求数据包--BA--加密明文pin请求*/
	/**
	 * @param strPassword 待加密的密码明文
	 * @param strCard12   卡号后面除了校验位的12�?
	 * @return
	 * @throws IOException
	 */
	public static byte[] makeRequestBA(String strPassword, String strCard12) throws IOException
	{
		String strCommand = "BA";
		Vector<String> vecData = new Vector<String>(3);
		vecData.add(strCommand);
		StringBuilder sb = new StringBuilder();
		sb.append(strPassword).append("FFFFFFF".substring(0,7-strPassword.length()));
		vecData.add(sb.toString());
		vecData.add(strCard12);

		return makeRequestData(vecData);
	}

	/* * * *
	 * 功能：解析交易数据包
	 * 参数�?
	 * bBuffer   - 要解析的数据缓冲�?
	 * */
	public static Vector<String> parseResponseBB(byte[] bBuffer) throws java.util.NoSuchElementException
	{
		Vector<String> vecData = new Vector<String>(3);

		//1-响应代码
		vecData.addElement(new String(bBuffer, 0, 2));
		//2-错误代码
		vecData.addElement(new String(bBuffer, 2, 2));
		//3-PIN
		vecData.addElement(new String(bBuffer, 4, 7));

		return vecData;
	}

	/*功能：制作请求数据包--JG--将PIN从LMK翻译到ZPK(LMK)请求*/
	/**
	 * @param strEncPIK 待加密的密码明文
	 * @param strEncPassword 待加密的密码明文
	 * @param strCard12   卡号后面除了校验位的12�?
	 * @return
	 * @throws IOException
	 */
	public static byte[] makeRequestJG(String strEncPIK, String strEncPassword, String strCard12) throws IOException
	{
		ByteArrayOutputStream byteArrayRetData = new ByteArrayOutputStream();
		String strCommand = "JG";
		byteArrayRetData.write(strCommand.getBytes());
		byteArrayRetData.write(strEncPIK.getBytes());
		byteArrayRetData.write("01".getBytes());
		byteArrayRetData.write(strCard12.getBytes());
		byteArrayRetData.write(strEncPassword.getBytes());

		return byteArrayRetData.toByteArray();//makeRequestData(vecData);
	}

	/* * * *
	 * 功能：解析交易数据包
	 * 参数�?
	 * bBuffer   - 要解析的数据缓冲�?
	 * */
	public static Vector<String> parseResponseJH(byte[] bBuffer) throws java.util.NoSuchElementException
	{
		Vector<String> vecData = new Vector<String>(3);

		//1-响应代码
		vecData.addElement(new String(bBuffer, 0, 2));
		//2-错误代码
		vecData.addElement(new String(bBuffer, 2, 2));
		//3-PIN
		vecData.addElement(new String(bBuffer, 4, 16));

		return vecData;
	}

	/*功能：制作请求数据包--CC--pin转加密：终端ZPK->后台ZPK(LMK)请求*/
	/**
	 * @param strCard12   卡号后面除了校验位的12�?
	 * @return
	 * @throws IOException
	 */
	public static byte[] makeRequestCC(String strInEncPIK, String strOutEncPIK, String strEncPassword, String strCard12) throws IOException
	{
		ByteArrayOutputStream byteArrayRetData = new ByteArrayOutputStream();
		String strCommand = "CC";
		byteArrayRetData.write(strCommand.getBytes());
		byteArrayRetData.write(strInEncPIK.getBytes());
		byteArrayRetData.write(strOutEncPIK.getBytes());
		byteArrayRetData.write("12".getBytes());
		byteArrayRetData.write(strEncPassword.getBytes());
		byteArrayRetData.write("01".getBytes());
		byteArrayRetData.write("01".getBytes());
		byteArrayRetData.write(strCard12.getBytes());

		return byteArrayRetData.toByteArray();
	}

	/*功能：制作请求数据包--CC--pin转加密：终端ZPK->后台ZPK(LMK)请求*/
	/**
	 * @param strCard12   卡号后面除了校验位的12�?
	 * @return
	 * @throws IOException
	 */
	public static byte[] makeRequestCC(String strInEncPIK, String strOutEncPIK, String strEncPassword, String strCard12, String strSrcFormat, String strDstFormat) throws IOException
	{
		ByteArrayOutputStream byteArrayRetData = new ByteArrayOutputStream();
		String strCommand = "CC";
		byteArrayRetData.write(strCommand.getBytes());
		byteArrayRetData.write(strInEncPIK.getBytes());
		byteArrayRetData.write(strOutEncPIK.getBytes());
		byteArrayRetData.write("12".getBytes());
		byteArrayRetData.write(strEncPassword.getBytes());
		byteArrayRetData.write(strSrcFormat.getBytes());
		byteArrayRetData.write(strDstFormat.getBytes());
		byteArrayRetData.write(strCard12.getBytes());

		return byteArrayRetData.toByteArray();
	}

	/* * * *
	 * 功能：解析交易数据包
	 * 参数�?
	 * bBuffer   - 要解析的数据缓冲�?
	 * */
	public static Vector<String> parseResponseCD(byte[] bBuffer) throws java.util.NoSuchElementException
	{
		Vector<String> vecData = new Vector<String>(4);

		//1-响应代码
		vecData.addElement(new String(bBuffer, 0, 2));
		//2-错误代码
		vecData.addElement(new String(bBuffer, 2, 2));
		//3-密码长度06
		vecData.addElement(new String(bBuffer, 4, 2));
		//4-PIN
		vecData.addElement(new String(bBuffer, 6, 16));
		//5-
		vecData.addElement(new String(bBuffer, 22, 2));

		return vecData;
	}

	/*功能：制作请求数据包--CA--pin转加密：终端TPK->后台ZPK(LMK)请求*/
	/**
	 * @param strCard12   卡号后面除了校验位的12�?
	 * @return
	 * @throws IOException
	 */
	public static byte[] makeRequestCA(String strInEncTPK, String strOutEncZPK, String strEncPassword, String strCard12, String strSrcFormat, String strDstFormat) throws IOException
	{
		ByteArrayOutputStream byteArrayRetData = new ByteArrayOutputStream();
		String strCommand = "CA";
		byteArrayRetData.write(strCommand.getBytes());
		byteArrayRetData.write(strInEncTPK.getBytes());
		byteArrayRetData.write(strOutEncZPK.getBytes());
		byteArrayRetData.write("12".getBytes());
		byteArrayRetData.write(strEncPassword.getBytes());
		byteArrayRetData.write(strSrcFormat.getBytes());
		byteArrayRetData.write(strDstFormat.getBytes());
		byteArrayRetData.write(strCard12.getBytes());

		return byteArrayRetData.toByteArray();
	}

	/* * * *
	 * 功能：解析交易数据包
	 * 参数�?
	 * bBuffer   - 要解析的数据缓冲�?
	 * */
	public static Vector<String> parseResponseCB(byte[] bBuffer) throws java.util.NoSuchElementException
	{
		Vector<String> vecData = new Vector<String>(4);

		//1-响应代码
		vecData.addElement(new String(bBuffer, 0, 2));
		//2-密码长度
		vecData.addElement(new String(bBuffer, 2, 2));
		//3-错误代码(要注意：这个指令里密码长度和错误代码的位置和CC/CD指令里的位置是反�?)
		vecData.addElement(new String(bBuffer, 4, 2));
		//4-PIN
		vecData.addElement(new String(bBuffer, 6, 16));
		//5-
		vecData.addElement(new String(bBuffer, 22, 2));

		return vecData;
	}

	/*功能：制作请求数据包--34--产生�?对公私钥密钥�?*/
	/**
	 * @param nKeyLen
	 * @param nKeyIndex
	 * @return
	 * @throws IOException
	 */
	public static byte[] makeRequest34(int nKeyLen, int nKeyIndex) throws IOException
	{
		ByteArrayOutputStream byteArrayRetData = new ByteArrayOutputStream();
		String strCommand = "34";
		byteArrayRetData.write(strCommand.getBytes());
		byteArrayRetData.write(StringUtils.Int2String(nKeyLen, 4).getBytes());
		byteArrayRetData.write(StringUtils.Int2String(nKeyIndex, 2).getBytes());

		return byteArrayRetData.toByteArray();
	}

	/* * * *
	 * 功能：解析交易数据包
	 * 参数�?
	 * bBuffer   - 要解析的数据缓冲�?
	 * */
	public static Vector parseResponse35(byte[] bBuffer) throws java.util.NoSuchElementException
	{
		Vector vecData = new Vector(5);

		//1-响应代码
		vecData.addElement(new String(bBuffer, 0, 2));
		//2-错误代码
		vecData.addElement(new String(bBuffer, 2, 2));
		//3-私钥长度
		String strPrivateKeyLen = new String(bBuffer, 4, 4);
		vecData.addElement(strPrivateKeyLen);
		System.out.println("strPrivateKeyLen:"+strPrivateKeyLen);
		//4-私钥密文
		int nPrivateKeyLen = Integer.parseInt(strPrivateKeyLen);
		byte[] bBufferKey = new byte[nPrivateKeyLen];
		System.arraycopy(bBuffer, 8, bBufferKey, 0, nPrivateKeyLen);
		vecData.addElement(bBufferKey);
		//5-公�??钥：ANS.1 DER编码方式  长度看测试的例子，长度为270字节，一般经�?16进制编码后成字符的长度是512，实际表达的长度�?256，差�?14字节，是因为前有9位后�?5位加密机附加的特殊字符串
		int nPublicKeyLen = 140;//270;
		byte[] bBufferPublicKey = new byte[nPublicKeyLen];
		System.out.println("bBufferPublicKey index:"+(8+nPrivateKeyLen));
		System.out.println("bBufferPublicKey end:"+(8+nPrivateKeyLen+nPublicKeyLen));
		System.arraycopy(bBuffer, 8+nPrivateKeyLen, bBufferPublicKey, 0, nPublicKeyLen);
		vecData.addElement(bBufferPublicKey);

		return vecData;
	}

	/*功能：制作请求数据包--FI--生成ZEK/ZAK请求*/
	/**
	 * @param strZEKZAKFlag
	 * @return
	 * @throws IOException
	 */
	public static byte[] makeRequestFI(String strZEKZAKFlag) throws IOException
	{
		ByteArrayOutputStream byteArrayRetData = new ByteArrayOutputStream();
		String strCommand = "FI";
		byteArrayRetData.write(strCommand.getBytes());
		byteArrayRetData.write(strZEKZAKFlag.getBytes());
		//String strZMK = "5C6FF58D938060B4288A45E9D4E74BEB";
		byteArrayRetData.write(_MakeCardZMK.getBytes());
		byteArrayRetData.write(";XX0".getBytes());

		return byteArrayRetData.toByteArray();
	}

	/* * * *
	 * 功能：解析交易数据包
	 * 参数�?
	 * bBuffer   - 要解析的数据缓冲�?
	 * */
	public static Vector<String> parseResponseFJ(byte[] bBuffer) throws java.util.NoSuchElementException
	{
		Vector<String> vecData = new Vector<String>(5);

		//1-响应代码
		vecData.addElement(new String(bBuffer, 0, 2));
		//2-错误代码
		vecData.addElement(new String(bBuffer, 2, 2));
		//3-1A+32H的ZEK传输
		vecData.addElement(new String(bBuffer, 4, 33));
		//4-1A+32H的ZEK存储
		vecData.addElement(new String(bBuffer, 37, 33));
		//5-16H的ZEK密码校验
		vecData.addElement(new String(bBuffer, 70, 16));

		return vecData;
	}

	/*功能：制作请求数据包--E0--使用带入的密钥进行数据加解密计算*/
	/**
	 * @param strEncFlag 加解密类型：0-DES加密 1-DES解密
	 * @param strZEK
	 * @param strData
	 * @return
	 * @throws IOException
	 */
	public static byte[] makeRequestE0(String strZEK, String strEncFlag, String strData) throws IOException
	{
		ByteArrayOutputStream byteArrayRetData = new ByteArrayOutputStream();
		String strCommand = "E0";
		byteArrayRetData.write(strCommand.getBytes());
		byteArrayRetData.write("0".getBytes());
		byteArrayRetData.write(strEncFlag.getBytes());
		byteArrayRetData.write("10".getBytes());
		byteArrayRetData.write(strZEK.getBytes());
		byteArrayRetData.write("11000000".getBytes());
//		byteArrayRetData.write(StringUtils.Int2String(strData.length(), 3).getBytes());
		StringBuilder sb = new StringBuilder();
		sb.append("000").append(Integer.toHexString(strData.length()/2));
		byteArrayRetData.write(sb.substring(sb.length()-3).getBytes());
		byteArrayRetData.write(strData.getBytes());

		return byteArrayRetData.toByteArray();
	}

	/* * * *
	 * 功能：解析交易数据包
	 * 参数�?
	 * bBuffer   - 要解析的数据缓冲�?
	 * */
	public static Vector parseResponseE1(byte[] bBuffer) throws java.util.NoSuchElementException
	{
		Vector vecData = new Vector(5);

		//1-响应代码
		vecData.addElement(new String(bBuffer, 0, 2));
		//2-错误代码
		vecData.addElement(new String(bBuffer, 2, 2));
		//3-输出模式
		vecData.addElement(new String(bBuffer, 4, 1));
		//4-消息数据长度
//		byte[] bytesDataLen = new byte[3];
//		System.arraycopy(bBuffer, 5, bytesDataLen, 0, 3);
//		vecData.addElement(bytesDataLen);
//		System.out.println("bytesDataLen[0]:"+bytesDataLen[0]);
//		System.out.println("bytesDataLen[1]:"+bytesDataLen[1]);
//		System.out.println("bytesDataLen[2]:"+bytesDataLen[2]);
		String strDatalen = new String(bBuffer, 5, 3);
		vecData.addElement(strDatalen);
		//5-消息数据
		int nDataLen = Integer.valueOf(strDatalen, 16);//16进制字符表达的长�?
		vecData.addElement(new String(bBuffer, 8, nDataLen*2));

		return vecData;
	}

	/*功能：制作请求数据包--98--密钥离散功能�?98/99�?*/
	/**
	 * @param strKey       -
	 * @param strKeyType
	 * @param strLSData1
	 * @param strLSData2
	 * @param strLSData3
	 * @return
	 * @throws IOException
	 */
	public static byte[] makeRequest98(String strKey, String strKeyType, String strLSData1, String strLSData2, String strLSData3) throws IOException
	{
		ByteArrayOutputStream byteArrayRetData = new ByteArrayOutputStream();
		String strCommand = "98";
		byteArrayRetData.write(strCommand.getBytes());
		byteArrayRetData.write(strKey.getBytes());
		byteArrayRetData.write(strKeyType.getBytes());
		byteArrayRetData.write(strLSData1.getBytes());
		byteArrayRetData.write(strLSData2.getBytes());
		byteArrayRetData.write(strLSData3.getBytes());

		return byteArrayRetData.toByteArray();
	}

	/* * * *
	 * 功能：解析交易数据包
	 * 参数�?
	 * bBuffer   - 要解析的数据缓冲�?
	 * */
	public static Vector parseResponse99(byte[] bBuffer) throws java.util.NoSuchElementException
	{
		Vector vecData = new Vector(5);

		//1-响应代码
		vecData.addElement(new String(bBuffer, 0, 2));
		//2-错误代码
		vecData.addElement(new String(bBuffer, 2, 2));
		//3-密钥密文
		vecData.addElement(new String(bBuffer, 4, 32));

		return vecData;
	}

	/*功能：制作请求数据包--MQ--对大消息生成MAC（MAB）（MQ/MR�?*/
	/**
	 * @param strZAK
	 * @param strData
	 * @return
	 * @throws IOException
	 */
	public static byte[] makeRequestMQ(String strZAK, String strData) throws IOException
	{
		ByteArrayOutputStream byteArrayRetData = new ByteArrayOutputStream();
		String strCommand = "MQ";
		byteArrayRetData.write(strCommand.getBytes());
		byteArrayRetData.write("0".getBytes());
		byteArrayRetData.write(strZAK.getBytes());
		StringBuilder sb = new StringBuilder();
		sb.append("000").append(Integer.toHexString(strData.length()/2));
		byteArrayRetData.write(sb.substring(sb.length()-3).getBytes());
		byteArrayRetData.write(strData.getBytes());

		return byteArrayRetData.toByteArray();
	}

	/* * * *
	 * 功能：解析交易数据包
	 * 参数�?
	 * bBuffer   - 要解析的数据缓冲�?
	 * */
	public static Vector parseResponseMR(byte[] bBuffer) throws java.util.NoSuchElementException
	{
		Vector vecData = new Vector(5);

		//1-响应代码
		vecData.addElement(new String(bBuffer, 0, 2));
		//2-错误代码
		vecData.addElement(new String(bBuffer, 2, 2));
		//3-MAB
		vecData.addElement(new String(bBuffer, 4, 16));

		return vecData;
	}

	/*功能：制作请求数据包--MS--用ANSI X9.19方式对大消息生成MAC（MS/MT�?*/
	/**
	 * @param strData
	 * @return
	 * @throws IOException
	 */
	public static byte[] makeRequestMS(String strKeyType, String strKeyLenType, String strKey, String strData) throws IOException
	{
		ByteArrayOutputStream byteArrayRetData = new ByteArrayOutputStream();
		String strCommand = "MS";
		byteArrayRetData.write(strCommand.getBytes());
		byteArrayRetData.write("0".getBytes());
		byteArrayRetData.write(strKeyType.getBytes());
		byteArrayRetData.write(strKeyLenType.getBytes());
		byteArrayRetData.write("0".getBytes());
		byteArrayRetData.write(strKey.getBytes());
		StringBuilder sb = new StringBuilder();
		sb.append("000").append(Integer.toHexString(strData.length()));
		byteArrayRetData.write(sb.substring(sb.length()-4).getBytes());
		byteArrayRetData.write(strData.getBytes());

		return byteArrayRetData.toByteArray();
	}

	/*功能：制作请求数据包--MS--用ANSI X9.19方式对大消息生成MAC（MS/MT�?*/
	/**
	 * @param strBlockNum   - 消息块号  0：仅�?块�??/1：第�?块�??/2：中间块�?/3：最后块�?
	 * @param strKeyType    - 密钥类型  0－TAK（终端认证密钥）/1－ZAK（区域认证密钥）
	 * @param strKeyLenType - 密钥长度类型  0－单倍长度DES密钥/1－双倍长度DES密钥
	 * @param strKey        - 密钥
	 * @param strDataBlock  - 消息�?
	 * @param strIV
	 * @return
	 * @throws IOException
	 */
	public static byte[] makeRequestMS(String strBlockNum, String strKeyType, String strKeyLenType, String strKey, String strDataBlock, String strIV) throws IOException
	{
		ByteArrayOutputStream byteArrayRetData = new ByteArrayOutputStream();
		String strCommand = "MS";
		byteArrayRetData.write(strCommand.getBytes());
		byteArrayRetData.write(strBlockNum.getBytes());
		byteArrayRetData.write(strKeyType.getBytes());
		byteArrayRetData.write(strKeyLenType.getBytes());
		byteArrayRetData.write("0".getBytes());
		byteArrayRetData.write(strKey.getBytes());
		byteArrayRetData.write(strIV.getBytes());
		StringBuilder sb = new StringBuilder();
		sb.append("000").append(Integer.toHexString(strDataBlock.length()));
		byteArrayRetData.write(sb.substring(sb.length()-4).getBytes());
		byteArrayRetData.write(strDataBlock.getBytes());

		return byteArrayRetData.toByteArray();
	}

	/*功能：制作请求数据包--MS--用ANSI X9.19方式对大消息生成MAC（MS/MT�?*/
	/**
	 * @param strBlockNum   - 消息块号  0：仅�?块�??/1：第�?块�??/2：中间块�?/3：最后块�?
	 * @param strKeyType    - 密钥类型  0－TAK（终端认证密钥）/1－ZAK（区域认证密钥）
	 * @param strKeyLenType - 密钥长度类型  0－单倍长度DES密钥/1－双倍长度DES密钥
	 * @param strKey        - 密钥
	 * @param strIV
	 * @return
	 * @throws IOException
	 */
	public static byte[] makeRequestMS(String strBlockNum, String strKeyType, String strKeyLenType, String strKey, byte[] bytesDataBlock, String strIV) throws IOException
	{
		ByteArrayOutputStream byteArrayRetData = new ByteArrayOutputStream();
		String strCommand = "MS";
		byteArrayRetData.write(strCommand.getBytes());
		byteArrayRetData.write(strBlockNum.getBytes());
		byteArrayRetData.write(strKeyType.getBytes());
		byteArrayRetData.write(strKeyLenType.getBytes());
		byteArrayRetData.write("0".getBytes());
		byteArrayRetData.write(strKey.getBytes());
		byteArrayRetData.write(strIV.getBytes());
		StringBuilder sb = new StringBuilder();
		sb.append("000").append(Integer.toHexString(bytesDataBlock.length));
		byteArrayRetData.write(sb.substring(sb.length()-4).getBytes());
		byteArrayRetData.write(bytesDataBlock);

		return byteArrayRetData.toByteArray();
	}

	/* * * *
	 * 功能：解析交易数据包
	 * 参数�?
	 * bBuffer   - 要解析的数据缓冲�?
	 * */
	public static Vector parseResponseMT(byte[] bBuffer)
	{
		Vector vecData = new Vector(5);

		//1-响应代码
		vecData.addElement(new String(bBuffer, 0, 2));
		//2-错误代码
		vecData.addElement(new String(bBuffer, 2, 2));
		//3-MAB
		vecData.addElement(new String(bBuffer, 4, 16));

		return vecData;
	}

	/*功能：制作请求数据包--HC--2.6.2	生成�?个TMK、TPK或PVK（HC/HD�?*/
	/**
	 * @param strTMK        - 终端密钥
	 * @return
	 * @throws IOException
	 */
	public static byte[] makeRequestHC(String strTMK) throws IOException
	{
		ByteArrayOutputStream byteArrayRetData = new ByteArrayOutputStream();
		String strCommand = "HC";
		byteArrayRetData.write(strCommand.getBytes());
		byteArrayRetData.write(strTMK.getBytes());

		return byteArrayRetData.toByteArray();
	}

	/* * * *
	 * 功能：解析交易数据包
	 * 参数�?
	 * bBuffer   - 要解析的数据缓冲�?
	 * */
	public static Vector parseResponseHD(byte[] bBuffer)
	{
		Vector vecData = new Vector(5);

		//1-响应代码
		vecData.addElement(new String(bBuffer, 0, 2));
		//2-错误代码
		vecData.addElement(new String(bBuffer, 2, 2));
		//3-当前TMK密钥下加密的新密�?
		vecData.addElement(new String(bBuffer, 4, 16));
		//4-LMK下加密的新密�?
		vecData.addElement(new String(bBuffer, 20, 16));
		//5-密钥校验�?

		return vecData;
	}

	/*功能：制作请求数据包--AE--2.6.3	将TMK、TPK或PVK从LMK转为另一TMK、TPK或PVK加密（AE/AF�?*/
	/**
	 * @param strTMK        - 终端密钥
	 * @return
	 * @throws IOException
	 */
	public static byte[] makeRequestAE(String strKey,String strTMK) throws IOException
	{
		ByteArrayOutputStream byteArrayRetData = new ByteArrayOutputStream();
		String strCommand = "AE";
		byteArrayRetData.write(strCommand.getBytes());
		byteArrayRetData.write(strKey.getBytes());
		byteArrayRetData.write(strTMK.getBytes());

		return byteArrayRetData.toByteArray();
	}

	/* * * *
	 * 功能：解析交易数据包
	 * 参数�?
	 * bBuffer   - 要解析的数据缓冲�?
	 * */
	public static Vector parseResponseAF(byte[] bBuffer)
	{
		Vector vecData = new Vector(5);

		//1-响应代码
		vecData.addElement(new String(bBuffer, 0, 2));
		//2-错误代码
		vecData.addElement(new String(bBuffer, 2, 2));
		//3-当前TMK密钥下加密的密钥
		vecData.addElement(new String(bBuffer, 4, 16));
		//4-密钥校验�?

		return vecData;
	}

	/*功能：制作请求数据包--HA--2.7.1	生成�?个TAK（HA/HB�?*/
	/**
	 * @param strTMK        - 终端密钥
	 * @return
	 * @throws IOException
	 */
	public static byte[] makeRequestHA(String strTMK) throws IOException
	{
		ByteArrayOutputStream byteArrayRetData = new ByteArrayOutputStream();
		String strCommand = "HA";
		byteArrayRetData.write(strCommand.getBytes());
		byteArrayRetData.write(strTMK.getBytes());

		return byteArrayRetData.toByteArray();
	}

	/* * * *
	 * 功能：解析交易数据包
	 * 参数�?
	 * bBuffer   - 要解析的数据缓冲�?
	 * */
	public static Vector parseResponseHB(byte[] bBuffer)
	{
		Vector vecData = new Vector(5);

		//1-响应代码
		vecData.addElement(new String(bBuffer, 0, 2));
		//2-错误代码
		vecData.addElement(new String(bBuffer, 2, 2));
		//3-当前TMK密钥下加密的新密�?
		vecData.addElement(new String(bBuffer, 4, 16));
		//4-LMK下加密的新密�?
		vecData.addElement(new String(bBuffer, 20, 16));
		//5-密钥校验�?

		return vecData;
	}

	/*功能：制作请求数据包--AG--2.7.4	将TAK从LMK转为TMK加密（AG/AH�?*/
	/**
	 * @param strTMK        - 终端密钥
	 * @return
	 * @throws IOException
	 */
	public static byte[] makeRequestAG(String strTAK,String strTMK) throws IOException
	{
		ByteArrayOutputStream byteArrayRetData = new ByteArrayOutputStream();
		String strCommand = "AG";
		byteArrayRetData.write(strCommand.getBytes());
		byteArrayRetData.write(strTMK.getBytes());
		byteArrayRetData.write(strTAK.getBytes());

		return byteArrayRetData.toByteArray();
	}

	/* * * *
	 * 功能：解析交易数据包
	 * 参数�?
	 * bBuffer   - 要解析的数据缓冲�?
	 * */
	public static Vector parseResponseAH(byte[] bBuffer)
	{
		Vector vecData = new Vector(4);

		//1-响应代码
		vecData.addElement(new String(bBuffer, 0, 2));
		//2-错误代码
		vecData.addElement(new String(bBuffer, 2, 2));
		//3-当前TMK密钥下加密的密钥
		vecData.addElement(new String(bBuffer, 4, 16));
		//4-密钥校验�?

		return vecData;
	}

	/*功能：制作请求数据包--EO--由公钥生成一个MAC*/
	/**
	 * @param strPublicKey
	 * @return
	 * @throws IOException
	 */
	public static byte[] makeRequestEO(String strPublicKey) throws IOException
	{
//		ByteArrayOutputStream byteArrayRetData = new ByteArrayOutputStream();
		String strCommand = "EO";
//		byteArrayRetData.write(CryptoUtils.byte2hex(strCommand.getBytes()).getBytes());
//		byteArrayRetData.write(CryptoUtils.byte2hex("01".getBytes()).getBytes());
//		byteArrayRetData.write("308201".getBytes());
//		byteArrayRetData.write("0A0282010100".getBytes());
//		byteArrayRetData.write(strPublicKey.getBytes());
//		byteArrayRetData.write("0203010001".getBytes());

		StringBuilder sb = new StringBuilder();
		sb.append(CryptoUtils.byte2hex(strCommand.getBytes()));
		sb.append(CryptoUtils.byte2hex("01".getBytes()));
		sb.append("308201").append("0A0282010100");
		sb.append(strPublicKey).append("0203010001");

		return CryptoUtils.hex2byte(sb.toString());
	}

	/* * * *
	 * 功能：解析交易数据包
	 * 参数�?
	 * bBuffer   - 要解析的数据缓冲�?
	 * */
	public static Vector parseResponseEP(byte[] bBuffer) throws java.util.NoSuchElementException
	{
		Vector vecData = new Vector(4);

//		//1-响应代码
//		vecData.addElement(new String(bBuffer, 0, 4));
//		//2-错误代码
//		vecData.addElement(new String(bBuffer, 4, 4));
//		//3-MAC
//		vecData.addElement(new String(bBuffer, 8, 8));
//		System.out.println("bytesMAC:"+new String(bBuffer, 8, 8));

		//1-响应代码
		vecData.addElement(new String(bBuffer, 0, 2));
		//2-错误代码
		vecData.addElement(new String(bBuffer, 2, 2));
		//3-MAC
		byte[] bytesMAC = new byte[4];
		System.arraycopy(bBuffer, 4, bytesMAC, 0, 4);
		vecData.addElement(bytesMAC);
		System.out.println("bytesMAC:"+new String(bytesMAC));
		System.out.println("bytesMAC:"+CryptoUtils.byte2hex(bytesMAC));
		//4-公钥 - ASN.1格式编码的DER
		//vecData.addElement(new String(bBuffer, 8, 512));//报错了，此项值暂时用不到，忽�?

		return vecData;
	}

	/*功能：制作请求数据包--GK--使用带入的密钥进行数据加解密计算*/
	/**
	 * @param strDESKeyType
	 * @param strDESKey
	 * @param strDESMAC
	 * @param bytesMAC
	 * @param strPublicKey
	 * @return
	 * @throws IOException
	 */
	public static byte[] makeRequestGK(String strDESKeyType, String strDESKey, String strDESMAC, byte[] bytesMAC, String strPublicKey) throws IOException
	{
//		ByteArrayOutputStream byteArrayRetData = new ByteArrayOutputStream();
		String strCommand = "GK";
//		byteArrayRetData.write(strCommand.getBytes());
//		byteArrayRetData.write("0101".getBytes());
//		byteArrayRetData.write(strDESKeyType.getBytes());
//		byteArrayRetData.write("1".getBytes());
//		byteArrayRetData.write(strDESKey.getBytes());
//		byteArrayRetData.write(strDESMAC.getBytes());
//		byteArrayRetData.write(bytesMAC);
//		byteArrayRetData.write(strPublicKey.getBytes());

//		StringBuffer sb = new StringBuffer();
//		sb.append(strCommand).append("0101").append(strDESKeyType).append("1").append(strDESKey).append(strDESMAC);
//		byteArrayRetData.write(CryptoUtils.byte2hex(sb.toString().getBytes()).getBytes());
//		byteArrayRetData.write(strMAC.getBytes());
//		byteArrayRetData.write("308201".getBytes());
//		byteArrayRetData.write("0A0282010100".getBytes());
//		byteArrayRetData.write(strPublicKey.getBytes());
//		byteArrayRetData.write("0203010001".getBytes());
//
//		return byteArrayRetData.toByteArray();

		StringBuffer sb = new StringBuffer();
		sb.append(strCommand).append("0101").append(strDESKeyType).append("1").append(strDESKey).append(strDESMAC);
		String strTemp = sb.toString();
		sb.setLength(0);
		sb.append(CryptoUtils.byte2hex(strTemp.getBytes()));
		sb.append(CryptoUtils.byte2hex(bytesMAC));
		sb.append("308201").append("0A0282010100");
		sb.append(strPublicKey).append("0203010001");
		System.out.println("GK:"+sb.toString());

		return CryptoUtils.hex2byte(sb.toString());

	}

	/* * * *
	 * 功能：解析交易数据包
	 * 参数�?
	 * bBuffer   - 要解析的数据缓冲�?
	 * */
	public static Vector parseResponseGL(byte[] bBuffer) throws java.util.NoSuchElementException
	{
		Vector vecData = new Vector(6);

		//1-响应代码
		vecData.addElement(new String(bBuffer, 0, 2));
		//2-错误代码
		vecData.addElement(new String(bBuffer, 2, 2));
		//3-对DES密钥的初始化�?
		vecData.addElement(new String(bBuffer, 4, 16));
		//4-DES密钥长度
		String strDataLen = new String(bBuffer, 20, 4);
		vecData.addElement(strDataLen);
		System.out.println("strDataLen:"+strDataLen);
		//5-DES密钥
		int nDataLen = Integer.parseInt(strDataLen);
		byte[] bytesDESKeyData = new byte[nDataLen];
		System.arraycopy(bBuffer, 24, bytesDESKeyData, 0, nDataLen);
		vecData.addElement(bytesDESKeyData);

		return vecData;
	}

	/*功能：制作请求数据包--GI--将DES密钥从公钥下加密转换为LMK下加�?*/
	/**
	 * @param strDESKeyType
	 * @param strDESKey
	 * @param strPrivateKeyIndex
	 * @return
	 * @throws IOException
	 */
	public static byte[] makeRequestGI(String strDESKeyType, String strDESKey, String strPrivateKeyIndex) throws IOException
	{
		ByteArrayOutputStream byteArrayRetData = new ByteArrayOutputStream();
		String strCommand = "GI";
		byteArrayRetData.write(strCommand.getBytes());
		byteArrayRetData.write("0101".getBytes());
		byteArrayRetData.write(strDESKeyType.getBytes());
		byte[] bytesDESKey = CryptoUtils.hex2byte(strDESKey);
		byteArrayRetData.write(StringUtils.Int2String(bytesDESKey.length, 4).getBytes());
		byteArrayRetData.write(bytesDESKey);
		byteArrayRetData.write(";".getBytes());
		byteArrayRetData.write(strPrivateKeyIndex.getBytes());
		byteArrayRetData.write(";".getBytes());
		byteArrayRetData.write(CryptoUtils.hex2byte("585830"));

//		System.out.println("GI Hex:"+CryptoUtils.byte2hex(byteArrayRetData.toByteArray()));

		return byteArrayRetData.toByteArray();
	}

	/* * * *
	 * 功能：解析交易数据包
	 * 参数�?
	 * bBuffer   - 要解析的数据缓冲�?
	 * */
	public static Vector<String> parseResponseGJ(byte[] bBuffer, int nDESKeyLen) throws java.util.NoSuchElementException
	{
		Vector<String> vecData = new Vector<String>(6);

		//1-响应代码
		vecData.addElement(new String(bBuffer, 0, 2));
		//2-错误代码
		vecData.addElement(new String(bBuffer, 2, 2));
		//3-对DES密钥的初始化�?
		vecData.addElement(new String(bBuffer, 4, 16));
		//4-DES密钥（LMK�?
		vecData.addElement(new String(bBuffer, 20, nDESKeyLen));//nDESKeyLen的长度应该和GK指令导出指令时的DES密钥标记相对应，当初的GK指令中DES密钥标记�?1这里就是32，产生的checkvalue就是16
		//5-DES密钥校验�? 16H还是6H取决于KCV的类型�?�项�?
		vecData.addElement(new String(bBuffer, 20+nDESKeyLen, 16));

		return vecData;
	}

	/*功能：制作请求数据包--A0--生成�?个密钥，同时可�?�的为交易在LMK下加密密�?*/
	/**
	 * @param strKeyType
	 * @param strKeyFlag
	 * @param strZMK
	 * @param strZMKFlag
	 * @return
	 * @throws IOException
	 */
	public static byte[] makeRequestA0(String strActionType, String strKeyType, String strKeyFlag, String strZMK, String strZMKFlag) throws IOException
	{
		ByteArrayOutputStream byteArrayRetData = new ByteArrayOutputStream();
		String strCommand = "A0";
		byteArrayRetData.write(strCommand.getBytes());
		byteArrayRetData.write(strActionType.getBytes());
		byteArrayRetData.write(strKeyType.getBytes());
		byteArrayRetData.write(strKeyFlag.getBytes());
		byteArrayRetData.write(strZMK.getBytes());
		byteArrayRetData.write(strZMKFlag.getBytes());

//		System.out.println("A0 Hex:"+CryptoUtils.byte2hex(byteArrayRetData.toByteArray()));

		return byteArrayRetData.toByteArray();
	}

	/* * * *
	 * 功能：解析交易数据包
	 * 参数�?
	 * bBuffer   - 要解析的数据缓冲�?
	 * */
	public static Vector<String> parseResponseA1(byte[] bBuffer, int nKeyLen) throws java.util.NoSuchElementException
	{
		Vector<String> vecData = new Vector<String>(5);

		//1-响应代码
		vecData.addElement(new String(bBuffer, 0, 2));
		//2-错误代码
		vecData.addElement(new String(bBuffer, 2, 2));
		//3-密钥（LMK�?
		vecData.addElement(new String(bBuffer, 4, nKeyLen));
		//4-密钥（ZMK�?
		//vecData.addElement(new String(bBuffer, 4+nKeyLen, nKeyLen));
		//5-密钥校验�? 6H
		//vecData.addElement(new String(bBuffer, 2+2*nKeyLen, 6));

		return vecData;
	}

	public void connectionHSM() throws UnknownHostException, IOException
	{
		//建立通讯
		Socket socket = new Socket(_HSMHost, _nPort);
		_OutToHSM = new DataOutputStream(socket.getOutputStream());
		_InputFromHSM = new DataInputStream(socket.getInputStream());
	}

	public void closeHSM() throws UnknownHostException, IOException
	{
		//断开通讯
		if(null!=_OutToHSM) _OutToHSM.close();
		if(null!=_InputFromHSM) _InputFromHSM.close();
	}


	public synchronized String doEncodePassword(boolean bErrorTry, String strPassword, String strCardNo) throws IOException
	{
		for (;strCardNo.length()<16;)
		{
			strCardNo = "0"+strCardNo;
		}
		int nLength = strCardNo.length();
		String strCardNo12 = strCardNo.substring(nLength-13, nLength-1);
		//System.out.println("strCardNo12:"+strCardNo12);

		//建立通讯
		try {
			connectionHSM();

			//BA-BB
			byte[] bRequest = makeRequestBA(strPassword, strCardNo12);
			//String strSend = "JGX7B4D14A7FDEAC51429B7BB06D381E34D010222795975996450993";
			//System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			byte[] bResponse = receive(_InputFromHSM);
			//System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			Vector<String> vecRet = parseResponseBB(bResponse);
			String strRetCode = (String)vecRet.get(1);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command BA/BB return error code "+strRetCode);
			}
			String strEncPassword = (String)vecRet.get(2);

			//JG-JH
			bRequest = makeRequestJG(_LocalEncPIK, strEncPassword, strCardNo12);
			//System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			bResponse = receive(_InputFromHSM);
			//System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			vecRet = parseResponseJH(bResponse);
			strRetCode = (String)vecRet.get(1);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command JG/JH return error code "+strRetCode);
			}
			return (String)vecRet.get(2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			closeHSM();
			connectionHSM();
			if(bErrorTry)
			{
				return doEncodePassword(false, strPassword, strCardNo);
			}
			else
			{
				throw e;
			}
		} finally {
			closeHSM();
		}
	}

	public synchronized String doFromEncodePassword(boolean bErrorTry, String strPassword, String strCardNo) throws IOException
	{
		int nLength = strCardNo.length();
		String strCardNo12 = strCardNo.substring(nLength-13, nLength-1);
		//System.out.println("strCardNo12:"+strCardNo12);

		//建立通讯
		try {
			if(null==_OutToHSM || null==_InputFromHSM) connectionHSM();

			//BA-BB
			byte[] bRequest = makeRequestBA(strPassword, strCardNo12);
			//String strSend = "JGX7B4D14A7FDEAC51429B7BB06D381E34D010222795975996450993";
			System.out.println("bRequest:"+new String(bRequest));
			System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			byte[] bResponse = receive(_InputFromHSM);
			System.out.println("bResponse:"+new String(bResponse));
			System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			Vector<String> vecRet = parseResponseBB(bResponse);
			String strRetCode = (String)vecRet.get(1);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command BA/BB return error code "+strRetCode);
			}
			String strEncPassword = (String)vecRet.get(2);

			//JG-JH
			bRequest = makeRequestJG(_FromEncPIK, strEncPassword, strCardNo12);
			System.out.println("bRequest:"+new String(bRequest));
			//System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			bResponse = receive(_InputFromHSM);
			System.out.println("bResponse:"+new String(bResponse));
			//System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			vecRet = parseResponseJH(bResponse);
			strRetCode = (String)vecRet.get(1);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command JG/JH return error code "+strRetCode);
			}
			return (String)vecRet.get(2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			closeHSM();
			connectionHSM();
			if(bErrorTry)
			{
				return doFromEncodePassword(false, strPassword, strCardNo);
			}
			else
			{
				throw e;
			}
		}
	}

	/**
	 * 用对方密钥加�?
	 *
	 * @param bErrorTry
	 * @param strFromEncPIK - 指定的对方密钥�??
	 * @param strPassword
	 * @param strCardNo
	 * @return
	 * @throws IOException
	 */
	public synchronized String doFromEncodePassword(boolean bErrorTry, String strFromEncPIK, String strPassword, String strCardNo) throws IOException
	{
		int nLength = strCardNo.length();
		String strCardNo12 = strCardNo.substring(nLength-13, nLength-1);
		//System.out.println("strCardNo12:"+strCardNo12);

		//建立通讯
		try {
			if(null==_OutToHSM || null==_InputFromHSM) connectionHSM();

			//BA-BB
			byte[] bRequest = makeRequestBA(strPassword, strCardNo12);
			//String strSend = "JGX7B4D14A7FDEAC51429B7BB06D381E34D010222795975996450993";
			System.out.println("bRequest:"+new String(bRequest));
			System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			byte[] bResponse = receive(_InputFromHSM);
			System.out.println("bResponse:"+new String(bResponse));
			System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			Vector<String> vecRet = parseResponseBB(bResponse);
			String strRetCode = (String)vecRet.get(1);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command BA/BB return error code "+strRetCode);
			}
			String strEncPassword = (String)vecRet.get(2);

			//JG-JH
			bRequest = makeRequestJG(strFromEncPIK, strEncPassword, strCardNo12);
			System.out.println("bRequest:"+new String(bRequest));
			//System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			bResponse = receive(_InputFromHSM);
			System.out.println("bResponse:"+new String(bResponse));
			//System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			vecRet = parseResponseJH(bResponse);
			strRetCode = (String)vecRet.get(1);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command JG/JH return error code "+strRetCode);
			}
			return (String)vecRet.get(2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			closeHSM();
			connectionHSM();
			if(bErrorTry)
			{
				return doFromEncodePassword(false, strFromEncPIK, strPassword, strCardNo);
			}
			else
			{
				throw e;
			}
		}
	}

	public synchronized String doTransferPinZPK2ZPK(boolean bErrorTry, String strEncPassword, String strCardNo) throws IOException
	{
		int nLength = strCardNo.length();
		String strCardNo12 = strCardNo.substring(nLength-13, nLength-1);
		System.out.println("strCardNo12:"+strCardNo12);

		//建立通讯
		try {
			if(null==_OutToHSM || null==_InputFromHSM) connectionHSM();

			//BA-BB
			byte[] bRequest = makeRequestCC(_FromEncPIK, _LocalEncPIK, strEncPassword, strCardNo12);
			System.out.println("bRequest:"+new String(bRequest));
			//System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			byte[] bResponse = receive(_InputFromHSM);
			System.out.println("bResponse:"+new String(bResponse));
			//System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			Vector<String> vecRet = parseResponseCD(bResponse);
			String strRetCode = (String)vecRet.get(1);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command CC/CD return error code "+strRetCode);
			}

			return (String)vecRet.get(2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			closeHSM();
			connectionHSM();
			if(bErrorTry)
			{
				return doTransferPinZPK2ZPK(false, strEncPassword, strCardNo);
			}
			else
			{
				throw e;
			}
		}
	}

	/**
	 * 带卡号转PIN
	 *
	 * @param bErrorTry
	 * @param strFromEncPIK - 指定的对方密钥�??
	 * @param strEncPassword
	 * @param strCardNo
	 * @return
	 * @throws IOException
	 */
	public synchronized String doTransferPinZPK2ZPK(boolean bErrorTry, String strFromEncPIK, String strEncPassword, String strCardNo) throws IOException
	{
		int nLength = strCardNo.length();
		String strCardNo12 = strCardNo.substring(nLength-13, nLength-1);
		System.out.println("strCardNo12:"+strCardNo12);

		//建立通讯
		try {
			if(null==_OutToHSM || null==_InputFromHSM) connectionHSM();

			//BA-BB
			byte[] bRequest = makeRequestCC(strFromEncPIK, _LocalEncPIK, strEncPassword, strCardNo12);
			System.out.println("bRequest:"+new String(bRequest));
			//System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			byte[] bResponse = receive(_InputFromHSM);
			System.out.println("bResponse:"+new String(bResponse));
			//System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			Vector<String> vecRet = parseResponseCD(bResponse);
			String strRetCode = (String)vecRet.get(1);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command CC/CD return error code "+strRetCode);
			}

			return (String)vecRet.get(2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			closeHSM();
			connectionHSM();
			if(bErrorTry)
			{
				return doTransferPinZPK2ZPK(false, strFromEncPIK, strEncPassword, strCardNo);
			}
			else
			{
				throw e;
			}
		}
	}

	/**
	 * 功能：转PIN，支持PIN是不带卡号的加密的情况�??
	 * @param bErrorTry
	 * @param strEncPassword
	 * @param strCardNo
	 * @param bNotCardNoPin
	 * @return
	 * @throws IOException
	 */
	public synchronized String doTransferPinZPK2ZPK(boolean bErrorTry, String strEncPassword, String strCardNo, boolean bNotCardNoPin) throws IOException
	{
		int nLength = strCardNo.length();
		String strCardNo12 = strCardNo.substring(nLength-13, nLength-1);
		System.out.println("strCardNo12:"+strCardNo12);

		//建立通讯
		try {
			if(null==_OutToHSM || null==_InputFromHSM) connectionHSM();

			//CC-CD
			String strTemp = bNotCardNoPin ? "000000000000" : strCardNo12;
			byte[] bRequest = makeRequestCC(_FromEncPIK, _LocalEncPIK, strEncPassword, strTemp, "01", "03");
			System.out.println("bRequest:"+new String(bRequest));
			//System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			byte[] bResponse = receive(_InputFromHSM);
			System.out.println("bResponse:"+new String(bResponse));
			//System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			Vector<String> vecRet = parseResponseCD(bResponse);
			String strRetCode = (String)vecRet.get(1);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command CC/CD return error code "+strRetCode);
			}
			strEncPassword = (String)vecRet.get(2);

			//CC-CD
			bRequest = makeRequestCC(_LocalEncPIK, _LocalEncPIK, strEncPassword, strCardNo12, "03", "01");
			System.out.println("bRequest:"+new String(bRequest));
			//System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			bResponse = receive(_InputFromHSM);
			System.out.println("bResponse:"+new String(bResponse));
			//System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			vecRet = parseResponseCD(bResponse);
			strRetCode = (String)vecRet.get(1);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command CC/CD return error code "+strRetCode);
			}

			return (String)vecRet.get(2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			closeHSM();
			connectionHSM();
			if(bErrorTry)
			{
				return doTransferPinZPK2ZPK(false, strEncPassword, strCardNo, bNotCardNoPin);
			}
			else
			{
				throw e;
			}
		}
	}

	/**
	 * 功能：转PIN，支持PIN是不带卡号的加密的情况�??
	 * @param bErrorTry
	 * @param strFromEncPIK - 指定的对方密钥�??
	 * @param strEncPassword
	 * @param strCardNo
	 * @param bNotCardNoPin
	 * @return
	 * @throws IOException
	 */
	public synchronized String doTransferPinZPK2ZPK(boolean bErrorTry, String strFromEncPIK, String strEncPassword, String strCardNo, boolean bNotCardNoPin) throws IOException
	{
		int nLength = strCardNo.length();
		String strCardNo12 = strCardNo.substring(nLength-13, nLength-1);
		System.out.println("strCardNo12:"+strCardNo12);

		//建立通讯
		try {
			if(null==_OutToHSM || null==_InputFromHSM) connectionHSM();

			//CC-CD
			String strTemp = bNotCardNoPin ? "000000000000" : strCardNo12;
			byte[] bRequest = makeRequestCC(strFromEncPIK, _LocalEncPIK, strEncPassword, strTemp, "01", "03");
			System.out.println("bRequest:"+new String(bRequest));
			//System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			byte[] bResponse = receive(_InputFromHSM);
			System.out.println("bResponse:"+new String(bResponse));
			//System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			Vector<String> vecRet = parseResponseCD(bResponse);
			String strRetCode = (String)vecRet.get(1);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command CC/CD return error code "+strRetCode);
			}
			strEncPassword = (String)vecRet.get(2);

			//CC-CD
			bRequest = makeRequestCC(_LocalEncPIK, _LocalEncPIK, strEncPassword, strCardNo12, "03", "01");
			System.out.println("bRequest:"+new String(bRequest));
			//System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			bResponse = receive(_InputFromHSM);
			System.out.println("bResponse:"+new String(bResponse));
			//System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			vecRet = parseResponseCD(bResponse);
			strRetCode = (String)vecRet.get(1);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command CC/CD return error code "+strRetCode);
			}

			return (String)vecRet.get(2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			closeHSM();
			connectionHSM();
			if(bErrorTry)
			{
				return doTransferPinZPK2ZPK(false, strFromEncPIK, strEncPassword, strCardNo, bNotCardNoPin);
			}
			else
			{
				throw e;
			}
		}
	}

	/**
	 * 功能：转PIN，支持PIN是不带卡号的加密的情况�??
	 * @param bErrorTry
	 * @param strFromEncPIK - 指定的对方密钥�??
	 * @param strEncPassword
	 * @param strCardNo
	 * @param bNotCardNoPin
	 * @return
	 * @throws IOException
	 */
	public synchronized String doTransferPinTPK2ZPK(boolean bErrorTry, String strFromEncPIK, String strEncPassword, String strCardNo, boolean bNotCardNoPin) throws IOException
	{
		int nLength = strCardNo.length();
		String strCardNo12 = strCardNo.substring(nLength-13, nLength-1);
		System.out.println("strCardNo12:"+strCardNo12);

		//建立通讯
		try {
			if(null==_OutToHSM || null==_InputFromHSM) connectionHSM();

			//CA-CB
			String strTemp = bNotCardNoPin ? "000000000000" : strCardNo12;
			byte[] bRequest = makeRequestCA(strFromEncPIK, _LocalEncPIK, strEncPassword, strTemp, "01", "03");
			System.out.println("bRequest:"+new String(bRequest));
			//System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			byte[] bResponse = receive(_InputFromHSM);
			System.out.println("bResponse:"+new String(bResponse));
			//System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			Vector<String> vecRet = parseResponseCB(bResponse);
			String strRetCode = (String)vecRet.get(2);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command CA/CB return error code "+strRetCode);
			}
			strEncPassword = (String)vecRet.get(2);

			//CC-CD
			bRequest = makeRequestCC(_LocalEncPIK, _LocalEncPIK, strEncPassword, strCardNo12, "03", "01");
			System.out.println("bRequest:"+new String(bRequest));
			//System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			bResponse = receive(_InputFromHSM);
			System.out.println("bResponse:"+new String(bResponse));
			//System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			vecRet = parseResponseCD(bResponse);
			strRetCode = (String)vecRet.get(1);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command CC/CD return error code "+strRetCode);
			}

			return (String)vecRet.get(2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			closeHSM();
			connectionHSM();
			if(bErrorTry)
			{
				return doTransferPinTPK2ZPK(false, strFromEncPIK, strEncPassword, strCardNo, bNotCardNoPin);
			}
			else
			{
				throw e;
			}
		}
	}

	/**
	 * @param bErrorTry
	 * @return
	 * @throws IOException
	 */
	public synchronized Vector<String> doCreateRSAKeyPair(boolean bErrorTry, int nKeySize, int nKeyIndex) throws IOException
	{
		//建立通讯
		try {
			if(null==_OutToHSM || null==_InputFromHSM) connectionHSM();

			//34-35
			byte[] bRequest = makeRequest34(nKeySize, nKeyIndex);
			//System.out.println("bRequest:"+new String(bRequest));
			//System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			byte[] bResponse = receive(_InputFromHSM);
			System.out.println("bResponse length:"+bResponse.length);
			//System.out.println("bResponse:"+new String(bResponse));
			//System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			Vector<String> vecRet = parseResponse35(bResponse);
			String strRetCode = (String)vecRet.get(1);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command 34/35 return error code "+strRetCode);
			}

			return vecRet;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			closeHSM();
			connectionHSM();
			if(bErrorTry)
			{
				return doCreateRSAKeyPair(false, nKeySize, nKeyIndex);
			}
			else
			{
				throw e;
			}
		}
	}

	/**
	 * @param bErrorTry
	 * @param strZEKZAKFlag 产生ZEK或是ZAK的标识，0-ZEK�?1-ZAK
	 * @return
	 * @throws IOException
	 */
	public synchronized Vector<String> doCreateZEKZAK(boolean bErrorTry, String strZEKZAKFlag) throws IOException
	{
		//建立通讯
		try {
			if(null==_OutToHSM || null==_InputFromHSM) connectionHSM();

			//FI-FJ
			byte[] bRequest = makeRequestFI(strZEKZAKFlag);
			System.out.println("bRequest:"+new String(bRequest));
			System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			byte[] bResponse = receive(_InputFromHSM);
			System.out.println("bResponse:"+new String(bResponse));
			System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			Vector<String> vecRet = parseResponseFJ(bResponse);
			String strRetCode = (String)vecRet.get(1);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command FI/FJ return error code "+strRetCode);
			}

			return vecRet;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			closeHSM();
			connectionHSM();
			if(bErrorTry)
			{
				return doCreateZEKZAK(false, strZEKZAKFlag);
			}
			else
			{
				throw e;
			}
		}
	}

	/**
	 * @param bErrorTry         出错重试�?�?
	 * @param strMakerPublicKey 厂商公钥
	 * @param strZEK            ZEK
	 * @param strZEKMAC         ZEK校验�?
	 * @return                  加密过的DES密钥
	 * @throws IOException
	 */
	public synchronized String doExportEncDESKey(boolean bErrorTry, String strMakerPublicKey, String strDESKeyType, String strZEK, String strZEKMAC) throws IOException
	{
		//建立通讯
		try {
			if(null==_OutToHSM || null==_InputFromHSM) connectionHSM();

			//EO-EP
			byte[] bRequest = makeRequestEO(strMakerPublicKey);
			System.out.println("bRequest:"+new String(bRequest));
			System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
//			System.out.println("bRequest Header:"+CryptoUtils.byte2hex(makeHexRequestHeader(bRequest)));
			//_OutToHSM.write(CryptoUtils.byte2hex(makeHexRequestHeader(bRequest)).getBytes());
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			byte[] bResponse = receive(_InputFromHSM);
			System.out.println("bResponse:"+new String(bResponse));
			System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			Vector vecRet = parseResponseEP(bResponse);
			String strRetCode = (String)vecRet.get(1);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command EO/EP return error code "+strRetCode);
			}

			bRequest = makeRequestGK(strDESKeyType, strZEK, strZEKMAC, (byte[])vecRet.get(2), strMakerPublicKey);
			System.out.println("bRequest:"+new String(bRequest));
			//System.out.println("bRequest Header:"+CryptoUtils.byte2hex(makeHexRequestHeader(bRequest)));
			System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
			//_OutToHSM.write(CryptoUtils.byte2hex(makeHexRequestHeader(bRequest)).getBytes());
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			bResponse = receive(_InputFromHSM);
			System.out.println("bResponse:"+new String(bResponse));
			System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			vecRet = parseResponseGL(bResponse);
			strRetCode = (String)vecRet.get(1);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command GK/GL return error code "+strRetCode);
			}

			return CryptoUtils.byte2hex((byte[])vecRet.get(4));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			closeHSM();
			connectionHSM();
			if(bErrorTry)
			{
				return doExportEncDESKey(false, strMakerPublicKey, strDESKeyType, strZEK, strZEKMAC);
			}
			else
			{
				throw e;
			}
		}
	}

	/**
	 * @param bErrorTry         出错重试�?�?
	 * @param strZEK            ZEK
	 * @param strEncFlag        加解密类型：0-DES加密 1-DES解密
	 * @param strCardDataInfo
	 * @return
	 * @throws IOException
	 */
	public synchronized String doEncCardDataInfo(boolean bErrorTry, String strZEK, String strEncFlag, String strCardDataInfo) throws IOException
	{
		//建立通讯
		try {
			if(null==_OutToHSM || null==_InputFromHSM) connectionHSM();

			//E0-E1
			byte[] bRequest = makeRequestE0(strZEK, strEncFlag, strCardDataInfo);
			//System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			byte[] bResponse = receive(_InputFromHSM);
			//System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			Vector<String> vecRet = parseResponseE1(bResponse);
			String strRetCode = (String)vecRet.get(1);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command E0/E1 return error code "+strRetCode);
			}

			return vecRet.get(4);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			closeHSM();
			connectionHSM();
			if(bErrorTry)
			{
				return doEncCardDataInfo(false, strZEK, strEncFlag, strCardDataInfo);
			}
			else
			{
				throw e;
			}
		}
	}

	/**
	 * 1.要注意加锁，不能用一个�?�道同时并发写入数据，会导致加密机加密数据报错�??
	 * 2.数据错误或连接错误之后，要注意断�?连接进行重连，否则数据�?�道里的残留数据可能导致加密机加密错误�??
	 * 3.要�?�虑添加加密机连接池功能，不断开连接而存储多个连接，便于快�?�交互，又可以保持连接加密机的处理能有好的并发处理特性�??
	 *
	 * @param bErrorTry         出错重试�?�?
	 * @param strKeyType        密钥类型 0－TAK（终端认证密钥）1－ZAK（区域认证密钥）
	 * @param strKeyLenType     密钥长度 0－单倍长度DES密钥  1－双倍长度DES密钥
	 * @param strKey            TAK �? ZAK
	 * @param strData           �?要做MAC的数�?
	 * @return
	 * @throws IOException
	 */
	public synchronized String doMACData(boolean bErrorTry, String strKeyType, String strKeyLenType, String strKey, String strData) throws IOException
	{
		//建立通讯
		try {
			if(null==_OutToHSM || null==_InputFromHSM) connectionHSM();

			//MQ-MR
			byte[] bRequest = makeRequestMS(strKeyType, strKeyLenType, strKey, strData);
			System.out.println("bRequest:"+new String(bRequest));
			System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			byte[] bResponse = receive(_InputFromHSM);
			System.out.println("bResponse:"+new String(bResponse));
			System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			Vector<String> vecRet = parseResponseMT(bResponse);
			String strRetCode = (String)vecRet.get(1);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command MS/MT return error code "+strRetCode);
			}

			return vecRet.get(2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			closeHSM();
			connectionHSM();
			if(bErrorTry)
			{
				return doMACData(false, strKeyType, strKeyLenType, strKey, strData);
			}
			else
			{
				throw e;
			}
		}
	}

	/**
	 * 1.要注意加锁，不能用一个�?�道同时并发写入数据，会导致加密机加密数据报错�??
	 * 2.数据错误或连接错误之后，要注意断�?连接进行重连，否则数据�?�道里的残留数据可能导致加密机加密错误�??
	 * 3.要�?�虑添加加密机连接池功能，不断开连接而存储多个连接，便于快�?�交互，又可以保持连接加密机的处理能有好的并发处理特性�??
	 *
	 * @param bErrorTry         出错重试�?�?
	 * @param strKeyType        密钥类型 0－TAK（终端认证密钥）1－ZAK（区域认证密钥）
	 * @param strKeyLenType     密钥长度 0－单倍长度DES密钥  1－双倍长度DES密钥
	 * @param strKey            TAK �? ZAK
	 * @param strData           �?要做MAC的数�?
	 * @return
	 * @throws IOException
	 */
	public synchronized String doPOSMAC(boolean bErrorTry, String strKeyType, String strKeyLenType, String strKey, String strData) throws IOException
	{
		return doPOSMAC(bErrorTry, strKeyType, strKeyLenType, strKey, strData.getBytes());
	}

	/**
	 * 1.要注意加锁，不能用一个�?�道同时并发写入数据，会导致加密机加密数据报错�??
	 * 2.数据错误或连接错误之后，要注意断�?连接进行重连，否则数据�?�道里的残留数据可能导致加密机加密错误�??
	 * 3.要�?�虑添加加密机连接池功能，不断开连接而存储多个连接，便于快�?�交互，又可以保持连接加密机的处理能有好的并发处理特性�??
	 *
	 * @param bErrorTry         出错重试�?�?
	 * @param strKeyType        密钥类型 0－TAK（终端认证密钥）1－ZAK（区域认证密钥）
	 * @param strKeyLenType     密钥长度 0－单倍长度DES密钥  1－双倍长度DES密钥
	 * @param strKey            TAK �? ZAK
	 * @param bytesData         �?要做MAC的数�?
	 * @return
	 * @throws IOException
	 */
	public synchronized String doPOSMAC(boolean bErrorTry, String strKeyType, String strKeyLenType, String strKey, byte[] bytesData) throws IOException
	{
		//建立通讯
		try {
			connectionHSM();

			int nLength = bytesData.length;

			byte[] bytesDataBlock = new byte[8];
			String strBlockNum = "1";//�?1�?
			if(nLength<9)
			{
				strBlockNum = "0"; //�?1�?
				System.arraycopy(bytesData, 0, bytesDataBlock, 0, nLength);
			}
			else
			{
				System.arraycopy(bytesData, 0, bytesDataBlock, 0, 8);
			}
			String strIV = "";

			//�?1�?
			//MS-MT
			byte[] bRequest = makeRequestMS(strBlockNum, strKeyType, strKeyLenType, strKey, bytesDataBlock, strIV);
			//System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			byte[] bResponse = receive(_InputFromHSM);
			//System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			Vector<String> vecRet = parseResponseMT(bResponse);
			String strRetCode = (String)vecRet.get(1);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command MS/MT return error code "+strRetCode);
			}
			strIV = vecRet.get(2);
			System.out.println("strIV:"+strIV);
			if(strBlockNum.equals("0")) return strIV;

			strBlockNum = "2"; //中间�?
			//中间�?
			int i=8;
			for(;i<nLength-8;i+=8)
			{
				System.arraycopy(bytesData, i, bytesDataBlock, 0, 8);
				//MS-MT
				bRequest = makeRequestMS(strBlockNum, strKeyType, strKeyLenType, strKey, bytesDataBlock, strIV);
				//System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
				_OutToHSM.write(makeRequestHeader(bRequest));
				_OutToHSM.write(bRequest);
				bResponse = receive(_InputFromHSM);
				//System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
				vecRet = parseResponseMT(bResponse);
				strRetCode = (String)vecRet.get(1);
				if(!strRetCode.equals("00"))
				{
					throw new IOException("HSM command MS/MT return error code "+strRetCode);
				}
				strIV = vecRet.get(2);
				System.out.println("strIV:"+strIV);
			}

			//�?后块
			byte[] bytesZeroBlock = new byte[8];
			strBlockNum = "3"; //�?末块
			System.arraycopy(bytesData, i, bytesZeroBlock, 0, nLength-i);
			//MS-MT
			bRequest = makeRequestMS(strBlockNum, strKeyType, strKeyLenType, strKey, bytesZeroBlock, strIV);
			//System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			bResponse = receive(_InputFromHSM);
			//System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			vecRet = parseResponseMT(bResponse);
			strRetCode = (String)vecRet.get(1);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command MS/MT return error code "+strRetCode);
			}
			strIV = vecRet.get(2);
			System.out.println("strIV:"+strIV);

			return strIV;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			closeHSM();
			connectionHSM();
			if(bErrorTry)
			{
				return doPOSMAC(bErrorTry, strKeyType, strKeyLenType, strKey, bytesData);
			}
			else
			{
				throw e;
			}
		} finally {
			closeHSM();
		}
	}

	/**
	 * 随机产生终端工作密钥
	 * @param bErrorTry         出错重试�?�?
	 * @param strTMK            TMK
	 * @return
	 * @throws IOException
	 */
	public synchronized String doGenTPK(boolean bErrorTry, String strTMK) throws IOException
	{
		//建立通讯
		try {
			if(null==_OutToHSM || null==_InputFromHSM) connectionHSM();

			//HC/HD
			byte[] bRequest = makeRequestHC(strTMK);
			System.out.println("bRequest:"+new String(bRequest));
			//System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			byte[] bResponse = receive(_InputFromHSM);
			System.out.println("bResponse:"+new String(bResponse));
			//System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			Vector<String> vecRet = parseResponseHD(bResponse);
			String strRetCode = (String)vecRet.get(1);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command HC/HD return error code "+strRetCode);
			}

			return vecRet.get(3);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			closeHSM();
			connectionHSM();
			if(bErrorTry)
			{
				return doGenTPK(false, strTMK);
			}
			else
			{
				throw e;
			}
		}
	}

	/**
	 * 终端MAC密钥LMK转为TMK加密
	 * @param bErrorTry         出错重试�?�?
	 * @param strTMK            TMK
	 * @return
	 * @throws IOException
	 */
	public synchronized String doTPKOnLMK2TMK(boolean bErrorTry, String strTPK, String strTMK) throws IOException
	{
		//建立通讯
		try {
			if(null==_OutToHSM || null==_InputFromHSM) connectionHSM();

			//HA/HB
			byte[] bRequest = makeRequestAE(strTPK, strTMK);
			System.out.println("bRequest:"+new String(bRequest));
			System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			byte[] bResponse = receive(_InputFromHSM);
			System.out.println("bResponse:"+new String(bResponse));
			System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			Vector<String> vecRet = parseResponseAF(bResponse);
			String strRetCode = (String)vecRet.get(1);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command AG/AH return error code "+strRetCode);
			}

			return vecRet.get(2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			closeHSM();
			connectionHSM();
			if(bErrorTry)
			{
				return doTPKOnLMK2TMK(false, strTPK, strTMK);
			}
			else
			{
				throw e;
			}
		}
	}

	/**
	 * 随机产生终端MAC密钥
	 * @param bErrorTry         出错重试�?�?
	 * @param strTMK            TMK
	 * @return
	 * @throws IOException
	 */
	public synchronized String doGenTAK(boolean bErrorTry, String strTMK) throws IOException
	{
		//建立通讯
		try {
			if(null==_OutToHSM || null==_InputFromHSM) connectionHSM();

			//HA/HB
			byte[] bRequest = makeRequestHA(strTMK);
			System.out.println("bRequest:"+new String(bRequest));
			//System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			byte[] bResponse = receive(_InputFromHSM);
			System.out.println("bResponse:"+new String(bResponse));
			//System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			Vector<String> vecRet = parseResponseHB(bResponse);
			String strRetCode = (String)vecRet.get(1);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command HA/HB return error code "+strRetCode);
			}

			return vecRet.get(3);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			closeHSM();
			connectionHSM();
			if(bErrorTry)
			{
				return doGenTAK(false, strTMK);
			}
			else
			{
				throw e;
			}
		}
	}

	/**
	 * 终端MAC密钥LMK转为TMK加密
	 * @param bErrorTry         出错重试�?�?
	 * @param strTMK            TMK
	 * @return
	 * @throws IOException
	 */
	public synchronized String doTAKOnLMK2TMK(boolean bErrorTry, String strTAK, String strTMK) throws IOException
	{
		//建立通讯
		try {
			if(null==_OutToHSM || null==_InputFromHSM) connectionHSM();

			//HA/HB
			byte[] bRequest = makeRequestAG(strTAK, strTMK);
			System.out.println("bRequest:"+new String(bRequest));
			System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			byte[] bResponse = receive(_InputFromHSM);
			System.out.println("bResponse:"+new String(bResponse));
			System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			Vector<String> vecRet = parseResponseAH(bResponse);
			String strRetCode = (String)vecRet.get(1);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command AG/AH return error code "+strRetCode);
			}

			return vecRet.get(2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			closeHSM();
			connectionHSM();
			if(bErrorTry)
			{
				return doTAKOnLMK2TMK(false, strTAK, strTMK);
			}
			else
			{
				throw e;
			}
		}
	}

	/**
	 * @param bErrorTry          出错重试�?�?
	 * @param strDESKeyType      ZEK DES密钥类型
	 * @param strDESKey          被加密的ZEK密钥
	 * @param strPrivateKeyIndex 用来解密的私钥的索引�?
	 * @return
	 * @throws IOException
	 */
	public synchronized String doImportEncDESKey(boolean bErrorTry, String strDESKeyType, String strDESKey, String strPrivateKeyIndex) throws IOException
	{
		//建立通讯
		try {
			if(null==_OutToHSM || null==_InputFromHSM) connectionHSM();

			byte[] bRequest = makeRequestGI(strDESKeyType, strDESKey, strPrivateKeyIndex);
			System.out.println("bRequest:"+new String(bRequest));
			System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			byte[] bResponse = receive(_InputFromHSM);
			System.out.println("bResponse:"+new String(bResponse));
			System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			Vector vecRet = parseResponseGJ(bResponse, 33);
			String strRetCode = (String)vecRet.get(1);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command GI/GJ return error code "+strRetCode);
			}

			return (String)vecRet.get(3);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			closeHSM();
			connectionHSM();
			if(bErrorTry)
			{
				return doImportEncDESKey(false, strDESKeyType, strDESKey, strPrivateKeyIndex);
			}
			else
			{
				throw e;
			}
		}
	}

	/**
	 * @param bErrorTry          出错重试�?�?
	 * @return
	 * @throws IOException
	 */
	public synchronized String doCreateZMKKey(boolean bErrorTry, String strKeyFlag) throws IOException
	{
		//建立通讯
		try {
			if(null==_OutToHSM || null==_InputFromHSM) connectionHSM();

			byte[] bRequest = makeRequestA0("0","000", strKeyFlag, "", "");
			System.out.println("bRequest:"+new String(bRequest));
			System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));
			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			byte[] bResponse = receive(_InputFromHSM);
			System.out.println("bResponse:"+new String(bResponse));
			System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));
			Vector vecRet = parseResponseA1(bResponse, 33);
			String strRetCode = (String)vecRet.get(1);
			if(!strRetCode.equals("00"))
			{
				throw new IOException("HSM command GI/GJ return error code "+strRetCode);
			}

			return (String)vecRet.get(3);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			closeHSM();
			connectionHSM();
			if(bErrorTry)
			{
				return doCreateZMKKey(false, strKeyFlag);
			}
			else
			{
				throw e;
			}
		}
	}

	public byte getVersion() {
		return _Version;
	}

	public void setVersion(byte version) {
		_Version = version;
	}

	/**
	 * SM2 公钥加密  (C1C3C2模式)
	 * @param publicKeyX           公钥X
	 * @param publicKeyY           公钥Y
	 * @param encryptedData       等待加密的明文
	 * @return Vector  0-指令代码   1-应答码  2-数据
	 * @throws IOException
	 */
	public  synchronized Vector<String> makeRequestKE(String publicKeyX, String publicKeyY, String encryptedData) throws IOException
	{
		Vector<String> vecData = new Vector<String>(5);
		//建立通讯
		try {
			if(null==_OutToHSM || null==_InputFromHSM) connectionHSM();
			ByteArrayOutputStream byteArrayRetData = new ByteArrayOutputStream();
			String strCommand = "KE";
			byteArrayRetData.write(strCommand.getBytes());
			byteArrayRetData.write("99".getBytes());
			byteArrayRetData.write(CryptoUtils.hex2byte(publicKeyX));
			byteArrayRetData.write(CryptoUtils.hex2byte(publicKeyY));
			String  encryptedDataLength = CryptoUtils.hex2byte(encryptedData).length+"";
			for (; encryptedDataLength.length() <4; ) {
				encryptedDataLength = "0"+encryptedDataLength;
			}
			byteArrayRetData.write((encryptedDataLength+"").getBytes());
			byteArrayRetData.write(CryptoUtils.hex2byte(encryptedData));

			byte[] bRequest = byteArrayRetData.toByteArray();
			System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));

			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			byte[] bResponse = receive(_InputFromHSM);
			System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));


			//0-响应代码
			vecData.addElement(new String(bResponse, 0, 2));
			//1-错误代码
			vecData.addElement(new String(bResponse, 2, 2));

			int len = Integer.valueOf(new String(bResponse, 4, 4));
//			vecData.addElement(new String(bResponse, 6, len));
			byte[] bBufferKey = new byte[len];
			System.arraycopy(bResponse, 8, bBufferKey, 0, len);
			//2-密文
			vecData.addElement(CryptoUtils.byte2hex(bBufferKey));

			return vecData;
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			closeHSM();
			connectionHSM();
			throw e;
		}

	}


	/**
	 * 私钥解密
	 * @param privateKey    私钥
	 * @param decryptData  加密的密文
	 * @return  Vector  0-指令代码   1-应答码  2-数据
	 * @throws IOException
	 */
	public  synchronized Vector<String> makeRequestKF(String privateKey, String decryptData) throws IOException
	{
		Vector<String> vecData = new Vector<String>(5);
		//建立通讯
		try {
			if(null==_OutToHSM || null==_InputFromHSM) connectionHSM();
			ByteArrayOutputStream byteArrayRetData = new ByteArrayOutputStream();
			String strCommand = "KF";
			byteArrayRetData.write(strCommand.getBytes());
			byteArrayRetData.write("99".getBytes());
			String privateKeyLength = CryptoUtils.hex2byte(privateKey).length+"";
			for (; privateKeyLength.length() <4; ) {
				privateKeyLength = "0"+privateKeyLength;
			}
			byteArrayRetData.write((privateKeyLength+"").getBytes());
			byteArrayRetData.write(CryptoUtils.hex2byte(privateKey));
			String decryptDataLength = CryptoUtils.hex2byte(decryptData).length+"";
			for (; decryptDataLength.length() <4; ) {
				decryptDataLength = "0"+decryptDataLength;
			}
			byteArrayRetData.write((decryptDataLength+"").getBytes());
			byteArrayRetData.write(CryptoUtils.hex2byte(decryptData));

			byte[] bRequest = byteArrayRetData.toByteArray();
			System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));

			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			byte[] bResponse = receive(_InputFromHSM);
			System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));


			//0-响应代码
			vecData.addElement(new String(bResponse, 0, 2));
			//1-错误代码
			vecData.addElement(new String(bResponse, 2, 2));

			int len = Integer.valueOf(new String(bResponse, 4, 4));
//			vecData.addElement(new String(bResponse, 6, len));
			byte[] bBufferKey = new byte[len];
			System.arraycopy(bResponse, 8, bBufferKey, 0, len);
			//2-解开的明文
			vecData.addElement(CryptoUtils.byte2hex(bBufferKey));

			return vecData;
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			closeHSM();
			connectionHSM();
			throw e;
		}
	}


	/**
	 * 生成RSA公私钥
	 * @return  Vector  0-指令代码   1-应答码  2-私钥 3-公钥
	 * @throws IOException
	 */
	public  synchronized Vector<String> makeRequest34() throws IOException
	{
		Vector<String> vecData = new Vector<String>(5);
		//建立通讯
		try {
			if(null==_OutToHSM || null==_InputFromHSM) connectionHSM();
			ByteArrayOutputStream byteArrayRetData = new ByteArrayOutputStream();
			String strCommand = "34";
			byteArrayRetData.write(strCommand.getBytes());
			byteArrayRetData.write("204899".getBytes());

			byte[] bRequest = byteArrayRetData.toByteArray();
			System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));

			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			byte[] bResponse = receive(_InputFromHSM);
			System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));


			//0-响应代码
			vecData.addElement(new String(bResponse, 0, 2));
			//1-错误代码
			vecData.addElement(new String(bResponse, 2, 2));

			byte[] bBufferPrivateKey = new byte[1204];
			System.arraycopy(bResponse, 8, bBufferPrivateKey, 0, 1204);
			//2-私钥
			vecData.addElement(CryptoUtils.byte2hex(bBufferPrivateKey));

			byte[] bBufferPublicKey = new byte[270];
			System.arraycopy(bResponse, 1212, bBufferPublicKey, 0, 270);
			//3-公钥
			vecData.addElement("30820122300D06092A864886F70D01010105000382010F00"+CryptoUtils.byte2hex(bBufferPublicKey));

			return vecData;
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			closeHSM();
			connectionHSM();
			throw e;
		}
	}




	/**
	 * 私钥解密
	 * @param privateKey    私钥
	 * @param decryptData  加密的密文
	 * @return  Vector  0-指令代码   1-应答码  2-数据
	 * @throws IOException
	 */
	public  synchronized Vector<String> makeRequest33(String privateKey, String decryptData) throws IOException
	{
		Vector<String> vecData = new Vector<String>(5);
		//建立通讯
		try {
			if(null==_OutToHSM || null==_InputFromHSM) connectionHSM();
			ByteArrayOutputStream byteArrayRetData = new ByteArrayOutputStream();
			String strCommand = "33";
			byteArrayRetData.write(strCommand.getBytes());
			byteArrayRetData.write("199".getBytes());
			String privateKeyLength = CryptoUtils.hex2byte(privateKey).length+"";
			for (; privateKeyLength.length() <4; ) {
				privateKeyLength = "0"+privateKeyLength;
			}
			byteArrayRetData.write((privateKeyLength+"").getBytes());
			byteArrayRetData.write(CryptoUtils.hex2byte(privateKey));
			String decryptDataLength = CryptoUtils.hex2byte(decryptData).length+"";
			for (; decryptDataLength.length() <4; ) {
				decryptDataLength = "0"+decryptDataLength;
			}
			byteArrayRetData.write((decryptDataLength+"").getBytes());
			byteArrayRetData.write(CryptoUtils.hex2byte(decryptData));

			byte[] bRequest = byteArrayRetData.toByteArray();
			System.out.println("bRequest:"+CryptoUtils.byte2hex(bRequest));

			_OutToHSM.write(makeRequestHeader(bRequest));
			_OutToHSM.write(bRequest);
			byte[] bResponse = receive(_InputFromHSM);
			System.out.println("bResponse:"+CryptoUtils.byte2hex(bResponse));


			//0-响应代码
			vecData.addElement(new String(bResponse, 0, 2));
			//1-错误代码
			vecData.addElement(new String(bResponse, 2, 2));

			int len = Integer.valueOf(new String(bResponse, 4, 4));
//			vecData.addElement(new String(bResponse, 6, len));
			byte[] bBufferKey = new byte[len];
			System.arraycopy(bResponse, 8, bBufferKey, 0, len);
			//2-解开的明文
			vecData.addElement(CryptoUtils.byte2hex(bBufferKey));

			return vecData;
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			closeHSM();
			connectionHSM();
			throw e;
		}
	}


}