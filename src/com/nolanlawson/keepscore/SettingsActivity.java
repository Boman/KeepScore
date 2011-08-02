package com.nolanlawson.keepscore;

import java.util.Arrays;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.KeyEvent;
import android.widget.Toast;

import com.nolanlawson.keepscore.helper.PreferenceHelper;
import com.nolanlawson.keepscore.util.IntegerUtil;

public class SettingsActivity extends PreferenceActivity {

	public static final String COLOR_SCHEME_CHANGED = "colorSchemeChanged";
	
	private boolean colorSchemeChanged = false;
	
	private EditTextPreference button1Pref, button2Pref, button3Pref, button4Pref, updateDelayPref, initialScorePref;
	private CheckBoxPreference useWakeLockPref;
	private Preference resetPref;
	private ListPreference colorSchemePref;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.settings);
		
		setUpPreferences();
	}
	
	private void setUpPreferences() {
		
		button1Pref = (EditTextPreference) findPreferenceById(R.string.pref_button_1);
		button2Pref = (EditTextPreference) findPreferenceById(R.string.pref_button_2);
		button3Pref = (EditTextPreference) findPreferenceById(R.string.pref_button_3);
		button4Pref = (EditTextPreference) findPreferenceById(R.string.pref_button_4);
		updateDelayPref = (EditTextPreference) findPreferenceById(R.string.pref_update_delay);
		initialScorePref = (EditTextPreference) findPreferenceById(R.string.pref_initial_score);
		resetPref = findPreferenceById(R.string.pref_reset);
		colorSchemePref = (ListPreference) findPreferenceById(R.string.pref_color_scheme);
		
		// update the preference's summary with whatever the value is, as it's changed
		for (EditTextPreference pref : new EditTextPreference[]{
				button1Pref, button2Pref, button3Pref, button4Pref}) {
			setDynamicSummary(pref);
		}
		
		// do a special check for the update delay value
		updateDelayPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (!IntegerUtil.validInt(newValue.toString()) 
						|| Integer.parseInt(newValue.toString()) < 1
						|| Integer.parseInt(newValue.toString()) > 600) {
					Toast.makeText(SettingsActivity.this, R.string.toast_valid_update_delay_values, Toast.LENGTH_LONG).show();
					return false;
				}
				PreferenceHelper.resetCache();
				return true;
			}
		});
		
		// do another special check for the initial score value
		
		initialScorePref.setSummary(initialScorePref.getText());
		initialScorePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				
				if (!IntegerUtil.validInt(newValue.toString())) {
					Toast.makeText(SettingsActivity.this, R.string.toast_valid_initial_score, Toast.LENGTH_LONG).show();
					return false;
				}
				preference.setSummary(newValue.toString());
				return true;
			}
		});
		
		// do a special popup for the reset preference
		resetPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				new AlertDialog.Builder(SettingsActivity.this)
					.setTitle(R.string.title_confirm)
					.setMessage(R.string.text_reset_confirm)
					.setCancelable(true)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							resetPreferences();
						}


					})
					.setNegativeButton(android.R.string.cancel, null)
					.show();
				return true;
			}
		});
		
		useWakeLockPref = (CheckBoxPreference) findPreferenceById(R.string.pref_use_wake_lock);
		
		setDynamicColorSchemeSummary(colorSchemePref);
	}
	
	private void resetPreferences() {
		
		PreferenceHelper.setIntPreference(R.string.pref_button_1, R.string.pref_button_1_default, this);
		PreferenceHelper.setIntPreference(R.string.pref_button_2, R.string.pref_button_2_default, this);
		PreferenceHelper.setIntPreference(R.string.pref_button_3, R.string.pref_button_3_default, this);
		PreferenceHelper.setIntPreference(R.string.pref_button_4, R.string.pref_button_4_default, this);
		PreferenceHelper.setIntPreference(R.string.pref_initial_score, R.string.pref_initial_score_default, this);
		PreferenceHelper.setIntPreference(R.string.pref_update_delay, R.string.pref_update_delay_default, this);
		PreferenceHelper.setBooleanPreference(R.string.pref_use_wake_lock, R.string.pref_use_wake_lock_default, this);
		PreferenceHelper.setStringPreference(R.string.pref_color_scheme, R.string.pref_color_scheme_default, this);
		PreferenceHelper.resetCache();
		
		button1Pref.setSummary(R.string.pref_button_1_default);
		button1Pref.setText(getString(R.string.pref_button_1_default));
		
		button2Pref.setSummary(R.string.pref_button_2_default);
		button2Pref.setText(getString(R.string.pref_button_2_default));
		
		button3Pref.setSummary(R.string.pref_button_3_default);
		button3Pref.setText(getString(R.string.pref_button_3_default));
		
		button4Pref.setSummary(R.string.pref_button_4_default);
		button4Pref.setText(getString(R.string.pref_button_4_default));
		
		initialScorePref.setSummary(R.string.pref_initial_score_default);
		initialScorePref.setText(getString(R.string.pref_initial_score_default));
		
		
		
		colorSchemePref.setSummary(
				colorSchemePref.getEntries()[
				Arrays.asList(colorSchemePref.getEntryValues()).indexOf(
				getString(R.string.pref_color_scheme_default))]);
		colorSchemePref.setValue(getString(R.string.pref_color_scheme_default));
		colorSchemeChanged = true;
		
		updateDelayPref.setText(getString(R.string.pref_update_delay_default));
		
		useWakeLockPref.setChecked(Boolean.parseBoolean(getString(R.string.pref_use_wake_lock_default)));
		
		Toast.makeText(this, R.string.toast_settings_reset, Toast.LENGTH_SHORT).show();
	}
	
	private Preference findPreferenceById(int resId) {
		return findPreference(getString(resId));
	}

	private void setDynamicSummary(EditTextPreference editTextPreference) {
		// set the summary to be whatever the value is, and change it if necessary
		
		editTextPreference.setSummary(editTextPreference.getText());
		
		editTextPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				
				if (!IntegerUtil.validInt(newValue.toString()) || Integer.parseInt(newValue.toString()) == 0) {
					Toast.makeText(SettingsActivity.this, R.string.toast_no_zeroes, Toast.LENGTH_LONG).show();
					return false;
				}
				
				preference.setSummary((CharSequence)newValue);
				return true;
			}
		});
	}
	
	private void setDynamicColorSchemeSummary(ListPreference preference) {
		// set the summary to be whatever the value is, and change it if necessary
		
		CharSequence entryValue = preference.getValue();
		int idx = Arrays.asList(preference.getEntryValues()).indexOf(entryValue);
		CharSequence entry = preference.getEntries()[idx];
		
		preference.setSummary(entry);
		preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				
				CharSequence entryValue = (CharSequence) newValue;
				int idx = Arrays.asList(((ListPreference)preference).getEntryValues()).indexOf(entryValue);
				CharSequence entry = ((ListPreference)preference).getEntries()[idx];
				
				preference.setSummary(entry);
				
				if (!newValue.equals(((ListPreference)preference).getValue())) {
					colorSchemeChanged = true;
				}
				
				PreferenceHelper.resetCache();
				return true;
			}
		});
	}	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 ) {
	    	
	    	// set result and finish
	    	Intent data = new Intent();
	    	data.putExtra(COLOR_SCHEME_CHANGED, colorSchemeChanged);
	    	setResult(RESULT_OK, data);
	    	finish();
	    	return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}	
}
