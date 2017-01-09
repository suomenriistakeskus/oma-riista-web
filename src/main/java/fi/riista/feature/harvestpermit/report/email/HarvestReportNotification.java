package fi.riista.feature.harvestpermit.report.email;

import com.github.jknack.handlebars.Handlebars;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.PropertyIdentifier;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.season.HarvestAreaDTO;
import fi.riista.feature.harvestpermit.HarvestPermitDTO;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.gamediary.harvest.HuntingAreaType;
import fi.riista.feature.gamediary.harvest.HuntingMethod;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.organization.OrganisationDTO;
import fi.riista.feature.organization.person.PersonDTO;
import fi.riista.feature.organization.Organisation;
import fi.riista.util.Locales;
import fi.riista.util.LocalisedString;
import org.springframework.context.MessageSource;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Objects;

public class HarvestReportNotification {
    private static final LocalisedString TEMPLATE = new LocalisedString(
            "email_harvest_report", "email_harvest_report.sv");

    private final Handlebars handlebars;
    private final MessageSource messageSource;
    private GeoLocation geoLocation;
    private GameAge age;
    private GameGender gender;
    private Double weight;
    private Long id;
    private Integer rev;
    private HarvestReport.State state;
    private HuntingAreaType huntingAreaType;
    private String huntingParty;
    private Double huntingAreaSize;
    private HuntingMethod huntingMethod;
    private Boolean reportedWithPhoneCall;

    public HarvestReportNotification(
            final Handlebars handlebars,
            final MessageSource messageSource) {

        this.handlebars = handlebars;
        this.messageSource = messageSource;
    }

    private String email;
    private PersonDTO author;
    private PersonDTO hunter;
    private HarvestPermitDTO harvestPermit;
    private HarvestAreaDTO quotaArea;
    private OrganisationDTO rhy;
    private OrganisationDTO rka;

    private GameSpeciesDTO species;
    private PropertyIdentifier propertyIdentifier;
    private Date pointOfTime;
    private Long createdByUserId;
    private Long modifiedByUserId;

    public HarvestReportNotification withReport(HarvestReport report, Harvest harvest) {
        this.propertyIdentifier = harvest.getPropertyIdentifier();
        this.author = PersonDTO.create(Objects.requireNonNull(harvest.getAuthor()));
        this.hunter = PersonDTO.create(Objects.requireNonNull(harvest.getActualShooter()));
        this.rhy = OrganisationDTO.create(Objects.requireNonNull(harvest.getRhy()));
        this.species = GameSpeciesDTO.create(Objects.requireNonNull(harvest.getSpecies()));
        this.pointOfTime = harvest.getPointOfTime();
        this.createdByUserId = report.getCreatedByUserId();
        this.modifiedByUserId = report.getModifiedByUserId();
        this.geoLocation = harvest.getGeoLocation();
        this.age = harvest.getSortedSpecimens().iterator().next().getAge();
        this.gender = harvest.getSortedSpecimens().iterator().next().getGender();
        this.weight = harvest.getSortedSpecimens().iterator().next().getWeight();

        this.id = report.getId();
        this.rev = report.getConsistencyVersion();
        this.state = report.getState();

        this.huntingAreaType = harvest.getHuntingAreaType();
        this.huntingParty = harvest.getHuntingParty();
        this.huntingAreaSize = harvest.getHuntingAreaSize();
        this.huntingMethod = harvest.getHuntingMethod();
        this.reportedWithPhoneCall = harvest.getReportedWithPhoneCall();

        return this;
    }

    public HarvestReportNotification withRiistakeskuksenAlue(final Organisation organisation) {
        if (organisation != null) {
            this.rka = OrganisationDTO.create(Objects.requireNonNull(organisation));
        }
        return this;
    }

    public HarvestReportNotification withPermit(final HarvestPermit permit) {
        if (permit != null) {
            this.harvestPermit = HarvestPermitDTO.create(permit, null, EnumSet.noneOf(HarvestPermitDTO.Inclusion.class));
        }

        return this;
    }

    public HarvestReportNotification withQuota(final HarvestQuota quota) {
        if (quota != null && quota.getHarvestArea() != null) {
            this.quotaArea = HarvestAreaDTO.create(quota.getHarvestArea());
        }
        return this;
    }

    public HarvestReportNotification withEmail(String email) {
        this.email = email;

        return this;
    }

    public MailMessageDTO.Builder build() {
        Objects.requireNonNull(this.author, "No person");
        Objects.requireNonNull(this.pointOfTime, "No pointOfTime");
        Objects.requireNonNull(this.species, "No species");
        Preconditions.checkState(StringUtils.hasText(this.email), "No email address");

        final String emailSubject = messageSource.getMessage("harvest.report.email.subject", null, Locales.FI);

        final HashMap<String, Object> model = Maps.newHashMap();
        model.put("timestamp", new Date());
        model.put("pointOfTime", this.pointOfTime);
        model.put("propertyIdentifier", this.propertyIdentifier);
        model.put("rhy", this.rhy);
        model.put("rka", this.rka);
        model.put("hunter", this.hunter);
        model.put("author", this.author);
        model.put("permit", this.harvestPermit);
        model.put("quotaArea", this.quotaArea);
        model.put("species", this.species);
        model.put("createdByUserId", this.createdByUserId);
        model.put("modifiedByUserId", this.modifiedByUserId);
        model.put("geoLocation", this.geoLocation);
        model.put("age", this.age);
        model.put("gender", this.gender);
        model.put("weight", this.weight);
        model.put("id", this.id);
        model.put("rev", this.rev);
        model.put("state", this.state);
        model.put("huntingAreaType", this.huntingAreaType);

        model.put("huntingParty", this.huntingParty);
        model.put("huntingAreaSize", this.huntingAreaSize);
        model.put("huntingMethod", this.huntingMethod);
        model.put("reportedWithPhoneCall", this.reportedWithPhoneCall);

        return new MailMessageDTO.Builder()
                .withTo(this.email)
                .withSubject(emailSubject)
                .appendHandlebarsBody(handlebars, TEMPLATE.getFinnish(), model)
                .appendBody("\n\n--------------------------------------------------------------------------------\n\n")
                .appendHandlebarsBody(handlebars, TEMPLATE.getSwedish(), model);
    }

}
