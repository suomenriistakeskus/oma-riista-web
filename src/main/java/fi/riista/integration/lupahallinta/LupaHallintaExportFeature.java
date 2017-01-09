package fi.riista.integration.lupahallinta;

import fi.riista.feature.common.entity.HasOfficialCode;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.integration.common.model.C_Address;
import fi.riista.integration.common.model.C_TypeCodeAndName;
import fi.riista.integration.lupahallinta.model.LH_Export;
import fi.riista.integration.lupahallinta.model.LH_GeoLocation;
import fi.riista.integration.lupahallinta.model.LH_Organisation;
import fi.riista.integration.lupahallinta.model.LH_Person;
import fi.riista.integration.lupahallinta.model.LH_Position;
import fi.riista.integration.lupahallinta.model.LH_PositionType;
import fi.riista.util.F;
import fi.riista.util.JaxbUtils;
import fi.riista.util.Locales;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Persistable;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
public class LupaHallintaExportFeature {

    private static final Function<Address, C_Address> toJaxbAddress = entity -> {
        if (entity == null) {
            return null;
        }

        final C_Address ret = new C_Address();
        ret.setKatuosoite(entity.getStreetAddress());
        ret.setPostinumero(entity.getPostalCode());
        ret.setPostitoimipaikka(entity.getCity());
        ret.setMaa(entity.getCountry());
        return ret;
    };

    @Value("#{environment['git.commit.id.describe']}")
    private String version;

    @Resource(name = "lupaHallintaExportMarshaller")
    private Jaxb2Marshaller jaxbMarshaller;

    @Resource
    private OrganisationRepository orgRepo;

    @Resource
    private OccupationRepository occupationRepo;

    @Resource
    private EnumLocaliser enumLocaliser;

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_LUPAHALLINTA_OCCUPATIONS')")
    public String export(final EnumSet<OccupationType> occupationTypes) {
        return JaxbUtils.marshalToString(constructExportData(occupationTypes), jaxbMarshaller);
    }

    private static Set<Integer> getAreaBoundOrganisationTypeCodes() {
        return EnumSet.allOf(OrganisationType.class).stream()
                .filter(OrganisationType::isDescendantOfRiistakeskusArea)
                .map(HasOfficialCode::getOfficialCode)
                .collect(toSet());
    }

    private static <T extends Organisation> String toXmlId(final T organisation) {
        return toXmlId(organisation.getOrganisationType().name(), organisation.getId());
    }

    private static <T extends Persistable<? extends Serializable>> String toXmlId(final T entity) {
        return toXmlId(entity.getClass().getSimpleName(), entity.getId());
    }

    private static String toXmlId(final String entityName, final Object entityId) {
        return String.format("%s-%s", entityName, entityId.toString());
    }

    private static void copyMissingRhyAddressesFromCoordinators(
            final Iterable<LH_Organisation> organisations, final Iterable<LH_Person> persons) {

        final Map<LH_Organisation, LH_Person> rhyToCoordinator = new HashMap<>();

        persons.forEach(person -> {
            person.getTehtavat().getTehtava().forEach(occupation -> {
                final LH_Organisation org = occupation.getOrganisaatio();

                if (occupation.getTyyppi() == LH_PositionType.TOIMINNANOHJAAJA &&
                        org.getTyyppi().getTyyppiKoodi() == OrganisationType.RHY.getOfficialCode()) {

                    rhyToCoordinator.put(org, person);
                }
            });
        });

        organisations.forEach(org -> {
            if (org.getOsoite() == null) {
                final LH_Person coordinator = rhyToCoordinator.get(org);

                if (coordinator != null) {
                    org.setOsoite(coordinator.getOsoite());
                }
            }
        });
    }

    private C_TypeCodeAndName getJaxbOrganisationType(final Organisation entity) {
        final OrganisationType orgType = entity.getOrganisationType();

        final C_TypeCodeAndName marshalledOrgType = new C_TypeCodeAndName();
        marshalledOrgType.setTyyppiKoodi(orgType.getOfficialCode());
        marshalledOrgType.setTyyppiNimi(enumLocaliser.getTranslation(orgType, Locales.FI));

        return marshalledOrgType;
    }

    private Map<Organisation, LH_Organisation> getJaxbOrganisationMappings(
            final Collection<Organisation> organisations) {

        final Map<Long, Organisation> orgsById = F.indexById(organisations);

        final Map<Organisation, LH_Organisation> resultMappings = F.toMapAsKeySet(organisations, entity -> {
            final LH_Organisation ret = new LH_Organisation();
            ret.setXmlId(toXmlId(entity));
            ret.setId(entity.getId());
            ret.setTyyppi(getJaxbOrganisationType(entity));
            ret.setNimiS(entity.getNameFinnish());
            ret.setNimiR(entity.getNameSwedish());
            ret.setRiistakeskusOrganisaatiokoodi(entity.getOfficialCode());
            ret.setLupaHallintaId(entity.getLhOrganisationId());

            if (entity.getGeoLocation() != null) {
                final LH_GeoLocation geoLocation = new LH_GeoLocation();
                geoLocation.setLeveys(entity.getGeoLocation().getLatitude());
                geoLocation.setPituus(entity.getGeoLocation().getLongitude());
                ret.setGeoSijainti(geoLocation);
            }

            if (entity instanceof Riistanhoitoyhdistys) {
                final Riistanhoitoyhdistys rhy = (Riistanhoitoyhdistys) entity;
                ret.setSahkopostiosoite(rhy.getEmail());
                ret.setPuhelinnumero(rhy.getPhoneNumber());
                ret.setOsoite(toJaxbAddress.apply(rhy.getAddress()));
            }

            return ret;
        });

        // Set area references on second pass.

        final Set<Integer> areaBoundOrganisationTypeCodes = getAreaBoundOrganisationTypeCodes();

        final List<LH_Organisation> areaBoundOrganisations = resultMappings.values().stream()
                .filter(org -> areaBoundOrganisationTypeCodes.contains(org.getTyyppi().getTyyppiKoodi()))
                .collect(toList());

        areaBoundOrganisations.forEach(org -> {
            final Organisation orgEntity = orgsById.get(org.getId());
            final Optional<Organisation> areaOption = orgEntity.getClosestAncestorOfType(OrganisationType.RKA);

            areaOption.map(resultMappings::get).ifPresent(org::setRiistakeskusAlue);
        });

        return resultMappings;
    }

    @Transactional(readOnly = true)
    public LH_Export constructExportData(final EnumSet<OccupationType> occupationTypes) {
        final LH_Export exportData = new LH_Export();

        exportData.setVersio(version);
        exportData.setAikaleima(DateTime.now());

        final EnumSet<OrganisationType> organisationTypes = EnumSet.noneOf(OrganisationType.class);
        occupationTypes.forEach(occupationType -> {
            organisationTypes.addAll(occupationType.getApplicableOrganisationTypes());
        });

        final List<Organisation> organisationsToExport = orgRepo.findByOrganisationType(organisationTypes);
        final Map<Organisation, LH_Organisation> orgMappings = getJaxbOrganisationMappings(organisationsToExport);

        exportData.setOrganisaatiot(new LH_Export.LH_Organisations());
        exportData.getOrganisaatiot().getOrganisaatio().addAll(orgMappings.values());

        final List<Occupation> occupations =
                occupationRepo.listNotDeletedFilteredByTypesWhileFetchingRelatedPersons(occupationTypes);

        final Map<Person, List<Occupation>> personOccupations = F.nullSafeGroupBy(occupations, Occupation::getPerson);
        final List<Person> occupiedPersons = new ArrayList<>(personOccupations.keySet());

        exportData.setHenkilot(new LH_Export.LH_Persons());
        exportData.getHenkilot().getHenkilo().addAll(occupiedPersons.stream().map(person -> {

            final LH_Person ret = new LH_Person();

            ret.setXmlId(toXmlId(person));
            ret.setId(person.getId());
            ret.setHenkilotunnus(person.getSsn());
            ret.setEtunimet(person.getFirstName());
            ret.setKutsumanimi(person.getByName());
            ret.setSukunimi(person.getLastName());
            ret.setSahkopostiosoite(person.getEmail());
            ret.setPuhelinnumero(person.getPhoneNumber());
            ret.setOsoite(toJaxbAddress.apply(person.getAddress()));
            ret.setKielikoodi(person.getLanguageCode());
            ret.setLupaHallintaId(person.getLhPersonId());
            ret.setRhyJasenyys(orgMappings.get(person.getRhyMembership()));

            ret.setTehtavat(new LH_Person.LH_Positions());
            ret.getTehtavat().getTehtava().addAll(personOccupations.get(person).stream().map(occ -> {

                final LH_Position ret1 = new LH_Position();
                ret1.setId(occ.getId());
                ret1.setTyyppi(occ.getOccupationType().getExportType());
                ret1.setAlkuPvm(occ.getBeginDate());
                ret1.setLoppuPvm(occ.getEndDate());
                ret1.setSuoritusvuosi(occ.getQualificationYear());
                ret1.setLisatieto(occ.getAdditionalInfo());
                ret1.setOrganisaatio(orgMappings.get(occ.getOrganisation()));

                if (occ.getOccupationType() == OccupationType.SRVA_YHTEYSHENKILO) {
                    ret1.setSoittojarjestys(occ.getCallOrder());
                }

                return ret1;
            })
            .collect(toList()));

            return ret;
        })
        .collect(toList()));

        copyMissingRhyAddressesFromCoordinators(
                exportData.getOrganisaatiot().getOrganisaatio(), exportData.getHenkilot().getHenkilo());

        return exportData;
    }
}
