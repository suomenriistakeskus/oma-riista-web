package fi.riista.feature.organization.rhy.gamedamageinspection;

import fi.riista.feature.common.money.FinnishBankAccount;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.util.LocalisedString;

import java.util.List;
import java.util.Map;

public class GameDamageInspectionEventExportDTO {

    private int year;
    private GameDamageType gameDamageType;
    private List<GameDamageInspectionEventDTO> dtos;
    private Map<Long, GameSpecies> idToGameSpecies;
    private LocalisedString rhyName;
    private FinnishBankAccount bankAccount;

    public static GameDamageInspectionEventExportDTO create(final int year,
                                                            final GameDamageType gameDamageType,
                                                            final List<GameDamageInspectionEventDTO> dtos,
                                                            final Map<Long, GameSpecies> idToGameSpecies,
                                                            final LocalisedString rhyName,
                                                            final FinnishBankAccount bankAccount) {
        return new GameDamageInspectionEventExportDTO(year,
                gameDamageType,
                dtos,
                idToGameSpecies,
                rhyName,
                bankAccount);
    }

    public GameDamageInspectionEventExportDTO() {}

    public GameDamageInspectionEventExportDTO(final int year,
                                              final GameDamageType gameDamageType,
                                              final List<GameDamageInspectionEventDTO> dtos,
                                              final Map<Long, GameSpecies> idToGameSpecies,
                                              final LocalisedString rhyName,
                                              final FinnishBankAccount bankAccount) {
        this.year = year;
        this.gameDamageType = gameDamageType;
        this.dtos = dtos;
        this.idToGameSpecies = idToGameSpecies;
        this.rhyName = rhyName;
        this.bankAccount = bankAccount;
    }

    public int getYear() {
        return year;
    }

    public GameDamageType getGameDamageType() {
        return gameDamageType;
    }

    public List<GameDamageInspectionEventDTO> getDtos() {
        return dtos;
    }

    public Map<Long, GameSpecies> getIdToGameSpecies() {
        return idToGameSpecies;
    }

    public LocalisedString getRhyName() {
        return rhyName;
    }

    public FinnishBankAccount getBankAccount() {
        return bankAccount;
    }
}
