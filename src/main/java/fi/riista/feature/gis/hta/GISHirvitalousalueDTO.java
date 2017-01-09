package fi.riista.feature.gis.hta;

import fi.riista.util.Localiser;
import org.hibernate.validator.constraints.SafeHtml;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class GISHirvitalousalueDTO {
    public static List<GISHirvitalousalueDTO> create(List<GISHirvitalousalue> entities) {
        return entities.stream().map(GISHirvitalousalueDTO::create).collect(toList());
    }

    public static GISHirvitalousalueDTO create(GISHirvitalousalue entity) {
        if (entity == null) {
            return null;
        }
        final GISHirvitalousalueDTO dto = new GISHirvitalousalueDTO();
        dto.setName(Localiser.select(entity.getNameLocalisation()));
        dto.setNumber(entity.getNumber());

        return dto;
    }

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String name;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String number;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
