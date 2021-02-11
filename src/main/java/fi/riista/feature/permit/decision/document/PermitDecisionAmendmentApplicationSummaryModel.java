package fi.riista.feature.permit.decision.document;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.amendment.AmendmentApplicationData;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PermitDecisionAmendmentApplicationSummaryModel {

    public static class SpeciesAmount {
        private final String name;
        private final float amount;

        private SpeciesAmount(final String name, final float amount) {
            this.name = name;
            this.amount = amount;
        }

        public String getName() {
            return name;
        }

        public float getAmount() {
            return amount;
        }
    }

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("d.M.yyyy HH:mm");

    private final Locale locale;
    private final SpeciesAmount speciesAmount;
    private final String originalPermitNumber;
    private final String pointOfTime;
    private final GameAge age;
    private final GameGender gender;
    private final String shooter;
    private final String partner;
    private final GeoLocation geoLocation;


    private final List<String> officialStatements;
    private final List<String> otherAttachments;


    public PermitDecisionAmendmentApplicationSummaryModel(final Locale locale,
                                                          final HarvestPermitApplication application,
                                                          final AmendmentApplicationData data) {
        this.locale = Objects.requireNonNull(locale);
        final HarvestPermitApplicationSpeciesAmount spa = application.getSpeciesAmounts().get(0);

        this.speciesAmount = new SpeciesAmount(
                StringUtils.capitalize(spa.getGameSpecies().getNameLocalisation().getTranslation(locale)),
                spa.getSpecimenAmount());
        this.originalPermitNumber = data.getOriginalPermit().getPermitNumber();
        this.pointOfTime = DTF.print(DateUtil.toLocalDateTimeNullSafe(data.getPointOfTime()));
        this.age = data.getAge();
        this.gender = data.getGender();
        this.shooter = personName(data.getShooter());
        this.partner = partnerName(locale, data.getPartner());
        this.geoLocation = data.getGeoLocation();

        this.officialStatements = application.getAttachmentFilenames(HarvestPermitApplicationAttachment.Type.OFFICIAL_STATEMENT);
        this.otherAttachments = application.getAttachmentFilenames(HarvestPermitApplicationAttachment.Type.OTHER);
    }

    private static String personName(final Person shooter) {
        if (shooter == null) {
            return "-";
        }
        return shooter.getLastName() + " " + shooter.getFirstName() + " " + shooter.getHunterNumber();
    }

    private static String partnerName(final Locale locale, final HuntingClub partner) {
        return partner.getNameLocalisation().getAnyTranslation(locale) + " " + partner.getOfficialCode();
    }

    public Locale getLocale() {
        return locale;
    }

    public SpeciesAmount getSpeciesAmount() {
        return speciesAmount;
    }

    public String getOriginalPermitNumber() {
        return originalPermitNumber;
    }

    public String getPointOfTime() {
        return pointOfTime;
    }

    public GameAge getAge() {
        return age;
    }

    public GameGender getGender() {
        return gender;
    }

    public String getShooter() {
        return shooter;
    }

    public String getPartner() {
        return partner;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public List<String> getOfficialStatements() {
        return officialStatements;
    }

    public List<String> getOtherAttachments() {
        return otherAttachments;
    }
}
