package com.sun.gamepintu;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 照片类
 * 
 * @author Sun
 * @date 2015年12月8日
 * @time 下午6:22:53
 */
public class Photo
{
	private static final String JSON_FILENAME = "filename";
	private static final String JSON_ORIENTATION = "orientation";
	private static Photo sPhoto;

	private String mFileName;
	private int mOrientation;

	private Photo(String filename, int orientation)
	{
		this.mFileName = filename;
		this.mOrientation = orientation;
	}
	
	public static Photo getInstance (String filename, int orientation) 
	{
		if (sPhoto == null)
		{
			synchronized (Photo.class)
			{
				if (sPhoto == null)
					sPhoto = new Photo(filename, orientation);
			}
		}
		return sPhoto;
	}

	public Photo(JSONObject object) throws JSONException
	{
		mFileName = object.getString(JSON_FILENAME);
		mOrientation = object.getInt(JSON_ORIENTATION);
	}

	public JSONObject toJSON() throws JSONException
	{
		JSONObject object = new JSONObject();
		object.put(JSON_FILENAME, mFileName);
		object.put(JSON_ORIENTATION, mOrientation);
		return object;
	}

	public String getFileName()
	{
		return mFileName;
	}

	public int getOrientation()
	{
		return mOrientation;
	}

}
