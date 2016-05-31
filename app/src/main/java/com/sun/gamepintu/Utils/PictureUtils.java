package com.sun.gamepintu.Utils;

import com.sun.gamepintu.View.GamePinTuLayout;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.view.Display;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * 照片处理工具类
 * 
 * @author Sun
 * @date 2015年12月8日
 * @time 下午6:37:21
 */
public class PictureUtils
{
	/**
	 * 压缩照片
	 * 
	 * @param activity
	 * @param path
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static BitmapDrawable getScaleDrawable(Activity activity, String path)
	{
		Display display = activity.getWindowManager().getDefaultDisplay();
		float destWidth = display.getWidth();
		float destHeight = display.getHeight();

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		float srcWidth = options.outWidth;
		float srcHeight = options.outHeight;

		int inSampleSize = 1;

		if (srcHeight > destHeight || srcWidth > srcWidth)
		{
			if (srcWidth > srcHeight)
			{
				inSampleSize = Math.round(srcHeight / destHeight);
			} else
			{
				inSampleSize = Math.round(srcWidth / destWidth);
			}
		}

		options = new BitmapFactory.Options();
		options.inSampleSize = inSampleSize;

		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		if (bitmap != null)
		{
			BitmapDrawable bitmapDrawable = new BitmapDrawable(activity.getResources(), bitmap);
			return bitmapDrawable;
		} else
			return null;
	}

	/**
	 * 卸载照片
	 * 
	 * @param imageView
	 */
	public static void cleanImageView(ImageView imageView)
	{
		if (!(imageView.getDrawable() instanceof BitmapDrawable))
			return;
		BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
		if (bitmapDrawable != null)
		{
			Bitmap bitmap = bitmapDrawable.getBitmap();
			if (bitmap != null)
				bitmap.recycle();
		}
		imageView.setImageDrawable(null);
	}

	/**
	 * 调整图片显示的方向
	 * 
	 * @param iView
	 * @param origImage
	 * @return
	 */
	public static BitmapDrawable getPortraitDrawable(GamePinTuLayout layout, BitmapDrawable origImage)
	{
		Matrix m = new Matrix();
		m.postRotate(90);
		Bitmap br = Bitmap.createBitmap(origImage.getBitmap(), 0, 0, origImage.getIntrinsicWidth(),
				origImage.getIntrinsicHeight(), m, true);
		origImage = new BitmapDrawable(layout.getResources(), br);
		return origImage;
	}

}
