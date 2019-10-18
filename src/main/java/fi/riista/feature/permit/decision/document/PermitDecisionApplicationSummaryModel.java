package fi.riista.feature.permit.decision.document;

import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.area.rhy.HarvestPermitAreaRhyDTO;
import fi.riista.util.F;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class PermitDecisionApplicationSummaryModel {
    public static class SpeciesAmount {
        private final String name;
        private final float amount;
        private final String description;

        private SpeciesAmount(final String name, final float amount, final String description) {
            this.name = name;
            this.amount = amount;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public float getAmount() {
            return amount;
        }

        public String getDescription() {
            return description;
        }
    }

    private final Locale locale;
    private final List<SpeciesAmount> speciesAmounts;

    private final String areaExternalCode;
    private final double landAreaSize;
    private final double waterAreaSize;
    private final double totalAreaSize;
    private final double stateLandAreaSize;
    private final double privateLandAreaSize;

    private final long partnerCount;
    private final List<OrganisationNameDTO> partners;

    private final Integer shooterOnlyClub;
    private final Integer shooterOtherClubPassive;
    private final Integer shooterOtherClubActive;

    private final boolean freeHunting;

    private final List<HarvestPermitAreaRhyDTO> rhys;

    private final long areaPermitCount;
    private final long shooterListCount;
    private final long otherAttachmentCount;

    public PermitDecisionApplicationSummaryModel(final @Nonnull Locale locale,
                                                 final @Nonnull HarvestPermitApplication application,
                                                 final @Nonnull GISZoneSizeDTO sizeDTO) {
        this.locale = Objects.requireNonNull(locale);
        this.speciesAmounts = F.mapNonNullsToList(application.getSpeciesAmounts(),
                spa -> transformSpeciesAmount(spa, locale));

        this.rhys = F.mapNonNullsToList(application.getArea().getRhy(), HarvestPermitAreaRhyDTO::create);

        this.areaExternalCode = application.getArea().getExternalId();
        this.landAreaSize = sizeDTO.getAll().getLand();
        this.waterAreaSize = sizeDTO.getAll().getWater();
        this.totalAreaSize = sizeDTO.getAll().getTotal();
        this.stateLandAreaSize = sizeDTO.getStateLandAreaSize();
        this.privateLandAreaSize = sizeDTO.getPrivateLandAreaSize();

        this.partnerCount = application.getPermitPartners().size();
        this.partners = application.getPermitPartners().stream()
                .map(OrganisationNameDTO::createWithOfficialCode)
                .collect(toList());

        this.freeHunting = application.getArea().isFreeHunting();

        this.shooterOnlyClub = application.getShooterOnlyClub();
        this.shooterOtherClubPassive = application.getShooterOtherClubPassive();
        this.shooterOtherClubActive = application.getShooterOtherClubActive();

        this.areaPermitCount = countAttachments(application.getAttachments(),
                HarvestPermitApplicationAttachment.Type.MH_AREA_PERMIT);
        this.shooterListCount = countAttachments(application.getAttachments(),
                HarvestPermitApplicationAttachment.Type.SHOOTER_LIST);
        this.otherAttachmentCount = countAttachments(application.getAttachments(),
                HarvestPermitApplicationAttachment.Type.OTHER);
    }

    private static long countAttachments(final List<HarvestPermitApplicationAttachment> attachments,
                                         final HarvestPermitApplicationAttachment.Type type) {
        return attachments.stream().filter(a -> a.getAttachmentType() == type).count();
    }

    private static SpeciesAmount transformSpeciesAmount(final HarvestPermitApplicationSpeciesAmount spa,
                                                        final Locale locale) {
        return new SpeciesAmount(
                StringUtils.capitalize(spa.getGameSpecies().getNameLocalisation().getTranslation(locale)),
                spa.getAmount(),
                spa.getMooselikeDescription());
    }

    public Locale getLocale() {
        return locale;
    }

    public List<SpeciesAmount> getSpeciesAmounts() {
        return speciesAmounts;
    }

    public String getAreaExternalCode() {
        return areaExternalCode;
    }

    public double getLandAreaSize() {
        return landAreaSize;
    }

    public double getWaterAreaSize() {
        return waterAreaSize;
    }

    public double getTotalAreaSize() {
        return totalAreaSize;
    }

    public double getStateLandAreaSize() {
        return stateLandAreaSize;
    }

    public double getPrivateLandAreaSize() {
        return privateLandAreaSize;
    }

    public long getPartnerCount() {
        return partnerCount;
    }

    public List<OrganisationNameDTO> getPartners() {
        return partners;
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

    public boolean isFreeHunting() {
        return freeHunting;
    }

    public List<HarvestPermitAreaRhyDTO> getRhys() {
        return rhys;
    }

    public long getAreaPermitCount() {
        return areaPermitCount;
    }

    public long getShooterListCount() {
        return shooterListCount;
    }

    public long getOtherAttachmentCount() {
        return otherAttachmentCount;
    }
}
