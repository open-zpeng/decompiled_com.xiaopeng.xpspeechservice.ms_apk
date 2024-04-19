package com.xiaopeng.lib.utils.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
/* loaded from: classes.dex */
public class BitmapUtils {
    public static void saveAsPicture(int w, int h, byte[] data, String savePath) {
        int[] RGBData = new int[w * h];
        decodeYUV420SP(RGBData, data, w, h);
        Bitmap bm = Bitmap.createBitmap(RGBData, w, h, Bitmap.Config.ARGB_8888);
        try {
            FileOutputStream outputStream = new FileOutputStream(savePath);
            bm.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap byte2Bmp(int w, int h, byte[] data, Bitmap.Config config) {
        int[] RGBData = new int[w * h];
        decodeYUV420SP(RGBData, data, w, h);
        Bitmap bm = Bitmap.createBitmap(RGBData, w, h, config);
        return bm;
    }

    public static Bitmap byte2Bmp(int w, int h, byte[] data, int quality) {
        YuvImage image = new YuvImage(data, 17, w, h, null);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compressToJpeg(new Rect(0, 0, w, h), quality, stream);
        Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmp;
    }

    private static void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
        int frameSize = width * height;
        int yp = 0;
        for (int j = 0; j < height; j++) {
            int v = ((j >> 1) * width) + frameSize;
            int u = 0;
            int uvp = 0;
            int i = 0;
            while (i < width) {
                int y = (yuv420sp[yp] & 255) - 16;
                if (y < 0) {
                    y = 0;
                }
                if ((i & 1) == 0) {
                    int uvp2 = v + 1;
                    int uvp3 = yuv420sp[v];
                    int v2 = (uvp3 & 255) - 128;
                    int v3 = uvp2 + 1;
                    u = (yuv420sp[uvp2] & 255) - 128;
                    uvp = v2;
                    v = v3;
                }
                int y1192 = y * 1192;
                int r = (uvp * 1634) + y1192;
                int g = (y1192 - (uvp * 833)) - (u * 400);
                int b = (u * 2066) + y1192;
                if (r < 0) {
                    r = 0;
                } else if (r > 262143) {
                    r = 262143;
                }
                if (g < 0) {
                    g = 0;
                } else if (g > 262143) {
                    g = 262143;
                }
                if (b < 0) {
                    b = 0;
                } else if (b > 262143) {
                    b = 262143;
                }
                rgb[yp] = (-16777216) | ((r << 6) & 16711680) | ((g >> 2) & 65280) | ((b >> 10) & 255);
                i++;
                yp++;
            }
        }
    }
}
