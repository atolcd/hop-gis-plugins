package com.atolcd.gis.gpx.type;

import com.atolcd.gis.gpx.type.WayPoint.WayPointException;
import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

public class TrackSegment {

  private List<WayPoint> points;

  public TrackSegment() {
    this.points = new ArrayList<WayPoint>();
  }

  public TrackSegment(LineString geometry) throws WayPointException {

    this.points = new ArrayList<WayPoint>();
    if (geometry != null && !geometry.isEmpty()) {

      for (Coordinate coordinate : geometry.getCoordinates()) {
        this.points.add(new WayPoint(coordinate));
      }
    }
  }

  public TrackSegment(List<WayPoint> points) {

    if (points == null) {
      this.points = new ArrayList<WayPoint>();
    } else {
      this.points = points;
    }
  }

  public List<WayPoint> getPoints() {
    return points;
  }
}
