package org.wololo.geojson;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Crs extends GeoJSON {
    private final String type;
    private final Map<String, Object> properties;

    @JsonCreator
    public Crs(@JsonProperty("type") String type, @JsonProperty("properties") Map<String, Object> properties) {
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
