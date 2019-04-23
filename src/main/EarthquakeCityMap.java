package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import main.CommonMarker;
import main.EarthquakeMarker;
import main.LandQuakeMarker;
import main.OceanQuakeMarker;
import parsing.ParseFeed;
import processing.core.PApplet;

/** EarthquakeCityMap
  * An application with an interactive map displaying earthquake data.
  * @author: Yufei Hu
  * Date: April 19, 2019
  * */
public class EarthquakeCityMap extends PApplet {
	
	// It's to get rid of eclipse warnings
	private static final long serialVersionUID = 1L;

	// Change the value of this variable to true to work offline
	private static final boolean offline = false;
	
	// This is where to find the local tiles, for working without an Internet connection
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	// Feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
	
	// The files containing city names and info and country names and info
	private String cityFile = "city-data.json";
	private String countryFile = "countries.geo.json";
	
	// The map
	private static UnfoldingMap map;
	
	// Markers for each city and earthquake
	private static List<Marker> cityMarkers;
	private static List<Marker> quakeMarkers;

	// A List of country markers
	private List<Marker> countryMarkers;
	
	// For events
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;
	private boolean cityGroupClicked = false;
	
	public void setup() {
		// Initialize canvas and map tiles
		size(900, 700, OPENGL);
		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 650, 600, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom";
		} else {
			map = new UnfoldingMap(this, 200, 50, 650, 600, new Microsoft.AerialProvider());
		}
		MapUtils.createDefaultEventDispatcher(this, map);
		
		// FOR TESTING:
		// earthquakesURL = "test1.atom";
		// earthquakesURL = "test2.atom";
		
		// FOR QUIZZING:
		earthquakesURL = "quiz2.atom";
		
	    // Load country features and markers
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		
		// Read in city data
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		cityMarkers = new ArrayList<Marker>();
		for (Feature city : cities) {
		    cityMarkers.add(new CityMarker(city));
		}
	    
		// Read in earthquake RSS feed
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    quakeMarkers = new ArrayList<Marker>();
	    for (PointFeature feature : earthquakes) {
		    if (isLand(feature)) {
		        quakeMarkers.add(new LandQuakeMarker(feature));
		    } else {
		        quakeMarkers.add(new OceanQuakeMarker(feature));
		    }
	    }

	    // FOR DEBUGGING:
	    // printQuakes();
	 	
	    // Add markers to map
	    map.addMarkers(quakeMarkers);
	    map.addMarkers(cityMarkers);
	    
	    sortAndPrint(cityMarkers.size());
	}
	
	public void draw() {
		background(0);
		map.draw();
		drawHorizontalLine();
		addKey();
	}
	
	private void drawDashedLine(int xStart, int yStart, int width, int height) {
		for (int y = yStart; y < yStart + height; y += 2) {
		    for (int x = xStart; x < xStart + width; x += 6) {
		    	pushStyle();
		        stroke(-(x + y >> 1 & 1));
		        line(x, y, x + 5, y);
		        popStyle();
		    }
		}
	}
	
	private void drawHorizontalLine() {
		if (mouseX > 200 && mouseX < 750 && mouseY > 50 && mouseY < 650) {
			drawDashedLine(200, mouseY, 650, 1);
		}
	}
	
	public static UnfoldingMap getMap() {
		return map;
	}
	
	public static List<Marker> getCityMarkers() {
		return cityMarkers;
	}
	
	public static List<Marker> getQuakeMarkers() {
		return quakeMarkers;
	}
	
	/** Sort and print earthquakes based on their magnitudes in descending order */
	private void sortAndPrint(int numToPrint) {
		ArrayList<EarthquakeMarker> markers = new ArrayList<EarthquakeMarker>();
		for (Marker marker : quakeMarkers) {
			markers.add((EarthquakeMarker)marker);
		}
		Collections.sort(markers);
		for (int i = 0; i < Math.min(markers.size(), numToPrint); i++) {
			System.out.println(markers.get(i));
		}
	}
	
	/** Event handler that gets called automatically when the mouse moves */
	@Override
	public void mouseMoved() {
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		}
		selectMarkerIfHover(quakeMarkers);
		selectMarkerIfHover(cityMarkers);
	}
	
	/** If there is a marker selected */
	private void selectMarkerIfHover(List<Marker> markers) {
		for (Marker marker : markers) {
			if (marker.isInside(map, mouseX, mouseY) && lastSelected == null) {
				lastSelected = (CommonMarker)marker;
				lastSelected.setSelected(true);
				return;
			}
		}
	}
	
	/** Event handler for mouse clicks */
	@Override
	public void mouseClicked() {
		if (lastClicked == null) {
			hideMarkers();
			
			for (Marker quakeMarker : quakeMarkers) {
				if (quakeMarker.isInside(map, mouseX, mouseY)) {
					lastClicked = (CommonMarker)quakeMarker;
					lastClicked.setSelected(true);
					quakeMarker.setHidden(false);
					EarthquakeMarker earthquakeMarker = (EarthquakeMarker)quakeMarker;
					earthquakeMarker.setClicked(true);
					double radiusThreat = earthquakeMarker.threatCircle();
					Location centerQuake = earthquakeMarker.getLocation();
					for (Marker cityMarker : cityMarkers) {
						if (isInsideThreatCircle(cityMarker, radiusThreat, centerQuake)) {
							cityMarker.setHidden(false);
						}
					}
					cityGroupClicked = true;
					return;
				}
			}
			
			for (Marker cityMarker : cityMarkers) {
				if (cityMarker.isInside(map, mouseX, mouseY)) {
					lastClicked = (CommonMarker)cityMarker;
					lastClicked.setSelected(true);
					cityMarker.setHidden(false);
					CityMarker cityMarker2 = (CityMarker)cityMarker;
					cityMarker2.setClicked(true);
					for (Marker quakeMarker : quakeMarkers) {
						double radiusThreat = ((EarthquakeMarker)quakeMarker).threatCircle();
						Location centerQuake = ((EarthquakeMarker)quakeMarker).getLocation();
						if (isInsideThreatCircle(cityMarker, radiusThreat, centerQuake)) {
							quakeMarker.setHidden(false);
						}
					}
					cityGroupClicked = true;
					return;
				}
			}
			
			if (!cityGroupClicked && (mouseX > 200 && mouseX < 750 && mouseY > 50 && mouseY < 650)){
				cityGroupClicked = true;
				unhideCityMarkersAboveLine();
			} else {
				cityGroupClicked = false;
				unhideMarkers();
			}
		} else {
			lastClicked.setSelected(false);
			lastClicked = null;
			unhideMarkers();
			cityGroupClicked = false;
		}
	}
	
	public static boolean isInsideThreatCircle(Marker cityMarker, double radiusThreat, Location centerQuake) {
		if (cityMarker.getDistanceTo(centerQuake) < radiusThreat) {
			return true;
		} else {
			return false;
		}
	}
	
	private void unhideCityMarkersAboveLine() {
		for (Marker marker : cityMarkers) {
			ScreenPosition posCity = ((CityMarker) marker).getScreenPosition(map);
			float yCity = posCity.y;
			if (yCity < mouseY) {
				marker.setHidden(false);
			}
		}
	}
	
	/** Loop over and hide all markers */
	private void hideMarkers() {
		for (Marker marker : quakeMarkers) {
			marker.setHidden(true);
		}
		for (Marker marker : cityMarkers) {
			marker.setHidden(true);
		}
	}
	
	/** Loop over and unhide all markers */
	private void unhideMarkers() {
		for (Marker marker : quakeMarkers) {
			EarthquakeMarker earthquakeMarker = (EarthquakeMarker)marker;
			earthquakeMarker.setClicked(false);
			marker.setHidden(false);
		}
		for (Marker marker : cityMarkers) {
			CityMarker cityMarker = (CityMarker)marker;
			cityMarker.setClicked(false);
			marker.setHidden(false);
		}
	}
	
	/** Helper method to draw keys in GUI */
	private void addKey() {
		int xbase = 25;
		int ybase = 50;
		fill(255, 250, 240);
		rect(xbase, ybase, 150, 250);
		
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", xbase + 25, ybase + 25);
		
		int tri_xbase = xbase + 35;
		int tri_ybase = ybase + 50;
		fill(150, 30, 30);
		triangle(tri_xbase, tri_ybase - CityMarker.TRI_SIZE, tri_xbase - CityMarker.TRI_SIZE, 
				 tri_ybase + CityMarker.TRI_SIZE, tri_xbase + CityMarker.TRI_SIZE, 
				 tri_ybase + CityMarker.TRI_SIZE);

		fill(0, 0, 0);
		textAlign(LEFT, CENTER);
		text("City Marker", tri_xbase + 15, tri_ybase);
		text("Land Quake", xbase + 50, ybase + 70);
		text("Ocean Quake", xbase + 50, ybase + 90);
		text("Size ~ Magnitude", xbase + 25, ybase + 110);
		
		fill(255, 255, 255);
		ellipse(xbase + 35, ybase + 70, 10, 10);
		rect(xbase + 35 - 5, ybase + 90 - 5, 10, 10);
		
		fill(color(255, 255, 0));
		ellipse(xbase + 35, ybase + 140, 12, 12);
		fill(color(0, 0, 255));
		ellipse(xbase + 35, ybase + 160, 12, 12);
		fill(color(255, 0, 0));
		ellipse(xbase + 35, ybase + 180, 12, 12);
		
		textAlign(LEFT, CENTER);
		fill(0, 0, 0);
		text("Shallow", xbase + 50, ybase + 140);
		text("Intermediate", xbase + 50, ybase + 160);
		text("Deep", xbase + 50, ybase + 180);
		text("Past hour", xbase + 50, ybase + 200);
		
		fill(255, 255, 255);
		int centerx = xbase + 35;
		int centery = ybase + 200;
		ellipse(centerx, centery, 12, 12);

		strokeWeight(2);
		line(centerx - 8, centery - 8, centerx + 8, centery + 8);
		line(centerx - 8, centery + 8, centerx + 8, centery - 8);
	}

	/** Checks whether this quake occurred on land.  If it did, it sets the 
	  * "country" property of its PointFeature to the country where it occurred
	  * and returns true.  Notice that the helper method isInCountry will
	  * set this "country" property already.  Otherwise it returns false.
	  * */
	private boolean isLand(PointFeature earthquake) {
		for (Marker marker : countryMarkers) {
			if (isInCountry(earthquake, marker)) {
				return true;
			}
		}
		return false;
	}
	
	/** Prints countries with number of earthquakes
	  * You will want to loop through the country markers or country features
	  * (either will work) and then for each country, loop through
	  * the quakes to count how many occurred in that country.
	  * Recall that the country markers have a "name" property, 
	  * And LandQuakeMarkers have a "country" property set.
	  * */
	private void printQuakes() {
		for (Marker countryMarker : countryMarkers) {
			int quakeCnt = 0;
			String countryName = (String)countryMarker.getProperty("name");
			for (Marker quakeMarker : quakeMarkers) {
				EarthquakeMarker earthquakeMarker = (EarthquakeMarker)quakeMarker;
				if (earthquakeMarker.isOnLand()) {
					String quakeCountryName = ((LandQuakeMarker)earthquakeMarker).getCountry();
					if (countryName.equals(quakeCountryName)) {
						quakeCnt += 1;
					}
				}
			}
			if (quakeCnt != 0) {
				System.out.println(countryName + ": " + quakeCnt);
			}
		}
		
		int oceanCnt = 0;
		for (Marker quakeMarker : quakeMarkers) {
			EarthquakeMarker earthquakeMarker = (EarthquakeMarker)quakeMarker;
			if (!earthquakeMarker.isOnLand()) {
				oceanCnt += 1;
			}
		}
		
		System.out.println("OCEAN QUAKES: " + oceanCnt);
	}
	
	/** Helper method to test whether a given earthquake is in a given country
	  * This will also add the country property to the properties of the earthquake feature if 
	  * it's in one of the countries.
	  * */
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		// getting location of feature
		Location checkLoc = earthquake.getLocation();

		// some countries represented it as MultiMarker
		// looping over SimplePolygonMarkers which make them up to use isInsideByLoc
		if (country.getClass() == MultiMarker.class) {
				
			// looping over markers making up MultiMarker
			for (Marker marker : ((MultiMarker)country).getMarkers()) {
					
				// check if inside
				if (((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));
					return true;
				}
			}
		}
		
		// check if inside country represented by SimplePolygonMarker
		else if (((AbstractShapeMarker)country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));
			return true;
		}
		
		return false;
	}
}
