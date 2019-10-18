package fi.riista.feature.huntingclub.members.notification;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

public class LeaderEmailDTO {

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");

    private final String name;
    private final String hunterNumber;
    private final Integer order;
    private final long huntingGroupId;
    private final String date;
    private final Date dateForTests;

    public LeaderEmailDTO(final String name,
                          final String hunterNumber,
                          final Integer order,
                          final long huntingGroupId,
                          final Date date) {

        this.name = name;
        this.hunterNumber = hunterNumber;
        this.order = order;
        this.huntingGroupId = huntingGroupId;
        this.date = DATE_FORMAT.print(date.getTime());
        this.dateForTests = date;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final LeaderEmailDTO that = (LeaderEmailDTO) obj;

        return new EqualsBuilder()
                .append(name, that.name)
                .append(hunterNumber, that.hunterNumber)
                .append(order, that.order)
                .append(huntingGroupId, that.huntingGroupId)
                .append(date, that.date)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(name)
                .append(hunterNumber)
                .append(order)
                .append(huntingGroupId)
                .append(date)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("name", name)
                .append("hunterNumber", hunterNumber)
                .append("order", order)
                .append("huntingGroupId", huntingGroupId)
                .append("date", date)
                .toString();
    }

    // Accessors ->

    public String getName() {
        return name;
    }

    public String getHunterNumber() {
        return hunterNumber;
    }

    public Integer getOrder() {
        return order;
    }

    public long getHuntingGroupId() {
        return huntingGroupId;
    }

    public String getDate() {
        return date;
    }

    public Date getDateForTests() {
        return dateForTests;
    }
}
