package com.util;

/**
 * Insert the type's description here.
 * Creation date: (2004-7-10 16:58:55)
 * @author: 李明轩(xun119@21cn.com)
 */
public class NumberUtils {
	/**
	 * NumberUtils constructor comment.
	 */
	public NumberUtils() {
		super();
	}
	//功能：做4舍5入
//参数：a - 要做4舍5入的数字
//		n - 做4舍5入保留的小数位数
	public static double do4Down5Up(double a, int n)
	{
		long lPower = Math.round(Math.pow(10, n));
		return (double)Math.round(a * lPower)/lPower;
	}
	//功能：做4舍5入
//参数：a - 要做4舍5入的数字
//		n - 做4舍5入保留的小数位数
	public static float do4Down5Up(float a, int n)
	{
		long lPower = Math.round(Math.pow(10, n));
		return (float)Math.round(a * lPower)/lPower;
	}
	public static double parseAsDouble(String strData, double dDefault)
	{
		double dRet = dDefault;
		try
		{
			dRet = Double.parseDouble(strData.trim());
		}
		catch(Exception ex)
		{
			//ex.printStackTrace(System.err);
		}

		return dRet;
	}
	public static float parseAsFloat(String strData, float fDefault)
	{
		float fRet = fDefault;
		try
		{
			fRet = Float.parseFloat(strData.trim());
		}
		catch(Exception ex)
		{
			//ex.printStackTrace(System.err);
		}

		return fRet;
	}
	public static int parseAsInt(String strData, int nDefault)
	{
		int nRet = nDefault;
		try
		{
			nRet = Integer.parseInt(strData.trim());
		}
		catch(Exception ex)
		{
			//ex.printStackTrace(System.err);
		}

		return nRet;
	}
	public static long parseAsLong(String strData, long lDefault)
	{
		long lRet = lDefault;
		try
		{
			lRet = Long.parseLong(strData.trim());
		}
		catch(Exception ex)
		{
			//ex.printStackTrace(System.err);
		}

		return lRet;
	}
	public static double Long2Double(long lngValue, double dDiv)
	{
		return (double)(lngValue/dDiv);
	}
	public static float Long2Float(long lngValue, float fDiv)
	{
		return (float)(lngValue/fDiv);
	}
	public static String toHexString(byte[] btArray, String strDelimer)
	{
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<btArray.length; ++i)
		{
			sb.append( StringUtils.align(Integer.toHexString(btArray[i]),false,'0',2) ).append(strDelimer);
		}
		return sb.toString();
	}
}