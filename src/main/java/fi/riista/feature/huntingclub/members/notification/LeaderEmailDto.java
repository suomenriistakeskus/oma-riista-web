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

    public LeaderEmailDto(Occupation o) {
        this(o.getCallOrder(), o.getPerson().getFullName(), o.getPerson().getHunterNumber(), o.getCreationTime());
    }

    private LeaderEmailDto(Integer order, String name, String hunterNumber, Date date) {
        this.order = order;
        this.name = name;
        this.hunterNumber = hunterNumber;
        this.date = DATE_FORMAT.print(date.getTime());
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
}
