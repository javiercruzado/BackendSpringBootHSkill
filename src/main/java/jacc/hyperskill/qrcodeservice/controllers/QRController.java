package jacc.hyperskill.qrcodeservice.controllers;

import jacc.hyperskill.qrcodeservice.utils.ImageUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/")
public class QRController {

    private static final int MIN_SIZE = 150;
    private static final int MAX_SIZE = 350;
    private static final Set<String> SUPPORTED_FORMATS = Set.of("png", "jpeg", "gif");
    private static final Set<String> SUPPORTED_CORRECTIONS = Set.of("l", "m", "q", "h");

    @GetMapping("health")
    public ResponseEntity<String> health() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("qrcode")
    public ResponseEntity<Object> qrcode(@RequestParam(required = false) String contents, @RequestParam(required = false, defaultValue = "250") int size
            , @RequestParam(required = false, defaultValue = "png") String type, @RequestParam(required = false, defaultValue = "L") String correction) {

        if (contents != null && contents.isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Contents cannot be null or blank"));
        }
        if (size < MIN_SIZE || size > MAX_SIZE) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error",
                            "Image size must be between " + MIN_SIZE + " and " + MAX_SIZE + " pixels"));
        }

        if (!SUPPORTED_CORRECTIONS.contains(correction.toLowerCase())) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error",
                            "Permitted error correction levels are L, M, Q, H"));
        }

        String normalizedType = type.toLowerCase();
        if (!SUPPORTED_FORMATS.contains(normalizedType)) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error",
                            "Only png, jpeg and gif image types are supported"));
        }

        MediaType mediaType = getMediaType(normalizedType);
        BufferedImage bufferedImage;
        if (contents != null) {
            bufferedImage = ImageUtils.getQRImage(contents, size, size, correction);
        } else {
            bufferedImage = ImageUtils.createImage(size, size, Color.WHITE, BufferedImage.TYPE_INT_RGB);
        }
        return ResponseEntity
                .ok()
                .contentType(mediaType)
                .body(bufferedImage);
    }

    private MediaType getMediaType(String type) {
        return switch (type) {
            case "jpeg" -> MediaType.IMAGE_JPEG;
            case "gif" -> MediaType.IMAGE_GIF;
            default -> MediaType.IMAGE_PNG;
        };
    }

}