package fi.riista.feature.huntingclub.permit;

import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;

import javaslang.Tuple;
import javaslang.Tuple2;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

public class MooselikeHuntingYearDTO {

    public static List<MooselikeHuntingYearDTO> create(final Stream<HarvestPermitSpeciesAmount> spas) {
        return spas
                .flatMap(spa -> spa.collectClosedRangeHuntingYears().mapToObj(y -> Tuple.of(y, spa.getGameSpecies())))
                .distinct()
                .collect(groupingBy(Tuple2::_1, mapping(Tuple2::_2, toList())))
                .entrySet().stream()
                .map(e -> new MooselikeHuntingYearDTO(e.getKey(), GameSpeciesDTO.transformList(e.getValue())))
                .sorted(comparing(MooselikeHuntingYearDTO::getYear))
                .collect(toList());
    }

    private final int year;
    private final List<GameSpeciesDTO> species;

    public MooselikeHuntingYearDTO(final int year, final List<GameSpeciesDTO> species) {
        this.year = year;
        this.species = species;
    }

    public int getYear() {
        return year;
    }

    public List<GameSpeciesDTO> getSpecies() {
        return species;
    }
}
