/*
 * Settings screen for bridge capture-strip visuals and display language.
 *
 * Module: lsposed-ime-bridge
 */
package com.brycewg.asrkb.imebridge;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import io.github.a110789.gboardhooker.R;
import com.google.android.material.color.DynamicColors;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public final class BridgeVisualSettingsActivity extends Activity {
    private BridgeVisualPrefs.VisualConfig visualConfig;
    private TextView widthValue;
    private TextView heightValue;
    private TextView languageValue;
    private TextView hostTargetValue;
    private Switch showRecordingAreaSwitch;
    private Switch recordingOnlyWaveformSwitch;
    private Switch tapToToggleRecordingSwitch;
    private BridgeWaveformPreviewView idlePreview;
    private BridgeWaveformPreviewView recordingPreview;
    private SeekBar widthSeekBar;
    private SeekBar heightSeekBar;
    private boolean explainedHostTarget;
    private boolean explainedShowRecordingArea;
    private boolean explainedRecordingOnlyWaveform;
    private boolean explainedTapToToggleRecording;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(BridgeLocaleHelper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 主题层面的动态取色（弹窗默认按钮色这类）；这个界面本身是纯代码手绘的，
        // 大部分颜色是直接读 R.color.bridge_accent 常量，不走主题属性，
        // 光这一行对手绘部分没用，真正生效的是下面的 accentColor()。
        DynamicColors.applyToActivityIfAvailable(this);
        super.onCreate(savedInstanceState);
        visualConfig = BridgeVisualPrefs.readForSettings(this);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        scrollView.setBackgroundColor(color(R.color.bridge_bg));
        scrollView.setClipToPadding(false);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(16), dp(20), dp(28));
        scrollView.addView(content, new ScrollView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        content.addView(createHeader());
        content.addView(createDocsButton(), topMargin(dp(24)));
        content.addView(createLanguageCard(), topMargin(dp(14)));
        content.addView(createHostTargetCard(), topMargin(dp(14)));
        content.addView(createShowRecordingAreaCard(), topMargin(dp(14)));
        content.addView(createRecordingOnlyWaveformCard(), topMargin(dp(14)));
        content.addView(createTapToToggleRecordingCard(), topMargin(dp(14)));
        content.addView(createPreviewCard(), topMargin(dp(14)));
        content.addView(createSizeCard(), topMargin(dp(14)));
        content.addView(createResetButton(), topMargin(dp(20)));

        setContentView(scrollView);
        applySystemBarInsets(scrollView);
    }

    private void applySystemBarInsets(View root) {
        final int extraTop = dp(12);
        final int extraBottom = dp(12);
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            int topInset;
            int bottomInset;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                android.graphics.Insets systemBars = insets.getInsets(
                    android.view.WindowInsets.Type.systemBars()
                );
                topInset = systemBars.top;
                bottomInset = systemBars.bottom;
            } else {
                topInset = insets.getSystemWindowInsetTop();
                bottomInset = insets.getSystemWindowInsetBottom();
            }
            v.setPadding(0, topInset + extraTop, 0, bottomInset + extraBottom);
            return insets;
        });
        root.requestApplyInsets();
    }

    private LinearLayout createHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);

        TextView eyebrow = new TextView(this);
        eyebrow.setText(R.string.bibi_app_name);
        eyebrow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        eyebrow.setLetterSpacing(0.04f);
        eyebrow.setAllCaps(true);
        eyebrow.setTextColor(accentColor());
        eyebrow.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        header.addView(eyebrow, matchWrap());

        TextView title = new TextView(this);
        title.setText(R.string.bridge_visual_title);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        title.setTextColor(color(R.color.bridge_text_primary));
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        title.setLetterSpacing(-0.015f);
        LinearLayout.LayoutParams titleParams = matchWrap();
        titleParams.topMargin = dp(6);
        header.addView(title, titleParams);

        TextView summary = new TextView(this);
        summary.setText(R.string.bridge_visual_summary);
        summary.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        summary.setLineSpacing(dp(2), 1f);
        summary.setTextColor(color(R.color.bridge_text_secondary));
        LinearLayout.LayoutParams summaryParams = matchWrap();
        summaryParams.topMargin = dp(10);
        header.addView(summary, summaryParams);
        return header;
    }

    private LinearLayout createDocsButton() {
        LinearLayout button = new LinearLayout(this);
        button.setOrientation(LinearLayout.VERTICAL);
        button.setGravity(Gravity.START);
        button.setPadding(dp(20), dp(18), dp(20), dp(18));
        button.setMinimumHeight(dp(72));
        button.setClickable(true);
        button.setFocusable(true);
        button.setBackground(filledAccentRipple(dp(20)));
        button.setOnClickListener(v -> openDocs());

        TextView title = new TextView(this);
        title.setText(R.string.bridge_docs_title);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        title.setTextColor(Color.WHITE);
        button.addView(title, matchWrap());

        TextView summary = new TextView(this);
        summary.setText(R.string.bridge_docs_summary);
        summary.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        summary.setLineSpacing(dp(1), 1f);
        summary.setTextColor(Color.argb(220, 255, 255, 255));
        LinearLayout.LayoutParams summaryParams = matchWrap();
        summaryParams.topMargin = dp(4);
        button.addView(summary, summaryParams);
        return button;
    }

    private void openDocs() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.bridge_docs_url)));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Throwable ignored) {
            Toast.makeText(this, R.string.bridge_docs_open_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private LinearLayout createLanguageCard() {
        LinearLayout card = createCard();
        card.setClickable(true);
        card.setFocusable(true);
        card.setBackground(rippleSurface(dp(20)));
        card.setOnClickListener(v -> showLanguagePicker());
        card.setContentDescription(getString(R.string.bridge_language_change));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout textColumn = new LinearLayout(this);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(textColumn, textParams);

        TextView title = sectionTitle(R.string.bridge_language_title);
        textColumn.addView(title, matchWrap());

        TextView summary = secondaryText(R.string.bridge_language_summary);
        LinearLayout.LayoutParams summaryParams = matchWrap();
        summaryParams.topMargin = dp(4);
        textColumn.addView(summary, summaryParams);

        languageValue = new TextView(this);
        languageValue.setText(labelForLanguageTag(BridgeLocalePrefs.read(this)));
        languageValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        languageValue.setTextColor(accentColor());
        languageValue.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        languageValue.setPadding(dp(12), dp(6), dp(12), dp(6));
        languageValue.setBackground(chipBackground(accentWithAlpha(26), dp(999)));
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        valueParams.setMarginStart(dp(12));
        row.addView(languageValue, valueParams);

        TextView chevron = new TextView(this);
        chevron.setText("›");
        chevron.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        chevron.setTextColor(color(R.color.bridge_text_tertiary));
        LinearLayout.LayoutParams chevronParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        chevronParams.setMarginStart(dp(4));
        row.addView(chevron, chevronParams);

        card.addView(row, matchWrap());
        return card;
    }

    private LinearLayout createHostTargetCard() {
        LinearLayout card = createCard();
        card.setClickable(true);
        card.setFocusable(true);
        card.setBackground(rippleSurface(dp(20)));
        card.setOnClickListener(v -> showHostTargetPicker());
        card.setContentDescription(getString(R.string.bridge_host_target_change));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout textColumn = new LinearLayout(this);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
            0,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            1f
        );
        row.addView(textColumn, textParams);

        textColumn.addView(sectionTitle(R.string.bridge_host_target_title), matchWrap());
        TextView summary = secondaryText(R.string.bridge_host_target_summary);
        LinearLayout.LayoutParams summaryParams = matchWrap();
        summaryParams.topMargin = dp(4);
        textColumn.addView(summary, summaryParams);

        hostTargetValue = new TextView(this);
        hostTargetValue.setText(labelForHostTarget(visualConfig.hostTarget));
        hostTargetValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        hostTargetValue.setTextColor(accentColor());
        hostTargetValue.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        hostTargetValue.setPadding(dp(12), dp(6), dp(12), dp(6));
        hostTargetValue.setBackground(chipBackground(accentWithAlpha(26), dp(999)));
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        valueParams.setMarginStart(dp(12));
        row.addView(hostTargetValue, valueParams);

        TextView chevron = new TextView(this);
        chevron.setText("›");
        chevron.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        chevron.setTextColor(color(R.color.bridge_text_tertiary));
        LinearLayout.LayoutParams chevronParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        chevronParams.setMarginStart(dp(4));
        row.addView(chevron, chevronParams);

        card.addView(row, matchWrap());
        return card;
    }

    private LinearLayout createShowRecordingAreaCard() {
        LinearLayout card = createCard();
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout textColumn = new LinearLayout(this);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
            0,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            1f
        );
        row.addView(textColumn, textParams);
        textColumn.addView(sectionTitle(R.string.bridge_show_recording_area_title), matchWrap());
        TextView summary = secondaryText(R.string.bridge_show_recording_area_summary);
        LinearLayout.LayoutParams summaryParams = matchWrap();
        summaryParams.topMargin = dp(4);
        textColumn.addView(summary, summaryParams);

        showRecordingAreaSwitch = new Switch(this);
        showRecordingAreaSwitch.setChecked(visualConfig.showRecordingArea);
        showRecordingAreaSwitch.setOnCheckedChangeListener(this::onShowRecordingAreaChanged);
        LinearLayout.LayoutParams switchParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        switchParams.setMarginStart(dp(12));
        row.addView(showRecordingAreaSwitch, switchParams);

        card.addView(row, matchWrap());
        return card;
    }

    private LinearLayout createRecordingOnlyWaveformCard() {
        LinearLayout card = createCard();
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout textColumn = new LinearLayout(this);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        row.addView(textColumn, new LinearLayout.LayoutParams(
            0,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            1f
        ));
        textColumn.addView(sectionTitle(R.string.bridge_recording_only_waveform_title), matchWrap());
        TextView summary = secondaryText(R.string.bridge_recording_only_waveform_summary);
        LinearLayout.LayoutParams summaryParams = matchWrap();
        summaryParams.topMargin = dp(4);
        textColumn.addView(summary, summaryParams);

        recordingOnlyWaveformSwitch = new Switch(this);
        recordingOnlyWaveformSwitch.setChecked(visualConfig.showWaveformOnlyWhileRecording);
        recordingOnlyWaveformSwitch.setOnCheckedChangeListener(
            this::onRecordingOnlyWaveformChanged
        );
        LinearLayout.LayoutParams switchParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        switchParams.setMarginStart(dp(12));
        row.addView(recordingOnlyWaveformSwitch, switchParams);

        card.addView(row, matchWrap());
        return card;
    }

    private LinearLayout createTapToToggleRecordingCard() {
        LinearLayout card = createCard();
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout textColumn = new LinearLayout(this);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        row.addView(textColumn, new LinearLayout.LayoutParams(
            0,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            1f
        ));
        textColumn.addView(sectionTitle(R.string.bridge_tap_to_toggle_recording_title), matchWrap());
        TextView summary = secondaryText(R.string.bridge_tap_to_toggle_recording_summary);
        LinearLayout.LayoutParams summaryParams = matchWrap();
        summaryParams.topMargin = dp(4);
        textColumn.addView(summary, summaryParams);

        tapToToggleRecordingSwitch = new Switch(this);
        tapToToggleRecordingSwitch.setChecked(visualConfig.tapToToggleRecording);
        tapToToggleRecordingSwitch.setOnCheckedChangeListener(
            this::onTapToToggleRecordingChanged
        );
        LinearLayout.LayoutParams switchParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        switchParams.setMarginStart(dp(12));
        row.addView(tapToToggleRecordingSwitch, switchParams);

        card.addView(row, matchWrap());
        return card;
    }

    private LinearLayout createPreviewCard() {
        LinearLayout card = createCard();
        card.addView(sectionTitle(R.string.bridge_visual_preview_title), matchWrap());

        LinearLayout stage = new LinearLayout(this);
        stage.setOrientation(LinearLayout.VERTICAL);
        stage.setPadding(dp(14), dp(14), dp(14), dp(16));
        stage.setBackground(rounded(color(R.color.bridge_preview_stage), dp(16)));
        LinearLayout.LayoutParams stageParams = matchWrap();
        stageParams.topMargin = dp(14);
        card.addView(stage, stageParams);

        stage.addView(createChip(R.string.bridge_visual_preview_idle, false), matchWrap());
        idlePreview = new BridgeWaveformPreviewView(this, false);
        idlePreview.setVisualConfig(visualConfig);
        LinearLayout.LayoutParams idleParams = matchWrap();
        idleParams.topMargin = dp(8);
        stage.addView(idlePreview, idleParams);

        View divider = new View(this);
        divider.setBackgroundColor(color(R.color.bridge_outline));
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            Math.max(1, dp(1))
        );
        dividerParams.topMargin = dp(14);
        dividerParams.bottomMargin = dp(14);
        stage.addView(divider, dividerParams);

        stage.addView(createChip(R.string.bridge_visual_preview_recording, true), matchWrap());
        recordingPreview = new BridgeWaveformPreviewView(this, true);
        recordingPreview.setVisualConfig(visualConfig);
        LinearLayout.LayoutParams recordingParams = matchWrap();
        recordingParams.topMargin = dp(8);
        stage.addView(recordingPreview, recordingParams);
        return card;
    }

    private LinearLayout createSizeCard() {
        LinearLayout card = createCard();

        widthValue = valueBadge();
        card.addView(createSliderHeader(R.string.bridge_visual_width_label, widthValue), matchWrap());
        widthSeekBar = createSeekBar(
            BridgeVisualPrefs.MAX_WIDTH_DP - BridgeVisualPrefs.MIN_WIDTH_DP,
            visualConfig.widthDp - BridgeVisualPrefs.MIN_WIDTH_DP,
            (progress) -> applyConfig(
                visualConfig.withSize(
                    BridgeVisualPrefs.MIN_WIDTH_DP + progress,
                    visualConfig.heightDp
                ),
                true
            )
        );
        LinearLayout.LayoutParams widthSeekParams = matchWrap();
        widthSeekParams.topMargin = dp(4);
        card.addView(widthSeekBar, widthSeekParams);

        heightValue = valueBadge();
        LinearLayout.LayoutParams heightHeaderParams = matchWrap();
        heightHeaderParams.topMargin = dp(22);
        card.addView(createSliderHeader(R.string.bridge_visual_height_label, heightValue), heightHeaderParams);
        heightSeekBar = createSeekBar(
            BridgeVisualPrefs.MAX_HEIGHT_DP - BridgeVisualPrefs.MIN_HEIGHT_DP,
            visualConfig.heightDp - BridgeVisualPrefs.MIN_HEIGHT_DP,
            (progress) -> applyConfig(
                visualConfig.withSize(
                    visualConfig.widthDp,
                    BridgeVisualPrefs.MIN_HEIGHT_DP + progress
                ),
                true
            )
        );
        LinearLayout.LayoutParams heightSeekParams = matchWrap();
        heightSeekParams.topMargin = dp(4);
        card.addView(heightSeekBar, heightSeekParams);

        TextView hint = secondaryText(R.string.bridge_visual_height_hint);
        hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams hintParams = matchWrap();
        hintParams.topMargin = dp(8);
        card.addView(hint, hintParams);

        updateLabels();
        return card;
    }

    private LinearLayout createSliderHeader(int labelRes, TextView valueView) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView label = sectionTitle(labelRes);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(label, labelParams);
        row.addView(valueView, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        return row;
    }

    private SeekBar createSeekBar(int max, int progress, ProgressConsumer consumer) {
        SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(max);
        seekBar.setProgress(progress);
        seekBar.setPadding(dp(4), dp(12), dp(4), dp(12));
        seekBar.setMinimumHeight(dp(40));
        int accent = accentColor();
        seekBar.setProgressTintList(ColorStateList.valueOf(accent));
        seekBar.setThumbTintList(ColorStateList.valueOf(accent));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            seekBar.setMaxHeight(dp(4));
        }
        seekBar.setProgressBackgroundTintList(
            ColorStateList.valueOf(color(R.color.bridge_outline))
        );
        seekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int value, boolean fromUser) {
                if (!fromUser) return;
                consumer.onProgress(value);
            }
        });
        return seekBar;
    }

    private Button createResetButton() {
        Button button = new Button(this);
        button.setText(R.string.bridge_visual_restore_defaults);
        button.setAllCaps(false);
        button.setMinHeight(dp(52));
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        button.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        button.setTextColor(accentColor());
        button.setStateListAnimator(null);
        button.setElevation(0f);
        button.setBackground(outlinedButtonBackground());
        button.setOnClickListener(v -> applyConfig(BridgeVisualPrefs.defaults(), true));
        return button;
    }

    private TextView createChip(int labelRes, boolean recording) {
        TextView chip = new TextView(this);
        chip.setText(labelRes);
        chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        chip.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        chip.setPadding(dp(10), dp(4), dp(10), dp(4));
        if (recording) {
            chip.setTextColor(accentColor());
            chip.setBackground(chipBackground(accentWithAlpha(31), dp(999)));
        } else {
            chip.setTextColor(color(R.color.bridge_text_secondary));
            chip.setBackground(chipBackground(color(R.color.bridge_chip_idle_bg), dp(999)));
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        chip.setLayoutParams(params);
        return chip;
    }

    private TextView valueBadge() {
        TextView badge = new TextView(this);
        badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        badge.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        badge.setTextColor(color(R.color.bridge_text_primary));
        badge.setPadding(dp(10), dp(4), dp(10), dp(4));
        badge.setBackground(chipBackground(color(R.color.bridge_surface_muted), dp(999)));
        return badge;
    }

    private void showHostTargetPicker() {
        showFeatureExplanationIfNeeded(
            !explainedHostTarget,
            R.string.bridge_host_target_title,
            R.string.feature_bridge_host_target_off_desc,
            R.string.feature_bridge_host_target_on_desc,
            () -> {
                explainedHostTarget = true;
                openHostTargetChoices();
            }
        );
    }

    private void openHostTargetChoices() {
        final String[] values = {
            BridgeContract.HOST_TARGET_AUTO,
            BridgeContract.HOST_TARGET_PRO,
            BridgeContract.HOST_TARGET_OPEN_SOURCE
        };
        CharSequence[] labels = new CharSequence[values.length];
        int checked = 0;
        for (int i = 0; i < values.length; i++) {
            labels[i] = labelForHostTarget(values[i]);
            if (values[i].equals(visualConfig.hostTarget)) checked = i;
        }
        new AlertDialog.Builder(this)
            .setTitle(R.string.bridge_host_target_title)
            .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                applyConfig(visualConfig.withHostTarget(values[which]), true);
                dialog.dismiss();
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    private void onShowRecordingAreaChanged(CompoundButton button, boolean checked) {
        if (visualConfig.showRecordingArea == checked) return;
        if (!explainedShowRecordingArea) {
            button.setOnCheckedChangeListener(null);
            button.setChecked(!checked);
            button.setOnCheckedChangeListener(this::onShowRecordingAreaChanged);
            showFeatureExplanationIfNeeded(
                true,
                R.string.bridge_show_recording_area_title,
                R.string.feature_bridge_show_recording_area_off_desc,
                R.string.feature_bridge_show_recording_area_on_desc,
                () -> {
                    explainedShowRecordingArea = true;
                    button.setOnCheckedChangeListener(null);
                    button.setChecked(checked);
                    button.setOnCheckedChangeListener(this::onShowRecordingAreaChanged);
                    applyConfig(visualConfig.withShowRecordingArea(checked), true);
                }
            );
            return;
        }
        applyConfig(visualConfig.withShowRecordingArea(checked), true);
    }

    private void onRecordingOnlyWaveformChanged(CompoundButton button, boolean checked) {
        if (visualConfig.showWaveformOnlyWhileRecording == checked) return;
        if (!explainedRecordingOnlyWaveform) {
            button.setOnCheckedChangeListener(null);
            button.setChecked(!checked);
            button.setOnCheckedChangeListener(this::onRecordingOnlyWaveformChanged);
            showFeatureExplanationIfNeeded(
                true,
                R.string.bridge_recording_only_waveform_title,
                R.string.feature_bridge_recording_only_waveform_off_desc,
                R.string.feature_bridge_recording_only_waveform_on_desc,
                () -> {
                    explainedRecordingOnlyWaveform = true;
                    button.setOnCheckedChangeListener(null);
                    button.setChecked(checked);
                    button.setOnCheckedChangeListener(this::onRecordingOnlyWaveformChanged);
                    applyConfig(
                        visualConfig.withShowWaveformOnlyWhileRecording(checked),
                        true
                    );
                }
            );
            return;
        }
        applyConfig(visualConfig.withShowWaveformOnlyWhileRecording(checked), true);
    }

    private void onTapToToggleRecordingChanged(CompoundButton button, boolean checked) {
        if (visualConfig.tapToToggleRecording == checked) return;
        if (!explainedTapToToggleRecording) {
            button.setOnCheckedChangeListener(null);
            button.setChecked(!checked);
            button.setOnCheckedChangeListener(this::onTapToToggleRecordingChanged);
            showFeatureExplanationIfNeeded(
                true,
                R.string.bridge_tap_to_toggle_recording_title,
                R.string.feature_bridge_tap_to_toggle_recording_off_desc,
                R.string.feature_bridge_tap_to_toggle_recording_on_desc,
                () -> {
                    explainedTapToToggleRecording = true;
                    button.setOnCheckedChangeListener(null);
                    button.setChecked(checked);
                    button.setOnCheckedChangeListener(this::onTapToToggleRecordingChanged);
                    applyConfig(visualConfig.withTapToToggleRecording(checked), true);
                }
            );
            return;
        }
        applyConfig(visualConfig.withTapToToggleRecording(checked), true);
    }

    private void showFeatureExplanationIfNeeded(
        boolean needed,
        int titleRes,
        int offDescRes,
        int onDescRes,
        Runnable onContinue
    ) {
        if (!needed) {
            onContinue.run();
            return;
        }
        new AlertDialog.Builder(this)
            .setTitle(titleRes)
            .setMessage(
                getString(offDescRes) + "\n\n" + getString(onDescRes)
            )
            .setPositiveButton(android.R.string.ok, (dialog, which) -> onContinue.run())
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    private String labelForHostTarget(String hostTarget) {
        String normalized = BridgeContract.normalizeHostTarget(hostTarget);
        if (BridgeContract.HOST_TARGET_PRO.equals(normalized)) {
            return getString(R.string.bridge_host_target_pro);
        }
        if (BridgeContract.HOST_TARGET_OPEN_SOURCE.equals(normalized)) {
            return getString(R.string.bridge_host_target_open_source);
        }
        return getString(R.string.bridge_host_target_auto);
    }

    private void showLanguagePicker() {
        String[] tags = BridgeLocalePrefs.supportedTags();
        CharSequence[] labels = new CharSequence[tags.length];
        String current = BridgeLocalePrefs.read(this);
        int checked = 0;
        for (int i = 0; i < tags.length; i++) {
            labels[i] = labelForLanguageTag(tags[i]);
            if (tags[i].equals(current)) checked = i;
        }
        new AlertDialog.Builder(this)
            .setTitle(R.string.bridge_language_title)
            .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                String selected = tags[which];
                if (!selected.equals(BridgeLocalePrefs.read(this))) {
                    BridgeLocalePrefs.save(this, selected);
                    dialog.dismiss();
                    recreate();
                    return;
                }
                dialog.dismiss();
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    private String labelForLanguageTag(String tag) {
        String normalized = BridgeLocalePrefs.normalize(tag);
        if (BridgeLocalePrefs.TAG_EN.equals(normalized)) {
            return getString(R.string.bridge_language_english);
        }
        if (BridgeLocalePrefs.TAG_ZH_CN.equals(normalized)) {
            return getString(R.string.bridge_language_zh_cn);
        }
        if (BridgeLocalePrefs.TAG_ZH_TW.equals(normalized)) {
            return getString(R.string.bridge_language_zh_tw);
        }
        return getString(R.string.bridge_language_system);
    }

    private void applyConfig(BridgeVisualPrefs.VisualConfig config, boolean save) {
        visualConfig = config;
        updateLabels();
        if (idlePreview != null) idlePreview.setVisualConfig(config);
        if (recordingPreview != null) recordingPreview.setVisualConfig(config);
        if (widthSeekBar != null) {
            widthSeekBar.setProgress(config.widthDp - BridgeVisualPrefs.MIN_WIDTH_DP);
        }
        if (heightSeekBar != null) {
            heightSeekBar.setProgress(config.heightDp - BridgeVisualPrefs.MIN_HEIGHT_DP);
        }
        if (hostTargetValue != null) {
            hostTargetValue.setText(labelForHostTarget(config.hostTarget));
        }
        if (showRecordingAreaSwitch != null &&
            showRecordingAreaSwitch.isChecked() != config.showRecordingArea) {
            showRecordingAreaSwitch.setOnCheckedChangeListener(null);
            showRecordingAreaSwitch.setChecked(config.showRecordingArea);
            showRecordingAreaSwitch.setOnCheckedChangeListener(this::onShowRecordingAreaChanged);
        }
        if (recordingOnlyWaveformSwitch != null &&
            recordingOnlyWaveformSwitch.isChecked() != config.showWaveformOnlyWhileRecording) {
            recordingOnlyWaveformSwitch.setOnCheckedChangeListener(null);
            recordingOnlyWaveformSwitch.setChecked(config.showWaveformOnlyWhileRecording);
            recordingOnlyWaveformSwitch.setOnCheckedChangeListener(
                this::onRecordingOnlyWaveformChanged
            );
        }
        if (tapToToggleRecordingSwitch != null &&
            tapToToggleRecordingSwitch.isChecked() != config.tapToToggleRecording) {
            tapToToggleRecordingSwitch.setOnCheckedChangeListener(null);
            tapToToggleRecordingSwitch.setChecked(config.tapToToggleRecording);
            tapToToggleRecordingSwitch.setOnCheckedChangeListener(
                this::onTapToToggleRecordingChanged
            );
        }
        if (save) BridgeVisualPrefs.saveForSettings(this, config);
    }

    private void updateLabels() {
        if (widthValue != null) {
            widthValue.setText(getString(R.string.bridge_visual_dp_value, visualConfig.widthDp));
        }
        if (heightValue != null) {
            heightValue.setText(getString(R.string.bridge_visual_dp_value, visualConfig.heightDp));
        }
    }

    private LinearLayout createCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackground(rounded(color(R.color.bridge_surface), dp(20)));
        card.setElevation(dp(1));
        return card;
    }

    private TextView sectionTitle(int stringRes) {
        TextView textView = new TextView(this);
        textView.setText(stringRes);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        textView.setTextColor(color(R.color.bridge_text_primary));
        return textView;
    }

    private TextView secondaryText(int stringRes) {
        TextView textView = new TextView(this);
        textView.setText(stringRes);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        textView.setLineSpacing(dp(1), 1f);
        textView.setTextColor(color(R.color.bridge_text_secondary));
        return textView;
    }

    private GradientDrawable rounded(int fillColor, int radiusPx) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fillColor);
        drawable.setCornerRadius(radiusPx);
        drawable.setStroke(Math.max(1, dp(1)), color(R.color.bridge_outline));
        return drawable;
    }

    private GradientDrawable chipBackground(int fillColor, int radiusPx) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fillColor);
        drawable.setCornerRadius(radiusPx);
        return drawable;
    }

    private RippleDrawable rippleSurface(int radiusPx) {
        GradientDrawable content = rounded(color(R.color.bridge_surface), radiusPx);
        return new RippleDrawable(
            ColorStateList.valueOf(accentWithAlpha(31)),
            content,
            null
        );
    }

    private RippleDrawable filledAccentRipple(int radiusPx) {
        GradientDrawable content = new GradientDrawable();
        content.setColor(accentColor());
        content.setCornerRadius(radiusPx);
        return new RippleDrawable(
            ColorStateList.valueOf(Color.argb(60, 255, 255, 255)),
            content,
            null
        );
    }

    private RippleDrawable outlinedButtonBackground() {
        GradientDrawable content = new GradientDrawable();
        content.setColor(Color.TRANSPARENT);
        content.setCornerRadius(dp(16));
        content.setStroke(Math.max(1, dp(1)), accentWithAlpha(51));
        return new RippleDrawable(
            ColorStateList.valueOf(accentWithAlpha(31)),
            content,
            null
        );
    }

    private int color(int resId) {
        return getColor(resId);
    }

    /**
     * 强调色：Android 12+ 有系统动态取色（跟随壁纸，Material You）时优先用它，
     * 拿不到就退回 R.color.bridge_accent 这个固定色——跟
     * BottomCaptureStripView.resolveWaveformColor() 是同一套技巧，只是这里失败时
     * 退回本屏自己的品牌色，而不是波形那边的默认绿。这个界面是纯代码手绘的，
     * 没有走主题属性，所以颜色要在每个用到的地方显式调这个方法，
     * 光靠 DynamicColors.applyToActivityIfAvailable() 是不够的。
     */
    private int accentColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                return getColor(android.R.color.system_accent1_600);
            } catch (Throwable ignored) {
                // Fall through for OEMs without the dynamic color resource.
            }
        }
        return color(R.color.bridge_accent);
    }

    /**
     * 强调色的半透明变体（chip 底色、涟漪、描边这些）。alpha 数值抄自
     * colors.xml 里对应的固定色本来的透明度（比如 bridge_accent_soft 是
     * #1AE53935，alpha 字节是 0x1A=26），只是底色换成 accentColor()，
     * 这样这些衍生色也会跟着系统动态取色变，不会出现"文字用了新颜色、
     * 背景还是旧颜色"这种不一致。
     */
    private int accentWithAlpha(int alpha) {
        return androidx.core.graphics.ColorUtils.setAlphaComponent(accentColor(), alpha);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private LinearLayout.LayoutParams topMargin(int margin) {
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = margin;
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private interface ProgressConsumer {
        void onProgress(int progress);
    }

    private abstract static class SimpleSeekListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }
}
