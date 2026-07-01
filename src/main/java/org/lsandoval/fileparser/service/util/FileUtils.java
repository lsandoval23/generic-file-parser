package org.lsandoval.fileparser.service.util;

public class FileUtils {

    public static String getBaseName(String filename) {
        if (filename == null) return null;
        int lastSeparator = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot < lastSeparator) {
            return filename.substring(lastSeparator + 1);
        }
        return filename.substring(lastSeparator + 1, lastDot);
    }


    public static String getExtension(String filename) {
        if (filename == null) return null;
        int lastSeparator = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot < lastSeparator) {
            return ""; // no extension
        }
        return filename.substring(lastDot + 1);
    }

}
