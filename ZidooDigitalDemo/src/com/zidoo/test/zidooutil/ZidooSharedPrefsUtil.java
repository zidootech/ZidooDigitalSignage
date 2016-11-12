/**
 * Copyright (C) 2016 Zidoo (www.zidoo.tv)
 * Created by : jiangbo@zidoo.tv
 */

package com.zidoo.test.zidooutil;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * SharedPreferences
 * */
public class ZidooSharedPrefsUtil {

	public static void putValue(Context context, String key, int value) {
		Editor sp = PreferenceManager.getDefaultSharedPreferences(context)
				.edit();
		sp.putInt(key, value);
		sp.commit();
	}

	public static void putValue(Context context, String key, boolean value) {
		Editor sp = PreferenceManager.getDefaultSharedPreferences(context)
				.edit();
		sp.putBoolean(key, value);
		sp.commit();
	}

	public static void putValue(Context context, String key, String value) {
		Editor sp = PreferenceManager.getDefaultSharedPreferences(context)
				.edit();
		sp.putString(key, value);
		sp.commit();
	}

	public static void putValue(Context context, String key, long value) {
		Editor sp = PreferenceManager.getDefaultSharedPreferences(context)
				.edit();
		sp.putLong(key, value);
		sp.commit();
	}

	public static int getValue(Context context, String key, int defValue) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		int value = sp.getInt(key, defValue);
		return value;
	}

	public static long getValue(Context context, String key, long defValue) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		long value = sp.getLong(key, defValue);
		return value;
	}

	public static boolean getValue(Context context, String key, boolean defValue) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean value = sp.getBoolean(key, defValue);
		return value;
	}

	public static String getValue(Context context, String key, String defValue) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		String value = sp.getString(key, defValue);
		return value;
	}

	public static void delValue(Context context, String key) {
		Editor sp = PreferenceManager.getDefaultSharedPreferences(context)
				.edit();
		sp.remove(key);
		sp.commit();
	}

}
