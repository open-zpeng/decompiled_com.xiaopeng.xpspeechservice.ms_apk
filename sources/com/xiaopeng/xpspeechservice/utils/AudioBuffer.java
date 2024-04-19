package com.xiaopeng.xpspeechservice.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
/* loaded from: classes.dex */
public class AudioBuffer {
    private static final boolean DBG = false;
    private static final int DEFAULT_SIZE = 131072;
    private static final int PRE_CACHE_SIZE = 0;
    private String TAG;
    private final int mBlockSize;
    private volatile ArrayList<byte[]> mBufferList;
    private volatile byte[] mBufferRead;
    private volatile byte[] mBufferWrite;
    private final Condition mCondition;
    private int mIndexRead;
    private volatile int mIndexWrite;
    private volatile boolean mIsEnd;
    private volatile boolean mIsWaitNewData;
    private int mListIndexRead;
    private final Lock mLock;
    private volatile int mPreCacheSize;

    public AudioBuffer() {
        this(DEFAULT_SIZE);
    }

    public AudioBuffer(int size) {
        this((String) null, size);
    }

    public AudioBuffer(String tagSuffix, int size) {
        this(tagSuffix, size, 0);
    }

    public AudioBuffer(int size, int blockSize) {
        this(null, size, blockSize);
    }

    public AudioBuffer(String tagSuffix, int size, int blockSize) {
        this.TAG = "AudioBuffer";
        this.mIndexWrite = 0;
        this.mIndexRead = 0;
        this.mListIndexRead = 0;
        this.mIsEnd = false;
        this.mIsWaitNewData = false;
        this.mPreCacheSize = 0;
        this.mLock = new ReentrantLock(true);
        this.mCondition = this.mLock.newCondition();
        size = size < 10 ? 10 : size;
        byte[] buffer = new byte[size];
        this.mBufferList = new ArrayList<>();
        this.mBufferList.add(buffer);
        this.mBufferWrite = buffer;
        this.mBufferRead = buffer;
        if (tagSuffix != null) {
            this.TAG += tagSuffix;
        }
        if (blockSize >= 10) {
            this.mBlockSize = blockSize;
        } else if (size < DEFAULT_SIZE) {
            this.mBlockSize = size;
        } else {
            this.mBlockSize = DEFAULT_SIZE;
        }
        LogUtils.v(this.TAG, "created buffer size %d block size %d", Integer.valueOf(size), Integer.valueOf(this.mBlockSize));
    }

    public void setPreCacheSize(int size) {
        this.mPreCacheSize = size;
    }

    public void write(byte[] buffer) {
        write(buffer, buffer.length);
    }

    public void write(byte[] buffer, int size) {
        if (this.mBufferList.isEmpty() || size <= 0) {
            return;
        }
        this.mLock.lock();
        try {
            try {
            } catch (Exception e) {
                LogUtils.e(this.TAG, "write fail", e);
            }
            if (this.mIsEnd) {
                return;
            }
            int demandLen = size;
            int srcIndex = 0;
            while (this.mIndexWrite + demandLen > this.mBufferWrite.length) {
                int freeSize = this.mBufferWrite.length - this.mIndexWrite;
                System.arraycopy(buffer, srcIndex, this.mBufferWrite, this.mIndexWrite, freeSize);
                demandLen -= freeSize;
                srcIndex += freeSize;
                LogUtils.v(this.TAG, "write make new buffer");
                this.mBufferWrite = new byte[this.mBlockSize];
                this.mBufferList.add(this.mBufferWrite);
                this.mIndexWrite = 0;
            }
            if (demandLen > 0) {
                System.arraycopy(buffer, srcIndex, this.mBufferWrite, this.mIndexWrite, demandLen);
                this.mIndexWrite += demandLen;
            }
            if (this.mIsWaitNewData) {
                this.mCondition.signal();
            }
        } finally {
            this.mLock.unlock();
        }
    }

    public int read(byte[] buffer) {
        int dataLen;
        if (this.mBufferList.isEmpty()) {
            return 0;
        }
        this.mLock.lock();
        try {
            int demandLen = buffer.length;
            int dstIndex = 0;
            if (this.mPreCacheSize > demandLen) {
                int cacheBufferIndex = 0;
                int cacheSize = 0;
                byte[] bufferCache = this.mBufferList.get(0);
                while (true) {
                    if (bufferCache == this.mBufferWrite) {
                        if (this.mIndexWrite + cacheSize >= this.mPreCacheSize || this.mIsEnd) {
                            break;
                        }
                        this.mIsWaitNewData = true;
                        this.mCondition.await();
                        this.mIsWaitNewData = false;
                    } else {
                        cacheSize += bufferCache.length;
                        cacheBufferIndex++;
                        bufferCache = this.mBufferList.get(cacheBufferIndex);
                    }
                }
                this.mPreCacheSize = 0;
            }
            while (true) {
                if (this.mBufferRead != this.mBufferWrite) {
                    int dataLen2 = this.mBufferRead.length - this.mIndexRead;
                    if (dataLen2 < demandLen) {
                        System.arraycopy(this.mBufferRead, this.mIndexRead, buffer, dstIndex, dataLen2);
                        dstIndex += dataLen2;
                        demandLen -= dataLen2;
                        this.mListIndexRead++;
                        this.mBufferRead = this.mBufferList.get(this.mListIndexRead);
                        this.mIndexRead = 0;
                    } else {
                        System.arraycopy(this.mBufferRead, this.mIndexRead, buffer, dstIndex, demandLen);
                        this.mIndexRead += demandLen;
                        dstIndex += demandLen;
                        demandLen = 0;
                    }
                }
                if (demandLen <= 0) {
                    break;
                }
                dataLen = this.mIndexWrite - this.mIndexRead;
                if (dataLen >= demandLen || this.mIsEnd) {
                    break;
                }
                this.mIsWaitNewData = true;
                this.mCondition.await();
                this.mIsWaitNewData = false;
            }
            if (this.mIsEnd && dataLen < demandLen) {
                demandLen = dataLen;
            }
            if (demandLen > 0) {
                System.arraycopy(this.mBufferRead, this.mIndexRead, buffer, dstIndex, demandLen);
                this.mIndexRead += demandLen;
                dstIndex += demandLen;
            }
            return dstIndex;
        } catch (Exception e) {
            LogUtils.e(this.TAG, "read fail", e);
            return 0;
        } finally {
            this.mLock.unlock();
        }
    }

    public void writeDone() {
        if (this.mBufferList.isEmpty()) {
            return;
        }
        this.mLock.lock();
        try {
            if (!this.mIsEnd) {
                this.mIsEnd = true;
                if (this.mIsWaitNewData) {
                    this.mCondition.signal();
                }
            }
        } finally {
            this.mLock.unlock();
        }
    }

    public byte[] getWholeBuffer() {
        this.mLock.lock();
        int size = 0;
        int index = 0;
        try {
            byte[] buffer = this.mBufferList.get(0);
            while (buffer != this.mBufferWrite) {
                size += buffer.length;
                index++;
                buffer = this.mBufferList.get(index);
            }
            byte[] wholeBuffer = new byte[size + this.mIndexWrite];
            int offset = 0;
            int index2 = 0;
            byte[] buffer2 = this.mBufferList.get(0);
            while (buffer2 != this.mBufferWrite) {
                System.arraycopy(buffer2, 0, wholeBuffer, offset, buffer2.length);
                offset += buffer2.length;
                index2++;
                buffer2 = this.mBufferList.get(index2);
            }
            System.arraycopy(buffer2, 0, wholeBuffer, offset, this.mIndexWrite);
            return wholeBuffer;
        } finally {
            this.mLock.unlock();
        }
    }

    public int getLength() {
        Lock lock;
        int size = 0;
        this.mLock.lock();
        try {
            if (!this.mBufferList.isEmpty()) {
                int index = 0;
                byte[] buffer = this.mBufferList.get(0);
                while (buffer != this.mBufferWrite) {
                    size += buffer.length;
                    index++;
                    buffer = this.mBufferList.get(index);
                }
                return size + this.mIndexWrite;
            }
            return 0;
        } finally {
            this.mLock.unlock();
        }
    }

    public void dumpToFile(String path) {
        try {
            FileOutputStream file = new FileOutputStream(new File(path));
            file.write(getWholeBuffer());
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
