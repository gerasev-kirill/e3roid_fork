/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2007 Kenny Root, Jeffrey Sharkey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.connectbot.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.connectbot.transport.AbsTransport;

import com.e3roid.lifecycle.E3LifeCycle;
import com.e3roid.util.Debug;

/**
 * Manager for SSH connections that runs as a background service. This service holds a list of
 * currently connected SSH bridges that are ready for connection up to a GUI if needed.
 * 
 * @author jsharkey
 * @author modified by raaar
 * @author modified by e3roid
 */
public class TerminalManager implements E3LifeCycle {

	private final Map<Integer, WeakReference<TerminalBridge>> mHostBridgeMap =
		new ConcurrentHashMap<Integer, WeakReference<TerminalBridge>>();
	
	private final List<TerminalBridge> bridges = new CopyOnWriteArrayList<TerminalBridge>();

	private final Resources mResources;
	private final SharedPreferences mPreferences;
	private boolean hardKeyboardHidden;
	private boolean resizeAllowed = true;

	public static final int DEBUG_TRANSPORT_ID  = Integer.MAX_VALUE;
	public static final int SYSTEM_TRANSPORT_ID = DEBUG_TRANSPORT_ID - 1;
	public static final int SCRIPT_TRANSPORT_ID = DEBUG_TRANSPORT_ID - 2;
	
	public TerminalManager(Context context) {
		mResources = context.getResources();
		mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		hardKeyboardHidden =
			(mResources.getConfiguration().hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES);
	}

	/**
	 * Disconnect all currently connected bridges.
	 */
	private void disconnectAll() {
		TerminalBridge[] bridgesArray = null;
		if (bridges.size() > 0) {
			bridgesArray = bridges.toArray(new TerminalBridge[bridges.size()]);
		}
		if (bridgesArray != null) {
			// disconnect and dispose of any existing bridges
			for (TerminalBridge bridge : bridgesArray) {
				bridge.dispatchDisconnect(true);
			}
		}
	}

	/**
	 * Open a new debug session using the given parameters.
	 */
	public TerminalBridge openDebugConnection() 
			throws IllegalArgumentException, IOException, InterruptedException {
		return openConnection(DEBUG_TRANSPORT_ID, Debug.connect());
	}

	/**
	 * Close the debug session
	 */
	public void closeDebugConnection() {
		Debug.disconnect();
	}
	
	/**
	 * Open a new session using the given parameters.
	 */
	public TerminalBridge openConnection(int id, AbsTransport transport) 
			throws IllegalArgumentException, IOException, InterruptedException {
	    if (getConnectedBridge(id) != null) {
	        throw new IllegalArgumentException("Connection already open");
	      }

	      TerminalBridge bridge = new TerminalBridge(this, transport, id);
	      bridge.connect();

	      WeakReference<TerminalBridge> wr = new WeakReference<TerminalBridge>(bridge);
	      bridges.add(bridge);
	      mHostBridgeMap.put(id, wr);

	      return bridge;
	}

	/**
	 * Find a connected {@link TerminalBridge} with the given HostBean.
	 * 
	 * @param id
	 *          the HostBean to search for
	 * @return TerminalBridge that uses the HostBean
	 */
	public TerminalBridge getConnectedBridge(int id) {
		WeakReference<TerminalBridge> wr = mHostBridgeMap.get(id);
		if (wr != null) {
			return wr.get();
		} else {
			return null;
		}
	}

	/**
	 * Called by child bridge when somehow it's been disconnected.
	 */
	public void closeConnection(TerminalBridge bridge, boolean killProcess) {
	    if (killProcess) {
	        bridges.remove(bridge);
	        mHostBridgeMap.remove(bridge.getId());
	    }
	}

	/**
	 * Allow {@link TerminalBridge} to resize when the parent has changed.
	 * 
	 * @param resizeAllowed
	 */
	public void setResizeAllowed(boolean resizeAllowed) {
		this.resizeAllowed = resizeAllowed;
	}

	public boolean isResizeAllowed() {
		return resizeAllowed;
	}

	public void stop() {
		resizeAllowed = false;
		disconnectAll();
	}

	public int getIntParameter(String key, int defValue) {
		return mPreferences.getInt(key, defValue);
	}

	public String getStringParameter(String key, String defValue) {
		return mPreferences.getString(key, defValue);
	}

	public boolean isHardKeyboardHidden() {
		return hardKeyboardHidden;
	}

	public void setHardKeyboardHidden(boolean b) {
		hardKeyboardHidden = b;
	}

	public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		mPreferences.registerOnSharedPreferenceChangeListener(listener);
	}
	
	public Resources getResources() {
		return mResources;
	}

	@Override
	public void onResume() {
		
	}

	@Override
	public void onPause() {
		
	}

	@Override
	public void onDispose() {
		stop();
	}
}
