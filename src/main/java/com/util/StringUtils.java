package com.util;

/**
 * Insert the type's description here.
 * Creation date: (2003-11-8 1:24:20)
 * @author: fenglinzi
 */
public class StringUtils {
	public static final int ACTION_EMPTY_AS_NULL = 0x001;
	public static final int ACTION_TRIM = 0x002;



	public static boolean isNullOrBlank2(String str) {
		if ((str == null) || (str.trim().length() == 0) || (str.trim().toLowerCase().equals("null"))) {
			return true;
		}
		return false;
	}

	/**
	 * StringUtils constructor comment.
	 */
	public StringUtils() {
		super();
	}
	public static String perform(String strObj, int intMethod) {
		if (strObj == null)
			return null;

		if ((intMethod & StringUtils.ACTION_EMPTY_AS_NULL) > 0) {
			if (null != strObj && strObj.trim().equals(""))
				return null;
		}

		if ((intMethod & StringUtils.ACTION_TRIM) > 0) {
			if (null != strObj)
				strObj = strObj.trim();
		}

		if ((intMethod & StringUtils.ACTION_REPLACE_RETURN_AS_BLANK) > 0) {
			if (null != strObj) {
				StringBuffer sb = new StringBuffer(strObj.trim());
				for (int i = 0; i < sb.length(); ++i) {
					if ('\n' == sb.charAt(i) || '\r' == sb.charAt(i)) {
						sb.setCharAt(i, ' ');
					}
				}
				strObj = sb.toString();
			}
		}

		return strObj;
	}
	public static final int ACTION_REPLACE_RETURN_AS_BLANK = 0x003;
	public static String Int2String(int nValue, int nBit) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < nBit; ++i) {
			sb.append('0');
		}
		sb.append(nValue);

		return sb.substring(sb.length() - nBit);
	}
	public static boolean isCharInASCII(String str) {
		int length = str.length();
		char[] charArray = new char[length];
		str.getChars(0, length, charArray, 0);

		for (int i = 0; i < length; i++) {
			if (charArray[i] < '0' || charArray[i] > 'z')
				return false;
		}

		return true;
	}
	public static String Long2String(long lnValue, int nBit) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < nBit; ++i) {
			sb.append('0');
		}
		sb.append(lnValue);

		return sb.substring(sb.length() - nBit);
	}
	public static String getParameter(String strParam) {
		String strRet = strParam;
		if (strRet == null)
			return null;

		try {
			strRet = new String(strRet.getBytes("8859_1"));
		} catch (java.io.UnsupportedEncodingException ex) {
			return null;
		}

		return strRet;
	}

	//获取随机数
	public static String random(int length){
		String random = "";
		/*随机数函数*/
		java.util.Random r=new java.util.Random();
		for(int i = 0;i<length;i++){
			/*生成36以内的随机数，不含36，并转化为36位*/
			random += Integer.toString(r.nextInt(36), 36);
		}
		return random;
	}

	public static String genRandomStr(int bit) {
		StringBuffer sbResult = new StringBuffer();
		java.util.Random rand =
			new java.util.Random(
				java.util.Calendar.getInstance().getTime().getTime());
		byte byteArray[] = new byte[bit];
		rand.nextBytes(byteArray);

		for (int i = 0; i < bit; i++) {
			byte sect = (byte) Math.abs(rand.nextInt() % 3);
			int temp;

			switch (sect) {
				//add a char of 0-9
				case 0 :
					temp = Math.abs(byteArray[i]) % 10 + 48;
					sbResult.append((char) temp);
					break;
					//add a char of A-Z
				case 1 :
					temp = Math.abs(byteArray[i]) % 26 + 65;
					sbResult.append((char) temp);
					break;
					//add a char of a-z
				case 2 :
					temp = Math.abs(byteArray[i]) % 26 + 97;
					sbResult.append((char) temp);
			}
		}

		return sbResult.toString();
	}
	/**
	 * �������һ��ָ�����Ⱥ����͵��ַ���
	 * @param bit
	 * @param type 0-0��9������ 1-A��Z����ĸ 2-a��z����ĸ 3-0��9�����ֺ�A��F����ĸ
	 * @return
	 */
	public static String genRandomStr(int bit, int type) {
		StringBuffer sbResult = new StringBuffer();
		java.util.Random rand =
			new java.util.Random(
				java.util.Calendar.getInstance().getTime().getTime());
		byte byteArray[] = new byte[bit];
		rand.nextBytes(byteArray);

		int temp;
		switch (type) 
		{
			//add a char of 0-9
			case 0 :
				for (int i = 0; i < bit; i++) {
					temp = Math.abs(byteArray[i]) % 10 + 48;
					sbResult.append((char) temp);
				}
				break;
			//add a char of A-Z
			case 1 :
				for (int i = 0; i < bit; i++) {
					temp = Math.abs(byteArray[i]) % 26 + 65;
					sbResult.append((char) temp);
				}
				break;
			//add a char of a-z
			case 2 :
				for (int i = 0; i < bit; i++) {
					temp = Math.abs(byteArray[i]) % 26 + 97;
					sbResult.append((char) temp);
				}
				//add a char of 0-9 A-F
			case 3 :
				for (int i = 0; i < bit; i++) {
					temp = Math.abs(byteArray[i]) % 16;
					if(temp<10) temp += 48;
					else temp += 65-10;
					sbResult.append((char) temp);
				}
				break;
		}

		return sbResult.toString();
	}
	/**
	 * �������һ��ָ�����Ⱥ����͵��ַ���
	 * @param bit
	 * @param type 0-0��9������ 1-A��Z����ĸ 2-a��z����ĸ 3-0��9�����ֺ�A��F����ĸ
	 * @return
	 */
	public static String genRandomStr(int bit, int type, int nFSData) {
		StringBuffer sbResult = new StringBuffer();

		java.util.Random rand =
			new java.util.Random(
				java.util.Calendar.getInstance().getTime().getTime());
		byte byteArray[] = new byte[bit];
		rand.nextBytes(byteArray);

		int temp;
		switch (type) 
		{
			//add a char of 0-9
			case 0 :
				for (int i = 0; i < bit; i++) {
					temp = Math.abs(byteArray[i]^nFSData) % 10 + 48;
					sbResult.append((char) temp);
				}
				break;
			//add a char of A-Z
			case 1 :
				for (int i = 0; i < bit; i++) {
					temp = Math.abs(byteArray[i]^nFSData) % 26 + 65;
					sbResult.append((char) temp);
				}
				break;
			//add a char of a-z
			case 2 :
				for (int i = 0; i < bit; i++) {
					temp = Math.abs(byteArray[i]^nFSData) % 26 + 97;
					sbResult.append((char) temp);
				}
				//add a char of 0-9 A-F
			case 3 :
				for (int i = 0; i < bit; i++) {
					temp = Math.abs(byteArray[i]^nFSData) % 16;
					if(temp<10) temp += 48;
					else temp += 65-10;
					sbResult.append((char) temp);
				}
				break;
		}

		return sbResult.toString();
	}
	/**
	 * �������һ��ָ�����Ⱥ����͵��ַ���
	 * @param bit
	 * @param type 0-0��9������ 1-A��Z����ĸ 2-a��z����ĸ 3-0��9�����ֺ�A��F����ĸ
	 * @return
	 */
	public static String genRandomStr(int bit, int type, String strFSData) {
		StringBuffer sbResult = new StringBuffer();
		sbResult.append(strFSData);
		while(sbResult.length()<bit)
		{
			sbResult.append(strFSData);
		}
		byte[] bytesFSData = sbResult.toString().getBytes();
		sbResult.setLength(0);

		java.util.Random rand =
			new java.util.Random(
				java.util.Calendar.getInstance().getTime().getTime());
		byte byteArray[] = new byte[bit];
		rand.nextBytes(byteArray);

		int temp;
		switch (type) 
		{
			//add a char of 0-9
			case 0 :
				for (int i = 0; i < bit; i++) {
					temp = Math.abs(byteArray[i]^bytesFSData[i]) % 10 + 48;
					sbResult.append((char) temp);
				}
				break;
			//add a char of A-Z
			case 1 :
				for (int i = 0; i < bit; i++) {
					temp = Math.abs(byteArray[i]^bytesFSData[i]) % 26 + 65;
					sbResult.append((char) temp);
				}
				break;
			//add a char of a-z
			case 2 :
				for (int i = 0; i < bit; i++) {
					temp = Math.abs(byteArray[i]^bytesFSData[i]) % 26 + 97;
					sbResult.append((char) temp);
				}
				//add a char of 0-9 A-F
			case 3 :
				for (int i = 0; i < bit; i++) {
					temp = Math.abs(byteArray[i]^bytesFSData[i]) % 16;
					if(temp<10) temp += 48;
					else temp += 65-10;
					sbResult.append((char) temp);
				}
				break;
		}

		return sbResult.toString();
	}
	public static String substring(String strSub, int start, int end, String strDefault)
	{
		if(null==strSub || start<0 || end<start)
		{
			return strDefault;
		}

		if(end>strSub.length())
		{
			end = strSub.length();
		}

		return strSub.substring(start, end);
	}
	/* ���ܣ���ָ�����ַ���������ֵ���ֵ��ԭ��ʽ���
	 * ������
	 * strValue - �ַ���������ֵ
	 * lngAdd - Ҫ��ӵ�ֵ����
	 * nBit - Ҫ������ַ������ĳ���
	 * */
	public static String formatLongStr(String strValue, long lngAdd, int nBit) throws NumberFormatException
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < nBit; ++i) {
			sb.append('0');
		}
		long lnNo = Long.parseLong(strValue)+lngAdd;
		sb.append(lnNo);
		int length = sb.length();
		return sb.substring(length - nBit, length);
	}

	/* ���ܣ������ַ������������ȵĲ�ָ������
	 * ������
	 * strValue - �ַ���������ֵ
	 * isAlignLeft - ָ������ķ���
	 * nBit - Ҫ������ַ������ĳ���
	 * */
	public static String align(String strValue, boolean isAlignLeft, char cInsert, int nBit) throws NumberFormatException
	{
		StringBuffer sb = new StringBuffer();
		if(isAlignLeft)
		{
			sb.append(strValue);
			for (int i = 0; i < nBit; ++i) {
				sb.append(cInsert);
			}
		}
		else
		{
			for (int i = 0; i < nBit; ++i) {
				sb.append(cInsert);
			}
			sb.append(strValue);
		}

		int length = sb.length();
		return sb.substring(length - nBit, length);
	}
}