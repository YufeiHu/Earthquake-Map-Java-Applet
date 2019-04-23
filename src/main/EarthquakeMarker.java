package main;

import de.fhpotsdam.unfolding.data.PointFeature;

import processing.core.PConstants;
import processing.core.PGraphics;

/** Implements a visual marker for earthquakes on an earthquake map
  * @author: Yufei Hu
  * */
public abstract class EarthquakeMarker extends CommonMarker implements Comparable<EarthquakeMarker> {
	
	// Did the earthquake occur on land? This will be set by the subclasses.
	protected boolean isOnLand;

	// The radius of the Earthquake marker
	protected float radius;
	
	// Constants for distance
	protected static final float kmPerMile = 1.6f;
	
	// Greater than or equal to this threshold is a moderate earthquake
	public static final float THRESHOLD_MODERATE = 5;
	
	// Greater than or equal to this threshold is a light earthquake
	public static final float THRESHOLD_LIGHT = 4;

	// Greater than or equal to this threshold is an intermediate depth
	public static final float THRESHOLD_INTERMEDIATE = 70;
	
	// Greater than or equal to this threshold is a deep depth
	public static final float THRESHOLD_DEEP = 300;

	// Abstract method implemented in derived classes
	public abstract void drawEarthquake(PGraphics pg, float x, float y);

	public EarthquakeMarker(PointFeature feature) {
		super(feature.getLocation());
		java.util.HashMap<String, Object> properties = feature.getProperties();
		float magnitude = Float.parseFloat(properties.get("magnitude").toString());
		properties.put("radius", 2 * magnitude);
		setProperties(properties);
		this.radius = 1.75f*getMagnitude();
	}
	
	/** Implements the comparison method in Comparator class */
	public int compareTo(EarthquakeMarker other) {
		float magThis = this.getMagnitude();
		float magOther = other.getMagnitude();
		if (magThis > magOther) {
			return -1;
		} else if (magThis < magOther) {
			return 1;
		} else {
			return 0;
		}
	}
	
	/** Calls abstract method drawEarthquake and then checks age and draws X if needed */
	@Override
	public void drawMarker(PGraphics pg, float x, float y) {
		pg.pushStyle();
		colorDetermine(pg);
		drawEarthquake(pg, x, y);
		String age = getStringProperty("age");
		if ("Past Hour".equals(age) || "Past Day".equals(age)) {
			pg.strokeWeight(2);
			int buffer = 2;
			pg.line(x - (radius + buffer), 
					y - (radius + buffer), 
					x + (radius + buffer), 
					y + (radius + buffer));
			pg.line(x - (radius + buffer), 
					y + (radius + buffer), 
					x + (radius + buffer), 
					y - (radius + buffer));
		}
		pg.popStyle();
	}

	/** Show the title of the earthquake if this marker is selected */
	public void showTitle(PGraphics pg, float x, float y) {
		String content = getTitle();
		float contentWidth = pg.textWidth(content);
		pg.fill(255, 255, 255);
		pg.rect(x, y, contentWidth + 4, 20);
		pg.fill(0, 0, 0);
		pg.text(content, x + 2, (float)(y + 14));
	}

	/** Return the "threat circle" radius, or distance up to
	  * which this earthquake can affect things, for this earthquake.
	  * */
	public double threatCircle() {	
		double miles = 20.0f * Math.pow(1.8, 2 * getMagnitude() - 5);
		double km = (miles * kmPerMile);
		return km;
	}
	
	/** Determine color of marker from depth
	  * Deep = red, intermediate = yellow, shallow = blue
	  * */
	private void colorDetermine(PGraphics pg) {
		float depth = getDepth();
		if (depth < THRESHOLD_INTERMEDIATE) {
			pg.fill(255, 255, 0);
		} else if (depth < THRESHOLD_DEEP) {
			pg.fill(0, 0, 255);
		} else {
			pg.fill(255, 0, 0);
		}
	}
	
	/** Returns an earthquake marker's string representation
	  * @return the string representation of an earthquake marker.
	  */
	public String toString() {
		return getTitle();
	}
	
	public float getMagnitude() {
		return Float.parseFloat(getProperty("magnitude").toString());
	}
	
	public float getDepth() {
		return Float.parseFloat(getProperty("depth").toString());	
	}
	
	public String getTitle() {
		return (String) getProperty("title");
	}
	
	public float getRadius() {
		return Float.parseFloat(getProperty("radius").toString());
	}
	
	public boolean isOnLand() {
		return isOnLand;
	}
}
