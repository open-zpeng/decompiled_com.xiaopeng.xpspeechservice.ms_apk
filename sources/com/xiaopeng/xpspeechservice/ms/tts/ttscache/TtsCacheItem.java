package com.xiaopeng.xpspeechservice.ms.tts.ttscache;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;
/* loaded from: classes.dex */
public class TtsCacheItem extends LitePalSupport {
    private String fileName;
    private int id;
    @Column(defaultValue = "0")
    private long lastTime;
    private int size;
    private String text;
    private long time;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return this.time;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public long getLastTime() {
        return this.lastTime;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return this.size;
    }
}
