package com.xiaopeng.lib.framework.netchannelmodule.http.traffic;

import android.support.annotation.NonNull;
import com.xiaopeng.lib.framework.netchannelmodule.http.statistic.BaseHttpCounter;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
/* loaded from: classes.dex */
public class CountingInputStream extends FilterInputStream {
    protected ICollector mCollector;
    protected BaseHttpCounter mCounter;

    public CountingInputStream(@NonNull ICollector collector, @NonNull InputStream in) throws IOException {
        super(in);
        this.mCollector = collector;
    }

    public CountingInputStream setStatisticCounter(@NonNull BaseHttpCounter counter) {
        this.mCounter = counter;
        return this;
    }

    @Override // java.io.FilterInputStream, java.io.InputStream
    public int read() throws IOException {
        BaseHttpCounter baseHttpCounter;
        int result = this.in.read();
        if (result != -1 && (baseHttpCounter = this.mCounter) != null) {
            baseHttpCounter.addReceivedSize(this.mCollector.getDomain(), 1L);
        }
        return result;
    }

    @Override // java.io.FilterInputStream, java.io.InputStream
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override // java.io.FilterInputStream, java.io.InputStream
    public int read(byte[] b, int off, int len) throws IOException {
        BaseHttpCounter baseHttpCounter;
        int length = this.in.read(b, off, len);
        if (length > 0 && (baseHttpCounter = this.mCounter) != null) {
            baseHttpCounter.addReceivedSize(this.mCollector.getDomain(), length);
        }
        return length;
    }

    @Override // java.io.FilterInputStream, java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        super.close();
    }
}
