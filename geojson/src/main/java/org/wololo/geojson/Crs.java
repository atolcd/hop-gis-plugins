package org.wololo.geojson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class Crs extends GeoJSON {
  private final String type;
  private final Map<String, Object> properties;

  @JsonCreator
  public Crs(
      @JsonProperty("type") String type,
      @JsonProperty("properties") Map<String, Object> properties) {
    super();
    this.type = type;
    this.properties = properties;
  }

  public String getType() {
    return type;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }
}
