package com.xiaopeng.lib.framework.netchannelmodule.remotestorage.exception;

import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.StorageException;
/* loaded from: classes.dex */
public class StorageExceptionImpl extends StorageException {
    private static final long serialVersionUID = 100;
    private int mReasonCode;

    public StorageExceptionImpl(int reasonCode) {
        super("");
        this.mReasonCode = reasonCode;
    }

    public StorageExceptionImpl(int reasonCode, String message) {
        super(message);
        this.mReasonCode = reasonCode;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.StorageException
    public int getReasonCode() {
        return this.mReasonCode;
    }
}
