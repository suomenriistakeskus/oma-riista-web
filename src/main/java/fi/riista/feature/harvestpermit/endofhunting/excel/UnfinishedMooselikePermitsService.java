package fi.riista.feature.harvestpermit.endofhunting.excel;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitSpeciesAmount;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.address.QAddress;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.util.LocalisedString;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static com.querydsl.core.types.dsl.Expressions.cases;
import static java.util.stream.Collectors.toList;

@Component
public class UnfinishedMooselikePermitsService {

    @Resource
    private JPQLQueryFactory queryFactory;

    @Transactional(readOnly = true)
    public List<UnfinishedMooselikePermitDTO> findUnfinishedWithinMooselikeHunting(final int huntingYear) {
        final QHarvestPermitSpeciesAmount SPECIES_AMOUNT = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;
        final QPerson ORIG_CONTACT_PERSON = new QPerson("originalContactPerson");
        final QAddress MR_ADDRESS = new QAddress("mrAddress");
        final QAddress OTHER_ADDRESS = new QAddress("otherAddress");
        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;

        final Expression<LocalisedString> speciesName = SPECIES.nameLocalisation();
        final Expression<LocalisedString> rhyName = RHY.nameLocalisation();

        final BooleanExpression mrAddressPresent = ORIG_CONTACT_PERSON.mrAddress.isNotNull();

        final Expression<String> streetAddress = cases()
                .when(mrAddressPresent).then(MR_ADDRESS.streetAddress)
                .otherwise(OTHER_ADDRESS.streetAddress);

        final Expression<String> postalCode = cases()
                .when(mrAddressPresent).then(MR_ADDRESS.postalCode)
                .otherwise(OTHER_ADDRESS.postalCode);

        final Expression<String> city = cases()
                .when(mrAddressPresent).then(MR_ADDRESS.city)
                .otherwise(OTHER_ADDRESS.city);

        final Expression<String> country = cases()
                .when(mrAddressPresent).then(MR_ADDRESS.country.coalesce(MR_ADDRESS.countryCode))
                .otherwise(OTHER_ADDRESS.country.coalesce(OTHER_ADDRESS.countryCode));

        return queryFactory
                .select(SPECIES_AMOUNT.id,
                        PERMIT.permitNumber,
                        SPECIES.officialCode,
                        speciesName,
                        PERMIT.permitHolder.code,
                        PERMIT.permitHolder.name,
                        rhyName,
                        ORIG_CONTACT_PERSON.firstName,
                        ORIG_CONTACT_PERSON.lastName,
                        ORIG_CONTACT_PERSON.hunterNumber,
                        ORIG_CONTACT_PERSON.phoneNumber,
                        ORIG_CONTACT_PERSON.email,
                        streetAddress,
                        postalCode,
                        city,
                        country)
                .from(SPECIES_AMOUNT)
                .join(SPECIES_AMOUNT.harvestPermit, PERMIT)
                .join(SPECIES_AMOUNT.gameSpecies, SPECIES)
                .join(PERMIT.originalContactPerson, ORIG_CONTACT_PERSON)
                .join(PERMIT.rhy, RHY)
                .leftJoin(ORIG_CONTACT_PERSON.mrAddress, MR_ADDRESS)
                .leftJoin(ORIG_CONTACT_PERSON.otherAddress, OTHER_ADDRESS)
                .where(SPECIES_AMOUNT.mooselikeHuntingFinished.isFalse(),
                        PERMIT.permitYear.eq(huntingYear),
                        PERMIT.isMooselikePermit())
                .orderBy(PERMIT.permitNumber.asc(), SPECIES.nameFinnish.asc())
                .fetch()
                .stream()
                .map(t -> {
                    final AddressDTO address = new AddressDTO();
                    address.setStreetAddress(t.get(streetAddress));
                    address.setPostalCode(t.get(postalCode));
                    address.setCity(t.get(city));
                    address.setCountry(t.get(country));

                    final PersonContactInfoDTO person = new PersonContactInfoDTO();
                    person.setFirstName(t.get(ORIG_CONTACT_PERSON.firstName));
                    person.setLastName(t.get(ORIG_CONTACT_PERSON.lastName));
                    person.setHunterNumber(t.get(ORIG_CONTACT_PERSON.hunterNumber));
                    person.setPhoneNumber(t.get(ORIG_CONTACT_PERSON.phoneNumber));
                    person.setEmail(t.get(ORIG_CONTACT_PERSON.email));
                    person.setAddress(address);

                    final UnfinishedMooselikePermitDTO dto = new UnfinishedMooselikePermitDTO();
                    dto.setSpeciesAmountId(t.get(SPECIES_AMOUNT.id));
                    dto.setPermitNumber(t.get(PERMIT.permitNumber));
                    dto.setGameSpeciesCode(t.get(SPECIES.officialCode));
                    dto.setSpeciesName(t.get(speciesName));
                    dto.setPermitHolderCustomerNumber(t.get(PERMIT.permitHolder.code));
                    dto.setPermitHolderName(t.get(PERMIT.permitHolder.name));
                    dto.setRhyName(t.get(rhyName));
                    dto.setOriginalContactPerson(person);
                    return dto;
                })
                .collect(toList());
    }
}
