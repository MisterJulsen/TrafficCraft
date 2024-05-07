package de.mrjulsen.legacydragonlib.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import de.mrjulsen.legacydragonlib.DragonLibConstants;

public final class IOUtils {

    public static InputStream readFile(String filePath) throws IOException {
        File file = new File(filePath);
        return new FileInputStream(file);
    }

    public static void saveInputStreamToFile(InputStream inputStream, String filePath) throws IOException {
        File file = new File(filePath);
        OutputStream outputStream = new FileOutputStream(file);

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outputStream.close();
    }

    public static void writeTextFile(String filePath, String content) throws IOException {
        Files.writeString(Path.of(filePath), content);
    }

    public static String readTextFile(String filePath) throws IOException {
        byte[] fileBytes = Files.readAllBytes(Path.of(filePath));
        return new String(fileBytes, StandardCharsets.UTF_8);
    }

    public static InputStream byteArrayToInputStream(byte[] byteArray) {
        return new ByteArrayInputStream(byteArray);
    }

    public static byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        return buffer.toByteArray();
    }

    public static boolean createDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            return created;
        } else {
            return false;
        }
    }

    public static String[] getFileNames(String directoryPath) {
        List<String> fileNames = new ArrayList<>();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directoryPath))) {
            for (Path path : directoryStream) {
                if (Files.isRegularFile(path)) {
                    fileNames.add(path.getFileName().toString());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return fileNames.toArray(new String[0]);
    }

    public static String formatBytes(long bytes) {
        int unit = 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (java.lang.Math.log(bytes) / java.lang.Math.log(unit));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / java.lang.Math.pow(unit, exp), pre);
    }

    public static String[] getFileNamesWithoutExtension(String directoryPath) {
        List<String> fileNames = new ArrayList<>();

        File directory = new File(directoryPath);
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        String fileName = file.getName();
                        int dotIndex = fileName.lastIndexOf(".");
                        if (dotIndex != -1) {
                            String fileNameWithoutExtension = fileName.substring(0, dotIndex);
                            fileNames.add(fileNameWithoutExtension);
                        }
                    }
                }
            }
        }

        return fileNames.toArray(new String[0]);
    }

    public static String getFileNameWithoutExtension(String filePath) {
        File file = new File(filePath);
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }

    public static String getFileExtension(String fileName) {
        if (fileName == null) {
            return null;
        }

        int lastDotIndex = fileName.lastIndexOf(".");

        if (lastDotIndex == -1) {
            return "";
        }

        return fileName.substring(lastDotIndex + 1);
    }

    public static boolean isValidFileName(String fileName) {
        // Gültige Zeichen für Dateinamen (ohne Pfad) in den meisten Betriebssystemen
        // Windows: \ / : * ? " < > |
        // Linux und macOS: /
        String invalidCharsRegex = "[\\\\/:*?\"<>|]";

        // Überprüfen, ob der Dateiname ungültige Zeichen enthält
        Pattern pattern = Pattern.compile(invalidCharsRegex);
        return !pattern.matcher(fileName).find();
    }

    public static String sanitizeFileName(String fileName) {
        // Verbote Zeichen für Dateinamen (ohne Pfad) in den meisten Betriebssystemen
        // Windows: \ / : * ? " < > |
        // Linux und macOS: /
        String invalidCharsRegex = "[\\\\/:*?\"<>|]";

        // Entfernen der ungültigen Zeichen aus dem Dateinamen
        String sanitizedFileName = fileName.replaceAll(invalidCharsRegex, "");

        return sanitizedFileName;
    }

    public static String getFileHash(String filePath) {        
        try {
            File file = new File(filePath);
            String data = file.getName() + file.length() + file.lastModified();
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");            
            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getConfigPath(String modid) {
        return String.format("%s/%s", DragonLibConstants.CONFIG_DIR, modid);
    }

    public static String createConfigDirectory(String modid) {
        String path = getConfigPath(modid);
        createDirectory(path);
        return path;
    }

    public static void writeConfigTextFile(String modid, String relFilePath, String content) throws IOException {
        String path = getConfigPath(modid) + "/" + relFilePath;
        createDirectory(path);
        Files.writeString(Path.of(path), content);
    }

    public static String readConfigTextFile(String modid, String relFilePath) throws IOException {
        byte[] fileBytes = Files.readAllBytes(Path.of(getConfigPath(modid) + "/" + relFilePath));
        return new String(fileBytes, StandardCharsets.UTF_8);
    }
}

