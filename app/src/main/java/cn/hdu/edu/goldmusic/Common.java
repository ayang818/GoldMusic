package cn.hdu.edu.goldmusic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Handler;

public class Common {
    static ExecutorService threadPool = Executors.newFixedThreadPool(10);

    static final int LOGIN = 1;
    static final int FETCH_RECOMMEND_LIST = 2;
    static final int FETCH_SONG_DETAIL = 3;
    static final int DOWNLOAD_IMG = 4;
    static final int UPDATE_SEEKBAR = 5;
    static final int UPDATE_CUR_DURATION = 6;


    public static void buildRequests(String links, MyHandler handler, int code) {
        Runnable task = () -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(links);
                connection = (HttpURLConnection) url.openConnection();
                //设置请求方法
                connection.setRequestMethod("GET");
                //设置连接超时时间（毫秒）
                connection.setConnectTimeout(5000);
                //设置读取超时时间（毫秒）
                connection.setReadTimeout(5000);

                //返回输入流
                InputStream in = connection.getInputStream();

                //读取输入流
                reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                Message msg = Message.obtain();
                msg.what = code;
                msg.obj = result.toString();
                handler.sendMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (connection != null) {//关闭连接
                    connection.disconnect();
                }
            }
        };
        threadPool.execute(task);
    }

    public static void buildImgDownloadRequests(String links, MyHandler handler, int code) {
        Runnable task = () -> {
            URL imgUrl = null;
            Bitmap bitmap = null;
            try {
                imgUrl = new URL(links);
                HttpURLConnection conn = (HttpURLConnection) imgUrl
                        .openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(is);
                is.close();
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Message msg = Message.obtain();
            msg.what = code;
            msg.obj = bitmap;
            handler.sendMessage(msg);
        };
        threadPool.execute(task);
    }

}
