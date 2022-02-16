package com.mclarkdev.tools.lightify.api;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import com.mclarkdev.tools.libextras.LibExtrasStrings;

public class LightifyClient {

	private final String gatewayIP;

	private final int gatewayPort;

	private final Socket socket;

	/**
	 * Create a new Client connection to a given gateway.
	 * 
	 * @param gatewayIP
	 * @throws IOException
	 */
	public LightifyClient(String gatewayIP) throws IOException {

		this(gatewayIP, 4000);
	}

	/**
	 * Create a new Client connection to a given gateway on a custom port.
	 * 
	 * @param gatewayIP
	 * @param gatewayPort
	 * @throws IOException
	 */
	public LightifyClient(String gatewayIP, int gatewayPort) throws IOException {

		this.gatewayIP = gatewayIP;
		this.gatewayPort = gatewayPort;

		// establish a connection or throw
		this.socket = new Socket(gatewayIP, gatewayPort);
	}

	/**
	 * Get the IP of the gateway.
	 * 
	 * @return
	 */
	public String getGatewayIP() {

		return gatewayIP;
	}

	/**
	 * Get the port of the gateway server.
	 * 
	 * @return
	 */
	public int getGatewayPort() {

		return gatewayPort;
	}

	/**
	 * Get the gateway connection address.
	 * 
	 * @return
	 */
	public String getGatewayAddress() {

		return getGatewayIP() + ":" + getGatewayPort();
	}

	/**
	 * Turn off all lights.
	 */
	public void blackout() {

		LightifyDevice[] devices;
		try {

			devices = getDevices();
		} catch (Exception e) {
			return;
		}

		// get all devices
		for (LightifyDevice device : devices) {

			// skip disconnected devices
			if (!device.isOnline()) {
				continue;
			}

			// skip if power state not available
			if (!device.supports(LightifyCapability.POWER)) {
				continue;
			}

			// skip if already turned off
			if (!device.getPoweredOn()) {
				continue;
			}

			try {

				// turn off
				setDevicePowerState(device, false);
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Disconnect the client from the gateway.
	 */
	public void disconnect() {

		try {

			// close socket
			socket.close();
		} catch (Exception e) {
		}
	}

	/**
	 * Send a binary command to the gateway.
	 * 
	 * @param command
	 */
	public byte[] sendCommand(byte[] command) throws Exception {

		// write command to socket
		socket.getOutputStream().write(command);

		// current time
		long timeout = 30 * 1000;
		long timeStart = System.currentTimeMillis();

		// wait for response or timeout
		while (socket.getInputStream().available() == 0) {

			if (System.currentTimeMillis() > (timeStart + timeout)) {

				throw new SocketTimeoutException("Failed to receive response for command.");
			}

			// release some cycles
			Thread.sleep(25);
		}

		// count number of bytes available
		int available = socket.getInputStream().available();
		byte[] response = new byte[available];

		// read the response
		socket.getInputStream().read(response);

		// return the response
		return response;
	}

	/**
	 * Get a list of known devices from the gateway.
	 */
	public LightifyDevice[] getDevices() throws Exception {

		return getDevices(null);
	}

	/**
	 * Get a list of known devices from the gateway whose name contains the provided
	 * string.
	 */
	public LightifyDevice[] getDevices(String contains) throws Exception {

		// base command
		byte[] command = { 0x0B, 0x00, 0x00, 0x13, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00 };

		// get response
		byte[] response = sendCommand(command);

		// get number of devices
		int deviceCount = response[9];

		ArrayList<LightifyDevice> devices = new ArrayList<>();

		for (int x = 0; x < deviceCount; x++) {

			// holder for device data
			byte[] deviceData = new byte[50];

			// data offset
			int start = 11 + (50 * x);

			// copy from blob to new
			System.arraycopy(response, start, deviceData, 0, 50);

			// build device object with blob
			LightifyDevice device = new LightifyDevice(deviceData);

			// if no match pattern
			if (contains == null || contains.equals("")) {

				// add to lsit
				devices.add(device);
			} else {

				// else check if name matches
				if (device.getName().contains(contains)) {

					// and add to lsit
					devices.add(device);
				}
			}
		}

		// return list of devices as array
		return devices.toArray(new LightifyDevice[devices.size()]);
	}

	/**
	 * Set the power state of a given device.
	 * 
	 * @param device
	 * @param poweredOn
	 * @return
	 * @throws Exception
	 */
	public boolean setDevicePowerState(LightifyDevice device, boolean poweredOn) throws Exception {

		// fail if unsupported
		if (!device.supports(LightifyCapability.POWER)) {

			throw new IllegalArgumentException("Device does not support power states.");
		}

		// base command
		byte[] command = { 0x0F, 0x00, 0x00, 0x32, 0x00, 0x00, 0x00, 0x00, //
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //
				0x00 };

		// get and reverse mac - convert to bytes
		byte[] rmacBytes = LibExtrasStrings.hexToBytes(//
				LibExtrasStrings.reverseString(device.getMac(), 2));

		// copy rmac into command
		System.arraycopy(rmacBytes, 0, command, 8, 8);

		// get desired state
		byte state = poweredOn ? (byte) 0x01 : (byte) 0x00;

		// update state in command
		command[command.length - 1] = state;

		// get response from command
		byte[] response = sendCommand(command);

		// TODO parse response
		return true;
	}

	/**
	 * Set the brightness of a given device.
	 * 
	 * Brightness range: 0 - 255;
	 * 
	 * @param device
	 * @param brightness
	 * @return
	 */
	public boolean setDeviceBrightness(LightifyDevice device, int brightness) throws Exception {

		// fail if unsupported
		if (!device.supports(LightifyCapability.BRIGHTNESS)) {

			throw new IllegalArgumentException("Device does not support brightness levels.");
		}

		// fail if out of range
		if (brightness > 255 || brightness < 0) {

			throw new IllegalArgumentException("Requested value is out of range.");
		}

		// base command
		byte[] command = { 0x11, 0x00, 0x00, 0x31, 0x00, 0x00, 0x00, 0x00, //
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //
				0x00, 0x00, 0x00 };

		// get and reverse mac - convert to bytes
		byte[] rmacBytes = LibExtrasStrings.hexToBytes(//
				LibExtrasStrings.reverseString(device.getMac(), 2));

		// copy rmac into command
		System.arraycopy(rmacBytes, 0, command, 8, 8);

		// brightness
		command[16] = (byte) brightness;

		// transition time
		command[17] = 0x05;

		// get response from command
		byte[] response = sendCommand(command);

		// TODO parse response

		return true;
	}

	/**
	 * Set the color temperature of a given device.
	 * 
	 * Temperature range: 2200K - 6500K
	 * 
	 * @param device
	 * @param temp
	 * @return
	 */
	public boolean setDeviceTemperature(LightifyDevice device, int temp) throws Exception {

		// fail if unsupported
		if (!device.supports(LightifyCapability.TEMPERATURE)) {

			throw new IllegalArgumentException("Device does not support temperatures.");
		}

		// fail if out of range
		if (temp > 6500 || temp < 1500) {

			throw new IllegalArgumentException("Requested value is out of range.");
		}

		// base command
		byte[] command = { 0x12, 0x00, 0x00, 0x33, 0x00, 0x00, 0x00, 0x00, //
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //
				0x00, 0x00, 0x00, 0x00 };

		// get and reverse mac - convert to bytes
		byte[] rmacBytes = LibExtrasStrings.hexToBytes(//
				LibExtrasStrings.reverseString(device.getMac(), 2));

		// copy rmac into command
		System.arraycopy(rmacBytes, 0, command, 8, 8);

		command[16] = (byte) (temp & 0xFF);
		command[17] = (byte) ((temp >> 8) & 0xFF);

		// transition time
		command[18] = 0x05;

		// get response from command
		byte[] response = sendCommand(command);

		// TODO parse response

		return true;
	}

	/**
	 * Set a custom color of a given device.
	 * 
	 * Color range: 0 - 255
	 * 
	 * @param device
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 * @throws Exception
	 */
	public boolean setDeviceColor(LightifyDevice device, int r, int g, int b) throws Exception {

		// fail if unsupported
		if (!device.supports(LightifyCapability.COLOR)) {

			throw new IllegalArgumentException("Device does not support colors.");
		}

		// base command
		byte[] command = { 0x14, 0x00, 0x00, 0x36, 0x00, 0x00, 0x00, 0x00, //
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

		// get and reverse mac - convert to bytes
		byte[] rmacBytes = LibExtrasStrings.hexToBytes(//
				LibExtrasStrings.reverseString(device.getMac(), 2));

		// copy rmac into command
		System.arraycopy(rmacBytes, 0, command, 8, 8);

		command[16] = (byte) r;
		command[17] = (byte) g;
		command[18] = (byte) b;

		command[20] = 0x05;

		// get response from command
		byte[] response = sendCommand(command);

		// TODO parse response

		return true;
	}

	// do color cycle : 11 00 00 D5 00 00 00 00 hh hh hh hh hh hh hh hh ss tt tt
}
