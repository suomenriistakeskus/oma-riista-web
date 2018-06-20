package fi.riista.feature.permit.application.email;

import com.github.jknack.handlebars.Handlebars;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.species.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.area.hta.HarvestPermitAreaHtaDTO;
import fi.riista.feature.permit.area.partner.HarvestPermitAreaPartner;
import fi.riista.feature.permit.area.rhy.HarvestPermitAreaRhyDTO;
import fi.riista.util.Locales;
import fi.riista.util.LocalisedString;
import fi.riista.util.NumberUtils;
import org.springframework.context.MessageSource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class HarvestPermitApplicationNotification {
    private static final LocalisedString TEMPLATE = new LocalisedString(
            "email_harvest_permit_application", "email_harvest_permit_application.sv");

    private static List<String> getAttachmentFilenames(final List<HarvestPermitApplicationAttachment> attachments,
                                                       final HarvestPermitApplicationAttachment.Type type) {
        return attachments.stream()
                .filter(a -> a.getAttachmentType() == type)
                .map(HarvestPermitApplicationAttachment::getName)
                .collect(toList());
    }

    private static Map<String, Object> transformSpeciesAmount(final HarvestPermitApplicationSpeciesAmount spa) {
        final Map<String, Object> map = new HashMap<>();
        map.put("name", spa.getGameSpecies().getNameLocalisation());
        map.put("amount", spa.getAmount());
        map.put("description", spa.getDescription());
        return map;
    }

    private static Map<String, Object> transformPartner(final HuntingClub club,
                                                        final List<String> allAreaPartners) {
        final Map<String, Object> map = new HashMap<>();
        map.put("name", club.getNameLocalisation());
        map.put("officialCode", club.getOfficialCode());
        if (allAreaPartners != null) {
            map.put("areaCodes", allAreaPartners.stream().collect(Collectors.joining(", ")));
        }
        return map;
    }

    private final Handlebars handlebars;
    private final MessageSource messageSource;
    private Locale locale;
    private HarvestPermitApplication application;
    private GISZoneSizeDTO areaSize;
    private Set<String> recipients;

    public static class Model {
        private final Date timestamp;

        private final Long id;
        private final Integer rev;
        private final Long createdByUserId;
        private final Long modifiedByUserId;

        private final Integer applicationNumber;
        private final HarvestPermitApplication.Status status;
        private final LocalisedString permitType;
        private final PersonContactInfoDTO contactPerson;

        private final OrganisationNameDTO permitHolder;
        private final List<Map<String, Object>> partners;
        private final List<Map<String, Object>> speciesAmounts;
        private final boolean includeSpeciesDescriptions;

        private final boolean freeHunting;
        private final double totalAreaSize;
        private final double waterAreaSize;
        private final double landAreaSize;
        private final double stateLandAreaSize;
        private final double privateLandAreaSize;
        private final List<HarvestPermitAreaRhyDTO> rhys;
        private final List<HarvestPermitAreaHtaDTO> htas;

        private final List<String> mhAreaPermits;
        private final List<String> shooterLists;
        private final List<String> otherAttachments;

        private final Integer shooterOnlyClub;
        private final Integer shooterOtherClubPassive;
        private final Integer shooterOtherClubActive;
        private final Integer shooterTotal;

        private final Boolean deliveryByMail;
        private final boolean additionalEmails;
        private final String email1;
        private final String email2;

        public Model(final HarvestPermitApplication application,
                     final GISZoneSizeDTO areaSize) {
            Objects.requireNonNull(application, "application is null");
            Objects.requireNonNull(areaSize, "areaSize is null");

            this.timestamp = new Date();
            this.id = application.getId();
            this.rev = application.getConsistencyVersion();
            this.createdByUserId = application.getCreatedByUserId();
            this.modifiedByUserId = application.getModifiedByUserId();

            this.applicationNumber = application.getApplicationNumber();
            this.status = application.getStatus();

            this.permitType = HarvestPermit.MOOSELIKE_PERMIT_NAME;
            this.contactPerson = PersonContactInfoDTO.create(Objects.requireNonNull(application.getContactPerson()));
            this.permitHolder = OrganisationNameDTO.createWithOfficialCode(application.getPermitHolder());

            if (application.getSpeciesAmounts() != null) {
                this.speciesAmounts = application.getSpeciesAmounts().stream()
                        .sorted(Comparator.comparingDouble(HarvestPermitApplicationSpeciesAmount::getAmount).reversed())
                        .map(HarvestPermitApplicationNotification::transformSpeciesAmount)
                        .collect(toList());
                this.includeSpeciesDescriptions = application.getSpeciesAmounts().stream()
                        .map(HarvestPermitApplicationSpeciesAmount::getDescription)
                        .anyMatch(StringUtils::hasText);
            } else {
                this.speciesAmounts = null;
                this.includeSpeciesDescriptions = false;
            }

            if (application.getPermitPartners() != null) {
                this.partners = application.getPermitPartners().stream()
                        .sorted(Comparator.comparing(Organisation::getOfficialCode))
                        .map(club -> {
                            if (application.getArea() == null || application.getArea().getPartners().isEmpty()) {
                                return HarvestPermitApplicationNotification.transformPartner(club, null);
                            }

                            final List<String> partnerAreaCodes = application.getArea().getPartners().stream()
                                    .filter(areaPartner -> club.equals(areaPartner.getSourceArea().getClub()))
                                    .map(HarvestPermitAreaPartner::getSourceArea)
                                    .map(HuntingClubArea::getExternalId)
                                    .collect(toList());
                            return HarvestPermitApplicationNotification.transformPartner(club, partnerAreaCodes);
                        })
                        .collect(Collectors.toList());
            } else {
                this.partners = emptyList();
            }

            if (application.getArea() != null) {
                this.rhys = application.getArea().getRhy().stream()
                        .map(HarvestPermitAreaRhyDTO::create)
                        .sorted(Comparator.<HarvestPermitAreaRhyDTO>comparingDouble(rhy -> rhy.getBothSize().getTotal()).reversed())
                        .collect(toList());

                this.htas = application.getArea().getHta().stream()
                        .map(HarvestPermitAreaHtaDTO::create)
                        .sorted(Comparator.comparingDouble(HarvestPermitAreaHtaDTO::getComputedAreaSize).reversed())
                        .collect(toList());

                this.freeHunting = application.getArea().isFreeHunting();
            } else {
                this.rhys = emptyList();
                this.htas = emptyList();
                this.freeHunting = false;
            }

            if (areaSize != null) {
                this.landAreaSize = areaSize.getAll().getLand();
                this.waterAreaSize = areaSize.getAll().getWater();
                this.totalAreaSize = areaSize.getAll().getTotal();
                this.stateLandAreaSize = areaSize.getStateLandAreaSize();
                this.privateLandAreaSize = areaSize.getPrivateLandAreaSize();
            } else {
                this.landAreaSize = 0;
                this.waterAreaSize = 0;
                this.totalAreaSize = 0;
                this.stateLandAreaSize = 0;
                this.privateLandAreaSize = 0;
            }

            this.mhAreaPermits = getAttachmentFilenames(application.getAttachments(),
                    HarvestPermitApplicationAttachment.Type.MH_AREA_PERMIT);
            this.shooterLists = getAttachmentFilenames(application.getAttachments(),
                    HarvestPermitApplicationAttachment.Type.SHOOTER_LIST);
            this.otherAttachments = getAttachmentFilenames(application.getAttachments(),
                    HarvestPermitApplicationAttachment.Type.OTHER);

            this.shooterOnlyClub = application.getShooterOnlyClub();
            this.shooterOtherClubPassive = application.getShooterOtherClubPassive();
            this.shooterOtherClubActive = application.getShooterOtherClubActive();
            this.shooterTotal = NumberUtils.nullsafeSum(
                    application.getShooterOnlyClub(),
                    application.getShooterOtherClubPassive());

            this.deliveryByMail = application.getDeliveryByMail();
            this.email1 = application.getEmail1();
            this.email2 = application.getEmail2();
            this.additionalEmails = email1 != null || email2 != null;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public Long getId() {
            return id;
        }

        public Integer getRev() {
            return rev;
        }

        public Long getCreatedByUserId() {
            return createdByUserId;
        }

        public Long getModifiedByUserId() {
            return modifiedByUserId;
        }

        public Integer getApplicationNumber() {
            return applicationNumber;
        }

        public HarvestPermitApplication.Status getStatus() {
            return status;
        }

        public LocalisedString getPermitType() {
            return permitType;
        }

        public PersonContactInfoDTO getContactPerson() {
            return contactPerson;
        }

        public OrganisationNameDTO getPermitHolder() {
            return permitHolder;
        }

        public List<Map<String, Object>> getPartners() {
            return partners;
        }

        public List<Map<String, Object>> getSpeciesAmounts() {
            return speciesAmounts;
        }

        public boolean isIncludeSpeciesDescriptions() {
            return includeSpeciesDescriptions;
        }

        public boolean isFreeHunting() {
            return freeHunting;
        }

        public double getTotalAreaSize() {
            return totalAreaSize;
        }

        public double getWaterAreaSize() {
            return waterAreaSize;
        }

        public double getLandAreaSize() {
            return landAreaSize;
        }

        public double getStateLandAreaSize() {
            return stateLandAreaSize;
        }

        public double getPrivateLandAreaSize() {
            return privateLandAreaSize;
        }

        public List<HarvestPermitAreaRhyDTO> getRhys() {
            return rhys;
        }

        public List<HarvestPermitAreaHtaDTO> getHtas() {
            return htas;
        }

        public List<String> getMhAreaPermits() {
            return mhAreaPermits;
        }

        public List<String> getShooterLists() {
            return shooterLists;
        }

        public List<String> getOtherAttachments() {
            return otherAttachments;
        }

        public Integer getShooterOnlyClub() {
            return shooterOnlyClub;
        }

        public Integer getShooterOtherClubPassive() {
            return shooterOtherClubPassive;
        }

        public Integer getShooterOtherClubActive() {
            return shooterOtherClubActive;
        }

        public Integer getShooterTotal() {
            return shooterTotal;
        }

        public Boolean getDeliveryByMail() {
            return deliveryByMail;
        }

        public boolean isAdditionalEmails() {
            return additionalEmails;
        }

        public String getEmail1() {
            return email1;
        }

        public String getEmail2() {
            return email2;
        }
    }

    public HarvestPermitApplication getApplication() {
        return application;
    }

    public HarvestPermitApplicationNotification(final Handlebars handlebars, final MessageSource messageSource) {
        this.handlebars = handlebars;
        this.messageSource = messageSource;
    }

    public HarvestPermitApplicationNotification withApplication(final HarvestPermitApplication application) {
        this.application = Objects.requireNonNull(application);
        return this;
    }

    public HarvestPermitApplicationNotification withLocale(final Locale locale) {
        this.locale = Objects.requireNonNull(locale);
        return this;
    }

    public HarvestPermitApplicationNotification withAreaSize(final GISZoneSizeDTO areaSize) {
        this.areaSize = Objects.requireNonNull(areaSize);
        return this;
    }

    public HarvestPermitApplicationNotification withRecipients(Set<String> recipients) {
        this.recipients = Objects.requireNonNull(recipients);
        return this;
    }

    public MailMessageDTO createMailMessage(final String emailFrom) {
        final MailMessageDTO.Builder builder = MailMessageDTO.builder()
                .withFrom(emailFrom)
                .withSubject(getMailSubject())
                .withRecipients(recipients);

        if (Locales.isSwedish(locale)) {
            builder.appendBody(createMessageBodySwedish())
                    .appendBody("\n\n")
                    .appendBody(getFooterTextSwedish());
        } else {
            builder.appendBody(createMessageBodyFinnish())
                    .appendBody("\n\n")
                    .appendBody(getFooterTextFinnish());
        }

        return builder.build();
    }

    public String getMailSubject() {
        return messageSource.getMessage("harvest.permit.application.email.subject", null, Locales.FI);
    }

    public String createMessageBodyFinnish() {
        return createMessageBody(TEMPLATE.getFinnish(), new Model(application, areaSize));
    }

    public String createMessageBodySwedish() {
        return createMessageBody(TEMPLATE.getSwedish(), new Model(application, areaSize));
    }

    private String createMessageBody(final String template, final Model model) {
        try {
            return handlebars.compile(template).apply(model);
        } catch (IOException e) {
            throw new RuntimeException("Could not render template", e);
        }
    }

    public String getFooterTextFinnish() {
        return getFooterText("Viestin tunnistetiedot");
    }

    public String getFooterTextSwedish() {
        return getFooterText("Meddelandets identifikationsuppgifter");
    }

    public String getFooterText(final String prefix) {
        final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        return String.format("<p>(%s: %s, id=%d, revision=%d, state=%s, c=%d, m=%d)</p>",
                prefix, df.format(application.getCreationTime()), application.getId(), application.getConsistencyVersion(),
                application.getStatus(), application.getCreatedByUserId(), application.getModifiedByUserId());
    }
}
