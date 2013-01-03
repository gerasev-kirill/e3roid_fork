package com.e3roid.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * Utility class to output information about android configuration
 */
public class EnvironmentInfo {
	
	/**
	 * Returns whether the app is running on the emulator. 
	 * @return whether the app is running on the emulator. 
	 */
	public static boolean isOnEmulator() {
        return ("sdk".equals(Build.MODEL) && "sdk".equals(Build.PRODUCT));		
	}

	/**
	 * Returns whether multi touch is supported on this device.
	 * @param context Context
	 * @return whether multi touch is supported on this device
	 */
	public static boolean isMultiTouchSupported(Context context) {
		return context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH);
	}
}
