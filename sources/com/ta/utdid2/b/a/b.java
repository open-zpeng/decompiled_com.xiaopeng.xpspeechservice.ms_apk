package com.ta.utdid2.b.a;

import java.io.UnsupportedEncodingException;
/* compiled from: Base64.java */
/* loaded from: classes.dex */
public class b {
    static final /* synthetic */ boolean a = !b.class.desiredAssertionStatus();

    /* JADX INFO: Access modifiers changed from: package-private */
    /* compiled from: Base64.java */
    /* loaded from: classes.dex */
    public static abstract class a {
        public int a;
        public byte[] b;

        a() {
        }
    }

    public static byte[] decode(String str, int flags) {
        return decode(str.getBytes(), flags);
    }

    public static byte[] decode(byte[] input, int flags) {
        return decode(input, 0, input.length, flags);
    }

    public static byte[] decode(byte[] input, int offset, int len, int flags) {
        C0014b c0014b = new C0014b(flags, new byte[(len * 3) / 4]);
        if (!c0014b.a(input, offset, len, true)) {
            throw new IllegalArgumentException("bad base-64");
        }
        if (c0014b.a == c0014b.b.length) {
            return c0014b.b;
        }
        byte[] bArr = new byte[c0014b.a];
        System.arraycopy(c0014b.b, 0, bArr, 0, c0014b.a);
        return bArr;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* compiled from: Base64.java */
    /* renamed from: com.ta.utdid2.b.a.b$b  reason: collision with other inner class name */
    /* loaded from: classes.dex */
    public static class C0014b extends a {
        private static final int[] a = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        private static final int[] b = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, 63, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        private final int[] c;
        private int state;
        private int value;

        public C0014b(int i, byte[] bArr) {
            this.b = bArr;
            this.c = (i & 8) == 0 ? a : b;
            this.state = 0;
            this.value = 0;
        }

        public boolean a(byte[] bArr, int i, int i2, boolean z) {
            int i3 = this.state;
            if (i3 == 6) {
                return false;
            }
            int i4 = i2 + i;
            int i5 = this.value;
            byte[] bArr2 = this.b;
            int[] iArr = this.c;
            int i6 = 0;
            int i7 = i5;
            int i8 = i3;
            int i9 = i;
            while (i9 < i4) {
                if (i8 == 0) {
                    while (true) {
                        int i10 = i9 + 4;
                        if (i10 > i4) {
                            break;
                        }
                        i7 = iArr[bArr[i9 + 3] & 255] | (iArr[bArr[i9 + 1] & 255] << 12) | (iArr[bArr[i9] & 255] << 18) | (iArr[bArr[i9 + 2] & 255] << 6);
                        if (i7 < 0) {
                            break;
                        }
                        bArr2[i6 + 2] = (byte) i7;
                        bArr2[i6 + 1] = (byte) (i7 >> 8);
                        bArr2[i6] = (byte) (i7 >> 16);
                        i6 += 3;
                        i9 = i10;
                    }
                    if (i9 >= i4) {
                        break;
                    }
                }
                int i11 = i9 + 1;
                int i12 = iArr[bArr[i9] & 255];
                if (i8 != 0) {
                    if (i8 != 1) {
                        if (i8 != 2) {
                            if (i8 != 3) {
                                if (i8 != 4) {
                                    if (i8 == 5 && i12 != -1) {
                                        this.state = 6;
                                        return false;
                                    }
                                } else if (i12 == -2) {
                                    i8++;
                                    i9 = i11;
                                } else if (i12 != -1) {
                                    this.state = 6;
                                    return false;
                                }
                                i9 = i11;
                            } else if (i12 >= 0) {
                                i7 = (i7 << 6) | i12;
                                bArr2[i6 + 2] = (byte) i7;
                                bArr2[i6 + 1] = (byte) (i7 >> 8);
                                bArr2[i6] = (byte) (i7 >> 16);
                                i6 += 3;
                                i8 = 0;
                                i9 = i11;
                            } else if (i12 == -2) {
                                bArr2[i6 + 1] = (byte) (i7 >> 2);
                                bArr2[i6] = (byte) (i7 >> 10);
                                i6 += 2;
                                i9 = i11;
                                i8 = 5;
                            } else {
                                if (i12 != -1) {
                                    this.state = 6;
                                    return false;
                                }
                                i9 = i11;
                            }
                        } else if (i12 >= 0) {
                            i7 = (i7 << 6) | i12;
                            i8++;
                            i9 = i11;
                        } else if (i12 == -2) {
                            bArr2[i6] = (byte) (i7 >> 4);
                            i6++;
                            i9 = i11;
                            i8 = 4;
                        } else {
                            if (i12 != -1) {
                                this.state = 6;
                                return false;
                            }
                            i9 = i11;
                        }
                    } else if (i12 >= 0) {
                        i7 = (i7 << 6) | i12;
                        i8++;
                        i9 = i11;
                    } else {
                        if (i12 != -1) {
                            this.state = 6;
                            return false;
                        }
                        i9 = i11;
                    }
                } else if (i12 >= 0) {
                    i8++;
                    i7 = i12;
                    i9 = i11;
                } else {
                    if (i12 != -1) {
                        this.state = 6;
                        return false;
                    }
                    i9 = i11;
                }
            }
            if (!z) {
                this.state = i8;
                this.value = i7;
                this.a = i6;
                return true;
            }
            if (i8 != 0) {
                if (i8 == 1) {
                    this.state = 6;
                    return false;
                } else if (i8 == 2) {
                    bArr2[i6] = (byte) (i7 >> 4);
                    i6++;
                } else if (i8 == 3) {
                    int i13 = i6 + 1;
                    bArr2[i6] = (byte) (i7 >> 10);
                    i6 = i13 + 1;
                    bArr2[i13] = (byte) (i7 >> 2);
                } else if (i8 == 4) {
                    this.state = 6;
                    return false;
                }
            }
            this.state = i8;
            this.a = i6;
            return true;
        }
    }

    public static String encodeToString(byte[] input, int flags) {
        try {
            return new String(encode(input, flags), "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    public static byte[] encode(byte[] input, int flags) {
        return encode(input, 0, input.length, flags);
    }

    public static byte[] encode(byte[] input, int offset, int len, int flags) {
        c cVar = new c(flags, null);
        int i = (len / 3) * 4;
        if (cVar.f135b) {
            if (len % 3 > 0) {
                i += 4;
            }
        } else {
            int i2 = len % 3;
            if (i2 != 0) {
                if (i2 == 1) {
                    i += 2;
                } else if (i2 == 2) {
                    i += 3;
                }
            }
        }
        if (cVar.f136c && len > 0) {
            i += (((len - 1) / 57) + 1) * (cVar.f137d ? 2 : 1);
        }
        cVar.b = new byte[i];
        cVar.a(input, offset, len, true);
        if (a || cVar.a == i) {
            return cVar.b;
        }
        throw new AssertionError();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* compiled from: Base64.java */
    /* loaded from: classes.dex */
    public static class c extends a {
        static final /* synthetic */ boolean a = !b.class.desiredAssertionStatus();
        private static final byte[] c = {65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47};
        private static final byte[] d = {65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 45, 95};
        int b;

        /* renamed from: b  reason: collision with other field name */
        public final boolean f135b;

        /* renamed from: c  reason: collision with other field name */
        public final boolean f136c;
        private int count;

        /* renamed from: d  reason: collision with other field name */
        public final boolean f137d;
        private final byte[] e;
        private final byte[] f;

        public c(int i, byte[] bArr) {
            this.b = bArr;
            this.f135b = (i & 1) == 0;
            this.f136c = (i & 2) == 0;
            this.f137d = (i & 4) != 0;
            this.f = (i & 8) == 0 ? c : d;
            this.e = new byte[2];
            this.b = 0;
            this.count = this.f136c ? 19 : -1;
        }

        /* JADX WARN: Removed duplicated region for block: B:105:0x009c A[EDGE_INSN: B:105:0x009c->B:28:0x009c ?: BREAK  , SYNTHETIC] */
        /* JADX WARN: Removed duplicated region for block: B:17:0x0061  */
        /* JADX WARN: Removed duplicated region for block: B:25:0x0097  */
        /* JADX WARN: Removed duplicated region for block: B:29:0x009e  */
        /* JADX WARN: Removed duplicated region for block: B:87:0x0196  */
        /* JADX WARN: Removed duplicated region for block: B:96:0x01c8  */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        public boolean a(byte[] r18, int r19, int r20, boolean r21) {
            /*
                Method dump skipped, instructions count: 546
                To view this dump add '--comments-level debug' option
            */
            throw new UnsupportedOperationException("Method not decompiled: com.ta.utdid2.b.a.b.c.a(byte[], int, int, boolean):boolean");
        }
    }

    private b() {
    }
}
