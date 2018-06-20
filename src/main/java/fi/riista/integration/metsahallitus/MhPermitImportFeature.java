package fi.riista.integration.metsahallitus;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.metsahallitus.permit.MhPermit;
import fi.riista.feature.metsahallitus.permit.MhPermitRepository;
import fi.riista.feature.metsahallitus.permit.QMhPermit;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.util.F;
import fi.riista.validation.Validators;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static fi.riista.util.Collect.nullSafeGroupingBy;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class MhPermitImportFeature {

    public static class Result {
        public final Set<Long> ids;
        public final Map<String, Set<String>> errors;

        public Result(final Set<Long> ids, final Map<String, Set<String>> errors) {
            this.ids = ids;
            this.errors = errors;
        }

        public Result merge(Result other) {
            final Set<Long> newIds = new HashSet<>();
            newIds.addAll(this.ids);
            newIds.addAll(other.ids);

            final Map<String, Set<String>> newErrors = new HashMap<>();
            newErrors.putAll(this.errors);
            other.errors.forEach((key, value) -> newErrors.computeIfAbsent(key, ignored -> new HashSet<>()).addAll(value));
            return new Result(newIds, newErrors);
        }

        public static Result empty() {
            return new Result(emptySet(), emptyMap());
        }
    }

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("dd.MM.yyyy");
    public static final int BATCH_SIZE = 16384;

    @Resource
    private MhPermitRepository mhPermitRepository;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private JPQLQueryFactory queryFactory;

    @Transactional
    public Result importPermits(final List<MhPermitImportDTO> origDtos) {
        final Map<String, Set<String>> allErrors = new HashMap<>();
        // report some errors first
        origDtos.stream().forEach(dto -> {
                    if (!Validators.isValidSsn(dto.getHenkiloTunnus())) {
                        getMhPermitErrorList(allErrors, dto)
                                .add("virheellinen arvo kentässä henkiloTunnus:" + dto.getHenkiloTunnus());
                    }
                    if (!Validators.isValidHunterNumber(dto.getMetsastajaNumero())) {
                        getMhPermitErrorList(allErrors, dto)
                                .add("virheellinen arvo kentässä metsastajaNumero:" + dto.getMetsastajaNumero());
                    }
                    if (isBlank(dto.getHenkiloTunnus()) && isBlank(dto.getMetsastajaNumero())) {
                        getMhPermitErrorList(allErrors, dto)
                                .add("virheellinen arvo kentässä henkiloTunnus ja metsastajaNumero, kumpikin ovat tyhjät");
                    }
                    if (isBlank(dto.getTilauksenTila())) {
                        getMhPermitErrorList(allErrors, dto)
                                .add("virheellinen arvo kentässä tilauksenTila:" + dto.getTilauksenTila());
                    }
                }
        );

        final List<MhPermitImportDTO> dtos = origDtos.stream()
                .filter(p -> MhPermitImportDTO.PAID.equals(p.getTilauksenTila()))
                .collect(toList());

        final Map<String, Person> ssnToPersonMapping = getSsnToPersonMap(dtos);
        final Map<String, Person> hunterNumberToPersonMapping = getHunterNumberToPersonMap(dtos);
        final Map<String, List<MhPermit>> permitIdentifierToPermitMapping = getPermitIdentifierToPermitMap(dtos);

        final List<MhPermit> savedOrModifierPermits = new LinkedList<>();

        dtos.stream().forEach(dto -> {
            final Optional<Person> personMaybe = findPerson(dto, ssnToPersonMapping, hunterNumberToPersonMapping);
            // if we don't have person in database already, skip - do not search VTJ for person
            // if permit state is not PAID, skip - there is no need for cancelled permits
            if (personMaybe.isPresent() && dto.getTilauksenTila().equals(MhPermitImportDTO.PAID)) {
                final Tuple2<MhPermit, List<String>> res = createEntity(dto, personMaybe.get(), permitIdentifierToPermitMapping);
                final MhPermit permit = res._1;
                final List<String> errors = res._2;
                if (errors.isEmpty() && permit != null) {
                    savedOrModifierPermits.add(permit);
                } else {
                    getMhPermitErrorList(allErrors, dto).addAll(errors);
                }
            }
        });

        mhPermitRepository.save(savedOrModifierPermits);
        return new Result(F.getUniqueIds(savedOrModifierPermits), allErrors);
    }

    @Transactional
    public void deleteExcept(final Set<Long> idsToKeep) {
        final QMhPermit MHPERMIT = QMhPermit.mhPermit;
        final Set<Long> all = Sets.newHashSet(queryFactory.select(MHPERMIT.id).from(MHPERMIT).fetch());
        final Set<Long> toDelete = Sets.difference(all, idsToKeep);
        Lists.partition(new LinkedList<>(toDelete), BATCH_SIZE)
                .forEach(toDeleteBatch -> queryFactory.delete(MHPERMIT).where(MHPERMIT.id.in(toDeleteBatch)).execute());
    }

    private static Set<String> getMhPermitErrorList(final Map<String, Set<String>> allErrors,
                                                    final MhPermitImportDTO dto) {

        return allErrors.computeIfAbsent(dto.getLuvanTunnus(), ignored -> new HashSet<>());
    }

    private Map<String, List<MhPermit>> getPermitIdentifierToPermitMap(final List<MhPermitImportDTO> dtos) {
        final List<String> permitIdentifiers = dtos.stream().map(MhPermitImportDTO::getLuvanTunnus).collect(toList());
        final QMhPermit PERMIT = QMhPermit.mhPermit;
        final List<MhPermit> mhPermits = transformInBatches(BATCH_SIZE, permitIdentifiers,
                s -> mhPermitRepository.findAllAsList(PERMIT.permitIdentifier.in(s)));
        return mhPermits.stream().collect(nullSafeGroupingBy(MhPermit::getPermitIdentifier));
    }

    private Map<String, Person> getSsnToPersonMap(final List<MhPermitImportDTO> dtos) {
        final Set<String> mhSsns = dtos.stream().map(MhPermitImportDTO::getHenkiloTunnus)
                .filter(StringUtils::isNotBlank)
                .filter(Validators::isValidSsn)
                .map(String::toUpperCase)
                .collect(toSet());

        final QPerson PERSON = QPerson.person;
        final List<Person> persons = transformInBatches(BATCH_SIZE, Lists.newArrayList(mhSsns),
                s -> personRepository.findAllAsList(PERSON.ssn.in(s)));
        return F.index(persons, Person::getSsn);
    }

    private Map<String, Person> getHunterNumberToPersonMap(List<MhPermitImportDTO> dtos) {
        final Set<String> mhHunterNumbers = dtos.stream().map(MhPermitImportDTO::getMetsastajaNumero)
                .filter(StringUtils::isNotBlank)
                .filter(Validators::isValidHunterNumber)
                .collect(toSet());

        final QPerson PERSON = QPerson.person;
        final List<Person> persons = transformInBatches(BATCH_SIZE, Lists.newArrayList(mhHunterNumbers),
                s -> personRepository.findAllAsList(PERSON.hunterNumber.in(s)));
        return F.index(persons, Person::getHunterNumber);

    }

    private static <T, A> List<T> transformInBatches(final int batchSize,
                                                     final List<A> input,
                                                     final Function<List<A>, List<T>> transformer) {
        final List<T> all = new LinkedList<>();
        final List<List<A>> partitions = Lists.partition(input, batchSize);
        partitions.forEach(s -> all.addAll(transformer.apply(s)));
        return all;
    }

    private static Optional<Person> findPerson(final MhPermitImportDTO dto,
                                               final Map<String, Person> ssnToPersonMapping,
                                               final Map<String, Person> hunterNumberToPersonMapping) {

        final String hunterNumber = dto.getMetsastajaNumero();
        final String ssn = dto.getHenkiloTunnus();
        return F.optionalFromSuppliers(
                () -> Optional.ofNullable(hunterNumberToPersonMapping.get(hunterNumber)),
                () -> Optional.ofNullable(ssnToPersonMapping.get(ssn))
        );
    }

    private static Tuple2<MhPermit, List<String>> createEntity(final MhPermitImportDTO dto,
                                                               final Person person,
                                                               final Map<String, List<MhPermit>> permitIdentifierToPermitMapping) {

        final List<String> errors = new LinkedList<>();
        final MhPermit mhPermit = findExistingOrCreate(dto.getLuvanTunnus(), person, permitIdentifierToPermitMapping);
        mhPermit.setPerson(person);

        mhPermit.setPermitIdentifier(ensureNotBlank(dto.getLuvanTunnus(), "luvanTunnus", errors));

        mhPermit.setPermitType(ensureNotBlank(dto.getLupaTyyppi(), "lupatyyppi", errors));
        mhPermit.setPermitTypeSwedish(emptyFallBackTo(dto.getLupaTyyppiSE(), mhPermit.getPermitType()));
        mhPermit.setPermitTypeEnglish(emptyFallBackTo(dto.getLupaTyyppiEN(), mhPermit.getPermitType()));

        mhPermit.setPermitName(ensureNotBlank(dto.getLuvanNimi(), "luvanNimi", errors));
        mhPermit.setPermitNameSwedish(emptyFallBackTo(dto.getLuvanNimiSE(), mhPermit.getPermitName()));
        mhPermit.setPermitNameEnglish(emptyFallBackTo(dto.getLuvanNimiEN(), mhPermit.getPermitName()));

        mhPermit.setAreaNumber(ensureNotBlank(dto.getAlueNro(), "alueNro", errors));
        mhPermit.setAreaName(ensureNotBlank(dto.getAlueenNimi(), "alueenNimi", errors));
        mhPermit.setAreaNameSwedish(emptyFallBackTo(dto.getAlueenNimiSE(), mhPermit.getAreaName()));
        mhPermit.setAreaNameEnglish(emptyFallBackTo(dto.getAlueenNimiEN(), mhPermit.getAreaName()));

        final Tuple2<LocalDate, LocalDate> dates = parseDates(dto, errors);
        mhPermit.setBeginDate(dates._1);
        mhPermit.setEndDate(dates._2);

        mhPermit.setUrl(checkUrl(dto.getUrl(), errors));

        return Tuple.of(mhPermit, errors);
    }

    private static MhPermit findExistingOrCreate(final String luvanTunnus,
                                                 final Person person,
                                                 final Map<String, List<MhPermit>> permitIdentifierToPermitMapping) {

        final List<MhPermit> mhPermits = permitIdentifierToPermitMapping.computeIfAbsent(luvanTunnus, p -> new LinkedList<>());
        final MhPermit permit = mhPermits
                .stream()
                .filter(p -> p.getPerson().equals(person))
                .findAny()
                .orElse(null);
        if (permit == null) {
            return new MhPermit();
        }
        return permit;
    }

    private static String emptyFallBackTo(String candidate, String fallBack) {
        return isBlank(candidate) ? fallBack : candidate;
    }

    private static String ensureNotBlank(final String value, final String fieldName, final List<String> errors) {
        if (isBlank(value)) {
            errors.add("tyhjä arvo kentässä:" + fieldName);
        }
        return value;
    }

    private static Tuple2<LocalDate, LocalDate> parseDates(final MhPermitImportDTO dto, final List<String> errors) {
        final String alkuPvm = dto.getAlkuPvm();
        final String loppuPvm = dto.getLoppuPvm();
        final LocalDate begin = parseDate(alkuPvm, "alkuPvm", errors);
        final LocalDate end = parseDate(loppuPvm, "loppuPvm", errors);
        if (begin != null && end != null && begin.isAfter(end)) {
            errors.add("alkuPvm on loppuPvm jälkeen, alkuPvm:" + alkuPvm + " loppuPvm:" + loppuPvm);
        }
        return Tuple.of(begin, end);
    }

    private static LocalDate parseDate(final String value, final String fieldName, final List<String> errors) {
        if (isBlank(value)) {
            errors.add("tyhjä arvo kentässä:" + fieldName);
        }
        try {
            return DATE_FORMAT.parseLocalDate(value);
        } catch (Exception e) {
            errors.add("virheellinen päivämäärä " + fieldName + " arvo:" + value);
            return null;
        }
    }

    private static String checkUrl(final String url, final List<String> errors) {
        if (StringUtils.isNotBlank(url)) {
            try {
                return new URL(url).toString();
            } catch (MalformedURLException e) {
                errors.add("virheellinen url: " + url);
            }
        }
        return url;
    }
}
