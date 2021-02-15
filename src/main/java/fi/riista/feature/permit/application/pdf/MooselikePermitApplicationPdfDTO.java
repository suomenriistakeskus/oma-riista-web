package fi.riista.feature.permit.application.pdf;

import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.mooselike.MooselikePermitApplicationSpeciesAmountDTO;
import fi.riista.feature.permit.area.hta.HarvestPermitAreaHtaDTO;
import fi.riista.feature.permit.area.partner.HarvestPermitAreaPartnerDTO;
import fi.riista.feature.permit.area.rhy.HarvestPermitAreaRhyDTO;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class MooselikePermitApplicationPdfDTO {
    private final int huntingYear;
    private final DateTime submitDate;
    private final Integer applicationNumber;
    private final String applicationName;
    private final boolean freeHunting;

    private final PersonContactInfoDTO contactPerson;
    private final PermitHolderDTO permitHolder;

    private final Integer shooterOnlyClub;
    private final Integer shooterOtherClubPassive;
    private final Integer shooterOtherClubActive;

    private final boolean deliveryByMail;
    private final String email1;
    private final String email2;

    private final List<MooselikePermitApplicationSpeciesAmountDTO> speciesAmounts;
    private final List<OrganisationNameDTO> partners;
    private final List<HarvestPermitAreaRhyDTO> rhys;
    private final List<HarvestPermitAreaHtaDTO> htas;

    private final double landAreaSize;
    private final double waterAreaSize;
    private final double totalAreaSize;
    private final double stateLandAreaSize;
    private final double privateLandAreaSize;

    private final List<String> mhAreaPermits;
    private final List<String> shooterLists;
    private final List<String> otherAttachments;

    private final String unionAreaId;
    private final List<HarvestPermitAreaPartnerDTO> areaPartners;
    private final String locale;

    public MooselikePermitApplicationPdfDTO(final @Nonnull HarvestPermitApplication application,
                                            final @Nonnull List<MooselikePermitApplicationSpeciesAmountDTO> speciesAmounts,
                                            final @Nonnull List<OrganisationNameDTO> partners,
                                            final @Nonnull List<HarvestPermitAreaRhyDTO> rhys,
                                            final @Nonnull List<HarvestPermitAreaHtaDTO> htas,
                                            final @Nonnull GISZoneSizeDTO areaSize,
                                            final @Nonnull String unionAreaId,
                                            final @Nonnull List<HarvestPermitAreaPartnerDTO> areaPartners,
                                            final @Nonnull String locale) {
        this.huntingYear = application.getApplicationYear();
        this.submitDate = application.getSubmitDate();
        this.applicationNumber = application.getApplicationNumber();
        this.applicationName = application.getHarvestPermitCategory().getApplicationName().getTranslation(application.getLocale());
        this.freeHunting = application.getArea().isFreeHunting();

        this.contactPerson = PersonContactInfoDTO.create(requireNonNull(application.getContactPerson()));
        this.permitHolder = PermitHolderDTO.createFrom(requireNonNull(application.getPermitHolder()));

        this.shooterOnlyClub = application.getShooterOnlyClub();
        this.shooterOtherClubPassive = application.getShooterOtherClubPassive();
        this.shooterOtherClubActive = application.getShooterOtherClubActive();

        this.deliveryByMail = Boolean.TRUE.equals(application.getDeliveryByMail());
        this.email1 = application.getEmail1();
        this.email2 = application.getEmail2();

        this.speciesAmounts = speciesAmounts;
        this.partners = partners;
        this.rhys = rhys;
        this.htas = htas;

        this.landAreaSize = areaSize.getAll().getLand();
        this.waterAreaSize = areaSize.getAll().getWater();
        this.totalAreaSize = areaSize.getAll().getTotal();
        this.stateLandAreaSize = areaSize.getStateLandAreaSize();
        this.privateLandAreaSize = areaSize.getPrivateLandAreaSize();

        this.mhAreaPermits = application.getAttachmentFilenames(HarvestPermitApplicationAttachment.Type.MH_AREA_PERMIT);
        this.shooterLists = application.getAttachmentFilenames(HarvestPermitApplicationAttachment.Type.SHOOTER_LIST);
        this.otherAttachments = application.getAttachmentFilenames(HarvestPermitApplicationAttachment.Type.OTHER);

        this.unionAreaId = unionAreaId;
        this.areaPartners = areaPartners;
        this.locale = locale;
    }

    public int getHuntingYear() {
        return huntingYear;
    }

    public DateTime getSubmitDate() {
        return submitDate;
    }

    public Integer getApplicationNumber() {
        return applicationNumber;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public boolean isFreeHunting() {
        return freeHunting;
    }

    public PersonContactInfoDTO getContactPerson() {
        return contactPerson;
    }

    public PermitHolderDTO getPermitHolder() {
        return permitHolder;
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

    public boolean getDeliveryByMail() {
        return deliveryByMail;
    }

    public String getEmail1() {
        return email1;
    }

    public String getEmail2() {
        return email2;
    }

    public List<MooselikePermitApplicationSpeciesAmountDTO> getSpeciesAmounts() {
        return speciesAmounts;
    }

    public List<OrganisationNameDTO> getPartners() {
        return partners;
    }

    public List<HarvestPermitAreaRhyDTO> getRhys() {
        return rhys;
    }

    public List<HarvestPermitAreaHtaDTO> getHtas() {
        return htas;
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

    public List<String> getMhAreaPermits() {
        return mhAreaPermits;
    }

    public List<String> getShooterLists() {
        return shooterLists;
    }

    public List<String> getOtherAttachments() {
        return otherAttachments;
    }

    public String getUnionAreaId() {
        return unionAreaId;
    }

    public List<HarvestPermitAreaPartnerDTO> getAreaPartners() {
        return areaPartners;
    }

    public String getLocale() {
        return locale;
    }
}
