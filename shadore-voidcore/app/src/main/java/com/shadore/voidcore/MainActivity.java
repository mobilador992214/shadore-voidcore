package com.shadore.voidcore;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends Activity {

    private final int BG = Color.rgb(7, 9, 18);
    private final int PANEL = Color.rgb(17, 22, 39);
    private final int PURPLE = Color.rgb(151, 72, 255);
    private final int BLUE = Color.rgb(0, 190, 255);
    private final int WHITE = Color.rgb(244, 246, 255);
    private final int MUTED = Color.rgb(167, 177, 205);

    private LinearLayout root;

    private TextView ramValue;
    private TextView storageValue;
    private TextView temperatureValue;
    private TextView deviceValue;

    private boolean gameMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        ScrollView scroll = new ScrollView(this);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(28));
        root.setBackgroundColor(BG);

        scroll.addView(root);
        setContentView(scroll);

        buildInterface();
        refreshStats(false);
    }

    private void buildInterface() {

        addText("shadøre", 34, WHITE, true);
        addText("vo!dcøre", 34, BLUE, true);

        addText(
                "ANDROID PERFORMANCE COMPANION",
                11,
                MUTED,
                true
        );

        addSpace(20);

        LinearLayout hero = panel();

        hero.addView(text(
                "PERFORMANCE DASHBOARD",
                12,
                BLUE,
                true
        ));

        hero.addView(text(
                "Optimalkan pengalaman bermain.",
                22,
                WHITE,
                true
        ), margin(0, 10, 0, 0));

        hero.addView(text(
                "Pantau informasi perangkat dan akses pengaturan Android dari satu tempat.",
                14,
                MUTED,
                false
        ), margin(0, 8, 0, 0));

        root.addView(hero);

        addSpace(14);

        addStatCard(
                "RAM TERSEDIA",
                "Membaca memori sistem"
        );

        addStatCard(
                "PENYIMPANAN",
                "Ruang penyimpanan internal"
        );

        addStatCard(
                "SUHU BATERAI",
                "Suhu sensor baterai jika tersedia"
        );

        addStatCard(
                "INFORMASI PERANGKAT",
                "Model HP dan versi Android"
        );

        addActionCard(
                "Mode Game",
                "Mengaktifkan profil mode game di aplikasi ini.",
                gameMode ? "Nonaktifkan Mode Game" : "Aktifkan Mode Game",
                v -> {
                    gameMode = !gameMode;

                    ((TextView) v).setText(
                            gameMode
                                    ? "Nonaktifkan Mode Game"
                                    : "Aktifkan Mode Game"
                    );

                    Toast.makeText(
                            this,
                            gameMode
                                    ? "Mode Game aplikasi aktif."
                                    : "Mode Game aplikasi nonaktif.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        addActionCard(
                "Pengaturan Layar",
                "Periksa refresh rate dan pengaturan tampilan yang tersedia.",
                "Buka Pengaturan Layar",
                v -> openSettings(Settings.ACTION_DISPLAY_SETTINGS)
        );

        addActionCard(
                "Pengaturan Baterai",
                "Periksa penggunaan baterai dan mode hemat daya.",
                "Buka Pengaturan Baterai",
                v -> openSettings(Settings.ACTION_BATTERY_SAVER_SETTINGS)
        );

        addActionCard(
                "Pengaturan Penyimpanan",
                "Periksa ruang penyimpanan dan kelola file yang tidak diperlukan.",
                "Buka Penyimpanan",
                v -> openSettings(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
        );

        addActionCard(
                "Perbarui Dashboard",
                "Baca ulang informasi perangkat yang tersedia.",
                "Refresh",
                v -> refreshStats(true)
        );

        addSpace(10);

        addText(
                "INFORMASI",
                13,
                BLUE,
                true
        );

        root.addView(text(
                "Aplikasi ini tidak melakukan overclock atau memaksa pengaturan GPU. "
                + "Peningkatan FPS bergantung pada perangkat, game, suhu, dan "
                + "batasan sistem Android.",
                12,
                MUTED,
                false
        ), margin(0, 8, 0, 0));
    }

    private void addStatCard(String title, String subtitle) {

        LinearLayout card = panel();

        card.addView(text(
                title,
                12,
                MUTED,
                true
        ));

        TextView value = text(
                "Memuat...",
                19,
                WHITE,
                true
        );

        card.addView(value, margin(0, 6, 0, 0));

        card.addView(text(
                subtitle,
                12,
                MUTED,
                false
        ), margin(0, 5, 0, 0));

        if (title.equals("RAM TERSEDIA")) {
            ramValue = value;
        } else if (title.equals("PENYIMPANAN")) {
            storageValue = value;
        } else if (title.equals("SUHU BATERAI")) {
            temperatureValue = value;
        } else {
            deviceValue = value;
        }

        root.addView(card, margin(0, 0, 0, 10));
    }

    private void refreshStats(boolean showMessage) {

        try {

            ActivityManager manager =
                    (ActivityManager) getSystemService(ACTIVITY_SERVICE);

            ActivityManager.MemoryInfo memoryInfo =
                    new ActivityManager.MemoryInfo();

            manager.getMemoryInfo(memoryInfo);

            ramValue.setText(
                    formatBytes(memoryInfo.availMem)
                    + " tersedia dari "
                    + formatBytes(memoryInfo.totalMem)
            );

            StatFs statFs = new StatFs(
                    Environment.getDataDirectory().getPath()
            );

            storageValue.setText(
                    formatBytes(statFs.getAvailableBytes())
                    + " tersedia dari "
                    + formatBytes(statFs.getTotalBytes())
            );

            Intent battery = registerReceiver(
                    null,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            );

            if (battery != null) {

                int temperature = battery.getIntExtra(
                        BatteryManager.EXTRA_TEMPERATURE,
                        -1
                );

                if (temperature >= 0) {

                    float celsius = temperature / 10.0f;

                    temperatureValue.setText(
                            String.format(
                                    Locale.getDefault(),
                                    "%.1f °C",
                                    celsius
                            )
                    );

                } else {

                    temperatureValue.setText("Tidak tersedia");
                }

            } else {

                temperatureValue.setText("Tidak tersedia");
            }

            deviceValue.setText(
                    Build.MANUFACTURER
                    + " "
                    + Build.MODEL
                    + " • Android "
                    + Build.VERSION.RELEASE
            );

            if (showMessage) {
                Toast.makeText(
                        this,
                        "Dashboard berhasil diperbarui.",
                        Toast.LENGTH_SHORT
                ).show();
            }

        } catch (Exception e) {

            if (showMessage) {
                Toast.makeText(
                        this,
                        "Sebagian informasi tidak tersedia.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    private String formatBytes(long bytes) {

        if (bytes < 0) {
            return "Tidak tersedia";
        }

        double gb = bytes / (1024.0 * 1024.0 * 1024.0);

        if (gb >= 1) {
            return String.format(
                    Locale.getDefault(),
                    "%.2f GB",
                    gb
            );
        }

        double mb = bytes / (1024.0 * 1024.0);

        return String.format(
                Locale.getDefault(),
                "%.0f MB",
                mb
        );
    }

    private void openSettings(String action) {

        try {

            startActivity(new Intent(action));

        } catch (Exception e) {

            try {

                startActivity(
                        new Intent(Settings.ACTION_SETTINGS)
                );

            } catch (Exception ignored) {

                Toast.makeText(
                        this,
                        "Pengaturan tidak dapat dibuka.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    private void addActionCard(
            String title,
            String description,
            String buttonText,
            View.OnClickListener listener
    ) {

        LinearLayout card = panel();

        card.addView(text(
                title,
                18,
                WHITE,
                true
        ));

        card.addView(text(
                description,
                13,
                MUTED,
                false
        ), margin(0, 7, 0, 0));

        TextView button = text(
                buttonText,
                14,
                WHITE,
                true
        );

        button.setGravity(Gravity.CENTER);

        button.setPadding(
                dp(12),
                dp(13),
                dp(12),
                dp(13)
        );

        button.setBackground(
                roundedBackground(
                        new int[]{PURPLE, BLUE},
                        18
                )
        );

        card.addView(button, margin(0, 13, 0, 0));

        button.setOnClickListener(listener);

        root.addView(card, margin(0, 0, 0, 12));
    }

    private LinearLayout panel() {

        LinearLayout box = new LinearLayout(this);

        box.setOrientation(LinearLayout.VERTICAL);

        box.setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(16)
        );

        box.setBackground(
                roundedBackground(
                        new int[]{PANEL, PANEL},
                        20
                )
        );

        return box;
    }

    private TextView text(
            String value,
            int size,
            int color,
            boolean bold
    ) {

        TextView view = new TextView(this);

        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);

        if (bold) {
            view.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );
        }

        return view;
    }

    private void addText(
            String value,
            int size,
            int color,
            boolean bold
    ) {

        root.addView(
                text(value, size, color, bold)
        );
    }

    private void addSpace(int height) {

        View space = new View(this);

        root.addView(
                space,
                new LinearLayout.LayoutParams(
                        1,
                        dp(height)
                )
        );
    }

    private LinearLayout.LayoutParams margin(
            int left,
            int top,
            int right,
            int bottom
    ) {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);

        params.leftMargin = dp(left);
        params.topMargin = dp(top);
        params.rightMargin = dp(right);
        params.bottomMargin = dp(bottom);

        return params;
    }

    private GradientDrawable roundedBackground(
            int[] colors,
            int radius
    ) {

        GradientDrawable drawable =
                new GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        colors
                );

        drawable.setCornerRadius(dp(radius));

        return drawable;
    }

    private int dp(int value) {

        return (int) (
                value
                * getResources().getDisplayMetrics().density
                + 0.5f
        );
    }
        }
