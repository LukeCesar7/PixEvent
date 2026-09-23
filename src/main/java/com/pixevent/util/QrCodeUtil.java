package com.pixevent.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;

public final class QrCodeUtil {

    private QrCodeUtil() {
    }

    /**
     * Gera um QR Code PNG a partir de um texto e retorna como data URL base64,
     * equivalente a QRCode.toDataURL(texto, { width: 300, margin: 2 }) no Node.
     */
    public static String toDataUrl(String texto, int width) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                    EncodeHintType.MARGIN, 2
            );
            BitMatrix matrix = writer.encode(texto, BarcodeFormat.QR_CODE, width, width, hints);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            String base64 = Base64.getEncoder().encodeToString(out.toByteArray());
            return "data:image/png;base64," + base64;
        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar QR Code", e);
        }
    }

    public static String toBase64(String texto, int width) {
        String dataUrl = toDataUrl(texto, width);
        return dataUrl.substring(dataUrl.indexOf(',') + 1);
    }
}
