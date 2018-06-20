package fi.riista.feature.common.dto;

import javax.validation.constraints.Min;

public class IdRevisionDTO {

    private long id;

    @Min(0)
    private int rev;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public int getRev() {
        return rev;
    }

    public void setRev(final int rev) {
        this.rev = rev;
    }
}
