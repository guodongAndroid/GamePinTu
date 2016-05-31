package com.sun.gamepintu;

import java.io.FileNotFoundException;

import com.sun.gamepintu.Utils.PictureUtils;
import com.sun.gamepintu.View.GamePinTuLayout;
import com.sun.gamepintu.View.GamePinTuLayout.GamePinTuListener;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity
{

	public static final int TAKE_PHOTO = 1;
	public static final int CROP_PHOTO = 2;
	public static final String path = Environment.getDataDirectory().getAbsolutePath() + "/PinTu";
	public static final String name = "output_image.png";
	public static final String BITMAP = "bitmap";
	private Uri mImageUri;

	private Bitmap mBitmap;

	private GamePinTuLayout mGamePinTuLayout;
	private TextView mLevel;
	private TextView mTime;
	private Button mPhoto;
	private Button mCamera;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mLevel = (TextView) findViewById(R.id.id_level);
		mTime = (TextView) findViewById(R.id.id_time);

		mCamera = (Button) findViewById(R.id.id_camera);
		mPhoto = (Button) findViewById(R.id.id_photo);

		mGamePinTuLayout = (GamePinTuLayout) findViewById(R.id.id_game);
		if (savedInstanceState != null)
		{
			mBitmap = (Bitmap) savedInstanceState.get(BITMAP);
			if (mBitmap != null)
				mGamePinTuLayout.setInitBitmap(mBitmap);
		}
		mGamePinTuLayout.setTimeEnabled(true);
		mGamePinTuLayout.setOnGamePinTuListener(new GamePinTuListener()
		{

			@Override
			public void timeChanged(int currentTime)
			{

				mTime.setText("" + currentTime);
			}

			@Override
			public void nextLevel(final int nextLevel)
			{

				new AlertDialog.Builder(MainActivity.this).setTitle("游戏信息").setMessage("拼图成功")
						.setPositiveButton("进入下一关", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{

						mGamePinTuLayout.nextLevel();
						mLevel.setText("" + nextLevel);
					}
				}).setNegativeButton("退出游戏", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						finish();
					}
				}).show();
			}

			@Override
			public void gameOver()
			{

				new AlertDialog.Builder(MainActivity.this).setTitle("游戏信息").setMessage("游戏结束")
						.setPositiveButton("重新开始", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{

						mGamePinTuLayout.restart();
					}
				}).setNegativeButton("退出游戏", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						finish();
					}
				}).show();
			}
		});

		mCamera.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startActivityForResult(new Intent(MainActivity.this, CrimeCameraActivity.class), TAKE_PHOTO);
			}
		});

		mPhoto.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), CROP_PHOTO);
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{

		switch (requestCode)
		{
			case TAKE_PHOTO:
				if (resultCode == RESULT_OK)
				{
					mImageUri = Uri.fromFile(CrimeCameraFragment.file);
					int orientation = data.getIntExtra(CrimeCameraFragment.EXTRA_PHOTO_ORIENTATION, 0);
					String path = CrimeCameraFragment.EXTRA_PHOTO_PATH + "/" + CrimeCameraFragment.EXTRA_PHOTO_NAME;
					BitmapDrawable bitmapDrawable = PictureUtils.getScaleDrawable(MainActivity.this, path);
					if (bitmapDrawable != null)
					{
						if (orientation == Configuration.ORIENTATION_PORTRAIT)
							bitmapDrawable = PictureUtils.getPortraitDrawable(mGamePinTuLayout, bitmapDrawable);
					}
					mBitmap = bitmapDrawable.getBitmap();
					if (mBitmap != null)
						mGamePinTuLayout.setInitBitmap(mBitmap);
				}
				break;

			case CROP_PHOTO:
				if (resultCode == RESULT_OK)
				{
					mImageUri = data.getData();
					try
					{
						mBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(mImageUri));
					}
					catch (FileNotFoundException e)
					{
						e.printStackTrace();
					}
					if (mBitmap != null)
						mGamePinTuLayout.setInitBitmap(mBitmap);
				}
				break;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putParcelable(BITMAP, mBitmap);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		mGamePinTuLayout.pause();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		mGamePinTuLayout.resume();
	}

}
