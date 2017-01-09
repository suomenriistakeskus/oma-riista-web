package fi.riista.integration.lupahallinta.parser;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.gis.hta.GISHirvitalousalueRepository;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount.RestrictionType;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.feature.huntingclub.register.RegisterHuntingClubException;
import fi.riista.feature.huntingclub.register.RegisterHuntingClubService;
import fi.riista.feature.organization.person.PersonLookupService;
import fi.riista.feature.organization.rhy.MergedRhyMapping;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import javaslang.Tuple;
import javaslang.Tuple3;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Component
public class PermitCSVImporter {

    private static final Logger LOG = LoggerFactory.getLogger(PermitCSVImporter.class);

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private PersonLookupService personLookupService;

    @Resource
    private RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepository;

    @Resource
    private RegisterHuntingClubService registerHuntingClubService;

    @Resource
    private HuntingClubGroupRepository huntingClubGroupRepository;

    @Resource
    private GameSpeciesRepository gameSpeciesRepository;

    @Resource
    private GISHirvitalousalueRepository hirvitalousalueRepository;

    public Tuple3<HarvestPermit, List<String>, PermitCSVLine> process(String[] line) {
        final List<String> errors = Lists.newLinkedList();
        final PermitCSVLine csvLine = new PermitCSVLineParser(errors).parse(line);

        if (!errors.isEmpty()) {
            // Error in format -> abort
            return Tuple.of(null, errors, csvLine);
        }

        final HarvestPermit harvestPermit = process(csvLine, errors);

        return Tuple.of(harvestPermit, errors, csvLine);
    }

    private HarvestPermit process(PermitCSVLine csvLine, List<String> errors) {
        Objects.requireNonNull(csvLine);
        Objects.requireNonNull(errors);

        final Person contactPerson = findContactPerson(csvLine, errors);
        final Riistanhoitoyhdistys rhy = findRHY(csvLine, errors);
        final HarvestPermit originalPermit = findOriginalPermit(csvLine);
        final HuntingClub permitHolder = originalPermit != null
                ? originalPermit.getPermitHolder()
                : findPermitHolder(csvLine, errors);
        final Set<HuntingClub> permitPartners = originalPermit != null
                ? new HashSet<>(originalPermit.getPermitPartners())
                : findPermitPartners(csvLine, errors);
        final GISHirvitalousalue mooseArea = findMooseArea(csvLine, errors);
        final String printingUrl = findPrintingUrl(csvLine, errors);
        final Set<Riistanhoitoyhdistys> relatedRhys = findRelatedRhys(csvLine, errors);
        final Integer permitAreaSize = findPermitAreaSize(csvLine, errors);

        if (!errors.isEmpty()) {
            return null;
        }

        if (skipOriginalPermitNotFound(csvLine, originalPermit)) {
            LOG.info("Skipping permit:{} type:{} because original permit:{} is not found",
                    csvLine.getPermitNumber(), csvLine.getPermitTypeCode(), csvLine.getOriginalPermitNumber());
            return null;
        }

        final HarvestPermit existingPermit = harvestPermitRepository.findByPermitNumber(csvLine.getPermitNumber());
        final HarvestPermit harvestPermit = existingPermit != null ? existingPermit : new HarvestPermit();

        if (harvestPermit.isNew() && csvLine.getSpeciesAmounts().isEmpty()) {
            // Skip non-existing permit without amounts
            return null;
        }

        updatePartners(errors, harvestPermit, permitPartners);
        if (!errors.isEmpty()) {
            return null;
        }

        harvestPermit.setOriginalContactPerson(contactPerson);
        harvestPermit.setPermitHolder(permitHolder);
        harvestPermit.setPermitNumber(csvLine.getPermitNumber());
        harvestPermit.setPermitTypeCode(csvLine.getPermitTypeCode());
        harvestPermit.setPermitType(csvLine.getPermitTypeName());
        harvestPermit.setHarvestsAsList(HarvestPermit.checkIsHarvestsAsList(csvLine.getPermitTypeCode()));
        harvestPermit.setRhy(rhy);
        harvestPermit.setOriginalPermit(originalPermit);
        harvestPermit.setPrintingUrl(printingUrl);
        harvestPermit.setMooseArea(mooseArea);
        harvestPermit.setRelatedRhys(relatedRhys);
        harvestPermit.setPermitAreaSize(permitAreaSize);

        updateSpeciesAmounts(csvLine, errors, harvestPermit);

        return harvestPermit;
    }

    private void updateSpeciesAmounts(PermitCSVLine csvLine, List<String> errors, HarvestPermit harvestPermit) {
        final List<HarvestPermitSpeciesAmount> copyOfSpeciesAmounts = harvestPermit.isMooselikePermitType()
                ? new ArrayList<>(harvestPermit.getSpeciesAmounts())
                : Collections.emptyList();

        if (!harvestPermit.isMooselikePermitType()) {
            harvestPermit.getSpeciesAmounts().clear();
        }

        // update existing and add new ones
        for (final PermitCSVLine.SpeciesAmount csvAmount : csvLine.getSpeciesAmounts()) {
            final GameSpecies gameSpecies = findSpecies(csvAmount, errors);

            if (gameSpecies != null) {
                final HarvestPermitSpeciesAmount hpsa = findExistingOrCreateNew(copyOfSpeciesAmounts, gameSpecies);

                final RestrictionType restrictionType = RestrictionType.ofNullable(csvAmount.getRestrictionType());
                final Float restrictionAmount = Optional.ofNullable(csvAmount.getRestrictionAmount()).map(BigDecimal::floatValue).orElse(null);

                hpsa.setHarvestPermit(harvestPermit);
                hpsa.setGameSpecies(gameSpecies);
                hpsa.setAmount(csvAmount.getAmount().floatValue());
                hpsa.setRestrictionType(restrictionType);
                hpsa.setRestrictionAmount(restrictionAmount);
                hpsa.setBeginDate(csvAmount.getBeginDate());
                hpsa.setEndDate(csvAmount.getEndDate());
                hpsa.setCreditorReference(CreditorReference.fromNullable(csvAmount.getReferenceNumber()));
                hpsa.setBeginDate2(csvAmount.getBeginDate2());
                hpsa.setEndDate2(csvAmount.getEndDate2());

                if (hpsa.isNew()) {
                    harvestPermit.getSpeciesAmounts().add(hpsa);
                }
            }
        }
        // remove if needed
        for (HarvestPermitSpeciesAmount existing : copyOfSpeciesAmounts) {
            Optional<PermitCSVLine.SpeciesAmount> csvAmount = find(csvLine.getSpeciesAmounts(), existing);
            if (!csvAmount.isPresent() && !existing.isNew()) {
                harvestPermit.getSpeciesAmounts().remove(existing);
            }
        }
    }

    private static Optional<PermitCSVLine.SpeciesAmount> find(final List<PermitCSVLine.SpeciesAmount> csvSpeciesAmounts,
                                                              final HarvestPermitSpeciesAmount existing) {
        return csvSpeciesAmounts.stream()
                .filter(csv -> csv.getSpeciesOfficialCode() == existing.getGameSpecies().getOfficialCode())
                .findFirst();
    }

    private static HarvestPermitSpeciesAmount findExistingOrCreateNew(final List<HarvestPermitSpeciesAmount> hpsas,
                                                                      final GameSpecies gameSpecies) {
        return hpsas.stream()
                .filter(hpsa -> hpsa.getGameSpecies().getOfficialCode() == gameSpecies.getOfficialCode())
                .findFirst()
                .orElseGet(HarvestPermitSpeciesAmount::new);
    }

    private void updatePartners(List<String> errors, HarvestPermit permit, Set<HuntingClub> newPartners) {
        if (permit.isNew()) {
            permit.getPermitPartners().addAll(newPartners);
            return;
        }
        final Set<HuntingClub> originalPartners = permit.getPermitPartners();
        final Set<HuntingClub> removed = difference(originalPartners, newPartners);

        final List<HuntingClubGroup> groups = removed.isEmpty()
                ? Collections.emptyList()
                : huntingClubGroupRepository.findByPermitAndClubs(permit, removed);
        if (groups.isEmpty()) {
            originalPartners.removeAll(removed);
            originalPartners.addAll(difference(newPartners, originalPartners));
        } else {
            groups.stream()
                    .map(g -> g.getParentOrganisation())
                    .distinct()
                    .forEach(c -> errors.add("Osakas yritetään poistaa mutta osakas on lupaan liitettyjä ryhmiä, osakas:" + c.getOfficialCode()));
        }
    }

    private static <T> Set<T> difference(Set<T> a, Set<T> b) {
        return Sets.difference(a, b).immutableCopy();
    }

    private static boolean skipOriginalPermitNotFound(PermitCSVLine csvLine, HarvestPermit originalPermit) {
        return originalPermit == null && HarvestPermit.isAmendmentPermitTypeCode(csvLine.getPermitTypeCode());
    }

    private HarvestPermit findOriginalPermit(PermitCSVLine csvLine) {
        final String originalPermitNumber = csvLine.getOriginalPermitNumber();
        if (Strings.isNullOrEmpty(originalPermitNumber)) {
            return null;
        }
        return harvestPermitRepository.findByPermitNumber(originalPermitNumber);
    }

    private GameSpecies findSpecies(PermitCSVLine.SpeciesAmount csvAmount, List<String> errors) {
        Objects.requireNonNull(csvAmount.getSpeciesOfficialCode());

        return gameSpeciesRepository
                .findByOfficialCode(csvAmount.getSpeciesOfficialCode())
                .orElseGet(() -> {
                    errors.add("Eläinlajia ei löydy:" + csvAmount.getSpeciesOfficialCode());
                    return null;
                });
    }

    private Person findContactPerson(PermitCSVLine csvLine, List<String> errors) {
        Preconditions.checkArgument(StringUtils.isNotBlank(csvLine.getContactPersonSsn()));

        return personLookupService.findBySsnFallbackVtj(csvLine.getContactPersonSsn())
                .orElseGet(() -> {
                    errors.add("Henkilöä ei löydy, HETU:" + csvLine.getContactPersonSsn());
                    return null;
                });
    }

    private Riistanhoitoyhdistys findRHY(PermitCSVLine csvLine, List<String> errors) {
        final String rhyOfficialCode = csvLine.getRhyOfficialCode();
        Preconditions.checkArgument(StringUtils.isNotBlank(rhyOfficialCode));

        return findRhyByOfficialCode(errors, rhyOfficialCode);
    }

    private Set<Riistanhoitoyhdistys> findRelatedRhys(PermitCSVLine csvLine, List<String> errors) {
        return csvLine.getRelatedRhys().stream()
                .map(c -> findRhyByOfficialCode(errors, c))
                .collect(toSet());
    }

    private Riistanhoitoyhdistys findRhyByOfficialCode(List<String> errors, String rhyOfficialCode) {
        Riistanhoitoyhdistys rhy = riistanhoitoyhdistysRepository.findByOfficialCode(rhyOfficialCode);

        if (rhy == null && MergedRhyMapping.isMappedToNewRhy(rhyOfficialCode)) {
            final String newRhyCode = MergedRhyMapping.getNewRhyCode(rhyOfficialCode);
            rhy = riistanhoitoyhdistysRepository.findByOfficialCode(newRhyCode);
        }

        if (rhy == null) {
            errors.add("RHY ei löydy:" + rhyOfficialCode);
        }

        return rhy;
    }

    private HuntingClub findPermitHolder(PermitCSVLine csvLine, List<String> errors) {
        if (!HarvestPermit.checkShouldResolvePermitHolder(csvLine.getPermitTypeCode())) {
            return null;
        }
        return tryFindOrCreateClub(errors, csvLine.getPermitHolder(), "Luvansaajaa ei löydy:");
    }


    private Set<HuntingClub> findPermitPartners(PermitCSVLine csvLine, List<String> errors) {
        if (!HarvestPermit.checkShouldResolvePermitPartners(csvLine.getPermitTypeCode())) {
            return Collections.emptySet();
        }
        return csvLine.getPermitPartners().stream()
                .map(officialCode -> tryFindOrCreateClub(errors, officialCode, "Lupaosakasta ei löydy:"))
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    private HuntingClub tryFindOrCreateClub(List<String> errors, String officialCode, String errMsgPrefix) {

        try {
            final HuntingClub club = registerHuntingClubService.findExistingOrCreate(officialCode);
            if (club == null) {
                errors.add(errMsgPrefix + officialCode);
            }
            return club;
        } catch (RegisterHuntingClubException hcre) {
            errors.add("Lupahallinnassa ei seuralle ole merkitty RHY:tä. Seura:" + officialCode);
            return null;
        }
    }

    private GISHirvitalousalue findMooseArea(PermitCSVLine csvLine, List<String> errors) {
        if (HarvestPermit.isMooselikePermitTypeCode(csvLine.getPermitTypeCode())) {
            final GISHirvitalousalue mooseArea = resolveMooseArea(csvLine.getHtaNumber());
            if (mooseArea == null) {
                errors.add("Lupatyypille vaaditaan hirvitaloustalue mutta ei löydy:" + csvLine.getHtaNumber());
            }
            return mooseArea;
        }
        return null;
    }

    private GISHirvitalousalue resolveMooseArea(String htaNumber) {
        if (StringUtils.isBlank(htaNumber)) {
            return null;
        }
        return hirvitalousalueRepository.findByNumber(htaNumber);
    }

    private static String findPrintingUrl(PermitCSVLine csvLine, List<String> errors) {
        final String url = csvLine.getPrintingUrl();
        if (HarvestPermit.isMooselikePermitTypeCode(csvLine.getPermitTypeCode()) && !isValidUrl(url)) {
            errors.add("Lupatyypille vaaditaan päätöksen URL, annettu arvo ei kelpaa:" + url);
        }
        return url;
    }

    private static boolean isValidUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private static Integer findPermitAreaSize(final PermitCSVLine csvLine, final List<String> errors) {
        if (!HarvestPermit.isMooselikePermitTypeCode(csvLine.getPermitTypeCode())) {
            return null;
        }
        final String permitAreaStr = csvLine.getPermitAreaSize();
        final Integer size = parseAreaSize(permitAreaStr);
        if (size == null) {
            errors.add("Lupatyypille vaaditaan alueen pinta-ala, annettu arvo ei kelpaa:" + permitAreaStr);
        }
        return size;
    }

    private static Integer parseAreaSize(final String permitAreaStr) {
        try {
            return Optional.ofNullable(permitAreaStr)
                    .map(String::trim)
                    .map(Strings::emptyToNull)
                    .map(Integer::parseInt)
                    .filter(x -> x > 0)
                    .orElse(null);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }
}
