# LibLightify

A simple manager for controlling Lightify devices.

## Maven Dependency

Include the library in your project by adding the following dependency to your pom.xml

```
<dependency>
	<groupId>com.mclarkdev.tools</groupId>
	<artifactId>liblightify</artifactId>
	<version>1.5.1</version>
</dependency>
```

## Example

To begin using the controller, you must first connect to the gateway and retrieve a list of devices.

```
// Setup controller object
LibLightify bridge = new LibLightify("192.168.10.220");

// Fetch device info
bridge.refresh();

// Retrieve list of devices
bridge.getDevices();
```

# License

Open source & free for all. ❤
