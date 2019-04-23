package main;

import java.util.List;

import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import main.CityMarker;
import main.EarthquakeCityMap;
import processing.core.PGraphics;

/** Implements a visual marker for ocean earthquakes on an earthquake map
  * @author: Yufei Hu
  * */
public class OceanQuakeMarker extends EarthquakeMarker {
	
	public OceanQuakeMarker(PointFeature quake) {
		super(quake);
		isOnLand = false;
	}

	@Override
	public void drawEarthquake(PGraphics pg, float x, float y) {
		pg.rect(x-radius, y-radius, 2*radius, 2*radius);
		if (clicked) {
			List<Marker> cityMarkers = EarthquakeCityMap.getCityMarkers();
			double radiusThreat = threatCircle();
			Location centerQuake = getLocation();
			pg.strokeWeight(2);
			pg.stroke(0);
			for (Marker cityMarker : cityMarkers) {
				if (EarthquakeCityMap.isInsideThreatCircle(cityMarker, radiusThreat, centerQuake)) {
					ScreenPosition posCity = ((CityMarker) cityMarker).getScreenPosition(EarthquakeCityMap.getMap());
					pg.line(x, y, posCity.x - 200, posCity.y - 50);
				}
			}
		} else {
			pg.noStroke();
		}
	}
}
