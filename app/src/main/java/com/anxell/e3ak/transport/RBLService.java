/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anxell.e3ak.transport;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.anxell.e3ak.transport.bpEncode;
import com.anxell.e3ak.transport.RBLGattAttributes;
import com.anxell.e3ak.util.Util;

/**
 * Service for managing connection and data communication with a GATT server
 * hosted on a given Bluetooth LE device.
 */
public class RBLService extends Service {
	private final static String TAG = RBLService.class.getSimpleName();
	private final static boolean debugFlag = false;
	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private String mBluetoothDeviceAddress;
	private BluetoothGatt mBluetoothGatt;
	private String tmpConAddr="";
	private boolean isNeedConnection = false;
	public final static String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_ERROR_133 = "ACTION_GATT_ERROR_133";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_GATT_RSSI = "ACTION_GATT_RSSI";
	public final static String ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE";
	public final static String EXTRA_DATA = "EXTRA_DATA";
	public final static String ACTION_GATT_WRITE_SUCCESS = "ACTION_GATT_WRITE_SUCCESS";
    private final int BLE_Packet_MAX_LEN = 20;
			
	public final static UUID UUID_BLE_E3K_SERVICE = UUID
			.fromString(RBLGattAttributes.BLE_E3K_SERVICE);

	public final static UUID UUID_BLE_E3K_CHAR = UUID
			.fromString(RBLGattAttributes.BLE_E3K_CHAR);

	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			String intentAction;
			boolean state;

			if (newState == BluetoothProfile.STATE_CONNECTED) {
				isNeedConnection = false;

				intentAction = ACTION_GATT_CONNECTED;
				broadcastUpdate(intentAction);

				state = mBluetoothGatt.discoverServices();

				Log.i(TAG, "Connected to GATT server.");
				// Attempts to discover services after successful connection.
				Log.i(TAG, "Attempting to start service discovery:" + state);

			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				intentAction = ACTION_GATT_DISCONNECTED;
				Log.e(TAG,"get disconnect event");
				if(status != 133)
				broadcastUpdate(intentAction);
/*
				try {
					gatt.close();
                    Log.i(TAG, "Closed the GATT server.");
				} catch (Exception e) {
					Log.d(TAG, "close ignoring: " + e);
				}
*/
			}

            if(status == 133){

				close();
				intentAction = ACTION_GATT_ERROR_133;
				String Address= gatt.getDevice().getAddress();
				Log.e(TAG, "GET Status = 133 ERROR!!"+Address);

				broadcastUpdate(intentAction,Address);
            }else
                Log.i(TAG, "Status = " + status);
		}
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {

				broadcastUpdate(ACTION_GATT_RSSI, rssi);
			} else {
				Log.i(TAG, "onReadRemoteRssi received: " + status);
			}
		};

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
			} else {
				Log.i(TAG, "onServicesDiscovered received: " + status);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_GATT_WRITE_SUCCESS, characteristic);
				Util.debugMessage(TAG,"send ok",debugFlag);
			}

			super.onCharacteristicWrite(gatt, characteristic, status);
		}
	};

	private void broadcastUpdate(final String action) {
		final Intent intent = new Intent(action);
		sendBroadcast(intent);
	}

	private void broadcastUpdate(final String action, String address) {
		final Intent intent = new Intent(action);
		intent.putExtra(EXTRA_DATA, address);
		sendBroadcast(intent);
	}

	private void broadcastUpdate(final String action, int rssi) {
		final Intent intent = new Intent(action);
		intent.putExtra(EXTRA_DATA, String.valueOf(rssi));
		sendBroadcast(intent);
	}

	private void broadcastUpdate(final String action,
			final BluetoothGattCharacteristic characteristic) {
		final Intent intent = new Intent(action);

        final byte[] rx = characteristic.getValue();
        intent.putExtra(EXTRA_DATA, rx);
        intent.putExtra("UUID", characteristic.getUuid().toString());

		sendBroadcast(intent);
	}

	public class LocalBinder extends Binder {
		public RBLService getService() {
			return RBLService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// After using a given device, you should make sure that
		// BluetoothGatt.close() is called
		// such that resources are cleaned up properly. In this particular
		// example, close() is
		// invoked when the UI is disconnected from the Service.
		close();
		return super.onUnbind(intent);
	}

	private final IBinder mBinder = new LocalBinder();

	/**
	 * Initializes a reference to the local Bluetooth adapter.
	 * 
	 * @return Return true if the initialization is successful.
	 */
	public boolean initialize() {
		// For API level 18 and above, get a reference to BluetoothAdapter
		// through
		// BluetoothManager.
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				Log.e(TAG, "Unable to initialize BluetoothManager.");
				return false;
			}
		}

		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
		}

		return true;
	}

	/**
	 * Connects to the GATT server hosted on the Bluetooth LE device.
	 * 
	 * @param address
	 *            The device address of the destination device.
	 * 
	 * @return Return true if the connection is initiated successfully. The
	 *         connection result is reported asynchronously through the
	 *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 *         callback.
	 */
	public boolean connect(final String address) {
		if (mBluetoothAdapter == null || address == null) {
			Log.w(TAG,
					"BluetoothAdapter not initialized or unspecified address.");
			return false;
		}
		Util.debugMessage(TAG,"bd address="+address,debugFlag);
		// Previously connected device. Try to reconnect.
		/*if (mBluetoothDeviceAddress != null
				&& address.equals(mBluetoothDeviceAddress)
				&& mBluetoothGatt != null) {
			Log.e(TAG,
					"Trying to use an existing mBluetoothGatt for connection.");
			if (mBluetoothGatt.connect()) {
				Log.e(TAG,"reconnect");

				return true;
			} else {
				return false;
			}
		}*/
		close();

		final BluetoothDevice device = mBluetoothAdapter
				.getRemoteDevice(address);
		if (device == null) {
			Log.w(TAG, "Device not found.  Unable to connect.");
			return false;
		}
		// We want to directly connect to the device, so we are setting the
		// autoConnect
		// parameter to false.
		mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
		tmpConAddr = address;
		Log.d(TAG, "Refresh Device Cache.");
		refreshDeviceCache(mBluetoothGatt);

		Log.d(TAG, "Trying to create a new connection.");
		mBluetoothDeviceAddress = address;

		return true;
	}

	/**
	 * Disconnects an existing connection or cancel a pending connection. The
	 * disconnection result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 * callback.
	 */
	public void disconnect() {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		//Log.e("sean","disconnect bd address="+mBluetoothGatt.getDevice().getAddress().toString());
		mBluetoothGatt.disconnect();

		 /*Timer check =new Timer();
		check.schedule(new TimerTask() {
			@Override
			public void run() {
				Log.e("sean","chech disconnect=");
				if(mBluetoothGatt != null){
				if(mBluetoothGatt.getConnectedDevices() != null){
					int size = mBluetoothGatt.getConnectedDevices().size();
					for(int i=0;i<size;i++)
						Log.e("sean","device no disconnect="+mBluetoothGatt.getConnectedDevices().get(i).getAddress().toString());

				}
				}
			}
		},5000);
		check = null;*/
	}

	/**
	 * After using a given BLE device, the app must call this method to ensure
	 * resources are released properly.
	 */
	public void close() {
		if (mBluetoothGatt == null) {
			return;
		}
		mBluetoothGatt.close();
		mBluetoothGatt = null;
	}

	/**
	 * Request a read on a given {@code BluetoothGattCharacteristic}. The read
	 * result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
	 * callback.
	 * 
	 * @param characteristic
	 *            The characteristic to read from.
	 */
	public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}

		mBluetoothGatt.readCharacteristic(characteristic);
	}

	public void readRssi() {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}

		mBluetoothGatt.readRemoteRssi();
	}

	public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}

		mBluetoothGatt.writeCharacteristic(characteristic);
	}

	public boolean writeData(BluetoothGattCharacteristic characteristic,byte data[]) {

		boolean res = false;
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return false;
		}

			ByteBuffer buff = ByteBuffer.wrap(data,0,data.length);
			bpEncode encode = new bpEncode();
		    byte tmp [];
			while(buff.hasRemaining())
			{
				Util.debugMessage(TAG,"buff limit="+buff.remaining(),debugFlag);
				if(buff.remaining()>20) {
					tmp = new byte[BLE_Packet_MAX_LEN];
					buff.get(tmp, 0, BLE_Packet_MAX_LEN);
				}
				else{
				 	tmp=new byte[buff.remaining()];
					buff.get(tmp,0,buff.remaining());
				}
				characteristic.setValue(tmp);
				Util.debugMessage(TAG,"Data="+ encode.BytetoHexString(tmp),debugFlag);
				res = mBluetoothGatt.writeCharacteristic(characteristic);
				buff.flip();
				if(res)
				buff.compact();
				else
				 break;
				tmp = null;
				try{
					Thread.sleep(300);
				}catch(InterruptedException e)
				{

				}


			}


		return res;
	}


	/**
	 * Enables or disables notification on a give characteristic.
	 * 
	 * @param characteristic
	 *            Characteristic to act on.
	 * @param enabled
	 *            If true, enable notification. False otherwise.
	 */
	public void setCharacteristicNotification(
			BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
		BluetoothGattDescriptor descriptor = characteristic
					.getDescriptor(UUID
							.fromString(RBLGattAttributes.BLE_E3K_CHAR_DESC));
			descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			mBluetoothGatt.writeDescriptor(descriptor);


	}

	public BluetoothGattService getSupportedGattService() {
		if (mBluetoothGatt == null)
			return null;

		return mBluetoothGatt.getService(UUID_BLE_E3K_SERVICE);
	}

	private boolean refreshDeviceCache(BluetoothGatt gatt){
		try {
			BluetoothGatt localBluetoothGatt = gatt;
			Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
			if (localMethod != null) {
				boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
				return bool;
			}
		}
		catch (Exception localException) {
			Log.e(TAG, "An exception occured while refreshing device");
		}
		return false;
	}

	public String getBluetoothDeviceAddress() {
		return mBluetoothDeviceAddress;
	}
}
