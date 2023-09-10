package com.mclarkdev.tools.lightify.api;

import java.awt.Color;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mclarkdev.tools.libextras.LibExtrasStrings;

/**
 * LibLightify // LibLightifyDevice
 */
public class LibLightifyDevice {

	private final String macAddress;

	private final LibLightifyCapability[] caps;

	private final String firmwareVersion;

	private final boolean online;

	private final boolean poweredOn;

	private final int brightness;

	private final long temperature;

	private final Color color;

	private final int white;

	private final String name;

	public LibLightifyDevice(byte[] bin) {

		byte[] mac = new byte[8];

		System.arraycopy(bin, 2, mac, 0, 8);

		this.macAddress = LibExtrasStrings.reverseString(LibExtrasStrings.bytesToHex(mac), 2);

		this.caps = LibLightifyCapability.getCapabilities(bin[10]);

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

	/**
	 * Returns the MAC address of the device.
	 * 
	 * @return MAC address of the device
	 */
	public String getMac() {

		return macAddress;
	}

	/**
	 * Returns the firmware string of the device.
	 * 
	 * @return firmware version of device
	 */
	public String getFirmwareVersion() {

		return firmwareVersion;
	}

	/**
	 * Returns true if the device is online and connected.
	 * 
	 * @return device is online
	 */
	public boolean isOnline() {

		return online;
	}

	/**
	 * Returns true if the device is powered on.
	 * 
	 * @return device powered on
	 */
	public boolean getPoweredOn() {

		return poweredOn;
	}

	/**
	 * Returns the brightness of the device.
	 * 
	 * @return brightness of the device (0..255)
	 */
	public int getBrightness() {

		return brightness;
	}

	/**
	 * Returns the color temperature of the device.
	 * 
	 * @return color temperature of the device
	 */
	public long getTemperature() {

		return temperature;
	}

	/**
	 * Returns the color of the device.
	 * 
	 * @return color of the device
	 */
	public Color getColor() {

		return color;
	}

	/**
	 * Returns amount of White present.
	 *
	 * @return value of White (0..255)
	 */
	public int getWhite() {

		return white;
	}

	/**
	 * Returns the name of the device.
	 * 
	 * @return name of the device
	 */
	public String getName() {

		return name;
	}

	/**
	 * Returns true if a device supports a specific capability.
	 * 
	 * @param theCapability the capability
	 * @return capability supported
	 */
	public boolean supports(LibLightifyCapability theCapability) {

		for (LibLightifyCapability capability : caps) {

			if (capability == theCapability) {

				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the device info as a JSON object.
	 * 
	 * @return device info
	 */
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
		for (LibLightifyCapability cap : caps) {
			capabilities.put(cap.toString());
		}

		// device power state
		if (supports(LibLightifyCapability.POWER)) {
			details.put("power", poweredOn);
		}

		// details for brightness feature
		if (supports(LibLightifyCapability.BRIGHTNESS)) {
			details.put("brightness", brightness);
		}

		// details for temperature feature
		if (supports(LibLightifyCapability.TEMPERATURE)) {
			details.put("temperature", temperature);
		}

		// details for color feature
		if (supports(LibLightifyCapability.COLOR)) {
			details.put("color", new JSONObject()//
					.put("red", color.getRed())//
					.put("green", color.getGreen())//
					.put("blue", color.getBlue()));
		}

		return details;
	}
}
