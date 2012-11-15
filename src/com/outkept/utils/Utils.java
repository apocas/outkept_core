package com.outkept.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.security.SecureRandom;

/**
 *
 * @author pedrodias
 */
public class Utils {

    public static boolean secureDelete(String filen) throws IOException {
        File file = new File(filen);
        if (file.exists()) {
            SecureRandom random = new SecureRandom();
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            FileChannel channel = raf.getChannel();
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, raf.length());
            while (buffer.hasRemaining()) {
                buffer.put((byte) 0);
            }
            buffer.force();
            buffer.rewind();
            while (buffer.hasRemaining()) {
                buffer.put((byte) 0xFF);
            }
            buffer.force();
            buffer.rewind();
            byte[] data = new byte[1];
            while (buffer.hasRemaining()) {
                random.nextBytes(data);
                buffer.put(data[0]);
            }
            buffer.force();
            file.delete();
            return true;
        }
        return false;
    }

    public static String readFile(String path) throws IOException {
        FileInputStream stream = new FileInputStream(new File(path));
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            return Charset.defaultCharset().decode(bb).toString();
        } finally {
            stream.close();
        }
    }
}
