package kz.arta.synergy.astdev.poico_test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import net.sf.sevenzipjbinding.IOutCreateArchive7z;
import net.sf.sevenzipjbinding.IOutCreateCallback;
import net.sf.sevenzipjbinding.IOutItem7z;
import net.sf.sevenzipjbinding.ISequentialInStream;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.OutItemFactory;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
import net.sf.sevenzipjbinding.util.ByteArrayStream;
import lzma.sdk.lzma.Encoder;

public class Archivator {

    public static void main(String[] args) throws IOException {
        archiveFiles();
    }

    public static void archiveFiles() throws IOException {
        Archivator arch = new Archivator();

        String sourceFile = "d:\\Projects\\java\\PoiCo_test\\src\\test\\resources\\FiraCode-Retina.ttf";
        String targetFile = "d:\\Projects\\java\\PoiCo_test\\src\\test\\resources\\target.7zip";

        File sourceFile_ = new File(sourceFile);
        File targetFile_ = new File(targetFile);
        boolean sourceFile_exists = sourceFile_.exists();
        boolean targetFile_exists = targetFile_.exists();

        arch.create(targetFile);
        arch.compress(sourceFile, targetFile);
    }

    public static class Item {

        private String path;
        private byte[] content;

        Item(String path, String content) {
            this(path, content.getBytes());
        }

        Item(String path, byte[] content) {
            this.path = path;
            this.content = content;
        }

        String getPath() {
            return path;
        }

        byte[] getContent() {
            return content;
        }
    }

    /**
     * The callback defines the modification to be made.
     */
    private final class MyCreateCallback implements IOutCreateCallback<IOutItem7z> {

        @Override
        public void setOperationResult(boolean operationResultOk)
                throws SevenZipException {
            // Track each operation result here
        }

        @Override
        public void setTotal(long total) throws SevenZipException {
            // Track operation progress here
        }

        @Override
        public void setCompleted(long complete) throws SevenZipException {
            // Track operation progress here
        }

        @Override
        public IOutItem7z getItemInformation(int index, OutItemFactory<IOutItem7z> outItemFactory) {
            IOutItem7z item = outItemFactory.createOutItem();

            if (items[index].getContent() == null) {
                // Directory
                item.setPropertyIsDir(true);
            } else {
                // File
                item.setDataSize((long) items[index].getContent().length);
            }

            item.setPropertyPath(items[index].getPath());

            return item;
        }

        @Override
        public ISequentialInStream getStream(int i) throws SevenZipException {
            if (items[i].getContent() == null) {
                return null;
            }
            return new ByteArrayStream(items[i].getContent(), true);
        }
    }

    private void compress(String in, String out) throws FileNotFoundException, IOException {

        /* Read the input file to be compressed */
        File inputToCompress = new File(in);
        BufferedInputStream inStream = new BufferedInputStream(new java.io.FileInputStream(inputToCompress));
        /* Create output file 7z File */
        File compressedOutput = new File(out);
        BufferedOutputStream outStream = new BufferedOutputStream(new java.io.FileOutputStream(compressedOutput));
        /* Create LZMA Encoder Object / Write Header Information */
        Encoder encoder = new Encoder();
        encoder.setAlgorithm(2);
        encoder.setDictionarySize(8388608);
        encoder.setNumFastBytes(128);
        encoder.setMatchFinder(1);
        encoder.setLcLpPb(3, 0, 2);
        encoder.setEndMarkerMode(false);
        encoder.writeCoderProperties(outStream);
        long fileSize;
        fileSize = inputToCompress.length();
        for (int i = 0; i < 8; i++) {
            outStream.write((int) (fileSize >>> (8 * i)) & 0xFF);
        }
        /* Write Compressed Data to File */
        encoder.code(inStream, outStream, -1, -1, null);
        /* Close Output Streams*/
        outStream.flush();
        outStream.close();
        inStream.close();
    }

private Item[] items = new Item[0];

    private void create(String filename) {

        boolean success = false;
        RandomAccessFile raf = null;
        IOutCreateArchive7z outArchive = null;
        try {
            raf = new RandomAccessFile(filename, "rw");

            // Open out-archive object
            outArchive = SevenZip.openOutArchive7z();

            // Configure archive
            outArchive.setLevel(5);
            outArchive.setSolid(true);

            // Create archive
            outArchive.createArchive(new RandomAccessFileOutStream(raf), items.length, new MyCreateCallback());

            success = true;
        } catch (SevenZipException e) {
            System.err.println("7z-Error occurs:");
            // Get more information using extended method
            e.printStackTraceExtended();
        } catch (Exception e) {
            System.err.println("Error occurs: " + e);
        } finally {
            if (outArchive != null) {
                try {
                    outArchive.close();
                } catch (IOException e) {
                    System.err.println("Error closing archive: " + e);
                    success = false;
                }
            }
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    System.err.println("Error closing file: " + e);
                    success = false;
                }
            }
        }
        if (success) {
            System.out.println("Compression operation succeeded");
        }
    }
}
