package com.example.licenta20.ui.ai;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.licenta20.BuildConfig;
import com.example.licenta20.R;
import com.example.licenta20.data.AppConfig;
import com.example.licenta20.data.AppDatabase;
import com.example.licenta20.data.DailyStat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AIFragment extends Fragment {
    private AppDatabase db;
    private LinearLayout chatContainer;
    private ScrollView chatScrollView;
    private EditText etMessage;
    private Button btnSend;

    public AIFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai, container, false);

        db = AppDatabase.getInstance(requireContext());
        chatContainer = view.findViewById(R.id.chatContainer);
        chatScrollView = view.findViewById(R.id.chatScrollView);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);

        addMessageToChat("Kairos AI", "Salut! Sunt antrenorul tău de productivitate. Cum te simți astăzi și cum te pot ajuta?", false);

        btnSend.setOnClickListener(v -> {
            String userText = etMessage.getText().toString().trim();
            if (!userText.isEmpty()) {
                addMessageToChat("Tu", userText, true);
                etMessage.setText("");
                generateGeminiResponse(userText);
            }
        });

        return view;
    }

    private void addMessageToChat(String sender, String message, boolean isUser) {
        TextView tv = new TextView(getContext());
        tv.setText(sender + ":\n" + message);
        tv.setTextColor(Color.WHITE);
        tv.setPadding(40, 30, 40, 30);
        tv.setTextSize(16f);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 16, 0, 16);

        if (isUser) {
            tv.setBackgroundColor(Color.parseColor("#333333"));
            params.gravity = Gravity.END;
        } else {
            tv.setBackgroundColor(Color.parseColor("#9C27B0"));
            params.gravity = Gravity.START;
        }
        tv.setLayoutParams(params);
        chatContainer.addView(tv);
        chatScrollView.post(() -> chatScrollView.fullScroll(View.FOCUS_DOWN));
    }

    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private void generateGeminiResponse(String userMessage) {
        addMessageToChat("Kairos AI", "Se gândește...", false);

        new Thread(() -> {
            try {
                // Extragem progresul utilizatorului
                String today = getTodayDate();
                DailyStat todayStat = db.dailyStatDao().getStatForDate(today);
                long totalTimeToday = (todayStat != null) ? todayStat.getTotalFocusTime() : 0;
                int focusMinutes = (int) (totalTimeToday / (1000 * 60));

                List<AppConfig> allConfigs = db.appDao().getAllConfigs();
                int totalIntercepts = 0;
                for (AppConfig config : allConfigs) {
                    totalIntercepts += config.getInterceptCount();
                }

                String systemContext = "Ești Kairos AI, un asistent de productivitate empatic. Utilizatorul a stat concentrat " + focusMinutes + " minute și a avut " + totalIntercepts + " blocări. Răspunde prietenos: " + userMessage;

                // Apelăm Gemini API ascunzând cheia prin BuildConfig
                URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + BuildConfig.GEMINI_API_KEY);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // Construim JSON-ul
                JSONObject jsonBody = new JSONObject();
                JSONObject part = new JSONObject().put("text", systemContext);
                JSONObject content = new JSONObject().put("parts", new JSONArray().put(part));
                jsonBody.put("contents", new JSONArray().put(content));

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.toString().getBytes("UTF-8"));
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);

                    JSONObject jsonResponse = new JSONObject(sb.toString());
                    String aiText = jsonResponse.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");

                    requireActivity().runOnUiThread(() -> {
                        chatContainer.removeViewAt(chatContainer.getChildCount() - 1);
                        addMessageToChat("Kairos AI", aiText, false);
                    });
                } else {
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
                    StringBuilder errorSb = new StringBuilder();
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) errorSb.append(errorLine);
                    throw new Exception("Eroare Server: " + errorSb.toString());
                }

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    chatContainer.removeViewAt(chatContainer.getChildCount() - 1);
                    addMessageToChat("Eroare", e.getMessage(), false);
                });
            }
        }).start();
    }
}