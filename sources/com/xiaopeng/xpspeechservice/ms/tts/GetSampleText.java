package com.xiaopeng.xpspeechservice.ms.tts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.xiaopeng.xpspeechservice.ms.R;
import com.xiaopeng.xpspeechservice.utils.LogUtils;
/* loaded from: classes.dex */
public class GetSampleText extends Activity {
    private static final String EXTRA_SAMPLE_TEXT = "sampleText";
    private static final String TAG = "GetSampleText";

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int result = 0;
        Intent returnData = new Intent();
        Intent i = getIntent();
        String language = i.getExtras().getString("language");
        String country = i.getExtras().getString("country");
        String variant = i.getExtras().getString("variant");
        LogUtils.i(TAG, "language=" + language + " country=" + country + " variant=" + variant);
        if (language != null) {
            if (language.equals("en")) {
                returnData.putExtra(EXTRA_SAMPLE_TEXT, getString(R.string.tts_sample_en));
            } else if (language.equals("zh")) {
                returnData.putExtra(EXTRA_SAMPLE_TEXT, getString(R.string.tts_sample_zh));
            } else {
                result = -2;
                returnData.putExtra(EXTRA_SAMPLE_TEXT, "");
            }
        } else {
            result = -2;
            returnData.putExtra(EXTRA_SAMPLE_TEXT, "");
        }
        setResult(result, returnData);
        finish();
    }
}
