package com.ta.utdid2.c.a;

import com.ta.utdid2.c.a.b;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.WeakHashMap;
import org.xmlpull.v1.XmlPullParserException;
/* compiled from: TransactionXMLFile.java */
/* loaded from: classes.dex */
public class d {
    private static final Object c = new Object();
    private File a;
    private final Object b = new Object();

    /* renamed from: a  reason: collision with other field name */
    private HashMap<File, a> f148a = new HashMap<>();

    public d(String str) {
        if (str != null && str.length() > 0) {
            this.a = new File(str);
            return;
        }
        throw new RuntimeException("Directory can not be empty");
    }

    private File a(File file, String str) {
        if (str.indexOf(File.separatorChar) < 0) {
            return new File(file, str);
        }
        throw new IllegalArgumentException("File " + str + " contains a path separator");
    }

    private File a() {
        File file;
        synchronized (this.b) {
            file = this.a;
        }
        return file;
    }

    private File b(String str) {
        File a2 = a();
        return a(a2, String.valueOf(str) + ".xml");
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:120:0x00e3 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:128:0x00f7 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:135:0x00d7 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Type inference failed for: r0v12 */
    /* JADX WARN: Type inference failed for: r0v13 */
    /* JADX WARN: Type inference failed for: r0v25 */
    /* JADX WARN: Type inference failed for: r0v26 */
    /* JADX WARN: Type inference failed for: r0v27 */
    /* JADX WARN: Type inference failed for: r0v31 */
    /* JADX WARN: Type inference failed for: r0v33 */
    /* JADX WARN: Type inference failed for: r0v34 */
    /* JADX WARN: Type inference failed for: r0v35 */
    /* JADX WARN: Type inference failed for: r0v7 */
    /* JADX WARN: Type inference failed for: r3v10, types: [java.io.FileInputStream] */
    /* JADX WARN: Type inference failed for: r3v11, types: [java.io.FileInputStream] */
    /* JADX WARN: Type inference failed for: r3v12 */
    /* JADX WARN: Type inference failed for: r3v13, types: [java.io.FileInputStream] */
    /* JADX WARN: Type inference failed for: r3v14 */
    /* JADX WARN: Type inference failed for: r3v19, types: [org.xmlpull.v1.XmlPullParserException] */
    /* JADX WARN: Type inference failed for: r3v22 */
    /* JADX WARN: Type inference failed for: r3v23 */
    /* JADX WARN: Type inference failed for: r3v24 */
    /* JADX WARN: Type inference failed for: r3v9 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public com.ta.utdid2.c.a.b a(java.lang.String r10, int r11) {
        /*
            Method dump skipped, instructions count: 281
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.ta.utdid2.c.a.d.a(java.lang.String, int):com.ta.utdid2.c.a.b");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static File a(File file) {
        return new File(String.valueOf(file.getPath()) + ".bak");
    }

    /* compiled from: TransactionXMLFile.java */
    /* loaded from: classes.dex */
    private static final class a implements b {
        private static final Object d = new Object();
        private WeakHashMap<b.InterfaceC0015b, Object> a;
        private final File b;
        private final int c;

        /* renamed from: c  reason: collision with other field name */
        private final File f149c;

        /* renamed from: c  reason: collision with other field name */
        private Map f150c;
        private boolean k = false;

        a(File file, int i, Map map) {
            this.b = file;
            this.f149c = d.a(file);
            this.c = i;
            this.f150c = map == null ? new HashMap() : map;
            this.a = new WeakHashMap<>();
        }

        @Override // com.ta.utdid2.c.a.b
        /* renamed from: a */
        public boolean mo62a() {
            File file = this.b;
            if (file != null && new File(file.getAbsolutePath()).exists()) {
                return true;
            }
            return false;
        }

        public void a(boolean z) {
            synchronized (this) {
                this.k = z;
            }
        }

        public boolean c() {
            boolean z;
            synchronized (this) {
                z = this.k;
            }
            return z;
        }

        public void a(Map map) {
            if (map != null) {
                synchronized (this) {
                    this.f150c = map;
                }
            }
        }

        @Override // com.ta.utdid2.c.a.b
        public Map<String, ?> getAll() {
            HashMap hashMap;
            synchronized (this) {
                hashMap = new HashMap(this.f150c);
            }
            return hashMap;
        }

        @Override // com.ta.utdid2.c.a.b
        public String getString(String key, String defValue) {
            String str;
            synchronized (this) {
                str = (String) this.f150c.get(key);
                if (str == null) {
                    str = defValue;
                }
            }
            return str;
        }

        @Override // com.ta.utdid2.c.a.b
        public long getLong(String key, long defValue) {
            long longValue;
            synchronized (this) {
                Long l = (Long) this.f150c.get(key);
                longValue = l != null ? l.longValue() : defValue;
            }
            return longValue;
        }

        /* compiled from: TransactionXMLFile.java */
        /* renamed from: com.ta.utdid2.c.a.d$a$a  reason: collision with other inner class name */
        /* loaded from: classes.dex */
        public final class C0016a implements b.a {
            private final Map<String, Object> d = new HashMap();
            private boolean l = false;

            public C0016a() {
            }

            @Override // com.ta.utdid2.c.a.b.a
            public b.a a(String str, String str2) {
                synchronized (this) {
                    this.d.put(str, str2);
                }
                return this;
            }

            @Override // com.ta.utdid2.c.a.b.a
            public b.a a(String str, int i) {
                synchronized (this) {
                    this.d.put(str, Integer.valueOf(i));
                }
                return this;
            }

            @Override // com.ta.utdid2.c.a.b.a
            public b.a a(String str, long j) {
                synchronized (this) {
                    this.d.put(str, Long.valueOf(j));
                }
                return this;
            }

            @Override // com.ta.utdid2.c.a.b.a
            public b.a a(String str, float f) {
                synchronized (this) {
                    this.d.put(str, Float.valueOf(f));
                }
                return this;
            }

            @Override // com.ta.utdid2.c.a.b.a
            public b.a a(String str, boolean z) {
                synchronized (this) {
                    this.d.put(str, Boolean.valueOf(z));
                }
                return this;
            }

            @Override // com.ta.utdid2.c.a.b.a
            public b.a a(String str) {
                synchronized (this) {
                    this.d.put(str, this);
                }
                return this;
            }

            @Override // com.ta.utdid2.c.a.b.a
            public b.a b() {
                synchronized (this) {
                    this.l = true;
                }
                return this;
            }

            @Override // com.ta.utdid2.c.a.b.a
            public boolean commit() {
                boolean z;
                ArrayList arrayList;
                HashSet<b.InterfaceC0015b> hashSet;
                boolean d;
                synchronized (d.c) {
                    z = a.this.a.size() > 0;
                    arrayList = null;
                    if (!z) {
                        hashSet = null;
                    } else {
                        arrayList = new ArrayList();
                        hashSet = new HashSet(a.this.a.keySet());
                    }
                    synchronized (this) {
                        if (this.l) {
                            a.this.f150c.clear();
                            this.l = false;
                        }
                        for (Map.Entry<String, Object> entry : this.d.entrySet()) {
                            String key = entry.getKey();
                            Object value = entry.getValue();
                            if (value == this) {
                                a.this.f150c.remove(key);
                            } else {
                                a.this.f150c.put(key, value);
                            }
                            if (z) {
                                arrayList.add(key);
                            }
                        }
                        this.d.clear();
                    }
                    d = a.this.d();
                    if (d) {
                        a.this.a(true);
                    }
                }
                if (z) {
                    for (int size = arrayList.size() - 1; size >= 0; size--) {
                        String str = (String) arrayList.get(size);
                        for (b.InterfaceC0015b interfaceC0015b : hashSet) {
                            if (interfaceC0015b != null) {
                                interfaceC0015b.a(a.this, str);
                            }
                        }
                    }
                }
                return d;
            }
        }

        @Override // com.ta.utdid2.c.a.b
        public b.a a() {
            return new C0016a();
        }

        private FileOutputStream a(File file) {
            try {
                return new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                if (!file.getParentFile().mkdir()) {
                    return null;
                }
                try {
                    return new FileOutputStream(file);
                } catch (FileNotFoundException e2) {
                    return null;
                }
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean d() {
            if (this.b.exists()) {
                if (!this.f149c.exists()) {
                    if (!this.b.renameTo(this.f149c)) {
                        return false;
                    }
                } else {
                    this.b.delete();
                }
            }
            try {
                FileOutputStream a = a(this.b);
                if (a == null) {
                    return false;
                }
                e.a(this.f150c, a);
                a.close();
                this.f149c.delete();
                return true;
            } catch (IOException | XmlPullParserException e) {
                if (this.b.exists()) {
                    this.b.delete();
                }
                return false;
            }
        }
    }
}
