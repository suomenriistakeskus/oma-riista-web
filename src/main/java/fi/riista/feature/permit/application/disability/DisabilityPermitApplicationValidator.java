package fi.riista.feature.permit.application.disability;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.PermitApplicationVehicleType;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.disability.justification.DisabilityPermitHuntingTypeInfo;
import fi.riista.feature.permit.application.disability.justification.DisabilityPermitVehicle;
import fi.riista.feature.permit.application.disability.justification.HuntingType;
import fi.riista.feature.permit.application.validation.ValidationUtil;
import org.apache.commons.lang.StringUtils;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class DisabilityPermitApplicationValidator {

    public static void validateForSending(final HarvestPermitApplication application,
                                          final DisabilityPermitApplication disabilityPermitApplication,
                                          final List<DisabilityPermitVehicle> vehicles,
                                          final List<DisabilityPermitHuntingTypeInfo> huntingTypeInfos) {
        requireNonNull(application);
        requireNonNull(disabilityPermitApplication);

        ValidationUtil.validateForSending(application);

        validateContent(application, disabilityPermitApplication, vehicles, huntingTypeInfos);
    }

    public static void validateForAmend(final HarvestPermitApplication application,
                                        final DisabilityPermitApplication disabilityPermitApplication,
                                        final List<DisabilityPermitVehicle> vehicles,
                                        final List<DisabilityPermitHuntingTypeInfo> huntingTypeInfos) {
        requireNonNull(application);
        requireNonNull(disabilityPermitApplication);

        ValidationUtil.validateForAmend(application);

        validateContent(application, disabilityPermitApplication, vehicles, huntingTypeInfos);
    }

    public static void validateContent(final HarvestPermitApplication application,
                                       final DisabilityPermitApplication disabilityPermitApplication,
                                       final List<DisabilityPermitVehicle> vehicles,
                                       final List<DisabilityPermitHuntingTypeInfo> huntingTypeInfos) {
        requireNonNull(application);
        requireNonNull(disabilityPermitApplication);

        if (application.getDeliveryByMail() == null) {
            throw new IllegalStateException("deliveryByMail is missing");
        }

        assertPermitHolderInformationValid(application);

        assertPeriod(disabilityPermitApplication);
        assertJustification(vehicles, huntingTypeInfos);
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

    private static void assertPeriod(final DisabilityPermitApplication disabilityPermitApplication) {
        if (!disabilityPermitApplication.getUseMotorVehicle() && !disabilityPermitApplication.getUseVehicleForWeaponTransport()) {
                failValidation("Either motor vehicle use or weapon transportation must be selected");
        }

        if (disabilityPermitApplication.getEndDate().isBefore(disabilityPermitApplication.getBeginDate())) {
            failValidation("End date must be after begin date");
        }
    }

    private static void assertJustification(final List<DisabilityPermitVehicle> vehicles,
                                            final List<DisabilityPermitHuntingTypeInfo> huntingTypeInfos) {
        vehicles.forEach(vehicle -> {
            final String vehicleDescription = vehicle.getDescription();
            if (vehicle.getType() == PermitApplicationVehicleType.MUU &&
                    StringUtils.isEmpty(vehicleDescription)) {
                failValidation("Description missing for vehicle");
            } else if (vehicle.getType() != PermitApplicationVehicleType.MUU &&
                    !StringUtils.isEmpty(vehicleDescription)) {
                failValidation("Extra description for vehicle");
            }
        });

        huntingTypeInfos.forEach(huntingTypeInfo -> {
            final String huntingTypeDescription = huntingTypeInfo.getHuntingTypeDescription();
            if (huntingTypeInfo.getHuntingType() == HuntingType.MUU &&
                    StringUtils.isEmpty(huntingTypeDescription)) {
                failValidation("Description missing for hunting type");
            } else if (huntingTypeInfo.getHuntingType() != HuntingType.MUU &&
                    !StringUtils.isEmpty(huntingTypeDescription)) {
                failValidation("Extra description for hunting type");
            }
        });
    }

    private static void failValidation(final String errorMessage) {
        throw new IllegalStateException(errorMessage);
    }

    private DisabilityPermitApplicationValidator() {
        throw new UnsupportedOperationException();
    }
}
