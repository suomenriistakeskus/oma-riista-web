package fi.riista.feature.permit.application.mooselike;

import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.validation.ValidationUtil;
import fi.riista.feature.permit.area.HarvestPermitArea;

import static java.util.Objects.requireNonNull;

public class MooselikePermitApplicationValidator {

    private MooselikePermitApplicationValidator() {
        throw new UnsupportedOperationException();
    }

    public static void validateForSending(final HarvestPermitApplication application) {
        requireNonNull(application);

        ValidationUtil.validateForSending(application);

        validateContent(application);
    }

    public static void validateForAmend(final HarvestPermitApplication application) {
        requireNonNull(application);

        ValidationUtil.validateForAmend(application);

        validateContent(application);
    }

    public static void validateContent(final HarvestPermitApplication application) {
        requireNonNull(application);

        ValidationUtil.validateSpeciesAmounts(application);
        ValidationUtil.validateCommonContent(application);

        if (application.getPermitPartners().isEmpty()) {
            throw new IllegalStateException("partners is empty");
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
            validateAttachmentsGiven(application);
            validateShooterCountsDefined(application);
        }
    }

    private static void validateAttachmentsGiven(final HarvestPermitApplication application) {
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

    private static void validateShooterCountsDefined(final HarvestPermitApplication application) {
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
