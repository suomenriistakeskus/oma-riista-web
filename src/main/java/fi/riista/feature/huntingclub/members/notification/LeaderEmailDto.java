package fi.riista.feature.huntingclub.members.notification;

import fi.riista.feature.organization.occupation.Occupation;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

public class LeaderEmailDto {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");

    private final Integer order;
    private final String name;
    private final String hunterNumber;
    private final String date;
    private final Date _dateForTests;

    public LeaderEmailDto(Occupation o) {
        this(o.getCallOrder(), o.getPerson().getFullName(), o.getPerson().getHunterNumber(), o.getModificationTime());
    }

    private LeaderEmailDto(Integer order, String name, String hunterNumber, Date date) {
        this.order = order;
        this.name = name;
        this.hunterNumber = hunterNumber;
        this.date = DATE_FORMAT.print(date.getTime());
        this._dateForTests = date;
    }


    public Integer getOrder() {
        return order;
    }

    public String getName() {
        return name;
    }

    public String getHunterNumber() {
        return hunterNumber;
    }

    public String getDate() {
        return date;
    }

    public Date get_dateForTests() {
        return _dateForTests;
    }
}
