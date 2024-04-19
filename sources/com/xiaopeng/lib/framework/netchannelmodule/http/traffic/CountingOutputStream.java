package com.xiaopeng.lib.framework.netchannelmodule.http.traffic;

import android.support.annotation.NonNull;
import com.xiaopeng.lib.framework.netchannelmodule.http.statistic.BaseHttpCounter;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
/* loaded from: classes.dex */
public class CountingOutputStream extends FilterOutputStream {
    protected ICollector mCollector;
    protected BaseHttpCounter mCounter;

    public CountingOutputStream(@NonNull ICollector collector, @NonNull OutputStream o) throws IOException {
        super(o);
        this.mCollector = collector;
    }

    public CountingOutputStream setStatisticCounter(@NonNull BaseHttpCounter counter) {
        this.mCounter = counter;
        return this;
    }

    @Override // java.io.FilterOutputStream, java.io.OutputStream
    public void write(int b) throws IOException {
        this.out.write(b);
        BaseHttpCounter baseHttpCounter = this.mCounter;
        if (baseHttpCounter != null) {
            baseHttpCounter.addSentSize(this.mCollector.getDomain(), 1L);
        }
    }

    @Override // java.io.FilterOutputStream, java.io.OutputStream
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override // java.io.FilterOutputStream, java.io.OutputStream
    public void write(byte[] b, int off, int len) throws IOException {
        BaseHttpCounter baseHttpCounter;
        this.out.write(b, off, len);
        if (len > 0 && (baseHttpCounter = this.mCounter) != null) {
            baseHttpCounter.addSentSize(this.mCollector.getDomain(), len);
        }
    }

    @Override // java.io.FilterOutputStream, java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        super.close();
    }

    @Override // java.io.FilterOutputStream, java.io.OutputStream, java.io.Flushable
    public void flush() throws IOException {
        super.flush();
    }
}
