package fi.riista.feature.organization.rhy.gamedamageinspection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fi.riista.config.jackson.LocalTimeToStringSerializer;
import fi.riista.config.jackson.StringToLocalTimeDeserializer;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysDTO;
import fi.riista.util.DtoUtil;
import org.hibernate.validator.constraints.SafeHtml;
import org.hibernate.validator.constraints.SafeHtml.WhiteListType;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.math.BigDecimal;

import static fi.riista.util.DateUtil.toLocalDateNullSafe;

public class GameDamageInspectionEventDTO extends BaseEntityDTO<Long> {

    private Long id;
    private Integer rev;

    @Valid
    private RiistanhoitoyhdistysDTO rhy;

    @NotNull
    private int gameSpeciesCode;

    @NotNull
    @SafeHtml(whitelistType = WhiteListType.NONE)
    @Size(max = 255)
    private String inspectorName;

    @Valid
    private GeoLocation geoLocation;

    @NotNull
    private LocalDate date;

    @NotNull
    @JsonSerialize(using = LocalTimeToStringSerializer.class)
    @JsonDeserialize(using = StringToLocalTimeDeserializer.class)
    private LocalTime beginTime;

    @NotNull
    @JsonSerialize(using = LocalTimeToStringSerializer.class)
    @JsonDeserialize(using = StringToLocalTimeDeserializer.class)
    private LocalTime endTime;

    @SafeHtml(whitelistType = WhiteListType.NONE)
    private String description;

    @NotNull
    private BigDecimal hourlyExpensesUnit;

    @NotNull
    private Integer kilometers;

    @NotNull
    private BigDecimal kilometerExpensesUnit;

    @NotNull
    private BigDecimal dailyAllowance;

    private Boolean lockedAsPastStatistics;

    public GameDamageInspectionEventDTO() {}

    public static GameDamageInspectionEventDTO createEmpty() {
        final GameDamageInspectionEventDTO dto = new GameDamageInspectionEventDTO();

        dto.setHourlyExpensesUnit(BigDecimal.ZERO);
        dto.setKilometers(Integer.valueOf(0));
        dto.setKilometerExpensesUnit(BigDecimal.ZERO);
        dto.setDailyAllowance(BigDecimal.ZERO);

        return dto;
    }

    public static GameDamageInspectionEventDTO create(final GameDamageInspectionEvent event,
                                                      final Riistanhoitoyhdistys rhy) {
        final GameDamageInspectionEventDTO dto = new GameDamageInspectionEventDTO();
        DtoUtil.copyBaseFields(event, dto);

        dto.setRhy(RiistanhoitoyhdistysDTO.create(rhy));

        dto.setGameSpeciesCode(event.getGameSpecies().getOfficialCode());
        dto.setInspectorName(event.getInspectorName());
        dto.setGeoLocation(event.getGeoLocation());
        dto.setDate(toLocalDateNullSafe(event.getDate()));
        dto.setBeginTime(event.getBeginTime());
        dto.setEndTime(event.getEndTime());
        dto.setDescription(event.getDescription());

        dto.setHourlyExpensesUnit(event.getHourlyExpensesUnit());
        dto.setKilometers(event.getKilometers());
        dto.setKilometerExpensesUnit(event.getKilometerExpensesUnit());
        dto.setDailyAllowance(event.getDailyAllowance());

        dto.setLockedAsPastStatistics(event.isLockedAsPastStatistics());

        return dto;
    }

    // Accessors -->

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public RiistanhoitoyhdistysDTO getRhy() {
        return rhy;
    }

    public void setRhy(final RiistanhoitoyhdistysDTO rhy) {
        this.rhy = rhy;
    }

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final int gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public String getInspectorName() {
        return inspectorName;
    }

    public void setInspectorName(final String inspectorName) {
        this.inspectorName = inspectorName;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(final GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(final LocalDate date) {
        this.date = date;
    }

    public LocalTime getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(final LocalTime beginTime) {
        this.beginTime = beginTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(final LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public BigDecimal getHourlyExpensesUnit() {
        return hourlyExpensesUnit;
    }

    public void setHourlyExpensesUnit(final BigDecimal hourlyExpensesUnit) {
        this.hourlyExpensesUnit = hourlyExpensesUnit;
    }

    public Integer getKilometers() {
        return kilometers;
    }

    public void setKilometers(final Integer kilometers) {
        this.kilometers = kilometers;
    }

    public BigDecimal getKilometerExpensesUnit() {
        return kilometerExpensesUnit;
    }

    public void setKilometerExpensesUnit(final BigDecimal kilometerExpensesUnit) {
        this.kilometerExpensesUnit = kilometerExpensesUnit;
    }

    public BigDecimal getDailyAllowance() {
        return dailyAllowance;
    }

    public void setDailyAllowance(final BigDecimal dailyAllowance) {
        this.dailyAllowance = dailyAllowance;
    }

    public Boolean getLockedAsPastStatistics() {
        return lockedAsPastStatistics;
    }

    public void setLockedAsPastStatistics(final Boolean lockedAsPastStatistics) {
        this.lockedAsPastStatistics = lockedAsPastStatistics;
    }
}
