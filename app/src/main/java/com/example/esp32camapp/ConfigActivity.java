package com.example.esp32camapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.*;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigActivity extends AppCompatActivity {

    private EditText editSsid, editPassword;
    private Button btnSend;
    private TextView txtIpResult;

    private final OkHttpClient client = new OkHttpClient();
    private final String ESP32_URL = "http://192.168.4.1/connect"; // 注意：ESP32 热点下的固定 IP

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        // 初始化控件
        editSsid = findViewById(R.id.editSsid);
        editPassword = findViewById(R.id.editPassword);
        btnSend = findViewById(R.id.btnSend);
        txtIpResult = findViewById(R.id.txtIpResult);

        btnSend.setOnClickListener(v -> sendWifiConfig());

    }

    private void sendWifiConfig() {
        String ssid = editSsid.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (ssid.isEmpty()) {
            Toast.makeText(this, "请输入 Wi-Fi 名称（SSID）", Toast.LENGTH_SHORT).show();
            return;
        }

        // 构建 POST 请求体
        RequestBody formBody = new FormBody.Builder()
                .add("ssid", ssid)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url(ESP32_URL)
                .post(formBody)
                .build();

        // 异步发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(ConfigActivity.this, "发送失败，请连接 ESP32 热点", Toast.LENGTH_LONG).show()
                );
                Log.e("ConfigWiFi", "请求失败", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                Log.d("ConfigWiFi", "ESP32 返回内容：" + res);

                runOnUiThread(() -> {
                    Toast.makeText(ConfigActivity.this, "ESP32 响应: " + res, Toast.LENGTH_SHORT).show();

                    // 提取 IP 地址
                    String ip = extractIpFromResponse(res);
                    if (!ip.isEmpty() && !ip.equals("0.0.0.0")) {
                        txtIpResult.setText("ESP32 返回的 IP: " + ip);

                        // ✅ 保存 IP 地址到本地
                        getSharedPreferences("esp32", MODE_PRIVATE)
                                .edit()
                                .putString("ip", ip)
                                .apply();

                        // 延迟跳转到 MainActivity
                        //new Handler().postDelayed(() -> {
                            //Intent intent = new Intent(ConfigActivity.this, MainActivity.class);
                            //startActivity(intent);
                            //finish(); // 关闭当前页面
                        //}, 3000); // 3 秒后跳转
                    } else {
                        txtIpResult.setText("ESP32 返回的 IP 无效，请稍后重试");
                        Toast.makeText(ConfigActivity.this, "ESP32 尚未获取到有效 IP，请稍后再试", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    // 使用正则提取 IP 地址
    private String extractIpFromResponse(String response) {
        Pattern pattern = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)");
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
}