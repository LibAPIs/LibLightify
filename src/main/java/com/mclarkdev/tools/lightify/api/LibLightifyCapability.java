package com.mclarkdev.tools.lightify.api;

import java.util.ArrayList;

/**
 * LibLightify // LibLightifyCapability
 */
public enum LibLightifyCapability {

	POWER(),

	BRIGHTNESS(),

	TEMPERATURE(),

	COLOR(),

	HARDWARE();

	/**
	 * Returns an array of capabilities supported by a given device.
	 * 
	 * @param capabilities capability data
	 * @return array of capabilities
	 */
	public static LibLightifyCapability[] getCapabilities(byte capabilities) {

		ArrayList<LibLightifyCapability> caps = new ArrayList<>();

		switch (capabilities) {

		case 0x0A:
			caps.add(COLOR);

		case 0x02:
			caps.add(TEMPERATURE);

			// TODO get code for this
		case 0x03:
			caps.add(BRIGHTNESS);

		case 0x04:
			caps.add(POWER);
			break;

		case 0x00:
		case 0x40:
			caps.add(HARDWARE);
		}

		return caps.toArray(new LibLightifyCapability[caps.size()]);
	}
}
