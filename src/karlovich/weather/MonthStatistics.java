package karlovich.weather;

import java.util.*;
import java.text.DecimalFormat;
import java.time.Month;

/**
 * A class for collecting overall weather statistics for a month
 * @author Ryland
 *
 */
public class MonthStatistics {

	private double minimumTemp;
	private double maximumTemp;
	private double averageTemp;
	
	private double averageHigh;
	private double averageLow;	
	private double lowestHigh;
	private double highestLow;
	
	private Map<Integer, Double> highTemps;
	private Map<Integer, Double> lowTemps;
	
	private int minimumHumidity;
	private int maximumHumidity;
	private double averageHumidity;
	
	private Month theMonth;
	private int theYear;
	
	public Month getMonth() {
		return theMonth;
	}
	
	public int getYear() {
		return theYear;
	}
	
	public static DecimalFormat df = new DecimalFormat("##.#");
	
	/**
	 * The constructor takes most statistics pre-calculated, meaning this class does not need to know
	 * about the raw readings.  It does take a map each for high and low temperatures associated with
	 * each day, from which it will calculate lower-level statistics, e.g. average high.
	 * @param minT
	 * @param maxT
	 * @param avgT
	 * @param highTs
	 * @param lowTs
	 * @param minH
	 * @param maxH
	 * @param avgH
	 * @param month
	 * @param year
	 */
	public MonthStatistics(double minT, double maxT, double avgT, Map<Integer, Double> highTs,
			Map<Integer, Double> lowTs, int minH, int maxH, double avgH, Month month, int year) {
		minimumTemp = minT;
		maximumTemp = maxT;
		averageTemp = avgT;
		highTemps = highTs;
		lowTemps = lowTs;
		minimumHumidity = minH;
		maximumHumidity = maxH;
		averageHumidity = avgH;
		theMonth = month;
		theYear = year;
		
		// Calculate additional fields
		averageHigh = getAverageTemp(highTemps);
		averageLow = getAverageTemp(lowTemps);
		lowestHigh = getMinTemp(highTemps);
		highestLow = getMaxTemp(lowTemps);
	}
	
	/**
	 * Calculate the average temperature of a given set of values.  This can be used with both the month's
	 * high temperatures and low temperatures to calculate the average high and average low respectively.
	 * @param source - a Map containing the values to be averaged associated with each day
	 * @return the average of the input temperatures
	 */
	public static double getAverageTemp(Map<Integer, Double> source) {
		OptionalDouble result = source.keySet().stream().mapToDouble(k -> source.get(k)).average();
		if (result.isPresent()) {
			return result.getAsDouble();
		}
		else {
			return 0.0;
		}
	}
	
	/**
	 * Get the maximum among a given set of input temperatures
	 * @param source - a Map containing the values to be averaged associated with each day
	 * @return the highest of the input temperatures
	 */
	public static double getMaxTemp(Map<Integer, Double> source) {
		OptionalDouble result = source.keySet().stream().mapToDouble(k -> source.get(k)).max();
		if (result.isPresent()) {
			return result.getAsDouble();
		}
		else {
			return 0.0;
		}
	}
	
	/**
	 * Get the minimum among a given set of input temperatures
	 * @param source - a Map containing the values to be averaged associated with each day
	 * @return the lowest of the input temperatures
	 */
	public static double getMinTemp(Map<Integer, Double> source) {
		OptionalDouble result = source.keySet().stream().mapToDouble(k -> source.get(k)).min();
		if (result.isPresent()) {
			return result.getAsDouble();
		}
		else {
			return 0.0;
		}
	}
	
	/**
	 * Output the month's statistics in a print-friendly format
	 */
	public void printStatistics() {
		System.out.println("WEATHER STATION STATISTICS FOR: " + theMonth + " " + theYear);
		System.out.println();
		System.out.println("---TEMPERATURE---");
		System.out.println("Average high: " + df.format(averageHigh));
		System.out.println("Average low: " + df.format(averageLow));
		System.out.println("Average temperature: " + df.format(averageTemp));
		System.out.println("Maximum temperature: " + maximumTemp);
		System.out.println("Minimum temperature: " + minimumTemp);
		System.out.println("Coldest day: " + lowestHigh);
		System.out.println("Warmest night: " + highestLow);
		System.out.println();
		System.out.println("---HUMIDITY---");
		System.out.println("Average humidity: " + df.format(averageHumidity));
		System.out.println("Maximum humidity: " + maximumHumidity);
		System.out.println("Minimum humidity: " + minimumHumidity);
		System.out.println();
		System.out.println();
	}
}
