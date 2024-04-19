package com.microsoft.cognitiveservices.speech.util;

import com.microsoft.cognitiveservices.speech.util.JsonValueJNI;
/* loaded from: classes.dex */
public class JsonBuilder implements AutoCloseable {
    private SafeHandle builderHandle;
    public int root;

    public JsonBuilder() {
        JsonBuilder createBuilder = JsonBuilderJNI.createBuilder();
        this.builderHandle = createBuilder.builderHandle;
        this.root = createBuilder.root;
    }

    public JsonBuilder(SafeHandle safeHandle, int i) {
        this.builderHandle = safeHandle;
        this.root = i;
    }

    public int addItem(int i, int i2, String str) {
        return JsonBuilderJNI.addItem(this.builderHandle, i, i2, str);
    }

    @Override // java.lang.AutoCloseable
    public void close() throws Exception {
        SafeHandle safeHandle = this.builderHandle;
        if (safeHandle != null) {
            safeHandle.close();
            this.builderHandle = null;
        }
    }

    public int setBoolean(int i, boolean z) {
        return JsonBuilderJNI.setItem(this.builderHandle, i, null, JsonValueJNI.ValueKind.BOOLEAN, null, z, 0, 0.0d);
    }

    public int setDouble(int i, double d) {
        return JsonBuilderJNI.setItem(this.builderHandle, i, null, JsonValueJNI.ValueKind.NUMBER, null, false, 0, d);
    }

    public int setInteger(int i, int i2) {
        return JsonBuilderJNI.setItem(this.builderHandle, i, null, JsonValueJNI.ValueKind.NUMBER, null, false, i2, 0.0d);
    }

    public int setJson(int i, String str) {
        return JsonBuilderJNI.setItem(this.builderHandle, i, str, JsonValueJNI.ValueKind.OBJECT, null, false, 0, 0.0d);
    }

    public int setString(int i, String str) {
        return JsonBuilderJNI.setItem(this.builderHandle, i, null, JsonValueJNI.ValueKind.STRING, str, false, 0, 0.0d);
    }

    public String toString() {
        return JsonBuilderJNI.asJsonCopy(this.builderHandle, this.root);
    }
}
