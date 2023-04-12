package fi.riista.feature.harvestpermit.report.excel;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayRepository;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.ListTransformer;
import org.joda.time.DateTime;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fi.riista.util.F.mapNullable;
import static fi.riista.util.jpa.CriteriaUtils.singleQueryFunction;
import static java.util.Optional.ofNullable;

@Component
public class HarvestReportReviewDTOTransformer extends ListTransformer<Harvest, HarvestReportReviewDTO> {

    @Resource
    private PersonRepository personRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private HarvestPermitRepository permitRepository;

    @Resource
    private GameSpeciesRepository speciesRepository;

    @Resource
    private GroupHuntingDayRepository huntingDayRepository;

    @Resource
    private UserRepository userRepository;

    @Nonnull
    @Override
    protected List<HarvestReportReviewDTO> transform(@Nonnull final List<Harvest> list) {

        final Function<Harvest, Person> authorMapping =
                singleQueryFunction(list, Harvest::getAuthor, personRepository, true);
        final Function<Harvest, Person> shooterMapping =
                singleQueryFunction(list, Harvest::getActualShooter, personRepository, true);
        final Function<Harvest, Organisation> rhyMapping =
                singleQueryFunction(list, Harvest::getRhy, organisationRepository, true);
        final Function<Harvest, HarvestPermit> permitMapping =
                singleQueryFunction(list, Harvest::getHarvestPermit, permitRepository, false);
        final Function<Harvest, GameSpecies> speciesMapping =
                singleQueryFunction(list, Harvest::getSpecies, speciesRepository, true);
        final Map<Long, Organisation> rkaMap =
                F.indexById(organisationRepository.findByOrganisationType(OrganisationType.RKA, Sort.unsorted()));

        final Map<Long, GroupHuntingDay> huntingDays =
                F.indexById(huntingDayRepository.findAllById(F.mapNonNullsToSet(list, harvest ->
                        mapNullable(harvest.getHuntingDayOfGroup(), GroupHuntingDay::getId))));
        final Map<Long, Organisation> groups =
                F.indexById(organisationRepository.findAllById(F.mapNonNullsToSet(huntingDays.values(),
                        d -> d.getGroup().getId())));

        final Map<Long, SystemUser> creators = F.indexById(userRepository.findAllById(list.stream()
                .map(Harvest::getCreatedByUserId)
                .collect(Collectors.toList())));

        // Fetch clubs and permits from database
        organisationRepository.findAllById(F.mapNonNullsToSet(groups.values(),
                group -> group.getParentOrganisation().getId()));
        permitRepository.findAllById(F.mapNonNullsToSet(groups.values(),
                group -> ((HuntingClubGroup) group).getHarvestPermit().getId()));

        return list.stream()
                .map(harvest -> {
                    final Optional<HuntingClubGroup> group = ofNullable(harvest.getHuntingDayOfGroup())
                            .map(GroupHuntingDay::getId)
                            .map(huntingDays::get)
                            .map(GroupHuntingDay::getGroup);

                    final HarvestPermit permit = F.firstNonNull(
                            permitMapping.apply(harvest),
                            group.map(HuntingClubGroup::getHarvestPermit).orElse(null));

                    final Organisation rhy = rhyMapping.apply(harvest);
                    final Organisation rka = rkaMap.get(rhy.getParentOrganisation().getId());
                    final DateTime approvalTime = F.firstNonNull(
                            harvest.getHarvestReportDate(),
                            F.mapNullable(harvest.getPointOfTimeApprovedToHuntingDay(), DateUtil::toDateTimeNullSafe));
                    return HarvestReportReviewDTOBuilder.builder()
                            .setId(harvest.getId())
                            .setSpecies(speciesMapping.apply(harvest).getNameLocalisation())
                            .setRkaName(rka.getNameLocalisation())
                            .setRhyName(rhy.getNameLocalisation())
                            .setPermitType(mapNullable(permit, HarvestPermit::getPermitType))
                            .setPermitNumber(mapNullable(permit, HarvestPermit::getPermitNumber))
                            .setPartnerOfficialCode(group.map(HuntingClubGroup::getParentOrganisation).map(Organisation::getOfficialCode).orElse(null))
                            .setPartner(group.map(HuntingClubGroup::getParentOrganisation).map(Organisation::getNameLocalisation).orElse(null))
                            .setPointOfTime(DateUtil.toLocalDateTimeNullSafe(harvest.getPointOfTime()))
                            .setCreationTime(DateUtil.toLocalDateTimeNullSafe(harvest.getCreationTime()))
                            .setHarvestReportDate(DateUtil.toLocalDateTimeNullSafe(approvalTime))
                            .setShooterHunterNumber(shooterMapping.apply(harvest).getHunterNumber())
                            .setAuthorHunterNumber(authorMapping.apply(harvest).getHunterNumber())
                            .setCreatedByModerator(creators.get(harvest.getCreatedByUserId()).isModeratorOrAdmin())
                            .build();
                })
                .collect(Collectors.toList());
    }

}
