package fi.riista.feature.permit.application.amendment;

import com.google.common.collect.Sets;
import fi.riista.config.Constants;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenRepository;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import fi.riista.feature.permit.application.DeliveryAddress;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationRepository;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.decision.PermitDecisionName;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import fi.riista.util.Locales;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Component
public class HarvestPermitAmendmentApplicationFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private HarvestPermitApplicationRepository harvestPermitApplicationRepository;

    @Resource
    private AmendmentApplicationDataRepository amendmentApplicationDataRepository;

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private HarvestSpecimenRepository harvestSpecimenRepository;

    @Resource
    private HuntingClubRepository huntingClubRepository;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Transactional
    public HarvestPermitAmendmentApplicationDTO createAmendmentApplication(
            final HarvestPermitAmendmentApplicationCreateDTO dto, final Locale locale) {

        final HarvestPermit permit =
                requireEntityService.requireHarvestPermit(dto.getOriginalPermitId(), EntityPermission.UPDATE);

        final Person contactPerson = resolveContactPerson(permit);
        final HarvestPermitApplication application = new HarvestPermitApplication();
        application.setLocale(Locales.isSwedish(locale) ? Locales.SV : Locales.FI);
        application.setDecisionLocale(application.getLocale()); // Default to application locale
        application.setHarvestPermitCategory(HarvestPermitCategory.MOOSELIKE_NEW);
        application.setApplicationName(PermitDecisionName.MOOSELIKE_AMENDMENT.getTranslation(locale));
        application.setContactPerson(contactPerson);
        application.setDeliveryAddress(DeliveryAddress.createFromPersonNullable(contactPerson));
        application.setPermitHolder(permit.getPermitHolder());
        application.setHuntingClub(permit.getHuntingClub());
        application.setRhy(permit.getRhy());

        application.setPermitPartners(Sets.newHashSet(permit.getPermitPartners()));
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        application.setApplicationYear(permit.getPermitDecision().getDecisionYear());
        application.setDeliveryByMail(activeUserService.isModeratorOrAdmin());

        final AmendmentApplicationData data = new AmendmentApplicationData();
        data.setApplication(application);
        data.setOriginalPermit(permit);

        final Harvest nonEdibleHarvest = Optional.ofNullable(dto.getNonEdibleHarvestId())
                .map((harvestRepository::getOne)).orElse(null);
        data.setNonEdibleHarvest(nonEdibleHarvest);

        final GameSpecies gameSpecies = nonEdibleHarvest != null ? nonEdibleHarvest.getSpecies() : requireSpecies(dto.getGameSpeciesCode());
        final HarvestPermitApplicationSpeciesAmount spa = HarvestPermitApplicationSpeciesAmount.createForHarvest(application, gameSpecies, 0f);

        if (nonEdibleHarvest != null) {
            data.setPointOfTime(nonEdibleHarvest.getPointOfTime());
            data.setGeoLocation(nonEdibleHarvest.getGeoLocation());

            final HuntingClub partner = nonEdibleHarvest.getHuntingClubGroup()
                    .map(HuntingClubGroup::getParentOrganisation)
                    .map(Organisation::getId)
                    .map(huntingClubRepository::getOne)
                    .orElseThrow(IllegalStateException::new);
            data.setPartner(partner);
            data.setShooter(nonEdibleHarvest.getActualShooter());

            final HarvestSpecimen specimen = harvestSpecimenRepository.findByHarvest(nonEdibleHarvest).get(0);

            spa.setSpecimenAmount(resolveAmount(specimen.getAge()));
            data.setAge(specimen.getAge());
            data.setGender(specimen.getGender());
        }

        harvestPermitApplicationRepository.saveAndFlush(application);
        harvestPermitApplicationSpeciesAmountRepository.save(spa);
        amendmentApplicationDataRepository.save(data);

        return new HarvestPermitAmendmentApplicationDTO(application, spa, data);
    }

    @Transactional(readOnly = true)
    public HarvestPermitAmendmentApplicationDTO getApplication(final long applicationId) {
        final HarvestPermitApplication application =
                requireEntityService.requireHarvestPermitApplication(applicationId, EntityPermission.READ);

        final AmendmentApplicationData data = amendmentApplicationDataRepository.getByApplication(application);
        return new HarvestPermitAmendmentApplicationDTO(application, data);
    }

    @Transactional
    public HarvestPermitAmendmentApplicationDTO updateApplication(final HarvestPermitAmendmentApplicationDTO dto,
                                                                  final Locale locale) {
        final HarvestPermitApplication application =
                requireEntityService.requireHarvestPermitApplication(dto.getId(), EntityPermission.UPDATE);

        application.setLocale(Locales.isSwedish(locale) ? Locales.SV : Locales.FI);

        final HarvestPermitApplicationSpeciesAmount spa = application.getSpeciesAmounts().get(0);
        spa.setGameSpecies(requireSpecies(dto.getGameSpeciesCode()));
        spa.setSpecimenAmount(resolveAmount(dto.getAge()));
        spa.setMooselikeDescription(dto.getDescription());

        final AmendmentApplicationData data = amendmentApplicationDataRepository.getByApplication(application);
        data.setPointOfTime(dto.getPointOfTime().toDateTime(Constants.DEFAULT_TIMEZONE));
        data.setAge(dto.getAge());
        data.setGender(dto.getGender());
        data.setGeoLocation(dto.getGeoLocation());
        data.setShooter(resolveShooter(dto));
        data.setPartner(resolvePartner(dto));

        return new HarvestPermitAmendmentApplicationDTO(application, data);
    }

    private GameSpecies requireSpecies(final Integer gameSpeciesCode) {
        return gameSpeciesService.requireByOfficialCode(gameSpeciesCode);
    }

    private static float resolveAmount(final GameAge age) {
        if (age == null) {
            return 0f;
        }
        return age == GameAge.YOUNG ? 0.5f : 1.0f;
    }

    private Person resolveShooter(final HarvestPermitAmendmentApplicationDTO dto) {
        return Optional.ofNullable(dto.getShooter())
                .map(PersonWithHunterNumberDTO::getHunterNumber)
                .flatMap(personRepository::findByHunterNumber)
                .orElse(null);
    }

    private HuntingClub resolvePartner(final HarvestPermitAmendmentApplicationDTO dto) {
        return Optional.ofNullable(dto.getPartner())
                .map(OrganisationNameDTO::getId)
                .map(huntingClubRepository::getOne)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<OrganisationNameDTO> listPartners(final long permitId) {
        final HarvestPermit originalPermit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);
        final Set<HuntingClub> partners = originalPermit.getPermitPartners();
        return F.mapNonNullsToList(partners, OrganisationNameDTO::create);
    }

    @Transactional(readOnly = true)
    public List<GameSpeciesDTO> listSpecies(final long permitId) {
        final HarvestPermit originalPermit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);
        return originalPermit.getSpeciesAmounts().stream()
                .map(HarvestPermitSpeciesAmount::getGameSpecies)
                .map(GameSpeciesDTO::create)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HarvestPermitAmendmentApplicationDTO> list(final long permitId) {
        final HarvestPermit original =
                requireEntityService.requireHarvestPermit(permitId, EntityPermission.UPDATE);

        final List<HarvestPermitApplication> amendmentApplications = harvestPermitApplicationRepository.findByOriginalPermit(original);

        final List<HarvestPermitAmendmentApplicationDTO> existingApplications = F.mapNonNullsToList(amendmentApplications, a -> {
            // n+1 but there should be only few anyway
            final AmendmentApplicationData data = amendmentApplicationDataRepository.getByApplication(a);
            return new HarvestPermitAmendmentApplicationDTO(a, data);
        });

        final List<Long> existingApplicationHarvestIds = existingApplications.stream().map(HarvestPermitAmendmentApplicationDTO::getNonEdibleHarvestId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        final List<Harvest> nonEdibles = harvestPermitApplicationRepository.findNonEdibleHarvestsByPermit(original).stream()
                .filter(h -> !existingApplicationHarvestIds.contains(h.getId()))
                .collect(Collectors.toList());

        // FIXME ehkä vähän WTF?
        final List<HarvestPermitAmendmentApplicationDTO> nonEdibleDtos = nonEdibles.stream().map(harvest -> {
            final HarvestSpecimen specimen = harvestSpecimenRepository.findByHarvest(harvest).get(0);
            return new HarvestPermitAmendmentApplicationDTO(harvest, specimen);
        }).collect(Collectors.toList());

        existingApplications.addAll(nonEdibleDtos);

        return existingApplications;
    }

    private Person resolveContactPerson(final HarvestPermit permit) {
        if (activeUserService.isModeratorOrAdmin()) {
            // When moderator fills in the amendment application, use the original contact person
            return permit.getOriginalContactPerson();
        }
        return activeUserService.requireActivePerson();
    }
}
