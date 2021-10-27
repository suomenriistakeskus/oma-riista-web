package fi.riista.feature.organization.rhy.gamedamageinspection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fi.riista.config.jackson.LocalTimeToStringSerializer;
import fi.riista.config.jackson.StringToLocalTimeDeserializer;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysDTO;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import org.hibernate.validator.constraints.SafeHtml;
import org.hibernate.validator.constraints.SafeHtml.WhiteListType;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static fi.riista.util.DateUtil.toLocalDateNullSafe;

public class GameDamageInspectionEventDTO extends BaseEntityDTO<Long> {

    private Long id;
    private Integer rev;

    @Valid
    private RiistanhoitoyhdistysDTO rhy;

    @NotNull
    private int gameSpeciesCode;

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

    private BigDecimal hourlyExpensesUnit;

    private BigDecimal dailyAllowance;

    private Boolean lockedAsPastStatistics;

    @Valid
    private List<GameDamageInspectionKmExpenseDTO> gameDamageInspectionKmExpenses;

    @NotNull
    private Boolean expensesIncluded;

    @Valid
    private PersonContactInfoDTO inspector;

    public GameDamageInspectionEventDTO() {}

    public static GameDamageInspectionEventDTO createEmpty() {
        final GameDamageInspectionEventDTO dto = new GameDamageInspectionEventDTO();

        dto.setHourlyExpensesUnit(BigDecimal.ZERO);
        dto.setDailyAllowance(BigDecimal.ZERO);
        dto.setGameDamageInspectionKmExpenses(Collections.emptyList());
        dto.setExpensesIncluded(true);

        return dto;
    }

    public static GameDamageInspectionEventDTO create(final GameDamageInspectionEvent event,
                                                      final Riistanhoitoyhdistys rhy,
                                                      final List<GameDamageInspectionKmExpenseDTO> expenses) {
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
        dto.setDailyAllowance(event.getDailyAllowance());

        dto.setLockedAsPastStatistics(event.isLockedAsPastStatistics());

        dto.setGameDamageInspectionKmExpenses(expenses);

        dto.setExpensesIncluded(event.getExpensesIncluded());

        dto.setInspector(F.mapNullable(event.getInspector(), PersonContactInfoDTO::create));

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

    public List<GameDamageInspectionKmExpenseDTO> getGameDamageInspectionKmExpenses() {
        return gameDamageInspectionKmExpenses;
    }

    public void setGameDamageInspectionKmExpenses(final List<GameDamageInspectionKmExpenseDTO> gameDamageInspectionKmExpenses) {
        this.gameDamageInspectionKmExpenses = gameDamageInspectionKmExpenses;
    }

    public Boolean isExpensesIncluded() {
        return expensesIncluded;
    }

    public void setExpensesIncluded(final Boolean expensesIncluded) {
        this.expensesIncluded = expensesIncluded;
    }

    public PersonContactInfoDTO getInspector() {
        return inspector;
    }

    public void setInspector(final PersonContactInfoDTO inspector) {
        this.inspector = inspector;
    }
}
