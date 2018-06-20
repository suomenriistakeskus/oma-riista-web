package fi.riista.feature.dashboard;

import com.fasterxml.jackson.annotation.JsonGetter;

public class DashboardRhyEditDTO {

    public static class EditCount {
        public int created;
        public int modified;

        @JsonGetter
        public int sum() {
            return created + modified;
        }
    }

    public static class RoleEditCount {
        public final EditCount coordinator = new EditCount();
        public final EditCount moderator = new EditCount();
    }

    public final String areaCode;
    public final String rhyCode;
    public final String rhyName;

    public final RoleEditCount occupations = new RoleEditCount();
    public final RoleEditCount events = new RoleEditCount();


    public DashboardRhyEditDTO(String areaCode, String rhyCode, String rhyName) {
        this.areaCode = areaCode;
        this.rhyCode = rhyCode;
        this.rhyName = rhyName;
    }
}
