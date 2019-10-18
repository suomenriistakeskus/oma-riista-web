package fi.riista.feature.permitplanning.hirvityvitys.dto;

import java.util.List;

public class JyvitysExcelRhyDTO {

    private final String name;

    private final List<JyvitysExcelVerotuslohkoDTO> verotuslohkoDTOs;

    public JyvitysExcelRhyDTO(final String name, final List<JyvitysExcelVerotuslohkoDTO> verotuslohkoDTOs) {
        this.name = name;
        this.verotuslohkoDTOs = verotuslohkoDTOs;
    }

    public String getName() {
        return name;
    }

    public List<JyvitysExcelVerotuslohkoDTO> getVerotuslohkoDTOs() {
        return verotuslohkoDTOs;
    }
}
