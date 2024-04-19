package com.danikula.videocache.sourcestorage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.danikula.videocache.Preconditions;
import com.danikula.videocache.SourceInfo;
/* loaded from: classes.dex */
class DatabaseSourceInfoStorage extends SQLiteOpenHelper implements SourceInfoStorage {
    private static final String COLUMN_URL = "url";
    private static final String CREATE_SQL = "CREATE TABLE SourceInfo (_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,url TEXT NOT NULL,mime TEXT,length INTEGER);";
    private static final String TABLE = "SourceInfo";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_LENGTH = "length";
    private static final String COLUMN_MIME = "mime";
    private static final String[] ALL_COLUMNS = {COLUMN_ID, "url", COLUMN_LENGTH, COLUMN_MIME};

    /* JADX INFO: Access modifiers changed from: package-private */
    public DatabaseSourceInfoStorage(Context context) {
        super(context, "AndroidVideoCache.db", (SQLiteDatabase.CursorFactory) null, 1);
        Preconditions.checkNotNull(context);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onCreate(SQLiteDatabase db) {
        Preconditions.checkNotNull(db);
        db.execSQL(CREATE_SQL);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new IllegalStateException("Should not be called. There is no any migration");
    }

    /* JADX WARN: Removed duplicated region for block: B:12:0x002d A[DONT_GENERATE] */
    @Override // com.danikula.videocache.sourcestorage.SourceInfoStorage
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public com.danikula.videocache.SourceInfo get(java.lang.String r10) {
        /*
            r9 = this;
            com.danikula.videocache.Preconditions.checkNotNull(r10)
            r0 = 0
            android.database.sqlite.SQLiteDatabase r1 = r9.getReadableDatabase()     // Catch: java.lang.Throwable -> L31
            java.lang.String r2 = "SourceInfo"
            java.lang.String[] r3 = com.danikula.videocache.sourcestorage.DatabaseSourceInfoStorage.ALL_COLUMNS     // Catch: java.lang.Throwable -> L31
            java.lang.String r4 = "url=?"
            r5 = 1
            java.lang.String[] r5 = new java.lang.String[r5]     // Catch: java.lang.Throwable -> L31
            r6 = 0
            r5[r6] = r10     // Catch: java.lang.Throwable -> L31
            r6 = 0
            r7 = 0
            r8 = 0
            android.database.Cursor r1 = r1.query(r2, r3, r4, r5, r6, r7, r8)     // Catch: java.lang.Throwable -> L31
            r0 = r1
            if (r0 == 0) goto L2a
            boolean r1 = r0.moveToFirst()     // Catch: java.lang.Throwable -> L31
            if (r1 != 0) goto L25
            goto L2a
        L25:
            com.danikula.videocache.SourceInfo r1 = r9.convert(r0)     // Catch: java.lang.Throwable -> L31
            goto L2b
        L2a:
            r1 = 0
        L2b:
            if (r0 == 0) goto L30
            r0.close()
        L30:
            return r1
        L31:
            r1 = move-exception
            if (r0 == 0) goto L37
            r0.close()
        L37:
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.danikula.videocache.sourcestorage.DatabaseSourceInfoStorage.get(java.lang.String):com.danikula.videocache.SourceInfo");
    }

    @Override // com.danikula.videocache.sourcestorage.SourceInfoStorage
    public void put(String url, SourceInfo sourceInfo) {
        Preconditions.checkAllNotNull(url, sourceInfo);
        SourceInfo sourceInfoFromDb = get(url);
        boolean exist = sourceInfoFromDb != null;
        ContentValues contentValues = convert(sourceInfo);
        if (exist) {
            getWritableDatabase().update(TABLE, contentValues, "url=?", new String[]{url});
        } else {
            getWritableDatabase().insert(TABLE, null, contentValues);
        }
    }

    @Override // com.danikula.videocache.sourcestorage.SourceInfoStorage
    public void release() {
        close();
    }

    private SourceInfo convert(Cursor cursor) {
        return new SourceInfo(cursor.getString(cursor.getColumnIndexOrThrow("url")), cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LENGTH)), cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MIME)));
    }

    private ContentValues convert(SourceInfo sourceInfo) {
        ContentValues values = new ContentValues();
        values.put("url", sourceInfo.url);
        values.put(COLUMN_LENGTH, Long.valueOf(sourceInfo.length));
        values.put(COLUMN_MIME, sourceInfo.mime);
        return values;
    }
}
