package com.nolanlawson.keepscore.helper;

import android.content.Context;

import com.nolanlawson.keepscore.R;

public enum ColorScheme {

	Light (android.R.color.background_light, android.R.color.primary_text_light_nodisable,
			R.color.green, R.color.red, R.color.light_blue, android.R.drawable.btn_default),
	Dark  (android.R.color.background_dark, android.R.color.secondary_text_dark_nodisable,
			R.color.green_4, R.color.red_4, R.color.light_blue, R.drawable.button_dark),
	Android  (R.color.android_color, android.R.color.primary_text_light_nodisable,
			R.color.green_5, R.color.red, R.color.light_blue, android.R.drawable.btn_default),
	;
	
	private int backgroundColorResId;
	private int foregroundColorResId;
	private int positiveColorResId;
	private int negativeColorResId;
	private int borderColorResId;
	private int buttonBackgroundDrawableResId;
	
	private ColorScheme(int backgroundColorResId, int foregroundColorResId,
			int positiveColorResId, int negativeColorResId, int borderColorResId,
			int buttonBackgroundDrawableResId) {
		this.backgroundColorResId = backgroundColorResId;
		this.foregroundColorResId = foregroundColorResId;
		this.positiveColorResId = positiveColorResId;
		this.negativeColorResId = negativeColorResId;
		this.borderColorResId = borderColorResId;
		this.buttonBackgroundDrawableResId = buttonBackgroundDrawableResId;
	}

	public int getBackgroundColorResId() {
		return backgroundColorResId;
	}

	public int getForegroundColorResId() {
		return foregroundColorResId;
	}

	public int getPositiveColorResId() {
		return positiveColorResId;
	}

	public int getNegativeColorResId() {
		return negativeColorResId;
	}

	public int getBorderColorResId() {
		return borderColorResId;
	}
	
	public int getButtonBackgroundDrawableResId() {
		return buttonBackgroundDrawableResId;
	}

	public static ColorScheme findByPreference(String preference, Context context) {
		
		if (preference.equals(context.getString(R.string.pref_color_scheme_choice_light))) {
			return Light;
		} else if (preference.equals(context.getString(R.string.pref_color_scheme_choice_dark))) {
			return Dark;
		} else { // Android
			return Android;
		}
	}
}
