package fi.riista.feature.gis.geojson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.geojson.FeatureCollection;

import java.util.HashMap;
import java.util.Map;

// This class was added because properties were removed from original third-party class
@JsonTypeInfo(property = "type", use = JsonTypeInfo.Id.NAME)
@JsonTypeName("FeatureCollection")
public class FeatureCollectionWithProperties extends FeatureCollection {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> properties = new HashMap<>();

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key) {
        return (T) properties.get(key);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
