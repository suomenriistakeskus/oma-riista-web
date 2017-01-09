package fi.riista.integration.support;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.LocalDate;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

    @Override
    public String marshal(LocalDate date) {
        return date.toString();
    }

    @Override
    public LocalDate unmarshal(String value) {
        return new LocalDate(value);
    }

}
