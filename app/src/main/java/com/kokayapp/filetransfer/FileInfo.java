package com.kokayapp.filetransfer;

import android.net.Uri;

import java.io.File;
import java.util.StringTokenizer;

/**
 * Created by Koji on 1/14/2017.
 */

public class FileInfo extends File {
    private final long size;
    private long soFar;
    private boolean checked;

    private final long fileSizeDivisor;
    private final String type;

    private static final long BYTE = 1;
    private static final long KILOBYTE = BYTE << 10;
    private static final long MEGABYTE = KILOBYTE << 10;
    private static final long GIGABYTE = MEGABYTE << 10;

    private static final String BYTE_STRING = "B";
    private static final String KILOBYTE_STRING = "KB";
    private static final String MEGABYTE_STRING = "MB";
    private static final String GIGABYTE_STRING = "GB";

    public FileInfo(String path) {
        super(path);
        this.size = super.length();
        this.soFar = 0;
        this.checked = true;
        this.fileSizeDivisor = getFileSizeDivisor(size);
        this.type = getType(size);
    }

    public FileInfo(String path, long size) {
        super(path);
        this.size = size;
        this.soFar = 0;
        this.checked = true;
        this.fileSizeDivisor = getFileSizeDivisor(size);
        this.type = getType(size);
    }

    private long getFileSizeDivisor(long size) {
        if(size >= GIGABYTE) return GIGABYTE;
        else if (size >= MEGABYTE) return MEGABYTE;
        else if (size >= KILOBYTE) return KILOBYTE;
        else return BYTE;
    }

    private String getType(long size) {
        if(size >= GIGABYTE) return GIGABYTE_STRING;
        else if (size >= MEGABYTE) return MEGABYTE_STRING;
        else if (size >= KILOBYTE) return KILOBYTE_STRING;
        else return BYTE_STRING;
    }

    public FileInfo(FileInfo rhs) {
        super(rhs.getPath());
        this.size = rhs.size;
        this.soFar = rhs.soFar;
        this.checked = true;
        this.fileSizeDivisor = rhs.fileSizeDivisor;
        this.type = rhs.type;
    }


    public static FileInfo parse(String parentPath, String fileInfoStr) {
        StringTokenizer st = new StringTokenizer(fileInfoStr);
        return new FileInfo(parentPath + st.nextToken(), Integer.parseInt(st.nextToken()));
    }

    public String getStringProgress() {
        return (soFar / (float) fileSizeDivisor) + " " + type + " / "
                + (size / (float) fileSizeDivisor) + " " + type;
    }

    public int getIntProgress() {
        return (int)((soFar / (float) size) * 100);
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public long getSize() {
        return size;
    }
    public long getSoFar() {
        return soFar;
    }

    public void addSoFar(int count) {
        soFar += count;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
