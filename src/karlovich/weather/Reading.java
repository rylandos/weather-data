package karlovich.weather;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * A representation of a single reading from a Netatmo outdoor weather station, consisting of timestamp, temperature and humidity
 * @author Ryland
 *
 */
public class Reading{

	private LocalDateTime timestamp;
	private double temperature;
	private int humidity;
	
	public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
	public static DateTimeFormatter outFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
	
	public Reading(String ts, String temp, String hum) {
		timestamp = LocalDateTime.parse(ts, formatter);
		temperature = Double.parseDouble(temp);
		humidity = Integer.parseInt(hum);
	}
	
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
	
	public int getDay() {
		return timestamp.getDayOfMonth();
	}
	
	public double getTemperature() {
		return temperature;
	}
	
	public int getHumidity() {
		return humidity;
	}
	
	public String toString() {
		return outFormatter.format(timestamp) + " reading - temperature: " + temperature + ", humidity: " + humidity;
	}
}
