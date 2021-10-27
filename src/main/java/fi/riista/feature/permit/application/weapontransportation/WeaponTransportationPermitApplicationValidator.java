package fi.riista.feature.permit.application.weapontransportation;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.validation.ValidationUtil;
import fi.riista.feature.permit.application.weapontransportation.justification.TransportedWeapon;
import fi.riista.feature.permit.application.weapontransportation.justification.TransportedWeaponType;
import fi.riista.feature.permit.application.weapontransportation.justification.WeaponTransportationVehicle;
import fi.riista.feature.permit.application.weapontransportation.justification.WeaponTransportationVehicleType;
import fi.riista.feature.permit.application.weapontransportation.reason.WeaponTransportationReasonType;
import org.apache.commons.lang.StringUtils;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class WeaponTransportationPermitApplicationValidator {

    public static void validateForSending(final HarvestPermitApplication application,
                                          final WeaponTransportationPermitApplication transportApplication,
                                          final List<TransportedWeapon> weaponInformation,
                                          final List<WeaponTransportationVehicle> vehicles) {
        requireNonNull(application);
        requireNonNull(transportApplication);

        ValidationUtil.validateForSending(application);

        validateContent(application, transportApplication, weaponInformation, vehicles);
    }

    public static void validateForAmend(final HarvestPermitApplication application,
                                        final WeaponTransportationPermitApplication transportApplication,
                                        final List<TransportedWeapon> weaponInformation,
                                        final List<WeaponTransportationVehicle> vehicles) {
        requireNonNull(application);
        requireNonNull(transportApplication);

        ValidationUtil.validateForAmend(application);

        validateContent(application, transportApplication, weaponInformation, vehicles);
    }

    public static void validateContent(final HarvestPermitApplication application,
                                       final WeaponTransportationPermitApplication transportApplication,
                                       final List<TransportedWeapon> weaponInformation,
                                       final List<WeaponTransportationVehicle> vehicles) {
        requireNonNull(application);
        requireNonNull(transportApplication);

        ValidationUtil.validateCommonContent(application);

        assertPermitHolderInformationValid(application);
        assertAreaInformationValid(transportApplication);

        if (StringUtils.isBlank(transportApplication.getAreaDescription())) {
            assertAreaAttachmentsPresent(application.getAttachments());
        }

        assertReason(transportApplication);
        assertJustification(weaponInformation, vehicles);
    }

    private static void assertPermitHolderInformationValid(final HarvestPermitApplication application) {
        final PermitHolder permitHolder = application.getPermitHolder();
        requireNonNull(permitHolder);

        if (StringUtils.isEmpty(permitHolder.getName())) {
            failValidation("Permit holder name missing");
        }

        if (StringUtils.isEmpty(permitHolder.getCode()) && !permitHolder.getType().equals(PermitHolder.PermitHolderType.PERSON)) {
            failValidation("Code missing for permit holder");
        }
    }

    private static void assertAreaAttachmentsPresent(final List<HarvestPermitApplicationAttachment> attachments) {
        if (attachments.stream().noneMatch(a -> a.getAttachmentType() == HarvestPermitApplicationAttachment.Type.PROTECTED_AREA)) {
            failValidation("Area attachment is missing");
        }
    }

    private static void assertAreaInformationValid(final WeaponTransportationPermitApplication transportApplication) {
        if (transportApplication.getGeoLocation() == null) {
            failValidation("Geolocation missing");
        }
    }

    private static void assertReason(final WeaponTransportationPermitApplication transportApplication) {
        final String reasonDescription = transportApplication.getReasonDescription();
        if (transportApplication.getReasonType() == WeaponTransportationReasonType.MUU) {
            if (StringUtils.isEmpty(reasonDescription)) {
                failValidation("Description missing for reason");
            }
        } else {
            if (!StringUtils.isEmpty(reasonDescription)) {
                failValidation("Extra description for reason");
            }
        }
    }

    private static void assertJustification(final List<TransportedWeapon> weaponInformation,
                                            final List<WeaponTransportationVehicle> vehicles) {
        vehicles.forEach(vehicle -> {
            final String vehicleDescription = vehicle.getDescription();
            if (vehicle.getType() == WeaponTransportationVehicleType.MUU &&
                    StringUtils.isEmpty(vehicleDescription)) {
                failValidation("Description missing for vehicle");
            } else if (vehicle.getType() != WeaponTransportationVehicleType.MUU &&
                    !StringUtils.isEmpty(vehicleDescription)) {
                failValidation("Extra description for vehicle");
            }
        });

        weaponInformation.forEach(weapon -> {
            final String weaponDescription = weapon.getDescription();
            if (weapon.getType() == TransportedWeaponType.MUU) {
                if (StringUtils.isEmpty(weaponDescription)) {
                    failValidation("Description missing for weapon");
                }
            } else {
                if (!StringUtils.isEmpty(weaponDescription)) {
                    failValidation("Extra description for weapon");
                }
            }
        });
    }

    private static void failValidation(final String errorMessage) {
        throw new IllegalStateException(errorMessage);
    }

    private WeaponTransportationPermitApplicationValidator() {
        throw new UnsupportedOperationException();
    }
}
