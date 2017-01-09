package fi.riista.integration.support;

import org.joda.time.LocalTime;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class LocalTimeAdapter extends XmlAdapter<String, LocalTime> {

    @Override
    public String marshal(LocalTime time) {
        return time.toString();
    }

    @Override
    public LocalTime unmarshal(String value) {
        return new LocalTime(value);
    }

}
