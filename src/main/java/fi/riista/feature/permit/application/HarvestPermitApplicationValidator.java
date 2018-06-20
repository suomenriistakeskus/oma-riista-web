package fi.riista.feature.permit.application;

import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.species.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.util.F;
import org.springframework.util.StringUtils;

import java.util.HashSet;

public class HarvestPermitApplicationValidator {
    private final HarvestPermitApplication application;

    public HarvestPermitApplicationValidator(final HarvestPermitApplication application) {
        this.application = application;
    }

    public void validateForSending() {
        // validate state
        if (application.getStatus() != HarvestPermitApplication.Status.DRAFT) {
            throw new IllegalPermitApplicationStateTransitionException(application.getId());
        }

        if (application.getSubmitDate() != null) {
            throw new IllegalStateException("Application submitDate already set for applicationId: " + application.getId());
        }

        if (application.getApplicationNumber() != null) {
            throw new IllegalStateException("Application number already set for applicationId: " + application.getId());
        }

        if (StringUtils.hasText(application.getPermitNumber())) {
            throw new IllegalStateException("Permit number already set for applicationId: " + application.getId());
        }

        validateContent();
    }

    public void validateForAmend() {
        if (application.getStatus() != HarvestPermitApplication.Status.AMENDING) {
            throw new IllegalPermitApplicationStateTransitionException(application.getId());
        }

        if (application.getSubmitDate() == null) {
            throw new IllegalStateException("Application submitDate missing for applicationId: " + application.getId());
        }

        if (application.getApplicationNumber() == null) {
            throw new IllegalStateException("Application number missing for applicationId: " + application.getId());
        }

        if (!StringUtils.hasText(application.getPermitNumber())) {
            throw new IllegalStateException("Permit number missing for applicationId: " + application.getId());
        }

        validateContent();
    }

    public void validateContent() {
        validateSpeciesAmounts();

        if (application.getRhy() == null) {
            throw new IllegalStateException("Application RHY is not available");
        }

        if (application.getPermitPartners().isEmpty()) {
            throw new IllegalStateException("partners is empty");
        }

        if (application.getDeliveryByMail() == null) {
            throw new IllegalStateException("deliveryByMail is missing");
        }

        if (application.getArea() == null) {
            throw new IllegalStateException("missing permitArea");
        }

        final HarvestPermitArea permitArea = application.getArea();

        permitArea.assertStatus(HarvestPermitArea.StatusCode.READY);

        final GISZone zone = permitArea.getZone();

        if (zone == null || zone.isGeometryEmpty()) {
            throw new IllegalStateException("Empty permit area");
        }

        if (zone.getComputedAreaSize() <= 0) {
            throw new IllegalStateException("computedAreaSize is missing");
        }

        if (zone.getWaterAreaSize() < 0) {
            throw new IllegalStateException("waterAreaSize is missing");
        }

        if (zone.getStateLandAreaSize() == null || zone.getStateLandAreaSize() < 0) {
            throw new IllegalStateException("stateLandAreaSize is missing");
        }

        if (zone.getPrivateLandAreaSize() == null || zone.getPrivateLandAreaSize() < 0) {
            throw new IllegalStateException("privateLandAreaSize is missing");
        }

        if (permitArea.isFreeHunting()) {
            validateAttachmentsGiven();
            validateShooterCountsDefined();
        }
    }

    private void validateSpeciesAmounts() {
        if (application.getSpeciesAmounts().isEmpty()) {
            throw new IllegalStateException("speciesAmounts are missing");
        }

        final HashSet<Long> uniqueSpeciesIds = F.mapNonNullsToSet(
                application.getSpeciesAmounts(), spa -> spa.getGameSpecies().getId());

        if (uniqueSpeciesIds.size() != application.getSpeciesAmounts().size()) {
            throw new IllegalStateException("speciesAmount species area not unique");
        }

        for (final HarvestPermitApplicationSpeciesAmount speciesAmount : application.getSpeciesAmounts()) {
            if (speciesAmount.getAmount() < 1.0) {
                throw new IllegalStateException("speciesAmount is invalid: " + speciesAmount.getAmount());
            }
        }
    }

    private void validateAttachmentsGiven() {
        final boolean mhAreaPermitExists = application.getAttachments().stream()
                .anyMatch(a -> a.getAttachmentType() == HarvestPermitApplicationAttachment.Type.MH_AREA_PERMIT);

        if (!mhAreaPermitExists) {
            throw new IllegalStateException("attachment of type MH_AREA_PERMIT is missing");
        }

        final boolean shooterListExists = application.getAttachments().stream()
                .anyMatch(a -> a.getAttachmentType() == HarvestPermitApplicationAttachment.Type.SHOOTER_LIST);
        if (!shooterListExists) {
            throw new IllegalStateException("attachment of type SHOOTER_LIST is missing");
        }
    }

    private void validateShooterCountsDefined() {
        if (application.getShooterOnlyClub() == null || application.getShooterOnlyClub() < 0) {
            throw new IllegalStateException("shooterOnlyClub count is missing");
        }

        if (application.getShooterOtherClubPassive() == null || application.getShooterOtherClubPassive() < 0) {
            throw new IllegalStateException("shooterOtherClubPassive count is missing");
        }

        if (application.getShooterOtherClubActive() == null || application.getShooterOtherClubActive() < 0) {
            throw new IllegalStateException("shooterOtherClubActive count is missing");
        }
    }
}
