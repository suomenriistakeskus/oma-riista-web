package fi.riista.feature.gamediary;

import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.util.LocalisedString;

import java.util.Collections;
import java.util.Map;

public class GameCategoryDTO {

    @JsonProperty
    private final int code;

    private final Map<String, String> name;

    public GameCategoryDTO(int id, Map<String, String> nameLocalisations) {
        this.code = id;
        this.name = Collections.unmodifiableMap(nameLocalisations);
    }

    public GameCategoryDTO(int id, LocalisedString nameLocalisations) {
        this(id, nameLocalisations.asMap());
    }

    public int getCode() {
        return code;
    }

    public Map<String, String> getName() {
        return name;
    }
}
