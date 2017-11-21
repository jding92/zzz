package ca.ubc.cs.cpsc210.translink.parsers; /**
 * Created by norm on 2016-09-02.
 */

import java.io.*;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipFileMemoryIterator implements Iterator<MemoryFile> {

    private byte[] buffer = new byte[8192];

    private InputStream is;
    private ZipInputStream zis;
    private ZipEntry ze;

    public ZipFileMemoryIterator(byte[] input) {
        is = new ByteArrayInputStream(input);
        zis = new ZipInputStream(new BufferedInputStream(is));
    }

    @Override
    public boolean hasNext() {
        try {
            return (ze = zis.getNextEntry()) != null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public MemoryFile next() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int count;

            String filename = ze.getName();

            while ((count = zis.read(buffer)) != -1) {
                baos.write(buffer, 0, count);
            }
            String ans = baos.toString("UTF-8");
            zis.closeEntry();

            return new MemoryFile(filename, ans);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove() {
        throw new RuntimeException("not implemented");
    }

    public void close() {
        try {
            zis.close();
            is.close();
        } catch (IOException e) {// nope
        }
    }
}
