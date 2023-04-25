package karlovich.weather;

import java.nio.file.*;
import java.time.Month;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main class to load raw weather data from a Netatmo station and calculate statistics from the data
 * @author Ryland
 *
 */
public class WeatherDataLoader {

	/**
	 * Read a single directory to find CSVs with weather station data and summarise their statistics
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// Change path if necessary - or read in from args or properties file
		Path sourceDir = Paths.get("/Users/ryland/Documents/Java/weather-data/Source");
		
		if (Files.exists(sourceDir)) {			
			List<Path> monthFiles = new ArrayList<Path>();
			
			// Look in this directory only with a depth of one
			Files.walk(sourceDir, 1).filter(f -> f.toString().endsWith(".csv")).forEach(p -> monthFiles.add(p));
			
			// Summarise monthly weather statistics and print
			List<MonthStatistics> monthStats = calculateMonthStatistics(monthFiles);
			printStats(monthStats);
			
			// Write data to a file
			writeDailyStats(monthStats, Paths.get("/Users/ryland/Documents/Java/weather-data/Output"));
		}
	}
	
	/**
	 * Read a CSV input file - assumption that it covers one month but not required - to parse a Reading from each line
	 * @param sourceFile - a CSV file containing weather station data in the format exported from a Netatmo station
	 * @return
	 */
	protected static List<Reading> readMonthData(Path sourceFile) {
		List<Reading> readings = new ArrayList<Reading>();
		
		try {
			// Skip the first three header lines then create a Reading from each remaining line
			Files.lines(sourceFile).skip(3).forEach(l -> {
				String[] cols = l.split(",");
				readings.add(new Reading(cols[1], cols[2], cols[3]));
			});
		} catch (IOException e) {
			System.out.println("Unable to read file " + sourceFile.toString());
			System.out.println(e.getMessage());
		}
		
		return readings;
	}
	
	/**
	 * Get summary statistics for an input list of data files
	 * @param files - CSV files as exported from Netatmo
	 */
	public static List<MonthStatistics> calculateMonthStatistics(List<Path> files) {
		List<MonthStatistics> monthStats = new ArrayList<MonthStatistics>();
		
		// For each file, get the data as Reading objects, then calculate statistics
		files.forEach(f -> { List<Reading> r = readMonthData(f); 
		MonthStatistics s = getMonthStatistics(r);
		monthStats.add(s);});
		
		return monthStats;
	}
	
	/**
	 * Sort month statistics by date and print the summary data
	 * @param monthStats
	 */
	public static void printStats(List<MonthStatistics> monthStats) {
		// Print statistics for each month in order
		Comparator<MonthStatistics> yearComparison = Comparator.comparing(MonthStatistics::getYear);
		Comparator<MonthStatistics> monthComparison = Comparator.comparing(MonthStatistics::getMonth);
		Comparator<MonthStatistics> yearMonthComparison = yearComparison.thenComparing(monthComparison);
		monthStats.stream().sorted(yearMonthComparison).forEach(s -> s.printStatistics());
	}
	
	public static void writeDailyStats(List<MonthStatistics> monthStats, Path targetDir) {
		// Make a map of stats grouped by year
		Map<Integer, List<MonthStatistics>> groupedStats = new HashMap<>();
		List<Integer> years = monthStats.stream().map(m -> m.getYear()).collect(Collectors.toList());
		years.forEach(y -> {
			groupedStats.put(y, monthStats.stream().filter(m -> m.getYear() == y).collect(Collectors.toList()));
		});
		
		groupedStats.forEach((y, ms) -> {
			System.out.println("High temperatures for year " + y + ":");
			Path highTempsFile = Paths.get(targetDir.toString(), y.toString() + "_highs.csv");
			Path lowTempsFile = Paths.get(targetDir.toString(), y.toString() + "_lows.csv");
			List<String> highLines = new ArrayList<>();
			List<String> lowLines = new ArrayList<>();
			
			ms.stream().sorted(Comparator.comparing(MonthStatistics::getMonth)).forEach(m -> {
				StringBuilder highline = new StringBuilder();
				StringBuilder lowline = new StringBuilder();
				m.getHighTemps().forEach((d, t) -> highline.append(t + ", "));
				m.getLowTemps().forEach((d, t) -> lowline.append(t + ", "));
				highLines.add(highline.toString());
				lowLines.add(lowline.toString());
				System.out.println(highline);
			});
			try {
				Files.write(highTempsFile, highLines);
				Files.write(lowTempsFile, lowLines);
			} catch (IOException e) {}
			System.out.println();
		});
	}
	
	/**
	 * Get top-level statistics for a list of weather station readings
	 * Assumption that the input readings are from one month - this is not checked (possible enhancement)
	 * @param readings - a list of Reading objects belonging to one month, to be summarised
	 * @return a MonthStatistics object representing the top and lower-level weather statistics for the month
	 */
	public static MonthStatistics getMonthStatistics(List<Reading> readings) {
		// Assumption that readings are from the same month - find out which from a sample Reading
		Reading sample = readings.stream().findFirst().get();
		Month month = sample.getTimestamp().getMonth();
		int year = sample.getTimestamp().getYear();
		
		// Group readings by the day in the month they occurred on - used for working out lower-level statistics
		// within a day, e.g. the day's high temperature
		Map<Integer, List<Reading>> readingsByDay = getReadingsByDay(readings);
		
		// Create a MonthStatistics by passing in top-level calculations for the readings
		MonthStatistics stats = new MonthStatistics(getLowestTemp(readings), getHighestTemp(readings),
				getAverageTemp(readings), getHighTemps(readingsByDay), getLowTemps(readingsByDay),
				getLowestHumidity(readings), getHighestHumidity(readings), getAverageHumidity(readings),
				month, year);
		
		return stats;
	}
	
	/**
	 * Get the highest temperature within a list of readings
	 * @param readings - a list of input readings over any time period
	 * @return the highest temperature among the readings
	 */
	public static double getHighestTemp(List<Reading> readings) {
		OptionalDouble result = readings.stream().mapToDouble(r -> r.getTemperature()).max();
		if (result.isPresent()) {
			return result.getAsDouble();
		}
		else {
			return 0.0;
		}
	}
	
	/**
	 * Get the lowest temperature within a list of readings
	 * @param readings - a list of input readings over any time period
	 * @return the lowest temperature among the readings
	 */
	public static double getLowestTemp(List<Reading> readings) {
		OptionalDouble result = readings.stream().mapToDouble(r -> r.getTemperature()).min();
		if (result.isPresent()) {
			return result.getAsDouble();
		}
		else {
			return 0.0;
		}
	}
	
	/**
	 * Get the average temperature within a list of readings.  This is significant in that is the average over all
	 * readings rather than reading extremes.  So over a month, this will give a true reflection of the average temperature
	 * over the month, rather than just an average between the average high and low.
	 * @param readings - a list of input readings over any time period
	 * @return the average temperature among the readings
	 */
	public static double getAverageTemp(List<Reading> readings) {
		OptionalDouble result = readings.stream().mapToDouble(r -> r.getTemperature()).average();
		if (result.isPresent()) {
			return result.getAsDouble();
		}
		else {
			return 0.0;
		}
	}
	
	/**
	 * Get the highest humidity within a list of readings
	 * @param readings - a list of input readings over any time period
	 * @return the highest humidity among the readings
	 */
	public static int getHighestHumidity(List<Reading> readings) {
		OptionalInt result = readings.stream().mapToInt(r -> r.getHumidity()).max();
		if (result.isPresent()) {
			return result.getAsInt();
		}
		else {
			return 0;
		}
	}
	
	/**
	 * Get the lowest humidity within a list of readings
	 * @param readings - a list of input readings over any time period
	 * @return the lowest humidity among the readings
	 */
	public static int getLowestHumidity(List<Reading> readings) {
		OptionalInt result = readings.stream().mapToInt(r -> r.getHumidity()).min();
		if (result.isPresent()) {
			return result.getAsInt();
		}
		else {
			return 0;
		}
	}
	
	/**
	 * Get the average humidity within a list of readings
	 * @param readings - a list of input readings over any time period
	 * @return the average humidity among the readings
	 */
	public static double getAverageHumidity(List<Reading> readings) {
		OptionalDouble result = readings.stream().mapToInt(r -> r.getHumidity()).average();
		if (result.isPresent()) {
			return result.getAsDouble();
		}
		else {
			return 0.0;
		}
	}
	
	/**
	 * Create a Map which groups a list of Reading objects by the day on which they occurred
	 * @param readings - a list of input readings over any time period
	 * @return a Map containing integer days of month (assumption of input being a single month) with their readings
	 */
	public static Map<Integer, List<Reading>> getReadingsByDay(List<Reading> readings) {
		// Group input readings by the day of the month on which they occurred
		return readings.stream().collect(Collectors.groupingBy(Reading::getDay));
	}
	
	/**
	 * Given a list of readings associated with a day, get the highest temperature for each day
	 * @param readingsByDay - a map of the list of readings associated with each day
	 * @return a Map with the highest temperature associated with each day
	 */
	public static Map<Integer, Double> getHighTemps(Map<Integer, List<Reading>> readingsByDay) {		
		// Find the highest temperature among the readings for each date
		Map<Integer, Double> results = new TreeMap<Integer, Double>();
		readingsByDay.keySet().stream().forEach(k -> results.put(k, getHighestTemp(readingsByDay.get(k))));
		return results;
	}
	
	/**
	 * Given a list of readings associated with a day, get the lowest temperature for each day
	 * @param readingsByDay - a map of the list of readings associated with each day
	 * @return a Map with the lowest temperature associated with each day
	 */
	public static Map<Integer, Double> getLowTemps(Map<Integer, List<Reading>> readingsByDay) {
		// Find the lowest temperature among the readings for each date
		Map<Integer, Double> results = new TreeMap<Integer, Double>();
		readingsByDay.keySet().stream().forEach(k -> results.put(k, getLowestTemp(readingsByDay.get(k))));
		return results;
	}

}
