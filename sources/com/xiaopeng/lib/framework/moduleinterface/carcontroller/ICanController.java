package com.xiaopeng.lib.framework.moduleinterface.carcontroller;
/* loaded from: classes.dex */
public interface ICanController extends ILifeCycle {

    /* loaded from: classes.dex */
    public static class CanDiagnoseEventMsg extends AbstractEventMsg<String> {
    }

    /* loaded from: classes.dex */
    public static class CanRawDataEventMsg extends AbstractEventMsg<int[]> {
    }

    byte[] getCanRawData() throws Exception;

    void sendCanDataSync() throws Exception;

    void setAdasMeta(byte[] metaValues) throws Exception;

    void setAdasPosition(byte[] positionValues) throws Exception;

    void setAdasProfLong(byte[] profLongValues) throws Exception;

    void setAdasProfShort(byte[] profShortValues) throws Exception;

    void setAdasSegment(byte[] segmentValues) throws Exception;

    void setAdasStub(byte[] stubValues) throws Exception;
}
