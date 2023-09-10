package com.mclarkdev.tools.lightify;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import com.mclarkdev.tools.libobjectpooler.LibObjectPooler;
import com.mclarkdev.tools.libobjectpooler.LibObjectPoolerController;
import com.mclarkdev.tools.lightify.api.LibLightifyClient;
import com.mclarkdev.tools.lightify.api.LibLightifyDevice;

/**
 * LibLightify // LibLightify
 * 
 * A simple manager for controlling Lightify devices.
 */
public class LibLightify {

	private final String gatewayHost;
	private final int gatewayPort;

	private ConcurrentHashMap<String, LibLightifyDevice> deviceCache;

	private LibObjectPooler<LibLightifyClient> clientPool;

	/**
	 * Create a new instance of the LibLightify client.
	 * 
	 * @param host the bridge host
	 * @param port the bridge port
	 */
	public LibLightify(String host, int port) {

		this.gatewayHost = host;
		this.gatewayPort = port;

		this.deviceCache = new ConcurrentHashMap<>();

		this.clientPool = new LibObjectPooler<>(2, pooledClientController);

		Timer t = new Timer();
		t.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				refresh();
			}
		}, 0, 30000);
	}

	/**
	 * Returns a list of all discovered devices.
	 * 
	 * @return all discovered devices
	 */
	public ConcurrentHashMap<String, LibLightifyDevice> getDevices() {

		return deviceCache;
	}

	/**
	 * Returns an instance of client connection from the pool.
	 * 
	 * @return pooled object instance
	 * @throws IllegalStateException failure getting object from pool
	 */
	public LibLightifyClient getClient() throws IllegalStateException {

		try {

			return clientPool.getWait();
		} catch (Exception e) {

			throw new IllegalStateException("Not connected to gateway.");
		}
	}

	/**
	 * Release a client connection back to the pool.
	 * 
	 * @param client the client connection
	 * @return released successfully
	 */
	public boolean releaseClient(LibLightifyClient client) {

		return clientPool.release(client);
	}

	/**
	 * Set the max age allowed for the connection.
	 * 
	 * @param maxAge max connection age
	 */
	public void setGatewaySocketMaxAge(long maxAge) {

		clientPool.setMaxAge(maxAge);
	}

	/**
	 * Set the max time a connection may remain idle.
	 * 
	 * @param maxIdle max idle time
	 */
	public void setGatewaySocketMaxIdle(long maxIdle) {

		clientPool.setMaxIdleTime(maxIdle);
	}

	/**
	 * Set the max allowed size of the connection pool.
	 * 
	 * @param maxPoolSize max connection pool size
	 */
	public void setGatewayPoolSize(int maxPoolSize) {

		clientPool.setMaxPoolSize(maxPoolSize);
	}

	/**
	 * Refresh all discovered devices.
	 */
	public void refresh() {

		// fetch all devices
		LibLightifyDevice[] devices = fetchClientDevices();

		// fail if null
		if (devices == null) {

			return;
		}

		// synchronize on cache
		synchronized (deviceCache) {

			// clear all entries
			deviceCache.clear();

			// loop each fetched device
			for (LibLightifyDevice device : devices) {

				// add to cache map
				deviceCache.put(device.getName(), device);
			}
		}
	}

	/**
	 * Returns true if currently connected to the bridge.
	 * 
	 * @return connected to the bridge
	 */
	public boolean isConnected() {

		// return false if null
		if (clientPool == null) {

			return false;
		}

		// return true if more then one connection
		return clientPool.getPoolSize() > 0;
	}

	/**
	 * Shutdown the manager and close all connections.
	 */
	public void shutdown() {

		deviceCache.clear();
		clientPool.shutdown();
	}

	/**
	 * Fetch a list of all devices from the bridge.
	 * 
	 * @return list of discovered devices
	 */
	private LibLightifyDevice[] fetchClientDevices() {

		try {

			// get client connection
			LibLightifyClient client = clientPool.getWait();

			// get details for each device
			LibLightifyDevice[] devices = client.getDevices();

			// release the client
			clientPool.release(client);

			// return list of devices
			return devices;

		} catch (Exception e) {

			// log and error
			// Logger.e("Failed to get device details.", e);
			return null;
		}
	}

	/**
	 * Controller for client connection pool.
	 */
	private LibObjectPoolerController<LibLightifyClient> pooledClientController = new LibObjectPoolerController<LibLightifyClient>() {

		@Override
		public LibLightifyClient onCreate() {

			try {

				// create and return new client connection
				return new LibLightifyClient(gatewayHost, gatewayPort);

			} catch (Exception e) {
				return null;
			}
		}

		@Override
		public void onDestroy(LibLightifyClient client) {

			// disconnect client
			client.disconnect();
			return;
		}
	};
}
