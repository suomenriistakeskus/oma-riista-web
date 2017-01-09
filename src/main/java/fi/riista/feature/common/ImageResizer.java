package fi.riista.feature.common;

import org.imgscalr.Scalr;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.awt.Color.WHITE;

@Component
public class ImageResizer {

    private static final int MAX_IMAGE_SIZE = 1024;

    private static final Scalr.Method QUALITY_SETTING = Scalr.Method.ULTRA_QUALITY;

    public byte[] toJpgDownscaleToSize(InputStream imageData) throws IOException {
        final BufferedImage image = ImageIO.read(imageData);
        // do not resize image up
        if (image.getWidth() <= MAX_IMAGE_SIZE && image.getHeight() <= MAX_IMAGE_SIZE) {
            return toJPG(image);
        }
        return toJPG(resize(image, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE, true));
    }

    public byte[] resize(@Nonnull final byte[] photoData,
                         final int width,
                         final int height,
                         final boolean keepProportions) throws IOException {
        final BufferedImage inputImage = ImageIO.read(new ByteArrayInputStream(photoData));
        return toJPG(resize(inputImage, width, height, keepProportions));
    }

    private static BufferedImage resize(BufferedImage image, int width, int height, boolean keepProportions) {
        if (keepProportions) {
            return resizeProportional(image, width, height);
        }
        return resizeAndCropCenter(image, width, height);
    }

    private static BufferedImage resizeProportional(BufferedImage image, int width, int height) {
        final boolean fitToHeight = ((double) image.getWidth()) / ((double) image.getHeight()) < ((double) width) / ((double) height);
        final Scalr.Mode mode = fitToHeight ? Scalr.Mode.FIT_TO_HEIGHT : Scalr.Mode.FIT_TO_WIDTH;
        return Scalr.resize(image, QUALITY_SETTING, mode, Math.min(width, height), Math.min(width, height));
    }

    private static BufferedImage resizeAndCropCenter(final BufferedImage image, final int width, final int height) {
        final boolean verticalCrop = ((double) image.getWidth()) / ((double) image.getHeight()) < ((double) width) / ((double) height);

        final Scalr.Mode mode = verticalCrop ? Scalr.Mode.FIT_TO_WIDTH : Scalr.Mode.FIT_TO_HEIGHT;
        final BufferedImage resized = Scalr.resize(image, QUALITY_SETTING, mode, width, height);

        final int dx = verticalCrop ? 0 : (resized.getWidth() - width) / 2;
        final int dy = !verticalCrop ? 0 : (resized.getHeight() - height) / 2;

        return Scalr.crop(resized, dx, dy, width, height);
    }

    public static byte[] toJPG(BufferedImage originalImage) throws IOException {
        BufferedImage image = removeAlpha(originalImage);

        ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();

        ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
        jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpgWriteParam.setCompressionQuality(0.9f);

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream();
             final MemoryCacheImageOutputStream mcios = new MemoryCacheImageOutputStream(bos)) {

            jpgWriter.setOutput(mcios);

            IIOImage outputImage = new IIOImage(image, null, null);
            jpgWriter.write(null, outputImage, jpgWriteParam);
            jpgWriter.dispose();

            bos.flush();
            return bos.toByteArray();
        }
    }

    private static BufferedImage removeAlpha(BufferedImage originalImage) {
        int w = originalImage.getWidth();
        int h = originalImage.getHeight();
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        image.createGraphics().drawImage(originalImage, 0, 0, w, h, WHITE, null);
        return image;
    }
}
