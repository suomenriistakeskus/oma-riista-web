package fi.riista.integration.mapexport;

import com.google.common.base.MoreObjects;

import javax.validation.constraints.NotNull;

public class MapPdfParameters {
    public enum PaperSize {
        A0(0.841, 1.189),
        A1(0.594, 0.841),
        A2(0.42, 0.594),
        A3(0.297, 0.42),
        A4(0.21, 0.297);

        private final double width;
        private final double height;

        PaperSize(final double width, final double height) {
            this.width = width;
            this.height = height;
        }

        public double getWidth(final PaperOrientation orientation) {
            return orientation == PaperOrientation.PORTRAIT ? width : height;
        }

        public double getHeight(final PaperOrientation orientation) {
            return orientation == PaperOrientation.PORTRAIT ? height : width;
        }
    }

    public enum PaperOrientation {
        PORTRAIT, LANDSCAPE;

        public String asLetter() {
            return this == PORTRAIT ? "P" : "L";
        }
    }

    public enum Overlay {
        NONE,
        VALTIONMAA
    }

    @NotNull
    private PaperSize paperSize;

    @NotNull
    private PaperOrientation paperOrientation;

    @NotNull
    private MapPdfBasemap layer;

    private Overlay overlay;

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

    public MapPdfBasemap getLayer() {
        return layer;
    }

    public void setLayer(final MapPdfBasemap layer) {
        this.layer = layer;
    }

    public Overlay getOverlay() {
        return overlay;
    }

    public void setOverlay(final Overlay overlay) {
        this.overlay = overlay;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("paperSize", paperSize)
                .add("paperOrientation", paperOrientation)
                .add("layer", layer)
                .add("overlay", overlay)
                .toString();
    }
}
