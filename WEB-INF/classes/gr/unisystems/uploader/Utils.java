package gr.unisystems.uploader;

import java.net.URL;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.zip.ZipOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.io.File;

public class Utils
{
    final String UPLOAD_FOLDER = "C:\\restFileHandler\\files\\";
    final String DOWNLOAD_FOLDER = "C:\\restFileHandler\\downloads\\";
    final Long MAX_FILE_SIZE;
    
    public Utils() {
        this.MAX_FILE_SIZE = 5242880L;
    }
    
    public void getAllFiles(final File dir, final List<File> fileList) {
        try {
            final File[] files = dir.listFiles();
            File[] array;
            for (int length = (array = files).length, i = 0; i < length; ++i) {
                final File file = array[i];
                fileList.add(file);
                if (file.isDirectory()) {
                    System.out.println("directory:" + file.getCanonicalPath());
                    this.getAllFiles(file, fileList);
                }
                else {
                    System.out.println("file:" + file.getCanonicalPath());
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void writeZipFile(final File directoryToZip, final List<File> fileList, final String afm, final String reqId, final int phaseId) {
        try {
            this.createTargetFolderIfNotExists("C:\\restFileHandler\\downloads\\");
            final File zipFile = new File("C:\\restFileHandler\\downloads\\\\" + afm + "_" + reqId + "_" + phaseId + ".zip");
            if (zipFile.isFile()) {
                zipFile.createNewFile();
            }
            final FileOutputStream fos = new FileOutputStream(zipFile);
            final ZipOutputStream zos = new ZipOutputStream(fos);
            for (final File file : fileList) {
                if (!file.isDirectory()) {
                    this.addToZip(directoryToZip, file, zos);
                }
            }
            zos.close();
            fos.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e2) {
            e2.printStackTrace();
        }
    }
    
    public void addToZip(final File directoryToZip, final File file, final ZipOutputStream zos) throws FileNotFoundException, IOException {
        final FileInputStream fis = new FileInputStream(file);
        final String zipFilePath = file.getCanonicalPath().substring(directoryToZip.getCanonicalPath().length() + 1, file.getCanonicalPath().length());
        System.out.println("Writing '" + zipFilePath + "' to zip file");
        final ZipEntry zipEntry = new ZipEntry(zipFilePath);
        zos.putNextEntry(zipEntry);
        final byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }
        zos.closeEntry();
        fis.close();
    }
    
    public void saveToFile(final InputStream inStream, final String target) throws IOException {
        OutputStream out = null;
        int read = 0;
        final byte[] bytes = new byte[1024];
        out = new FileOutputStream(new File(target));
        while ((read = inStream.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
        out.flush();
        out.close();
    }
    
    public void createPOSTFolderIfNotExists(final String dirName, final String afm, final String phaseId, final String reqId) throws SecurityException {
        final String directoryPath = String.valueOf(dirName) + "\\" + afm + "\\" + reqId + "\\" + phaseId + "\\";
        final File theDir = new File(directoryPath);
        if (!theDir.isDirectory()) {
            theDir.mkdirs();
        }
    }
    
    public void createTargetFolderIfNotExists(final String dirName) throws SecurityException {
        final File theDir = new File(dirName);
        if (!theDir.isDirectory()) {
            theDir.mkdirs();
        }
    }
    
    public String getFileExtension(final String fullName) {
        System.out.println("fullName: " + fullName);
        final String fileName = new File(fullName).getName();
        final int dotIndex = fileName.lastIndexOf(46);
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }
    
    public File getFileFromResources(final String fileName) {
        final ClassLoader classLoader = this.getClass().getClassLoader();
        final URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("Error finding: " + fileName);
        }
        return new File(resource.getFile());
    }
}
