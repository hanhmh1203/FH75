package com.vanch.vhxdemo;

import lab.sodino.language.util.Strings;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.vanch.vhxdemo.helper.Utility;

import de.greenrobot.event.EventBus;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * 
 */
public class ConfigUI extends Fragment {
	public static class LangChanged {

	}

	// Context context = getActivity();
	CheckBox autolinkBox, detectsoundBox, skipnameBox, singletagbBox,
			checkshocCheckBox;
	//Spinner sSpinner, abSpinner, qSpinner;
	EditText timeouEditText;
	SeekBar powerSeekBar;
	TextView powerTextView, swTextView, hdTextView, verTextView;
	ConfigParam mParam;
	private RadioButton checkBoxLangEng, checkBoxLangCh;
	//CheckBox checkBoxCheckUpdate;
	CheckBox checkBoxCheckTest;

	// other widgets
	TextView textViewQueryTitle, textViewSession, textViewTimeout, textViewPower,
			textViewSoftVer, textViewHardVer;
	
	public static final long cmd_timeout = 500;
	
	public static final String key_autolink = "autolink";
	public static final String key_checksound = "check_sound";
	public static final String key_skipname = "skip_name";
	public static final String key_singletag = "single_tag";
	public static final String key_checkshock = "check_shock";
	public static final String key_session_s = "session_s";
	public static final String key_session_ab = "session_ab";
	public static final String key_session_q = "session_q";
	public static final String key_session_timeout = "session_timeout";
	public static final String key_power = "power";
	public static final String key_last_connect = "last_connect";
	public static final String key_check_update = "check_update";
	public static final String key_check_Test = "check_Test";
	private static final String TAG = "config";

	public ConfigUI() {
		// Required empty public constructor
	}

	public static ConfigUI me;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		me =this;
		View view = inflater.inflate(R.layout.config, null);
		autolinkBox = (CheckBox) view.findViewById(R.id.radio_autolink);
		detectsoundBox = (CheckBox) view.findViewById(R.id.radio_detectsound);
		skipnameBox = (CheckBox) view.findViewById(R.id.radio_skipname);
		singletagbBox = (CheckBox) view.findViewById(R.id.radio_singletag);
		singletagbBox.setVisibility(View.INVISIBLE);// 隐藏单标签,modify by martrin
													// 20131114

		checkshocCheckBox = (CheckBox) view.findViewById(R.id.radio_chechshock);

		/*sSpinner = (Spinner) view.findViewById(R.id.spinner_s);
		abSpinner = (Spinner) view.findViewById(R.id.spinner_ab);
		qSpinner = (Spinner) view.findViewById(R.id.spinner_q);*/

		timeouEditText = (EditText) view.findViewById(R.id.edit_timeout);

		checkBoxLangCh = (RadioButton) view.findViewById(R.id.radio_ch);
		checkBoxLangCh.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					Strings.setLanguage(getActivity(), Strings.LANGUAGE_CHINESE);
					Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
					editor.putString(MainActivity.lang_key, Strings.LANGUAGE_CHINESE);
					editor.commit();
					EventBus.getDefault().post(new LangChanged());
				}
			}
		});

		checkBoxLangEng = (RadioButton) view.findViewById(R.id.radio_eng);
		checkBoxLangEng.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					Strings.setLanguage(getActivity(), Strings.LANGUAGE_ENGLISH);
					Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
					editor.putString(MainActivity.lang_key, Strings.LANGUAGE_ENGLISH);
					editor.commit();
					EventBus.getDefault().post(new LangChanged());
				}
			}
		});

		powerSeekBar = (SeekBar) view.findViewById(R.id.seekBar1);
		powerSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				powerTextView.setText("" + (progress ));
			}
		});

		powerTextView = (TextView) view.findViewById(R.id.text_power);
		swTextView = (TextView) view.findViewById(R.id.text_sw_version);
		hdTextView = (TextView) view.findViewById(R.id.text_hd_version);
		verTextView = (TextView) view.findViewById(R.id.text_ver);
		verTextView.setText("V" + getVerStr());
		verTextView.setVisibility(View.INVISIBLE);

		// other widgets
		textViewQueryTitle = (TextView) view.findViewById(R.id.txt_queryparamtitle);
		//textViewSession = (TextView) view.findViewById(R.id.txt_session);

		textViewTimeout = (TextView) view.findViewById(R.id.txt_timeout);
		textViewPower = (TextView) view.findViewById(R.id.text_txpower);
		/*textViewSoftVer = (TextView) view
				.findViewById(R.id.text_sofe_version_title);
		textViewHardVer = (TextView) view
				.findViewById(R.id.text_hd_version_title);*/
		
		checkshocCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setConfigCheckshock(getActivity(), isChecked);
			}
		});
		
		skipnameBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setConfigSkipsame(getActivity(), isChecked);
			}
		});
		
		detectsoundBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setConfigChecksound(getActivity(), isChecked);
			}
		});
		
		singletagbBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				//setConfigSingletag((getActivity(), isChecked);
				setConfigSingletag(getActivity(), isChecked);
			}
		});

		autolinkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setConfigAutolink(getActivity(), isChecked);
			}
		});
		
		
		timeouEditText.addTextChangedListener(new TextWatcher() {
			private int selectionStart ;
            private int selectionEnd ;
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() == 0)
					return;
				selectionStart = timeouEditText.getSelectionStart();
                selectionEnd = timeouEditText.getSelectionEnd();
                
				if (!Utility.isNumber(s.toString())) {
					updateText(s);
					Toast.makeText(getActivity(), "Timeout  must be number...", Toast.LENGTH_LONG).show();
					return;
				}
				setConfigTimeout(getActivity(), Integer.valueOf(s.toString()));
			}
			
			private void updateText(Editable s) {
				s.delete(selectionStart-1, selectionEnd);
                int tempSelection = selectionStart;
                timeouEditText.setText(s);
                timeouEditText.setSelection(tempSelection);
			}
		});
		
		/*checkBoxCheckUpdate = (CheckBox) view.findViewById(R.id.radio_check_update);
		checkBoxCheckUpdate.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setConfigCheckUpdate(getActivity(), isChecked);
			}
		});*/
		
		// add by martrin 20131114
		/*checkBoxCheckTest = (CheckBox) view.findViewById(R.id.checkBoxTest);
		checkBoxCheckTest
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						setConfigCheckTest(getActivity(), isChecked);
					}
				});*/

		updateLang();
		return view;
	}


	private String getPackageName() {
		return "com.vanch.vhxdemo";
	}

	private String getVerStr() {
		String verName = "1.x.x";
		try {
			verName = getActivity().getPackageManager().getPackageInfo(
					getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
		}

		return verName;
	}

	@Override
	public void onResume() {
		EventBus.getDefault().register(this);
//		ConfigParam param = (ConfigParam) EventBus.getDefault().getStickyEvent(
//				ConfigParam.class);
//		if (param != null) {
//			Log.d(TAG, "get param:" + param);
//			this.mParam = param;
//		}
		refreshUI();
		super.onResume();
	}

	private void refreshUI() {
		autolinkBox.setChecked(getConfigAutolink(getActivity()));
		detectsoundBox.setChecked(getConfigChecksound(getActivity()));
		skipnameBox.setChecked(getConfigSkipsame(getActivity()));
		singletagbBox.setChecked(getConfigSingletag(getActivity()));
		checkshocCheckBox.setChecked(getConfigCheckshock(getActivity()));

		//			sSpinner.setSelection(mParam.getS());
		//			abSpinner.setSelection(mParam.getAb());
		//			qSpinner.setSelection(mParam.getQ());
		//
		int timeout = getConfigTimeout(getActivity());
		if (timeout == 0) {
			timeout = 1000;
			setConfigTimeout(getActivity(), timeout);
		}
		timeouEditText.setText("" + timeout);
		// powerSeekBar.setProgress(mParam.getPower() - 20);
		powerSeekBar.setProgress(20);
		//
		//			swTextView.setText(mParam.getSwVersion());
		//			hdTextView.setText(mParam.getHwVersion());
			
			

		String lang = getConfigLang(getActivity());
		if (lang.equals(Strings.LANGUAGE_ENGLISH)) {
			checkBoxLangEng.setChecked(true);
		} else {
			checkBoxLangCh.setChecked(true);
		}

		// add by martrin 20131114
		//checkBoxCheckTest.setChecked(getConfigCheckTest(getActivity()));

	}

	public void onEventMainThread(LangChanged e) {
		updateLang();
	}

	private void updateLang() {
		autolinkBox.setText(Strings.getString(R.string.autolink));
		detectsoundBox.setText(Strings.getString(R.string.detectsound));
		skipnameBox.setText(Strings.getString(R.string.skipname));
		singletagbBox.setText(Strings.getString(R.string.singletag));
		checkshocCheckBox.setText(Strings.getString(R.string.checkshock));
		textViewQueryTitle.setText(Strings.getString(R.string.queryparam));
		//textViewSession.setText(Strings.getString(R.string.session));
		textViewTimeout.setText(Strings.getString(R.string.timeout));
		textViewPower.setText(Strings.getString(R.string.txpower));
		//textViewSoftVer.setText(Strings.getString(R.string.softversion_title));
		//textViewHardVer.setText(Strings.getString(R.string.hdversion));
		checkBoxLangCh.setText(Strings.getString(R.string.radio_lang_chinese));
		checkBoxLangEng.setText(Strings.getString(R.string.radio_lang_eng));
		timeouEditText.setHint(Strings.getString(R.string.timeout));
		//checkBoxCheckUpdate.setText(Strings.getString(R.string.radio_check_update));
	}

	@Override
	public void onPause() {
		EventBus.getDefault().unregister(this);
		super.onPause();
	}
	
	public static void setConfigAutolink(Activity activity, boolean autolink) {
		setBoolean(activity, key_autolink, autolink);
	}
	
	public static boolean getConfigAutolink(Activity activity) {
		return getBoolean(activity, key_autolink, true);
	}
	
	public static void setConfigChecksound(Activity activity, boolean checksound) {
		setBoolean(activity, key_checksound, checksound);
	}
	
	public static boolean getConfigChecksound(Activity activity) {
		return getBoolean(activity, key_checksound, true);
	}
	
	public static void setConfigSkipsame(Activity activity, boolean skipsame) {
		setBoolean(activity, key_skipname, skipsame);
	}
	
	public static boolean getConfigSkipsame(Activity activity) {
		return getBoolean(activity, key_skipname, true);
	}
	
	public static void setConfigSingletag(Activity activity, boolean singletag) {
		setBoolean(activity, key_singletag, singletag);
	}
	
	public static boolean getConfigSingletag(Activity activity) {
		return getBoolean(activity, key_singletag, true);
	}
	
	public static void setConfigCheckshock(Activity activity, boolean checkshock) {
		setBoolean(activity, key_checkshock, checkshock);
	}
	
	public static boolean getConfigCheckshock(Activity activity) {
		return getBoolean(activity, key_checkshock, true);
	}
	
	public static void setConfigSeesionS(Activity activity, int value) {
		setInt(activity, key_session_s, value);
	}
	
	public static int getConfigSessionS(Activity activity) {
		return getInt(activity, key_session_s, 0);
	}
	
	//
	public static void setConfigSeesionAb(Activity activity, int value) {
		setInt(activity, key_session_ab, value);
	}
	
	public static int getConfigSessionAb(Activity activity) {
		return getInt(activity, key_session_ab, 0);
	}
	
	public static void setConfigSeesionQ(Activity activity, int value) {
		setInt(activity, key_session_q, value);
	}
	
	public static int getConfigSessionQ(Activity activity) {
		return getInt(activity, key_session_q,0);
	}
	
	public static void setConfigTimeout(Activity activity, int value) {
		setInt(activity, key_session_timeout, value);
	}
	
	public static int getConfigTimeout(Activity activity) {
		int timeout = getInt(activity, key_session_timeout,1000);
		if (timeout == 0)
		{
			timeout = 3000;
			setConfigTimeout(activity, timeout);
		}
		return timeout;
	}
	
	public static void setConfigLang(Activity activity, String lang) {
		setString(activity, MainActivity.lang_key, lang);
	}
	
	public static String getConfigLang(Activity activity) {
		return getString(activity, MainActivity.lang_key, Strings.LANGUAGE_ENGLISH);
	}
	
	public static void setConfigLastConnect(Activity activity, String mac) {
		setString(activity, key_last_connect, mac);
	}
	
	public static String getConfigLastConnect(Activity activity) {
		return getString(activity, key_last_connect, "");
	}
	
	public static void setConfigCheckUpdate(Activity activity, boolean check) {
		setBoolean(activity, key_check_update, check);
	}
	
	public static boolean getConfigCheckUpdate(Activity activity) {
		return getBoolean(activity, key_check_update, true);
	}
	
	public static void setConfigCheckTest(Activity activity, boolean check) {
		setBoolean(activity, key_check_Test, check);
	}

	public static boolean getConfigCheckTest(Activity activity) {
		return getBoolean(activity, key_check_Test, true);
	}

	private static void setBoolean(Activity activity, String key, boolean value) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
		Editor editor = sp.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	private static boolean getBoolean(Activity activity, String key, boolean def) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
		return sp.getBoolean(key, def);
	}
	
	private static void setInt(Activity activity, String key, int value) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
		Editor editor = sp.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	
	private static int getInt(Activity activity, String key, int defValue) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
		return sp.getInt(key, defValue);
	}
	
	private static void setString(Activity activity, String key, String value) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
		Editor editor = sp.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	private static String getString(Activity activity, String key, String defaultVal) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
		return sp.getString(key, defaultVal);
	}
}
