package utils.uploadUtil;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class NTDUploadUtil {

    public static String javaUnzipFile(String Filepath, String DestinationFolderPath) {
        try {
            File destDir = new File(DestinationFolderPath);
            FileInputStream fin = new FileInputStream(Filepath);
            BufferedInputStream bin = new BufferedInputStream(fin);
            ZipInputStream zis = new ZipInputStream(bin);

            String unzipDestinationFolder = zis.getNextEntry().getName();

            ZipEntry zipEntry = null;
            int count = 0;
            while ((zipEntry = zis.getNextEntry()) != null) {
                // ...
                count++;
                File newFile = newFile(destDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    // write file content
                    FileOutputStream fos = new FileOutputStream(newFile);
                    BufferedOutputStream bufout = new BufferedOutputStream(fos);
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while ((len = zis.read(buffer)) != -1) {
                        bufout.write(buffer, 0, len);
                    }
                    zis.closeEntry();
                    bufout.close();
                    fos.close();
                }
            }
            System.out.println(count);
            zis.close();
            return unzipDestinationFolder;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
