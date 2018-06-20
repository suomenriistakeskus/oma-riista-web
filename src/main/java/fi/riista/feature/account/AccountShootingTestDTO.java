package fi.riista.feature.account;

import fi.riista.feature.shootingtest.ShootingTestType;
import org.joda.time.LocalDate;

public class AccountShootingTestDTO {
    private String rhyName;
    private ShootingTestType type;
    private LocalDate begin;
    private LocalDate end;
    private boolean expired;

    public String getRhyName() {
        return rhyName;
    }

    public void setRhyName(String rhyName) {
        this.rhyName = rhyName;
    }

    public ShootingTestType getType() {
        return type;
    }

    public void setType(final ShootingTestType type) {
        this.type = type;
    }

    public LocalDate getBegin() {
        return begin;
    }

    public void setBegin(final LocalDate begin) {
        this.begin = begin;
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setEnd(final LocalDate end) {
        this.end = end;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(final boolean expired) {
        this.expired = expired;
    }
}