package org.litepal.exceptions;
/* loaded from: classes.dex */
public class DataSupportException extends RuntimeException {
    private static final long serialVersionUID = 1;

    public DataSupportException(String errorMessage) {
        super(errorMessage);
    }

    public DataSupportException(String errorMessage, Throwable throwable) {
        super(errorMessage, throwable);
    }
}
