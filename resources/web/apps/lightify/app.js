var devices = null;

function appLoad() {
	appUpdate();
}

function appUpdate() {

	get("/-/lightify/details", onGetDetails, {});
}

function onGetDetails(data) {

	data = JSON.parse(data);
	devices = data.properties.devices;

	var table = document.getElementById("deviceList");

	for (var row = table.rows.length - 1; row > 0; row--) {
		table.deleteRow(row);
	}

	for ( var device in devices) {

		var row = document.createElement("tr");
		table.appendChild(row);

		var name = document.createElement("td");
		name.innerHTML = devices[device].name;
		row.appendChild(name);

		var mac = document.createElement("td");
		mac.innerHTML = devices[device].mac;
		row.appendChild(mac);

		var firmware = document.createElement("td");
		firmware.innerHTML = devices[device].firmware;
		row.appendChild(firmware);

		var online = document.createElement("td");
		online.innerHTML = devices[device].online;
		row.appendChild(online);

		var power = document.createElement("td");
		power.innerHTML = devices[device].power;
		row.appendChild(power);

		var brightness = document.createElement("td");
		row.appendChild(brightness);
		if ('brightness' in devices[device]) {

			brightness.innerHTML = devices[device].brightness;
		}

		var temperature = document.createElement("td");
		row.appendChild(temperature);
		if ('temperature' in devices[device]) {

			var temp = devices[device].temperature;
			var color = getColorForTemperature(temp);
			var colorRGB = rgb(color[0], color[1], color[2]);

			temperature.innerHTML = temp;
			temperature.style.backgroundColor = colorRGB;
		}

		var color = document.createElement("td");
		color.style.minWidth = "30px";
		row.appendChild(color);
		if ('color' in devices[device]) {

			var r = devices[device].color.red;
			var g = devices[device].color.green;
			var b = devices[device].color.blue;

			color.innerHTML = rgb(r, g, b);
			color.style.backgroundColor = rgb(r, g, b);
		}

		var capabilities = document.createElement("td");
		capabilities.innerHTML = devices[device].capabilities;
		row.appendChild(capabilities);
	}
}

function rgb(r, g, b) {
	return "rgb(" + r + "," + g + "," + b + ")";
}
