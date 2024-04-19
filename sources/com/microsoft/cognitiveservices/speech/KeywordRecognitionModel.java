package com.microsoft.cognitiveservices.speech;

import com.lzy.okgo.model.Progress;
import com.microsoft.cognitiveservices.speech.util.Contracts;
import com.microsoft.cognitiveservices.speech.util.IntRef;
import com.microsoft.cognitiveservices.speech.util.SafeHandle;
import com.microsoft.cognitiveservices.speech.util.SafeHandleType;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
/* loaded from: classes.dex */
public class KeywordRecognitionModel implements AutoCloseable {
    private boolean disposed = false;
    private SafeHandle keywordModelHandle;

    static {
        Class<?> cls = SpeechConfig.speechConfigClass;
    }

    KeywordRecognitionModel(IntRef intRef) {
        this.keywordModelHandle = null;
        Contracts.throwIfNull(intRef, "modelHandle");
        this.keywordModelHandle = new SafeHandle(intRef.getValue(), SafeHandleType.KeywordModel);
    }

    private static final native long createKeywordRecognitionModelFromFile(String str, IntRef intRef);

    public static KeywordRecognitionModel fromFile(String str) {
        Contracts.throwIfFileDoesNotExist(str, Progress.FILE_NAME);
        try {
            File file = new File(str);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(new File(str)));
            byte[] bArr = new byte[2];
            bufferedInputStream.mark(4);
            int read = bufferedInputStream.read(bArr);
            bufferedInputStream.reset();
            boolean z = true;
            if (read != 2 || bArr[0] != 80 || bArr[1] != 75) {
                z = false;
            }
            if (z) {
                KeywordRecognitionModel fromStream = fromStream(bufferedInputStream, file.getName(), z);
                bufferedInputStream.close();
                return fromStream;
            }
            bufferedInputStream.close();
            IntRef intRef = new IntRef(0L);
            Contracts.throwIfFail(createKeywordRecognitionModelFromFile(file.getCanonicalPath(), intRef));
            return new KeywordRecognitionModel(intRef);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("fileName not found");
        } catch (IOException e2) {
            throw new IllegalArgumentException("could not access file " + e2.toString());
        }
    }

    public static KeywordRecognitionModel fromStream(InputStream inputStream, String str, boolean z) throws IOException {
        FileOutputStream fileOutputStream;
        Contracts.throwIfNull(inputStream, "inputStream");
        Contracts.throwIfNullOrWhitespace(str, "name");
        if (str.contains(File.separator) || str.contains(".") || str.contains(":")) {
            throw new IOException("name must not contain separator, ., or :");
        }
        String property = System.getProperty("java.io.tmpdir");
        Contracts.throwIfNullOrWhitespace(property, "tempFolder");
        String canonicalPath = new File(property).getCanonicalPath();
        Contracts.throwIfNullOrWhitespace(canonicalPath, "canonicalTempFolder");
        File canonicalFile = new File(canonicalPath, "speech-sdk-keyword-" + str).getCanonicalFile();
        if (!canonicalFile.getCanonicalPath().startsWith(canonicalPath)) {
            throw new IOException("invalid kws temp directory " + canonicalFile.getCanonicalPath());
        }
        if (!canonicalFile.exists()) {
            if (!canonicalFile.mkdirs()) {
                throw new IllegalArgumentException("cannot create directory");
            }
            canonicalFile.deleteOnExit();
            if (!canonicalFile.isDirectory()) {
                throw new IllegalArgumentException("path is not a directory");
            }
            byte[] bArr = new byte[1048576];
            FileOutputStream fileOutputStream2 = null;
            if (z) {
                ZipInputStream zipInputStream = new ZipInputStream(inputStream);
                while (true) {
                    ZipEntry nextEntry = zipInputStream.getNextEntry();
                    if (nextEntry == null) {
                        zipInputStream.close();
                        break;
                    } else if (!nextEntry.isDirectory()) {
                        String str2 = "" + nextEntry.getName();
                        str2 = (str2.length() > 128 || str2.contains("..")) ? "" : "";
                        Contracts.throwIfNullOrWhitespace(str2, "zipEntry.name");
                        File file = new File(canonicalFile, str2);
                        if (!file.getCanonicalPath().startsWith(canonicalFile.getCanonicalPath())) {
                            throw new IOException("invalid file " + file.getCanonicalPath());
                        } else if (file.exists() && !file.delete()) {
                            throw new IllegalArgumentException("could not delete " + file.getCanonicalPath());
                        } else {
                            file.deleteOnExit();
                            try {
                                fileOutputStream = new FileOutputStream(file);
                                while (true) {
                                    try {
                                        int read = zipInputStream.read(bArr);
                                        if (read <= 0) {
                                            break;
                                        }
                                        fileOutputStream.write(bArr, 0, read);
                                    } catch (Throwable th) {
                                        th = th;
                                        safeClose(fileOutputStream);
                                        throw th;
                                    }
                                }
                                safeClose(fileOutputStream);
                            } catch (Throwable th2) {
                                th = th2;
                                fileOutputStream = null;
                            }
                        }
                    }
                }
            } else {
                try {
                    FileOutputStream fileOutputStream3 = new FileOutputStream(new File(canonicalFile, "kws.table"));
                    while (true) {
                        try {
                            int read2 = inputStream.read(bArr);
                            if (read2 <= 0) {
                                break;
                            }
                            fileOutputStream3.write(bArr, 0, read2);
                        } catch (Throwable th3) {
                            th = th3;
                            fileOutputStream2 = fileOutputStream3;
                            safeClose(fileOutputStream2);
                            throw th;
                        }
                    }
                    safeClose(fileOutputStream3);
                } catch (Throwable th4) {
                    th = th4;
                }
            }
        }
        File file2 = new File(canonicalFile, "kws.table");
        if (file2.exists() && file2.isFile()) {
            IntRef intRef = new IntRef(0L);
            Contracts.throwIfFail(createKeywordRecognitionModelFromFile(file2.getCanonicalPath(), intRef));
            return new KeywordRecognitionModel(intRef);
        }
        throw new IllegalArgumentException("zip did not contain kws.table");
    }

    private static void safeClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
            }
        }
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        if (this.disposed) {
            return;
        }
        SafeHandle safeHandle = this.keywordModelHandle;
        if (safeHandle != null) {
            safeHandle.close();
            this.keywordModelHandle = null;
        }
        this.disposed = true;
    }

    public SafeHandle getImpl() {
        return this.keywordModelHandle;
    }
}
