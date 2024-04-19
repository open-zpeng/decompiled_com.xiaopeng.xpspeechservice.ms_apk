package com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http;

import android.support.annotation.Nullable;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.xmart.IServerCallback;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
/* loaded from: classes.dex */
public interface IRequest {
    IResponse execute() throws IOException;

    void execute(Callback callback);

    void execute(IServerCallback callback);

    String getUrl();

    String header(String key);

    IRequest headers(String key, String value);

    @Nullable
    Map<String, List<String>> headers();

    IRequest isMultipart(boolean isMultipart);

    IRequest params(String key, float value);

    IRequest params(String key, int value);

    IRequest params(String key, File file);

    IRequest params(String key, String value);

    IRequest params(String key, boolean value);

    IRequest params(Map<String, String> params, boolean... isReplace);

    IRequest removeAllHeaders();

    IRequest removeAllParams();

    IRequest removeHeader(String key);

    IRequest removeParam(String key);

    IRequest tag(Object tag);

    IRequest uploadFile(String file) throws IOException;

    IRequest uploadJson(String json);
}
