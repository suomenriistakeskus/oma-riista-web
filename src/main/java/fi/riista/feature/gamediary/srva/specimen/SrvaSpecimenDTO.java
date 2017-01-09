package fi.riista.feature.gamediary.srva.specimen;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.util.DtoUtil;

import javax.annotation.Nonnull;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class SrvaSpecimenDTO extends BaseEntityDTO<Long> {

    public static @Nonnull SrvaSpecimenDTO create(@Nonnull final SrvaSpecimen entity) {
        final SrvaSpecimenDTO dto = new SrvaSpecimenDTO();
        DtoUtil.copyBaseFields(entity, dto);
        dto.setAge(entity.getAge());
        dto.setGender(entity.getGender());
        return dto;
    }

    public static @Nonnull List<SrvaSpecimenDTO> create(@Nonnull final List<SrvaSpecimen> srvaSpecimens) {
        return srvaSpecimens.stream().map(SrvaSpecimenDTO::create).collect(toList());
    }

    @JsonIgnore
    private Long id;

    @JsonIgnore
    private Integer rev;

    private GameGender gender;

    private GameAge age;

    // Accessors -->
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public GameGender getGender() {
        return gender;
    }

    public void setGender(final GameGender gender) {
        this.gender = gender;
    }

    public GameAge getAge() {
        return age;
    }

    public void setAge(GameAge age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "SrvaSpecimenDTO{" +
                "id=" + id +
                ", rev=" + rev +
                ", gender=" + gender +
                ", age=" + age +
                '}';
    }
}
