package com.mclarkdev.tools.lightify.api;

import java.awt.Color;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mclarkdev.tools.libextras.LibExtrasStrings;

public class LightifyDevice {

	private final String macAddress;

	private final LightifyCapability[] caps;

	private final String firmwareVersion;

	private final boolean online;

	private final boolean poweredOn;

	private final int brightness;

	private final long temperature;

	private final Color color;

	private final int white;

	private final String name;

	public LightifyDevice(byte[] bin) {

		byte[] mac = new byte[8];

		System.arraycopy(bin, 2, mac, 0, 8);

		this.macAddress = LibExtrasStrings.reverseString(LibExtrasStrings.bytesToHex(mac), 2);

		this.caps = LightifyCapability.getCapabilities(bin[10]);

		this.firmwareVersion = "" + (int) bin[11] + //
				"" + (int) bin[12] + //
				"" + (int) bin[13] + //
				"" + (int) bin[14];

		this.online = bin[15] == 0x02;

		this.poweredOn = bin[18] == 0x01;

		this.brightness = bin[19];

		this.temperature = ((bin[21] & 0xFF) << 8) | (bin[20] & 0xFF);

		int r = bin[22] & 0xFF;
		int g = bin[23] & 0xFF;
		int b = bin[24] & 0xFF;
		this.color = new Color(r, g, b);

		this.white = bin[25];

		byte[] nameBytes = new byte[16];
		System.arraycopy(bin, 26, nameBytes, 0, 16);
		this.name = new String(nameBytes).trim();
	}

	public String getMac() {

		return macAddress;
	}

	public String getFirmwareVersion() {

		return firmwareVersion;
	}

	public boolean isOnline() {

		return online;
	}

	public boolean getPoweredOn() {

		return poweredOn;
	}

	public int getBrightness() {

		return brightness;
	}

	public long getTemperature() {

		return temperature;
	}

	public Color getColor() {

		return color;
	}

	public int getWhite() {

		return white;
	}

	public String getName() {

		return name;
	}

	public boolean supports(LightifyCapability theCapability) {

		for (LightifyCapability capability : caps) {

			if (capability == theCapability) {

				return true;
			}
		}
		return false;
	}

	public JSONObject getDetails() {

		// basic device details
		JSONObject details = new JSONObject()//
				.put("name", name)//
				.put("mac", macAddress)//
				.put("online", online)//
				.put("firmware", firmwareVersion);

		// add device capabilities
		JSONArray capabilities = new JSONArray();
		details.put("capabilities", capabilities);
		for (LightifyCapability cap : caps) {
			capabilities.put(cap.toString());
		}

		// device power state
		if (supports(LightifyCapability.POWER)) {
			details.put("power", poweredOn);
		}

		// details for brightness feature
		if (supports(LightifyCapability.BRIGHTNESS)) {
			details.put("brightness", brightness);
		}

		// details for temperature feature
		if (supports(LightifyCapability.TEMPERATURE)) {
			details.put("temperature", temperature);
		}

		// details for color feature
		if (supports(LightifyCapability.COLOR)) {
			details.put("color", new JSONObject()//
					.put("red", color.getRed())//
					.put("green", color.getGreen())//
					.put("blue", color.getBlue()));
		}

		return details;
	}
}
