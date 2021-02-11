package fi.riista.feature.permitplanning.hirvityvitys;

import com.google.common.base.Preconditions;
import com.querydsl.core.group.GroupBy;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.gis.verotuslohko.GISVerotusLohko;
import fi.riista.feature.gis.verotuslohko.GISVerotusLohkoRepository;
import fi.riista.feature.gis.verotuslohko.QGISVerotusLohko;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.QHarvestPermitApplication;
import fi.riista.feature.permit.application.QHarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.area.QHarvestPermitArea;
import fi.riista.feature.permit.area.rhy.QHarvestPermitAreaRhy;
import fi.riista.feature.permitplanning.hirvityvitys.dto.JyvitysExcelApplicationDTO;
import fi.riista.feature.permitplanning.hirvityvitys.dto.JyvitysExcelApplicationVerotuslohkoDTO;
import fi.riista.feature.permitplanning.hirvityvitys.dto.JyvitysExcelRhyDTO;
import fi.riista.feature.permitplanning.hirvityvitys.dto.JyvitysExcelVerotuslohkoDTO;
import fi.riista.util.F;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static fi.riista.util.NumberUtils.getIntValueOrZero;
import static java.util.Objects.requireNonNull;

@Component
public class JyvitysExcelFeature {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Resource
    private RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepository;

    @Resource
    private GISVerotusLohkoRepository verotusLohkoRepository;

    @Resource
    private EnumLocaliser i18n;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional(readOnly = true)
    public JyvitysExcelView export(final int huntingYear, @Nonnull final String officialCode) {
        requireNonNull(officialCode);
        Preconditions.checkState(activeUserService.isModeratorOrAdmin(), "Unauthorized access");

        final Riistanhoitoyhdistys rhy = riistanhoitoyhdistysRepository.findByOfficialCode(officialCode);
        final List<GISVerotusLohko> verotusLohkos = fetchVerotusLohkos(huntingYear, rhy.getOfficialCode());

        final ArrayList<JyvitysExcelVerotuslohkoDTO> jyvitysExcelVerotuslohkoDTOS =
                F.mapNonNullsToList(verotusLohkos, lohko ->
                        JyvitysExcelVerotuslohkoDTO.from(lohko));
        final JyvitysExcelRhyDTO jyvitysExcelRhyDTO =
                new JyvitysExcelRhyDTO(i18n.getTranslation(rhy.getNameLocalisation()), jyvitysExcelVerotuslohkoDTOS);
        final List<HarvestPermitApplication> applications = fetchActiveApplicationsInRhy(huntingYear, rhy);

        final Map<Long, Float> amountIndex = fetchMooseAmounts(F.getUniqueIds(applications));
        final List<JyvitysExcelApplicationDTO> applicationDTOS = mapApplicationsToDTOs(rhy, applications, amountIndex);

        return new JyvitysExcelView(i18n, jyvitysExcelRhyDTO, applicationDTOS);
    }


    private List<JyvitysExcelApplicationDTO> mapApplicationsToDTOs(final Riistanhoitoyhdistys rhy,
                                                                   final List<HarvestPermitApplication> applications,
                                                                   final Map<Long, Float> amountMap) {
        return F.mapNonNullsToList(applications, application ->
                JyvitysExcelApplicationDTO.Builder.builder()
                        .withApplicationNumber(requireNonNull(application.getApplicationNumber()))
                        .withApplicant(extractApplicantName(application))
                        .withOtherRhysInArea(collectOtherRhys(rhy, application))
                        .withShooterOnlyClub(getIntValueOrZero(application.getShooterOnlyClub()))
                        .withShooterOtherClubPassive(getIntValueOrZero(application.getShooterOtherClubPassive()))

                        .withAppliedAmount(amountMap.getOrDefault(application.getId(), 0f))
                        .withLohkoList(F.mapNonNullsToList(application.getArea().getVerotusLohko(),
                                JyvitysExcelApplicationVerotuslohkoDTO::new))
                        .build());
    }


    private List<String> collectOtherRhys(final Riistanhoitoyhdistys rhy, final HarvestPermitApplication application) {
        return application.getArea().getRhy().stream()
                .filter(areaRhy -> !areaRhy.getRhy().equals(rhy))
                .map(areaRhy -> i18n.getTranslation(areaRhy.getRhy().getNameLocalisation()))
                .collect(Collectors.toList());
    }

    private static String extractApplicantName(final HarvestPermitApplication application) {
        return Optional.ofNullable(application.getHuntingClub())
                .map(HuntingClub::getNameFinnish)
                .orElseGet(() -> application.getContactPerson().getFullName());
    }


    private List<GISVerotusLohko> fetchVerotusLohkos(final int huntingYear, final String rhyCode) {
        final QGISVerotusLohko LOHKO = QGISVerotusLohko.gISVerotusLohko;
        return verotusLohkoRepository.findAllAsList(
                LOHKO.huntingYear.eq(huntingYear)
                        .and(LOHKO.officialCode.startsWith(rhyCode)));
    }

    private List<HarvestPermitApplication> fetchActiveApplicationsInRhy(final int huntingYear,
                                                                        final Riistanhoitoyhdistys rhy) {


        QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;
        QHarvestPermitArea AREA = QHarvestPermitArea.harvestPermitArea;
        QHarvestPermitAreaRhy HPARHY = QHarvestPermitAreaRhy.harvestPermitAreaRhy;

        return jpqlQueryFactory
                .selectFrom(APPLICATION)
                .innerJoin(APPLICATION.area, AREA)
                .innerJoin(AREA.rhy, HPARHY)
                .where(HPARHY.rhy.eq(rhy))
                .where(APPLICATION.status.eq(HarvestPermitApplication.Status.ACTIVE))
                .where(APPLICATION.applicationYear.eq(huntingYear))
                .orderBy(APPLICATION.applicationNumber.desc())
                .fetch();
    }

    private Map<Long, Float> fetchMooseAmounts(final Collection<Long> applicationIds) {
        QHarvestPermitApplicationSpeciesAmount SPA =
                QHarvestPermitApplicationSpeciesAmount.harvestPermitApplicationSpeciesAmount;
        QGameSpecies SPECIES = QGameSpecies.gameSpecies;

        return jpqlQueryFactory.query()
                .select(SPA.harvestPermitApplication.id, SPA.specimenAmount)
                .from(SPA)
                .innerJoin(SPA.gameSpecies, SPECIES)
                .where(SPA.harvestPermitApplication.id.in(applicationIds))
                .where(SPECIES.officialCode.eq(GameSpecies.OFFICIAL_CODE_MOOSE))
                .transform(GroupBy.groupBy(SPA.harvestPermitApplication.id).as(SPA.specimenAmount));
    }
}
