package enclosure.pi.monitor.arduino;

public interface Command {
	public static final String START_MARKER = "<";
	public static final String END_MARKER = ">";
	public static final String NONE = "none";
	
	//commands
	
	//EXTRACTOR
	public static final String GET_RPM_CMD = "r";
	public static final String SET_SPEED_CMD = "s";
	
	//LIGHTS
	public static final String LIGHTS_CMD = "l";
	
	//TEMPERATURE
	public static final String TEMPERATURE_CMD = "t";
	
	//air quality
	public static final String AIR_QUALITY = "a";
	
	//For monitoring / all sensors
	public static final String ALL_SESNORS = "m";
	
}
