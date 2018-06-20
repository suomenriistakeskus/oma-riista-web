package fi.riista.feature.permit.application.pdf;

import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.species.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.area.hta.HarvestPermitAreaHtaDTO;
import fi.riista.feature.permit.area.rhy.HarvestPermitAreaRhyDTO;
import org.joda.time.DateTime;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class HarvestPermitApplicationPdfDTO {
    private final int huntingYear;
    private final DateTime submitDate;
    private final Integer applicationNumber;
    private final String applicationName;
    private final boolean freeHunting;

    private final PersonContactInfoDTO contactPerson;
    private final OrganisationNameDTO permitHolder;

    private final Integer shooterOnlyClub;
    private final Integer shooterOtherClubPassive;
    private final Integer shooterOtherClubActive;

    private final boolean deliveryByMail;
    private final String email1;
    private final String email2;

    private final List<Map<String, Object>> speciesAmounts;
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

    public HarvestPermitApplicationPdfDTO(final HarvestPermitApplication application,
                                          final GISZoneSizeDTO areaSize) {
        this.huntingYear = application.getHuntingYear();
        this.submitDate = application.getSubmitDate();
        this.applicationNumber = application.getApplicationNumber();
        this.applicationName = "Hirvieläinten pyyntilupa";
        this.freeHunting = application.getArea().isFreeHunting();

        this.contactPerson = PersonContactInfoDTO.create(application.getContactPerson());
        this.permitHolder = OrganisationNameDTO.createWithOfficialCode(application.getPermitHolder());

        this.shooterOnlyClub = application.getShooterOnlyClub();
        this.shooterOtherClubPassive = application.getShooterOtherClubPassive();
        this.shooterOtherClubActive = application.getShooterOtherClubActive();

        this.deliveryByMail = application.getDeliveryByMail();
        this.email1 = application.getEmail1();
        this.email2 = application.getEmail2();

        this.speciesAmounts = application.getSpeciesAmounts().stream()
                .sorted(Comparator.comparingDouble(HarvestPermitApplicationSpeciesAmount::getAmount).reversed())
                .map(HarvestPermitApplicationPdfDTO::transformSpeciesAmount)
                .collect(toList());

        this.partners = application.getPermitPartners().stream()
                .map(OrganisationNameDTO::createWithOfficialCode)
                .sorted(Comparator.comparing(OrganisationNameDTO::getOfficialCode))
                .collect(toList());

        this.rhys = application.getArea().getRhy().stream()
                .map(HarvestPermitAreaRhyDTO::create)
                .sorted(Comparator.<HarvestPermitAreaRhyDTO>comparingDouble(rhy -> rhy.getBothSize().getTotal()).reversed())
                .collect(toList());

        this.htas = application.getArea().getHta().stream()
                .map(HarvestPermitAreaHtaDTO::create)
                .sorted(Comparator.comparingDouble(HarvestPermitAreaHtaDTO::getComputedAreaSize).reversed())
                .collect(toList());

        this.landAreaSize = areaSize.getAll().getLand();
        this.waterAreaSize = areaSize.getAll().getWater();
        this.totalAreaSize = areaSize.getAll().getTotal();
        this.stateLandAreaSize = areaSize.getStateLandAreaSize();
        this.privateLandAreaSize = areaSize.getPrivateLandAreaSize();

        this.mhAreaPermits = getAttachmentFilenames(application.getAttachments(),
                HarvestPermitApplicationAttachment.Type.MH_AREA_PERMIT);
        this.shooterLists = getAttachmentFilenames(application.getAttachments(),
                HarvestPermitApplicationAttachment.Type.SHOOTER_LIST);
        this.otherAttachments = getAttachmentFilenames(application.getAttachments(),
                HarvestPermitApplicationAttachment.Type.OTHER);
    }

    private static List<String> getAttachmentFilenames(final List<HarvestPermitApplicationAttachment> attachments,
                                                       final HarvestPermitApplicationAttachment.Type type) {
        return attachments.stream()
                .filter(a -> a.getAttachmentType() == type)
                .map(HarvestPermitApplicationAttachment::getName)
                .collect(toList());
    }

    private static Map<String, Object> transformSpeciesAmount(final HarvestPermitApplicationSpeciesAmount spa) {
        final Map<String, Object> map = new HashMap<>();
        map.put("name", spa.getGameSpecies().getNameFinnish());
        map.put("amount", spa.getAmount());
        map.put("description", spa.getDescription());
        return map;
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

    public OrganisationNameDTO getPermitHolder() {
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

    public List<Map<String, Object>> getSpeciesAmounts() {
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
}
