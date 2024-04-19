package com.xiaopeng.xpspeechservice.ms.tts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.HashMap;
/* loaded from: classes.dex */
public class CheckVoiceData extends Activity {
    private static final String[] supportedLanguages = {"zho-CHN", "eng-USA"};

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        ArrayList<String> langCountryVars;
        super.onCreate(savedInstanceState);
        int result = 1;
        ArrayList<String> available = new ArrayList<>();
        ArrayList<String> unavailable = new ArrayList<>();
        HashMap<String, Boolean> languageCountry = new HashMap<>();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && (langCountryVars = bundle.getStringArrayList("checkVoiceDataFor")) != null) {
            for (int i = 0; i < langCountryVars.size(); i++) {
                if (langCountryVars.get(i).length() > 0) {
                    languageCountry.put(langCountryVars.get(i), true);
                }
            }
        }
        for (int i2 = 0; i2 < supportedLanguages.length; i2++) {
            if (languageCountry.size() < 1 || languageCountry.containsKey(supportedLanguages[i2])) {
                available.add(supportedLanguages[i2]);
            }
        }
        if (languageCountry.size() > 0) {
            result = 0;
        }
        Intent returnData = new Intent();
        returnData.putStringArrayListExtra("availableVoices", available);
        returnData.putStringArrayListExtra("unavailableVoices", unavailable);
        setResult(result, returnData);
        finish();
    }
}
