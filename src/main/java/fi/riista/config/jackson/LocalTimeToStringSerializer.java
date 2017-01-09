package fi.riista.config.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;

public class LocalTimeToStringSerializer extends JsonSerializer<LocalTime> {

    private static final DateTimeFormatter FORMAT = DateTimeFormat.forPattern("HH:mm");

    @Override
    public void serialize(LocalTime value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeObject(FORMAT.print(value));
    }
}
