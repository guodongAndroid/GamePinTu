package com.sun.gamepintu.Utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * 关闭类
 * 
 * @author Sun
 * @date 2015年12月4日
 * @time 下午4:21:33
 */
public final class CloseUtils
{
	public CloseUtils()
	{
	}

	/**
	 * 关闭Closeable对象
	 * 
	 * @param closeable closeable为可变参数
	 */
	public static void closeQuietly(Closeable... closeable)
	{
		if (closeable.length != 0)
		{
			try
			{
				for (Closeable close : closeable)
				{
					close.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
