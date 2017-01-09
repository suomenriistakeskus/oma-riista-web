package fi.riista.feature.common.dto;

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.util.LocalisedString;

public class CodesetEntryDTO {

    @JsonProperty
    private final int code;

    private final Map<String, String> name;

    public CodesetEntryDTO(int id, Map<String, String> nameLocalisations) {
        this.code = id;
        this.name = Collections.unmodifiableMap(nameLocalisations);
    }

    public CodesetEntryDTO(int id, LocalisedString nameLocalisations) {
        this(id, nameLocalisations.asMap());
    }

    public int getCode() {
        return code;
    }

    public Map<String, String> getName() {
        return name;
    }

}
