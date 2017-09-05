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

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for
 * demonstration purposes.
 */
public class RBLGattAttributes {
	private static HashMap<String, String> attributes = new HashMap<>();

    //E-3000
	public static String BLE_E3K_SERVICE = "0000E0FF-3C17-D293-8E48-14FE2E4DA212";
	public static String BLE_E3K_CHAR = "0000FFE1-0000-1000-8000-00805F9B34FB";


	/*static String BLE_E3K_SERVICE = "00005000-0000-1000-8000-00805f9b34fb";
	static String BLE_E3K_CHAR =  "00005a02-0000-1000-8000-00805F9b34fb";
	static String BLE_E3K_tCHAR = "00005a01-0000-1000-8000-00805F9b34fb";*/
	public static String BLE_E3K_CHAR_DESC ="00002902-0000-1000-8000-00805f9b34fb";

	static {
		// RB8762 Services.
        attributes.put(BLE_E3K_SERVICE, "BLE E-3000 Service");

		// RB8762 Characteristics.
		attributes.put(BLE_E3K_CHAR, "BLE E-3000 characteristics");

		// RB8762 Descriptor of Characteristics
		attributes.put(BLE_E3K_CHAR_DESC, "BLE E-3000 descriptor of characteristics");


	}

	public static String lookup(String uuid, String defaultName) {
		String name = attributes.get(uuid);
		return name == null ? defaultName : name;
	}
}
