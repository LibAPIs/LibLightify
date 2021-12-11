function getColorForTemperature(temp) {

	temp = temp / 100;

	var r = 127;
	var g = 127;
	var b = 127;

	// red
	if (temp <= 66) {
		r = 255
	} else {
		r = temp - 60;
		r = 329.698727446 * (r ^ -0.1332047592);
		if (r < 0) {
			red = 0;
		}
		if (r > 255) {
			red = 255;
		}
	}

	// green
	if (temp <= 66) {
		g = temp;
		g = 99.4708025861 * Math.log(g) - 161.1195681661
		if (g < 0) {
			g = 0;
		}
		if (g > 255) {
			g = 255;
		}
	} else {
		g = temp - 60;
		g = 288.1221695283 * (g ^ -0.0755148492);
		if (g < 0) {
			g = 0;
		}
		if (g > 255) {
			g = 255;
		}
	}

	// blue
	if (temp >= 66) {
		b = 255;
	} else {

		if (temp <= 19) {
			b = 0;
		} else {
			b = temp - 10;
			
			b = 138.5177312231 * Math.log(b) - 305.0447927307
			if (b < 0) {
				b = 0;
			}
			if (b > 255) {
				b = 255;
			}
		}

	}

	return [ r, g, b ];
}
