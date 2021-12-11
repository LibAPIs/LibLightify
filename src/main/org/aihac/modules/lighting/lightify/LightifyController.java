package main.org.aihac.modules.lighting.lightify;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import com.mclarkdev.tools.libobjectpooler.LibObjectPooler;
import com.mclarkdev.tools.libobjectpooler.LibObjectPoolerController;

import main.org.aihac.modules.lighting.lightify.api.LightifyClient;
import main.org.aihac.modules.lighting.lightify.api.LightifyDevice;

public class LightifyController {

	private final String gatewayHost;
	private final int gatewayPort;

	private ConcurrentHashMap<String, LightifyDevice> deviceCache;

	private LibObjectPooler<LightifyClient> clientPool;

	public LightifyController(String host, int port) {

		this.gatewayHost = host;
		this.gatewayPort = port;

		deviceCache = new ConcurrentHashMap<>();

		clientPool = new LibObjectPooler<>(2, pooledClientController);

		Timer t = new Timer();
		t.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				refresh();
			}
		}, 0, 30000);
	}

	public ConcurrentHashMap<String, LightifyDevice> getDevices() {

		return deviceCache;
	}

	public LightifyClient getClient() throws IllegalStateException {

		try {

			return clientPool.getWait();
		} catch (Exception e) {

			throw new IllegalStateException("Not connected to gateway.");
		}
	}

	public boolean releaseClient(LightifyClient client) {

		return clientPool.release(client);
	}

	public void setGatewaySocketMaxAge(long maxAge) {

		clientPool.setMaxAge(maxAge);
	}

	public void setGatewaySocketMaxIdle(long maxIdle) {

		clientPool.setMaxIdleTime(maxIdle);
	}

	public void setGatewayPoolSize(int maxPoolSize) {

		clientPool.setMaxPoolSize(maxPoolSize);
	}

	public void refresh() {

		// fetch all devices
		LightifyDevice[] devices = fetchClientDevices();

		// fail if null
		if (devices == null) {

			// Logger.w("No Lightify devices found.");
			return;
		}

		// synchronize on cache
		synchronized (deviceCache) {

			// clear all entries
			deviceCache.clear();

			// loop each fetched device
			for (LightifyDevice device : devices) {

				// add to cache map
				deviceCache.put(device.getName(), device);
			}
		}
	}

	public boolean isConnected() {

		// return false if null
		if (clientPool == null) {

			return false;
		}

		// return true if more then one connection
		return clientPool.getPoolSize() > 0;
	}

	public void shutdown() {

		deviceCache.clear();
		clientPool.shutdown();
	}

	private LightifyDevice[] fetchClientDevices() {

		try {

			// log debug
			// Logger.d("Polling Lightify devices...");

			// get client connection
			LightifyClient client = clientPool.getWait();

			// get details for each device
			LightifyDevice[] devices = client.getDevices();

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
	private LibObjectPoolerController<LightifyClient> pooledClientController = new LibObjectPoolerController<LightifyClient>() {

		@Override
		public LightifyClient onCreate() {

			try {

				// log connecting
				// Logger.d("Connecting to Lightify gateway [ " + gatewayHost + " : " +
				// gatewayPort + " ]");

				// create and return new client connection
				return new LightifyClient(gatewayHost, gatewayPort);

			} catch (Exception e) {

				// log connection error
				// Logger.e("Failed to connect to gateway [ " + gatewayHost + ":" + gatewayPort
				// + " ]", e);
				return null;
			}
		}

		@Override
		public void onDestroy(LightifyClient client) {

			// disconnect client
			client.disconnect();

			return;
		}
	};
}
