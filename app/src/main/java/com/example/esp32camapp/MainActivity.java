package com.example.esp32camapp;

import android.app.DownloadManager;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private TextView txtStatus;
    private Button btnRefresh;
    private Button btnSavePhoto;
    private Button btnSyncPhotos;

    private String ip = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        txtStatus = findViewById(R.id.txtStatus);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnSavePhoto = findViewById(R.id.btnSavePhoto);
        btnSyncPhotos = findViewById(R.id.btnSyncPhotos); // 新增同步按钮

        SharedPreferences prefs = getSharedPreferences("esp32", MODE_PRIVATE);
        ip = prefs.getString("ip", "");

        if (!ip.isEmpty() && !ip.equals("0.0.0.0")) {
            txtStatus.setText("读取到 ESP32 IP: " + ip);
            String url = "http://" + ip + "/liscct";

            // 配置 WebView
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebChromeClient(new WebChromeClient());

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    txtStatus.setText("正在加载控制页面...");
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    txtStatus.setText("页面加载完成 ✅");
                }

                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    txtStatus.setText("页面加载失败，请检查 ESP32 状态");
                    Log.e("WebView", "加载失败: " + description);
                }
            });

            webView.setDownloadListener((url1, userAgent, contentDisposition, mimeType, contentLength) -> {
                Log.d("WebView", "触发下载: " + url1);
                String fileName = "esp32_photo_" + System.currentTimeMillis() + ".jpg";
                downloadImage(url1, fileName);
            });

            webView.loadUrl(url);

            btnRefresh.setOnClickListener(v -> webView.reload());

            btnSavePhoto.setOnClickListener(v -> {
                String imageUrl = "http://" + ip + "/capture";
                String fileName = "esp32_photo_" + System.currentTimeMillis() + ".jpg";
                downloadImage(imageUrl, fileName);
            });

            btnSyncPhotos.setOnClickListener(v -> syncPhotosPaged());

        } else {
            txtStatus.setText("未找到有效的 IP 地址，请先进行配网");
            Toast.makeText(this, "请先设置 ESP32 的 IP 地址", Toast.LENGTH_LONG).show();
        }
    }

    // 保存单张图片
    private void downloadImage(String imageUrl, String fileName) {
        try {
            Uri uri = Uri.parse(imageUrl);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle("保存照片");
            request.setDescription("正在下载 " + fileName);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

            DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            manager.enqueue(request);

            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
            MediaScannerConnection.scanFile(
                    this,
                    new String[]{file.getAbsolutePath()},
                    new String[]{"image/jpeg"},
                    (path, uriResult) -> Log.d("MediaScanner", "照片已添加到图库: " + path)
            );

            Toast.makeText(this, "开始下载照片", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "下载失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ✅ 分页同步照片
    private void syncPhotosPaged() {
        new Thread(() -> {
            int page = 1;
            int limit = 5;
            boolean hasMore = true;

            try {
                while (hasMore) {
                    URL url = new URL("http://" + ip + "/list.json?page=" + page + "&limit=" + limit);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder json = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        json.append(line);
                    }
                    reader.close();

                    JSONArray photoList = new JSONArray(json.toString());
                    if (photoList.length() == 0) {
                        hasMore = false;
                        break;
                    }

                    for (int i = 0; i < photoList.length(); i++) {
                        String fileName = photoList.getString(i);
                        String downloadUrl = "http://" + ip + "/download?name=" + URLEncoder.encode(fileName, "UTF-8");

                        // 下载
                        URL fileUrl = new URL(downloadUrl);
                        HttpURLConnection fileConn = (HttpURLConnection) fileUrl.openConnection();
                        InputStream input = new BufferedInputStream(fileConn.getInputStream());

                        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "ESP32Sync");
                        if (!dir.exists()) dir.mkdirs();
                        File outFile = new File(dir, fileName);

                        OutputStream output = new FileOutputStream(outFile);
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = input.read(buffer)) != -1) {
                            output.write(buffer, 0, len);
                        }
                        output.flush();
                        output.close();
                        input.close();

                        // 通知图库
                        MediaScannerConnection.scanFile(this, new String[]{outFile.getAbsolutePath()}, null, null);

                        // 删除 ESP32 文件
                        URL delUrl = new URL("http://" + ip + "/delete?name=" + URLEncoder.encode(fileName, "UTF-8"));
                        HttpURLConnection delConn = (HttpURLConnection) delUrl.openConnection();
                        delConn.getResponseCode();
                        delConn.disconnect();
                    }

                    page++; // 下一页
                }

                runOnUiThread(() -> Toast.makeText(this, "同步完成 ✅", Toast.LENGTH_LONG).show());

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "同步失败：" + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}