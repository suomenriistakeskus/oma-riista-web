package fi.riista.feature.huntingclub.area.print;

import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

public class HuntingClubAreaPrintRequestDTO {
    public enum PaperSize {
        A3, A4
    }

    public enum PaperOrientation {
        PORTRAIT, LANDSCAPE;

        public String asLetter() {
            return this == PORTRAIT ? "P" : "L";
        }
    }

    @NotNull
    private PaperSize paperSize;

    @NotNull
    private PaperOrientation paperOrientation;

    @NotNull @Range(min = 75, max = 600)
    private Integer paperDpi;

    public Integer getPaperDpi() {
        return paperDpi;
    }

    public void setPaperDpi(final Integer paperDpi) {
        this.paperDpi = paperDpi;
    }

    public PaperSize getPaperSize() {
        return paperSize;
    }

    public void setPaperSize(final PaperSize paperSize) {
        this.paperSize = paperSize;
    }

    public PaperOrientation getPaperOrientation() {
        return paperOrientation;
    }

    public void setPaperOrientation(final PaperOrientation paperOrientation) {
        this.paperOrientation = paperOrientation;
    }
}
