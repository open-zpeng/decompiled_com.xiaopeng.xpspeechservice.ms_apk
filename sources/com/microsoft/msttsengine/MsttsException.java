package com.microsoft.msttsengine;
/* loaded from: classes.dex */
public class MsttsException extends RuntimeException {
    public static final int TTSERR_ALREADY_INITIALIZED = -2147188734;
    public static final int TTSERR_AUDIOSEG_CRCFAILURE = -2147188700;
    public static final int TTSERR_BAUDIO_LOAD_FAILED = -2147188697;
    public static final int TTSERR_BUFFER_TOO_SMALL = -2147188723;
    public static final int TTSERR_CORRUPTDATA = -2147188712;
    public static final int TTSERR_CORRUPTFILE = -2147188721;
    public static final int TTSERR_DATA_EXISTED = -2147188709;
    public static final int TTSERR_DATA_NOTEXIST = -2147188710;
    public static final int TTSERR_EXCEPTION_IN_INITIALIZATION = -2147188703;
    public static final int TTSERR_FAILED_TO_PARSE = -2147188711;
    public static final int TTSERR_FILE_MUST_BE_UNICODE = -2147188726;
    public static final int TTSERR_FORMAT_NOT_SPECIFIED = -2147188722;
    public static final int TTSERR_INPUT_LENGTH_EXCEEDED = -2147188696;
    public static final int TTSERR_INVALID_FLAGS = -2147188732;
    public static final int TTSERR_INVALID_INI = -2147188705;
    public static final int TTSERR_INVALID_LETTER = -2147188702;
    public static final int TTSERR_INVALID_OBJECTSTATE = -2147188719;
    public static final int TTSERR_INVALID_PHONEME = -2147188714;
    public static final int TTSERR_INVALID_SETTING = -2147188708;
    public static final int TTSERR_ITEM_DUPLICATED = -2147188704;
    public static final int TTSERR_ITEM_EXISTED = -2147188706;
    public static final int TTSERR_ITEM_NOT_FOUND = -2147188707;
    public static final int TTSERR_ITEM_OUT_OF_ORDER = -2147188705;
    public static final int TTSERR_LEXICON_LOAD_FAILED = -2147188699;
    public static final int TTSERR_NOT_IN_LEX = -2147188713;
    public static final int TTSERR_OPENFILE = -2147188720;
    public static final int TTSERR_OUTPUT_NOISE = -2147188701;
    public static final int TTSERR_PHONEME_CONVERSION_FAILED = -2147188698;
    public static final int TTSERR_READFILE = -2147188718;
    public static final int TTSERR_TEMPLATE_EXCEPTION = -2147188715;
    public static final int TTSERR_UNINITIALIZED = -2147188735;
    public static final int TTSERR_UNIT_NAME_NOT_FOUND = -2147188695;
    public static final int TTSERR_UNSUPPORTED_FORMAT = -2147188733;
    public static final int TTSERR_UNSUPPORTED_LANGUAGE = -2147188716;
    public static final int TTSERR_WRITEFILE = -2147188717;
    private int errorCode;

    public MsttsException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return this.errorCode;
    }
}
