package com.sun.gamepintu;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.sun.gamepintu.Utils.CloseUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CrimeCameraFragment extends Fragment
{
	private static final String TAG = "CrimeCameraFragment";
	public static final String EXTRA_PHOTO_FILENAME = "com.sun.criminalintent.photo.filename";
	public static final String EXTRA_PHOTO_ORIENTATION = "com.sun.criminalintent.photo_orientation";
	public static final String EXTRA_PHOTO_PATH = Environment.getExternalStorageDirectory().getAbsoluteFile()
			+ "/PinTu";
	public static final String EXTRA_PHOTO_NAME = "image.png";
	public static File file;

	private Camera mCamera;
	private SurfaceView mSurfaceView;
	private Button mTakeButton;
	private View mProgressContainer;

	private Camera.ShutterCallback mShutterCallback = new ShutterCallback()
	{
		@Override
		public void onShutter()
		{
			mProgressContainer.setVisibility(View.VISIBLE);
		}
	};

	private Camera.PictureCallback mJpegCallback = new PictureCallback()
	{
		@Override
		public void onPictureTaken(byte[] data, Camera camera)
		{
			// String filename = UUID.randomUUID().toString() + ".png";
			FileOutputStream fos = null;
			boolean isSuccess = true;

			file = new File(EXTRA_PHOTO_PATH);
			if (!file.exists())
				file.mkdirs();
			file = new File(EXTRA_PHOTO_PATH, EXTRA_PHOTO_NAME);
			
			try
			{
				file.createNewFile();
				fos = new FileOutputStream(file);
				fos.write(data);
			}
			catch (Exception e)
			{
				Log.e(TAG, "Error writring to file " + EXTRA_PHOTO_NAME, e);
				isSuccess = false;
			}
			finally
			{
				try
				{
					CloseUtils.closeQuietly(fos);
				}
				catch (Exception e)
				{
					Log.e(TAG, "Error writring to file " + EXTRA_PHOTO_NAME, e);
					isSuccess = false;
				}
			}

			if (isSuccess)
			{
				Log.e(TAG, "PNG saved at " + EXTRA_PHOTO_NAME);
				getActivity().setResult(Activity.RESULT_OK,
						new Intent().putExtra(EXTRA_PHOTO_FILENAME, EXTRA_PHOTO_NAME).putExtra(EXTRA_PHOTO_ORIENTATION,
								getActivity().getResources().getConfiguration().orientation));
			} else
				getActivity().setResult(Activity.RESULT_CANCELED);

			getActivity().finish();
		}
	};

	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_crime_camera, container, false);
		mTakeButton = (Button) view.findViewById(R.id.crime_camera_takePictureButton);
		mTakeButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (mCamera != null)
					mCamera.takePicture(mShutterCallback, null, mJpegCallback);
			}
		});

		mProgressContainer = view.findViewById(R.id.crime_camera_progressContainer);
		mProgressContainer.setVisibility(View.INVISIBLE);

		mSurfaceView = (SurfaceView) view.findViewById(R.id.crime_camera_surfaceView);
		SurfaceHolder holder = mSurfaceView.getHolder();
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		holder.addCallback(new Callback()
		{
			@Override
			public void surfaceDestroyed(SurfaceHolder holder)
			{
				if (mCamera != null)
					mCamera.stopPreview();
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder)
			{
				try
				{
					if (mCamera != null)
						mCamera.setPreviewDisplay(holder);
				}
				catch (IOException e)
				{
					Log.e(TAG, "Error setting up preview display", e);
				}
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
			{
				if (mCamera == null)
					return;
				try
				{
					Camera.Parameters parameters = mCamera.getParameters();
					Size size = getBestSupportedSize(parameters.getSupportedPreviewSizes(), width, height);
					parameters.setPreviewSize(size.width, size.height);
					// 设置图片尺寸大小
					size = getBestSupportedSize(parameters.getSupportedPictureSizes(), width, height);
					parameters.setPictureSize(size.width, size.height);
					mCamera.setParameters(parameters);
					mCamera.startPreview();
					if (getActivity().getResources()
							.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
						mCamera.setDisplayOrientation(90);
				}
				catch (Exception e)
				{
					Log.e(TAG, "Could not start preview", e);
					mCamera.release();
					mCamera = null;
				}
			}
		});

		return view;
	}

	private Size getBestSupportedSize(List<Size> sizes, int width, int height)
	{
		Size bestSize = sizes.get(0);
		int largestArea = bestSize.width * bestSize.height;
		for (Size size : sizes)
		{
			int area = size.width * size.height;
			if (area > largestArea)
			{
				bestSize = size;
				largestArea = area;
			}
		}
		return bestSize;
	}

	@TargetApi(9)
	@Override
	public void onResume()
	{
		super.onResume();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
			mCamera = Camera.open(0);
		else
			mCamera = Camera.open();
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if (mCamera != null)
		{
			mCamera.release();
			mCamera = null;
		}
	}
	
	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		if (mCamera != null)
		{
			mCamera.release();
			mCamera = null;
		}
	}

}
