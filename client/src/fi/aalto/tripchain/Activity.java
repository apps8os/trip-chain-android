package fi.aalto.tripchain;

public enum Activity {
	IN_VEHICLE("in-vehicle"), 
	ON_FOOT("on-foot"),
	STILL("still"), 
	UNKNOWN("unknown"), 
	ON_BICYCLE("on-bicycle"), 
	TILTING("tilting");
		
	private final String name;
	 
	private Activity(String name) {
		this.name = name;
	}
	
	@Override 
	public String toString() {
		return this.name;
	}
}
