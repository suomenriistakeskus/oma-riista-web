package fi.riista.feature.harvestpermit.report.email;

import com.github.jknack.handlebars.Handlebars;
import com.google.common.collect.Maps;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.common.entity.PropertyIdentifier;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.HuntingAreaType;
import fi.riista.feature.gamediary.harvest.HuntingMethod;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenDTO;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenOps;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.harvestpermit.season.HarvestAreaDTO;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationDTO;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.util.Locales;
import fi.riista.util.LocalisedString;
import org.springframework.context.MessageSource;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class HarvestReportNotification {
    private static final LocalisedString TEMPLATE = new LocalisedString(
            "email_harvest_report", "email_harvest_report.sv");

    private final Handlebars handlebars;
    private final MessageSource messageSource;

    private Set<String> recipients;

    private Long id;
    private Integer rev;
    private Long createdByUserId;
    private Long modifiedByUserId;
    private HarvestReportState state;

    private Date pointOfTime;
    private GameSpeciesDTO species;
    private PersonContactInfoDTO author;
    private PersonContactInfoDTO hunter;
    private int amount;
    private List<HarvestSpecimenDTO> specimens;
    private String permitNumber;

    private OrganisationDTO rhy;
    private OrganisationDTO rka;
    private GeoLocation geoLocation;
    private PropertyIdentifier propertyIdentifier;
    private LocalisedString municipality;
    private HarvestAreaDTO quotaArea;

    private HuntingAreaType huntingAreaType;
    private String huntingParty;
    private Double huntingAreaSize;
    private HuntingMethod huntingMethod;
    private Boolean reportedWithPhoneCall;
    private Boolean feedingPlace;
    private Boolean taigaBeanGoose;

    public HarvestReportNotification(
            final Handlebars handlebars,
            final MessageSource messageSource) {

        this.handlebars = handlebars;
        this.messageSource = messageSource;
    }

    public MailMessageDTO build(final String emailFrom) {
        Objects.requireNonNull(this.author, "No person");
        Objects.requireNonNull(this.pointOfTime, "No pointOfTime");
        Objects.requireNonNull(this.species, "No species");

        final String emailSubject = messageSource.getMessage("harvest.report.email.subject", null, Locales.FI);

        final HashMap<String, Object> model = Maps.newHashMap();
        model.put("timestamp", new Date());
        model.put("id", this.id);
        model.put("rev", this.rev);
        model.put("createdByUserId", this.createdByUserId);
        model.put("modifiedByUserId", this.modifiedByUserId);
        model.put("state", this.state);

        model.put("pointOfTime", this.pointOfTime);
        model.put("species", this.species);
        model.put("author", this.author);
        model.put("hunter", this.hunter);
        model.put("amount", this.amount);
        model.put("specimens", this.specimens);
        model.put("permitNumber", this.permitNumber);

        model.put("rhy", this.rhy);
        model.put("rka", this.rka);
        model.put("geoLocation", this.geoLocation);
        model.put("propertyIdentifier", this.propertyIdentifier);
        model.put("municipality", this.municipality);
        model.put("quotaArea", this.quotaArea);

        model.put("huntingAreaType", this.huntingAreaType);
        model.put("huntingParty", this.huntingParty);
        model.put("huntingAreaSize", this.huntingAreaSize);
        model.put("huntingMethod", this.huntingMethod);
        model.put("reportedWithPhoneCall", this.reportedWithPhoneCall);
        model.put("feedingPlace", this.feedingPlace);
        model.put("taigaBeanGoose", this.taigaBeanGoose);

        return MailMessageDTO.builder()
                .withFrom(emailFrom)
                .withSubject(emailSubject)
                .withRecipients(recipients)
                .appendHandlebarsBody(handlebars, TEMPLATE.getFinnish(), model)
                .appendBody("\n\n--------------------------------------------------------------------------------\n\n")
                .appendHandlebarsBody(handlebars, TEMPLATE.getSwedish(), model)
                .build();
    }

    public HarvestReportNotification withHarvest(final Harvest harvest) {
        this.id = harvest.getId();
        this.rev = harvest.getConsistencyVersion();
        this.createdByUserId = harvest.getCreatedByUserId();
        this.modifiedByUserId = harvest.getModifiedByUserId();
        this.state = harvest.getHarvestReportState();

        this.pointOfTime = harvest.getPointOfTime();
        this.species = GameSpeciesDTO.create(Objects.requireNonNull(harvest.getSpecies()));
        this.author = PersonContactInfoDTO.create(Objects.requireNonNull(harvest.getAuthor()));
        this.hunter = PersonContactInfoDTO.create(Objects.requireNonNull(harvest.getActualShooter()));
        this.amount = harvest.getAmount();

        final int gameSpeciesCode = harvest.getSpecies().getOfficialCode();
        final List<HarvestSpecimen> entitySpecimenList = harvest.getSortedSpecimens();
        if (!entitySpecimenList.isEmpty()) {
            final HarvestSpecimenOps specimenOps = new HarvestSpecimenOps(
                    gameSpeciesCode, HarvestSpecVersion.MOST_RECENT);
            this.specimens = specimenOps.transformList(entitySpecimenList);
        }

        this.rhy = OrganisationDTO.create(Objects.requireNonNull(harvest.getRhy()));

        if (gameSpeciesCode != GameSpecies.OFFICIAL_CODE_WILD_BOAR &&
                gameSpeciesCode != GameSpecies.OFFICIAL_CODE_BEAN_GOOSE &&
                gameSpeciesCode != GameSpecies.OFFICIAL_CODE_EUROPEAN_POLECAT) {
            this.geoLocation = harvest.getGeoLocation();
            this.propertyIdentifier = harvest.getPropertyIdentifier();
        }

        this.huntingAreaType = harvest.getHuntingAreaType();
        this.huntingParty = harvest.getHuntingParty();
        this.huntingAreaSize = harvest.getHuntingAreaSize();
        this.huntingMethod = harvest.getHuntingMethod();
        this.reportedWithPhoneCall = harvest.getReportedWithPhoneCall();
        this.feedingPlace = harvest.getFeedingPlace();

        if (harvest.isTaigaBeanGoose()) {
            this.taigaBeanGoose = Boolean.TRUE;
        } else if (harvest.isTundraBeanGoose()) {
            this.taigaBeanGoose = Boolean.FALSE;
        }

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
            this.permitNumber = permit.getPermitNumber();
        }

        return this;
    }

    public HarvestReportNotification withMunicipality(final Municipality municipality) {
        if (municipality != null) {
            this.municipality = municipality.getNameLocalisation();
        }
        return this;
    }

    public HarvestReportNotification withQuota(final HarvestQuota quota) {
        if (quota != null && quota.getHarvestArea() != null) {
            this.quotaArea = HarvestAreaDTO.create(quota.getHarvestArea());
        }
        return this;
    }

    public HarvestReportNotification withRecipients(Set<String> recipients) {
        this.recipients = recipients;

        return this;
    }

}
