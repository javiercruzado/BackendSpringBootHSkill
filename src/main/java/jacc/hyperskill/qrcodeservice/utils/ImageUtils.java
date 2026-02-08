package jacc.hyperskill.qrcodeservice.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

public final class ImageUtils {

    public static BufferedImage createImage(int width, int height, Color background, int imageType) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be > 0");
        }

        BufferedImage image = new BufferedImage(width, height, imageType);
        Graphics2D g = image.createGraphics();
        try {
            if (background != null) {
                g.setColor(background);
                g.fillRect(0, 0, width, height);
            } else {
                if ((imageType != BufferedImage.TYPE_INT_ARGB && imageType != BufferedImage.TYPE_4BYTE_ABGR)
                        && (imageType != BufferedImage.TYPE_INT_ARGB_PRE)) {
                    g.setColor(Color.BLACK);
                    g.fillRect(0, 0, width, height);
                }
            }
            return image;
        } finally {
            g.dispose();
        }
    }

    public static BufferedImage getQRImage(String data, int width, int height, String correction) {
        try {
            Map<EncodeHintType, ?> hints = Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            switch (correction) {
                case "L" -> hints = Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
                case "M" -> hints = Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
                case "Q" -> hints = Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);
                case "H" -> hints = Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            }

            return MatrixToImageWriter.toBufferedImage(
                    new QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, width, height, hints));
        } catch (WriterException e) {
            throw new RuntimeException("Error generating QR code", e);
        }
    }
}

