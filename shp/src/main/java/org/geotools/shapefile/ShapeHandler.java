package org.geotools.shapefile;

import com.vividsolutions.jump.io.EndianDataInputStream;
import com.vividsolutions.jump.io.EndianDataOutputStream;
import java.io.IOException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

/** Interface implemented by all the ShapeType handlers */
public interface ShapeHandler {
  /**
   * Returns one of the ShapeType int defined by the specification.
   *
   * <ul>
   *   <li>0 Null Shape
   *   <li>1 Point
   *   <li>3 PolyLine
   *   <li>5 Polygon
   *   <li>8 MultiPoint
   *   <li>11 PointZ
   *   <li>13 PolyLineZ
   *   <li>15 PolygonZ
   *   <li>18 MultiPointZ
   *   <li>21 PointM
   *   <li>23 PolyLineM
   *   <li>25 PolygonM
   *   <li>28 MultiPointM
   *   <li>31 MultiPatch
   * </ul>
   */
  public int getShapeType();

  public Geometry read(
      EndianDataInputStream file, GeometryFactory geometryFactory, int contentLength)
      throws IOException, InvalidShapefileException;

  public void write(Geometry geometry, EndianDataOutputStream file) throws IOException;

  public int getLength(Geometry geometry); // length in 16bit words

  /** Return a empty geometry. */
  public Geometry getEmptyGeometry(GeometryFactory factory);
}
