package fi.riista.feature.permit.application.disability.basicinfo;

import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.permit.application.disability.DisabilityPermitApplication;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

public class BasicInfoDTO implements HasBeginAndEndDate {

    public static BasicInfoDTO create(final @Nonnull DisabilityPermitApplication application) {
        requireNonNull(application);

        final BasicInfoDTO dto = new BasicInfoDTO();
        dto.setUseMotorVehicle(application.getUseMotorVehicle());
        dto.setUseVehicleForWeaponTransport(application.getUseVehicleForWeaponTransport());
        dto.setBeginDate(application.getBeginDate());
        dto.setEndDate(application.getEndDate());
        return dto;
    }

    private boolean useMotorVehicle;
    private boolean useVehicleForWeaponTransport;

    @NotNull
    private LocalDate beginDate;

    @NotNull
    private LocalDate endDate;

    public BasicInfoDTO() {
    }

    public BasicInfoDTO(final boolean useMotorVehicle, final boolean useVehicleForWeaponTransport,
                        final LocalDate beginDate, final LocalDate endDate) {
        this.useMotorVehicle = useMotorVehicle;
        this.useVehicleForWeaponTransport = useVehicleForWeaponTransport;
        this.beginDate = beginDate;
        this.endDate = endDate;
    }

    @AssertTrue
    public boolean isValidUsage() {
        return useMotorVehicle == true || useVehicleForWeaponTransport == true;
    }

    public boolean getUseMotorVehicle() {
        return useMotorVehicle;
    }

    public void setUseMotorVehicle(final boolean useMotorVehicle) {
        this.useMotorVehicle = useMotorVehicle;
    }

    public boolean getUseVehicleForWeaponTransport() {
        return useVehicleForWeaponTransport;
    }

    public void setUseVehicleForWeaponTransport(final boolean useVehicleForWeaponTransport) {
        this.useVehicleForWeaponTransport = useVehicleForWeaponTransport;
    }

    @Override
    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(final LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }
}
