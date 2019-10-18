package fi.riista.feature.gamediary.srva.method;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.util.DtoUtil;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;

public class SrvaMethodDTO extends BaseEntityDTO<Long> {

    public static @Nonnull SrvaMethodDTO create(@Nonnull final SrvaMethod entity) {
        Objects.requireNonNull(entity);

        final SrvaMethodDTO dto = new SrvaMethodDTO();
        DtoUtil.copyBaseFields(entity, dto);
        dto.setChecked(entity.isChecked());
        dto.setName(entity.getName());
        return dto;
    }

    public static @Nonnull List<SrvaMethodDTO> create(@Nonnull final List<SrvaMethod> srvaMethods) {
        return srvaMethods.stream().map(SrvaMethodDTO::create).collect(toList());
    }

    @JsonIgnore
    private Long id;

    @JsonIgnore
    private Integer rev;

    @NotNull
    private SrvaMethodEnum name;

    private boolean isChecked;

    public SrvaMethodDTO(final SrvaMethodEnum name) {
        this(name, false);
    }

    public SrvaMethodDTO(final SrvaMethodEnum name, final boolean isChecked) {
        this.name = name;
        this.isChecked = isChecked;
    }

    public SrvaMethodDTO() {
    }

    @Override
    public String toString() {
        return "SrvaMethodDTO{" +
                "id=" + id +
                ", rev=" + rev +
                ", name=" + name +
                ", isChecked=" + isChecked +
                '}';
    }

    //Accessors -->

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

    public SrvaMethodEnum getName() {
        return name;
    }

    public void setName(final SrvaMethodEnum name) {
        this.name = name;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(final boolean checked) {
        isChecked = checked;
    }

}
