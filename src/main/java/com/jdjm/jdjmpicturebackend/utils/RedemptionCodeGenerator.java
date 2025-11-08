package com.jdjm.jdjmpicturebackend.utils;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.zip.CRC32;

public class RedemptionCodeGenerator {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int RANDOM_LENGTH = 16;
    private static final SecureRandom random = new SecureRandom();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public enum VIPType {
        MONTHLY("M"),
        QUARTERLY("Q"),
        YEARLY("Y");

        private final String code;

        VIPType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    public static String generate(VIPType type) {
        String datePart = LocalDate.now().format(DATE_FORMATTER);
        String typePart = type.getCode();
        String randomPart = generateRandomString(RANDOM_LENGTH);
        String checksum = generateChecksum(datePart + typePart + randomPart);

        return String.format("VIP-%s-%s-%s-%s", datePart, typePart, randomPart, checksum);
    }

    public static boolean validate(String code) {
        if (code == null || !code.startsWith("VIP-")) {
            return false;
        }

        String[] parts = code.split("-");
        if (parts.length != 5) {
            return false;
        }

        String datePart = parts[1];
        String typePart = parts[2];
        String randomPart = parts[3];
        String providedChecksum = parts[4];

        String expectedChecksum = generateChecksum(datePart + typePart + randomPart);

        return expectedChecksum.equals(providedChecksum);
    }

    private static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private static String generateChecksum(String data) {
        CRC32 crc32 = new CRC32();
        crc32.update(data.getBytes());
        long checksum = crc32.getValue();
        return String.format("%08X", checksum).substring(0, 6);
    }

    public static VIPType extractType(String code) {
        if (!validate(code)) {
            return null;
        }

        String[] parts = code.split("-");
        String typeCode = parts[2];

        for (VIPType type : VIPType.values()) {
            if (type.getCode().equals(typeCode)) {
                return type;
            }
        }

        return null;
    }

    public static String extractDate(String code) {
        if (!validate(code)) {
            return null;
        }

        String[] parts = code.split("-");
        return parts[1];
    }

    public static String generateCode() {
        return generate(VIPType.MONTHLY);
    }
}