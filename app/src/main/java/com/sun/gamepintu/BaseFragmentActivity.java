package com.sun.gamepintu;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * FragmentActivity基类（抽象类）
 * @author Sun
 * @date 2015年12月3日
 * @time 下午2:46:00
 */
public abstract class BaseFragmentActivity extends FragmentActivity
{
	protected abstract Fragment creatFragment();

	@Override
	public void onCreate(Bundle arg0)
	{
		super.onCreate(arg0);
		setContentView(R.layout.activity_fragment);
		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);

		if (fragment == null)
		{
			fragment = creatFragment();
			fm.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
		}

	}
}
