package karlovich.weather;

public enum DayFormat {
	D24HOUR("24-hour"),
	D9MET("Met");
	
	public final String type;
	
	private DayFormat(String type) {
		this.type = type;
	}
}
