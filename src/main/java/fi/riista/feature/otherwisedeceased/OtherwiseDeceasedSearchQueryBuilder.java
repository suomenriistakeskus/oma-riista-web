package fi.riista.feature.otherwisedeceased;

import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.organization.QOrganisation;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.List;

import static fi.riista.config.Constants.DEFAULT_TIMEZONE;

public class OtherwiseDeceasedSearchQueryBuilder {

    // Factories

    public static OtherwiseDeceasedSearchQueryBuilder create(final JPQLQueryFactory jpqlQueryFactory) {
        return new OtherwiseDeceasedSearchQueryBuilder(jpqlQueryFactory);
    }

    // Constants

    private final QOtherwiseDeceased OTHERWISE_DECEASED = QOtherwiseDeceased.otherwiseDeceased;
    private final QGameSpecies GAME_SPECIES = QGameSpecies.gameSpecies;
    private final QOrganisation ORGANISATION = QOrganisation.organisation;

    // Attributes

    private final JPQLQueryFactory jpqlQueryFactory;
    private Integer gameSpeciesCode;
    private DateTime beginTime;
    private DateTime endTime;
    private String rkaOfficialCode;
    private String rhyOfficialCode;
    private OtherwiseDeceasedCause cause;
    private Boolean rejected;

    // Constructors

    private OtherwiseDeceasedSearchQueryBuilder(final JPQLQueryFactory jpqlQueryFactory) {
        this.jpqlQueryFactory = jpqlQueryFactory;
    }

    // Factory methods

    public OtherwiseDeceasedSearchQueryBuilder withGameSpecies(final Integer gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
        return this;
    }

    public OtherwiseDeceasedSearchQueryBuilder withBeginDate(final LocalDate beginDate) {
        this.beginTime = beginDate.toDateTimeAtStartOfDay(DEFAULT_TIMEZONE);
        return this;
    }

    public OtherwiseDeceasedSearchQueryBuilder withEndDate(final LocalDate endDate) {
        this.endTime = endDate.toDateTimeAtStartOfDay(DEFAULT_TIMEZONE).plusDays(1).minusMillis(1);
        return this;
    }

    public OtherwiseDeceasedSearchQueryBuilder withRka(final String officialCode) {
        this.rkaOfficialCode = officialCode;
        return this;
    }

    public OtherwiseDeceasedSearchQueryBuilder withRhy(final String officialCode) {
        this.rhyOfficialCode = officialCode;
        return this;
    }

    public OtherwiseDeceasedSearchQueryBuilder withCause(final OtherwiseDeceasedCause cause) {
        this.cause = cause;
        return this;
    }

    public OtherwiseDeceasedSearchQueryBuilder withRejected(final Boolean showRejected) {
        this.rejected = showRejected;
        return this;
    }

    public OtherwiseDeceasedSearchQueryBuilder withFilter(final OtherwiseDeceasedFilterDTO filter) {
        return this
                .withGameSpecies(filter.getGameSpeciesCode())
                .withBeginDate(filter.getBeginDate())
                .withEndDate(filter.getEndDate())
                .withRka(filter.getRkaOfficialCode())
                .withRhy(filter.getRhyOfficialCode())
                .withCause(filter.getCause())
                .withRejected(filter.isShowRejected());
    }

    public List<OtherwiseDeceased> list() {
        return build().select(OTHERWISE_DECEASED)
                .orderBy(OTHERWISE_DECEASED.pointOfTime.asc())
                .fetch();
    }

    private JPQLQuery<?> build() {
        final JPQLQuery<?> query = jpqlQueryFactory
                .from(OTHERWISE_DECEASED);

        if (beginTime != null) {
            query.where(OTHERWISE_DECEASED.pointOfTime.goe(beginTime));
        }

        if (endTime != null) {
            query.where(OTHERWISE_DECEASED.pointOfTime.loe(endTime));
        }

        if (gameSpeciesCode != null) {
            query.join(OTHERWISE_DECEASED.species, GAME_SPECIES);
            query.where(GAME_SPECIES.officialCode.eq(gameSpeciesCode));
        }

        if (rhyOfficialCode != null) {
            query.join(OTHERWISE_DECEASED.rhy, ORGANISATION);
            query.where(ORGANISATION.officialCode.eq(rhyOfficialCode));
        } else if (rkaOfficialCode != null) {
            query.join(OTHERWISE_DECEASED.rka, ORGANISATION);
            query.where(ORGANISATION.officialCode.eq(rkaOfficialCode));
        }

        if (cause != null) {
            query.where(OTHERWISE_DECEASED.cause.eq(cause));
        }

        if (rejected != null) {
            query.where(OTHERWISE_DECEASED.rejected.eq(rejected));
        }

        return query;
    }
}
