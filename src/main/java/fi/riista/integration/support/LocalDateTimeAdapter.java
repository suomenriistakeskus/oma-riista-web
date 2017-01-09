package fi.riista.integration.support;

import org.joda.time.LocalDateTime;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

    @Override
    public String marshal(LocalDateTime time) {
        return time.toString();
    }

    @Override
    public LocalDateTime unmarshal(String value) {
        return new LocalDateTime(value);
    }

}
