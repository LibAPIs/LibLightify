package main.org.aihac.modules.lighting.lightify.api;

import java.util.ArrayList;

public enum LightifyCapability {

	POWER(),

	BRIGHTNESS(),

	TEMPERATURE(),

	COLOR(),

	HARDWARE();

	public static LightifyCapability[] getCapabilities(byte capabilities) {

		ArrayList<LightifyCapability> caps = new ArrayList<>();

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

		return caps.toArray(new LightifyCapability[caps.size()]);
	}
}
