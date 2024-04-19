package com.xiaopeng.xpspeechservice.ms.tts.uploaddata;
/* loaded from: classes.dex */
public class UploadDataBean {
    private String engine = Constant.ENGINE;
    private String text = "";
    private String source = "";
    private long startTime = 0;
    private long endTime = 0;
    private String state = EngineEndType.NOT_STARTED.getDesc();
    private String mode = TtsModeType.UNSET.getDesc();
    private int firstFrameLatency = 0;
    private int dataMaxLatency = 0;
    private int dataLength = 0;
    private int mediaDecodeLatency = 0;
    private int mediaFirstFrameLatency = 0;
    private String mediaState = EngineEndType.NOT_STARTED.getDesc();
    private int offlineFirstDataLatency = 0;
    private int offlineSynthTime = 0;
    private String offlineState = EngineEndType.NOT_STARTED.getDesc();
    private String onlineDataType = "";
    private String onlineMsgId = "";
    private int onlineFirstDataLatency = 0;
    private int onlineDataMaxLatency = 0;
    private int onlineDecodeLatency = 0;
    private int onlineSynthTime = 0;
    private int onlineDataLength = 0;
    private String onlineState = EngineEndType.NOT_STARTED.getDesc();

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getEngine() {
        return this.engine;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return this.source;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getEndTime() {
        return this.endTime;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return this.state;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return this.mode;
    }

    public void setFirstFrameLatency(int firstFrameLatency) {
        this.firstFrameLatency = firstFrameLatency;
    }

    public int getFirstFrameLatency() {
        return this.firstFrameLatency;
    }

    public void setDataMaxLatency(int dataMaxLatency) {
        this.dataMaxLatency = dataMaxLatency;
    }

    public int getDataMaxLatency() {
        return this.dataMaxLatency;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public int getDataLength() {
        return this.dataLength;
    }

    public void setMediaDecodeLatency(int mediaDecodeLatency) {
        this.mediaDecodeLatency = mediaDecodeLatency;
    }

    public int getMediaDecodeLatency() {
        return this.mediaDecodeLatency;
    }

    public void setMediaFirstFrameLatency(int mediaFirstFrameLatency) {
        this.mediaFirstFrameLatency = mediaFirstFrameLatency;
    }

    public int getMediaFirstFrameLatency() {
        return this.mediaFirstFrameLatency;
    }

    public void setMediaState(String mediaState) {
        this.mediaState = mediaState;
    }

    public String getMediaState() {
        return this.mediaState;
    }

    public void setOfflineFirstDataLatency(int offlineFirstDataLatency) {
        this.offlineFirstDataLatency = offlineFirstDataLatency;
    }

    public int getOfflineFirstDataLatency() {
        return this.offlineFirstDataLatency;
    }

    public void setOfflineSynthTime(int offlineSynthTime) {
        this.offlineSynthTime = offlineSynthTime;
    }

    public int getOfflineSynthTime() {
        return this.offlineSynthTime;
    }

    public void setOfflineState(String offlineState) {
        this.offlineState = offlineState;
    }

    public String getOfflineState() {
        return this.offlineState;
    }

    public void setOnlineDataType(String onlineDataType) {
        this.onlineDataType = onlineDataType;
    }

    public String getOnlineDataType() {
        return this.onlineDataType;
    }

    public void setOnlineMsgId(String onlineMsgId) {
        this.onlineMsgId = onlineMsgId;
    }

    public String getOnlineMsgId() {
        return this.onlineMsgId;
    }

    public void setOnlineFirstDataLatency(int onlineFirstDataLatency) {
        this.onlineFirstDataLatency = onlineFirstDataLatency;
    }

    public int getOnlineFirstDataLatency() {
        return this.onlineFirstDataLatency;
    }

    public void setOnlineDataMaxLatency(int onlineDataMaxLatency) {
        this.onlineDataMaxLatency = onlineDataMaxLatency;
    }

    public int getOnlineDataMaxLatency() {
        return this.onlineDataMaxLatency;
    }

    public void setOnlineDecodeLatency(int onlineDecodeLatency) {
        this.onlineDecodeLatency = onlineDecodeLatency;
    }

    public int getOnlineDecodeLatency() {
        return this.onlineDecodeLatency;
    }

    public void setOnlineSynthTime(int onlineSynthTime) {
        this.onlineSynthTime = onlineSynthTime;
    }

    public int getOnlineSynthTime() {
        return this.onlineSynthTime;
    }

    public void setOnlineDataLength(int onlineDataLength) {
        this.onlineDataLength = onlineDataLength;
    }

    public int getOnlineDataLength() {
        return this.onlineDataLength;
    }

    public void setOnlineState(String onlineState) {
        this.onlineState = onlineState;
    }

    public String getOnlineState() {
        return this.onlineState;
    }
}
