package com.kokayapp.filetransfer;

import android.net.Uri;

import java.io.File;
import java.util.StringTokenizer;

/**
 * Created by Koji on 1/14/2017.
 */

public class FileInfo extends File {
    private long soFar;
    private boolean checked;

    private final long fileSizeDivisor;
    private final String type;

    private static final long BYTE = 1024;
    private static final long MEGABYTE = BYTE << 10;
    private static final long GIGABYTE = MEGABYTE << 10;

    public FileInfo(FileInfo rhs) {
        super(rhs.getPath());
        this.soFar = rhs.soFar;
        this.checked = true;
        this.fileSizeDivisor = rhs.fileSizeDivisor;
        this.type = rhs.type;
    }

    public FileInfo(String path) {
        super(path);
        this.soFar = 0;
        this.checked = true;
        if(this.length() >= GIGABYTE) {
            fileSizeDivisor = MEGABYTE;
            type = "GB";
        } else if (this.length() >= MEGABYTE) {
            fileSizeDivisor = BYTE;
            type = "MB";
        } else {
            fileSizeDivisor = 1;
            type = "B";
        }
    }

    public static FileInfo parse(String line) {
        StringTokenizer st = new StringTokenizer(line);
        return new FileInfo(st.nextToken());
    }

    public String getStringProgress() {
        return (soFar / (float) fileSizeDivisor) + " " + type + " / "
                + (this.length() / (float) fileSizeDivisor) + " " + type;
    }

    public int getIntProgress() {
        return (int)((soFar / (float) this.length()) * 100);
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
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
