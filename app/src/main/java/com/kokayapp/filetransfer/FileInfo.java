package com.kokayapp.filetransfer;

import android.net.Uri;

import java.util.StringTokenizer;

/**
 * Created by Koji on 1/14/2017.
 */

public class FileInfo {
    private final String uri;
    private final String title;
    private final long size;
    private long soFar;
    private boolean checked;

    private final long fileSizeDivisor;
    private final String type;

    private static final long BYTE = 1024;
    private static final long MEGABYTE = BYTE << 10;
    private static final long GIGABYTE = MEGABYTE << 10;

    public FileInfo(FileInfo rhs) {
        this.uri = rhs.uri;
        this.title = rhs.uri;
        this.size = rhs.size;
        this.soFar = rhs.soFar;
        this.checked = true;
        this.fileSizeDivisor = rhs.fileSizeDivisor;
        this.type = rhs.type;
    }

    public FileInfo(String uri, long size) {
        this.uri = uri;
        this.title = uri;
        this.size = size;
        this.soFar = 0;
        this.checked = true;
        if(size >= GIGABYTE) {
            fileSizeDivisor = MEGABYTE;
            type = "GB";
        } else if (size >= MEGABYTE) {
            fileSizeDivisor = BYTE;
            type = "MB";
        } else {
            fileSizeDivisor = 1;
            type = "B";
        }
    }

    public static FileInfo parse(String line) {
        StringTokenizer st = new StringTokenizer(line);
        return new FileInfo(st.nextToken(), Long.parseLong(st.nextToken()));
    }

    public String getTitle() {
        return title;
    }

    public String getUri() {
        return uri;
    }

    public long getSize() {
        return size;
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

    public long getSoFar() {
        return soFar;
    }

    public void addSoFar(int count) {
        soFar += count;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof String) return this.uri.equals(obj);
        else return this.uri.equals(((FileInfo)obj).getUri());
    }
}
