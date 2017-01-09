package fi.riista.integration.support;

import fi.riista.config.Constants;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DateTimeAdapter extends XmlAdapter<String, DateTime> {

    private static final DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser();

    private static final DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();

    @Override
    public String marshal(DateTime dateTime) {
        return formatter.print(dateTime.withZone(Constants.DEFAULT_TIMEZONE));
    }

    @Override
    public DateTime unmarshal(String value) {
        return parser.parseDateTime(value);
    }

}
