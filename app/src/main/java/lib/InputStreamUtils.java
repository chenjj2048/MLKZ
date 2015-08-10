package lib;

/**
 * Created by 1 on 2015/7/2.
 * java中byte,String,InputStream之间的转换
 * 来源：http://zhoujingxian.iteye.com/blog/1682480
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamUtils {

    final static int BUFFER_SIZE = 4096;

    // 将InputStream转换成String
    public static String InputStreamTOString(InputStream in) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[BUFFER_SIZE];
        int count = -1;
        while ((count = in.read(data, 0, BUFFER_SIZE)) != -1)
            outStream.write(data, 0, count);
        data = null;
        return new String(outStream.toByteArray(), "ISO-8859-1");
    }

    // 将InputStream转换成某种字符编码的String
    public static String InputStreamTOString(InputStream in, String encoding) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[BUFFER_SIZE];
        int count = -1;
        while ((count = in.read(data, 0, BUFFER_SIZE)) != -1)
            outStream.write(data, 0, count);
        data = null;
        return new String(outStream.toByteArray(), "ISO-8859-1");
    }

    // 将String转换成InputStream
    public static InputStream StringTOInputStream(String in) throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes("ISO-8859-1"));
        return is;
    }

    // 将InputStream转换成byte数组
    public static byte[] InputStreamTOByte(InputStream in) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[BUFFER_SIZE];
        int count = -1;
        while ((count = in.read(data, 0, BUFFER_SIZE)) != -1)
            outStream.write(data, 0, count);
        data = null;
        return outStream.toByteArray();
    }

    // 将byte数组转换成InputStream
    public static InputStream byteTOInputStream(byte[] in) throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(in);
        return is;
    }

    // 将byte数组转换成String
    public static String byteTOString(byte[] in) throws Exception {
        InputStream is = byteTOInputStream(in);
        return InputStreamTOString(is);
    }

    //==============================以下为自己写===================================
    //字节流转图片
    public static Bitmap bytesToBitmap(byte[] in) {
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = InputStreamUtils.byteTOInputStream(in);
            BitmapFactory.Options opts = new BitmapFactory.Options();
//            opts.inSampleSize = 2;
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            bitmap = BitmapFactory.decodeStream(is, null, opts);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null)
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        return bitmap;
    }
}