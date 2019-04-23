package main;

import java.util.List;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import processing.core.PConstants;
import processing.core.PGraphics;

/** Implements a visual marker for cities on an earthquake map
  * @author: Yufei Hu
  * */
public class CityMarker extends CommonMarker {
	
	// The size of the triangle marker
	public static int TRI_SIZE = 5;
	
	public CityMarker(Location location) {
		super(location);
	}
	
	public CityMarker(Feature city) {
		super(((PointFeature)city).getLocation(), city.getProperties());
	}
	
	/** pg is the graphics object on which you call the graphics
	  * methods.  e.g. pg.fill(255, 0, 0) will set the color to red
	  * x and y are the center of the object to draw. 
	  * They will be used to calculate the coordinates to pass
	  * into any shape drawing methods.  
	  * e.g. pg.rect(x, y, 10, 10) will draw a 10x10 square
	  * whose upper left corner is at position x, y
	  * */
	public void drawMarker(PGraphics pg, float x, float y) {
		pg.pushStyle();
		pg.fill(150, 30, 30);
		pg.triangle(x, y - 5, (float)(x - 4.33), (float)(y + 2.5), (float)(x + 4.33), (float)(y + 2.5));
		
		if (clicked) {
			int xbase = 0;
			int ybase = 500;
			String content = "Earthquakes that will impact the selected city:\n";
			pg.fill(255, 250, 240);
			pg.rect(xbase, ybase, 650, 100);
			float magSum = 0;
			int numQuake = 0;
			List<Marker> quakeMarkers = EarthquakeCityMap.getQuakeMarkers();
			for (Marker quakeMarker : quakeMarkers) {
				double radiusThreat = ((EarthquakeMarker)quakeMarker).threatCircle();
				Location centerQuake = ((EarthquakeMarker)quakeMarker).getLocation();
				if (EarthquakeCityMap.isInsideThreatCircle(this, radiusThreat, centerQuake)) {
					content += ((EarthquakeMarker)quakeMarker).getTitle() + "\n";
					magSum += ((EarthquakeMarker)quakeMarker).getMagnitude();
					numQuake += 1;
				}
			}
			if (numQuake > 0) {
				String avgMagContent = "Average magnitude: " + (float)(magSum / numQuake);
				pg.fill(0, 0, 0);
				pg.text(content, xbase + 2, ybase + 2, xbase + 375 - 2, ybase + 100 - 2);
				pg.fill(0, 0, 0);
				pg.text(avgMagContent, xbase + 375 + 2, ybase + 2, xbase + 650 - 2, ybase + 100 - 2);
			} else {
				pg.fill(0, 0, 0);
				pg.text("No earthquake detected", xbase + 2, ybase + 2, xbase + 650 - 2, ybase + 100 - 2);
			}
		}
		
		pg.popStyle();
	}
	
	/** Show the title of the city if this marker is selected */
	public void showTitle(PGraphics pg, float x, float y) {
		String content = getCity() + ", " + getCountry() + ", " + getPopulation() + " Millions";
		float contentWidth = pg.textWidth(content);
		pg.pushStyle();
		pg.fill(255, 255, 255);
		pg.rect(x, y, contentWidth + 4, 20);
		pg.fill(0, 0, 0);
		pg.text(content, x + 2, (float)(y + 14));
		pg.popStyle();
	}
	
	private String getCity() {
		return getStringProperty("name");
	}
	
	private String getCountry() {
		return getStringProperty("country");
	}
	
	private float getPopulation() {
		return Float.parseFloat(getStringProperty("population"));
	}
}
