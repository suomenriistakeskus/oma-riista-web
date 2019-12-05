package fi.riista.feature.organization.rhy.gamedamageinspection;

import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysDTO;

import org.iban4j.Iban;
import org.joda.time.Hours;

import java.math.BigDecimal;
import java.util.List;


public class GameDamageInspectionEventSummaryDTO {
    private RiistanhoitoyhdistysDTO rhy;
    private BigDecimal kilometerExpenses;
    private BigDecimal dailyAllowances;
    private BigDecimal rhyExpenses;
    private BigDecimal totalExpenses;
    private Iban iban;

    public GameDamageInspectionEventSummaryDTO() {}

    public static GameDamageInspectionEventSummaryDTO createEmpty() {
        final GameDamageInspectionEventSummaryDTO dto = new GameDamageInspectionEventSummaryDTO();

        dto.setKilometerExpenses(BigDecimal.ZERO);
        dto.setDailyAllowances(BigDecimal.ZERO);
        dto.setRhyExpenses(BigDecimal.ZERO);
        dto.setTotalExpenses(BigDecimal.ZERO);

        return dto;
    }

    public static GameDamageInspectionEventSummaryDTO create(final Riistanhoitoyhdistys rhy,
                                                             final List<GameDamageInspectionEvent> events,
                                                             final Iban iban) {
        final GameDamageInspectionEventSummaryDTO dto = new GameDamageInspectionEventSummaryDTO();

        dto.setRhy(RiistanhoitoyhdistysDTO.create(rhy));

        final BigDecimal kilometerExpenses = events.stream()
                .map(event -> event.getKilometerExpensesUnit().multiply(BigDecimal.valueOf(event.getKilometers())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setKilometerExpenses(kilometerExpenses);

        final BigDecimal dailyAllowances = events.stream()
                .map(event -> event.getDailyAllowance())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setDailyAllowances(dailyAllowances);

        final BigDecimal rhyExpenses = events.stream()
                .map(event ->
                        event.getHourlyExpensesUnit().multiply(BigDecimal.valueOf(Hours.hoursBetween(event.getBeginTime(), event.getEndTime()).getHours())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setRhyExpenses(rhyExpenses);

        dto.setTotalExpenses(rhyExpenses.add(kilometerExpenses).add(dailyAllowances));

        dto.setIban(iban);

        return dto;
    }

    public RiistanhoitoyhdistysDTO getRhy() {
        return rhy;
    }

    public void setRhy(final RiistanhoitoyhdistysDTO rhy) {
        this.rhy = rhy;
    }

    public BigDecimal getKilometerExpenses() {
        return kilometerExpenses;
    }

    public void setKilometerExpenses(final BigDecimal kilometerExpenses) {
        this.kilometerExpenses = kilometerExpenses;
    }

    public BigDecimal getDailyAllowances() {
        return dailyAllowances;
    }

    public void setDailyAllowances(final BigDecimal dailyAllowances) {
        this.dailyAllowances = dailyAllowances;
    }

    public BigDecimal getRhyExpenses() {
        return rhyExpenses;
    }

    public void setRhyExpenses(final BigDecimal rhyExpenses) {
        this.rhyExpenses = rhyExpenses;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(final BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public Iban getIban() {
        return iban;
    }

    public void setIban(final Iban iban) {
        this.iban = iban;
    }

}
