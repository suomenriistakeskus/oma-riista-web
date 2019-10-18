package fi.riista.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class LocalisedStringJsonSerializer extends JsonSerializer<LocalisedString> {
    @Override
    public void serialize(final LocalisedString value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException {
        if (value != null) {
            gen.writeObject(value.asMap());
        } else {
            gen.writeObject(LocalisedString.EMPTY.asMap());
        }
    }
}
