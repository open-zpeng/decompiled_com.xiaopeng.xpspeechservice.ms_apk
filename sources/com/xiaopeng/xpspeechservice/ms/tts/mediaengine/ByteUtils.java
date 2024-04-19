package com.xiaopeng.xpspeechservice.ms.tts.mediaengine;

import java.io.PrintStream;
/* loaded from: classes.dex */
public class ByteUtils {
    public static int bytes2Int(byte[] src) {
        int value = (src[0] & 255) | ((src[1] & 255) << 8) | ((src[2] & 255) << 16) | ((src[3] & 255) << 24);
        return value;
    }

    public static byte[] int2bytes(int value) {
        PrintStream printStream = System.out;
        printStream.print("length: int = " + value);
        byte[] src = {(byte) (value & 255), (byte) ((value >> 8) & 255), (byte) ((value >> 16) & 255), (byte) ((value >> 24) & 255)};
        PrintStream printStream2 = System.out;
        printStream2.print("length: bytes = " + src);
        return src;
    }
}
