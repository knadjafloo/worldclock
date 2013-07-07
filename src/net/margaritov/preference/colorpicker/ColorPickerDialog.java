/*
 * Copyright (C) 2010 Daniel Nilsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.margaritov.preference.colorpicker;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.threebars.worldclock2.R;

public class ColorPickerDialog 
	extends 
		Dialog 
	implements
		ColorPickerView.OnColorChangedListener,
		View.OnClickListener {

	private ColorPickerView mColorPicker;

	private ColorPickerPanelView mOldColor;
	private ColorPickerPanelView mNewColor;
	
	private EditText mHexVal;
	private boolean mHexInternalTextChange;
	private boolean mHexValueEnabled = false;
	private ColorStateList mHexDefaultTextColor;

	private OnColorChangedListener mListener;

	private String type;
	
	public interface OnColorChangedListener {
		public void onColorChanged(int color, String type);
	}
	
	public ColorPickerDialog(Context context, int initialColor) {
		super(context);

		init(initialColor);
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return this.type;
	}

	private void init(int color) {
		// To fight color banding.
		getWindow().setFormat(PixelFormat.RGBA_8888);

		setUp(color);

	}

	private void setUp(int color) {
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View layout = inflater.inflate(R.layout.dialog_color_picker, null);

		setContentView(layout);

		setTitle(R.string.dialog_color_picker);
		
		mColorPicker = (ColorPickerView) layout.findViewById(R.id.color_picker_view);
		mOldColor = (ColorPickerPanelView) layout.findViewById(R.id.old_color_panel);
		mNewColor = (ColorPickerPanelView) layout.findViewById(R.id.new_color_panel);
		
		mHexVal = (EditText) layout.findViewById(R.id.hex_val);
		mHexDefaultTextColor = mHexVal.getTextColors();
		mHexVal.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {
				if (mHexValueEnabled) {
					if (mHexInternalTextChange) return;
					
					if (s.length() > 5 || s.length() < 10) {
						try {
							int c = convertToColorInt(s.toString());
							mColorPicker.setColor(c, true);
							mHexVal.setTextColor(mHexDefaultTextColor);
						} catch (NumberFormatException e) {
							mHexVal.setTextColor(Color.RED);
						}
					} else
						mHexVal.setTextColor(Color.RED);
				}
			}
		});
		
		((LinearLayout) mOldColor.getParent()).setPadding(
			Math.round(mColorPicker.getDrawingOffset()), 
			0, 
			Math.round(mColorPicker.getDrawingOffset()), 
			0
		);	
		
		mOldColor.setOnClickListener(this);
		mNewColor.setOnClickListener(this);
		mColorPicker.setOnColorChangedListener(this);
		mOldColor.setColor(color);
		mColorPicker.setColor(color, true);

	}

	@Override
	public void onColorChanged(int color) {

		mNewColor.setColor(color);
		
		if (mHexValueEnabled)
			updateHexValue(color);

		/*
		if (mListener != null) {
			mListener.onColorChanged(color);
		}
		*/

	}
	
	public void setHexValueEnabled(boolean enable) {
		mHexValueEnabled = enable;
		if (enable) {
			mHexVal.setVisibility(View.VISIBLE);
			updateHexLengthFilter();
			updateHexValue(getColor());
		}
		else
			mHexVal.setVisibility(View.GONE);
	}
	
	public boolean getHexValueEnabled() {
		return mHexValueEnabled;
	}
	
	private void updateHexLengthFilter() {
		if (getAlphaSliderVisible())
			mHexVal.setFilters(new InputFilter[] {new InputFilter.LengthFilter(9)});
		else
			mHexVal.setFilters(new InputFilter[] {new InputFilter.LengthFilter(7)});
	}

	private void updateHexValue(int color) {
		mHexInternalTextChange = true;
		if (getAlphaSliderVisible())
			mHexVal.setText(convertToARGB(color));
		else
			mHexVal.setText(convertToRGB(color));
		mHexInternalTextChange = false;
	}

	public void setAlphaSliderVisible(boolean visible) {
		mColorPicker.setAlphaSliderVisible(visible);
		if (mHexValueEnabled) {
			updateHexLengthFilter();
			updateHexValue(getColor());
		}
	}
	
	public boolean getAlphaSliderVisible() {
		return mColorPicker.getAlphaSliderVisible();
	}
	
	/**
	 * Set a OnColorChangedListener to get notified when the color
	 * selected by the user has changed.
	 * @param listener
	 */
	public void setOnColorChangedListener(OnColorChangedListener listener){
		mListener = listener;
	}

	public int getColor() {
		return mColorPicker.getColor();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.new_color_panel) {
			if (mListener != null) {
				mListener.onColorChanged(mNewColor.getColor(), type);
			}
		}
		dismiss();
	}
	
	@Override
	public Bundle onSaveInstanceState() {
		Bundle state = super.onSaveInstanceState();
		state.putInt("old_color", mOldColor.getColor());
		state.putInt("new_color", mNewColor.getColor());
		return state;
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mOldColor.setColor(savedInstanceState.getInt("old_color"));
		mColorPicker.setColor(savedInstanceState.getInt("new_color"), true);
	}
	
	/**
	 * For custom purposes. Not used by ColorPickerPreferrence
	 * @param color
	 * @author Unknown
	 */
    public static String convertToARGB(int color) {
        String alpha = Integer.toHexString(Color.alpha(color));
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));

        if (alpha.length() == 1) {
            alpha = "0" + alpha;
        }

        if (red.length() == 1) {
            red = "0" + red;
        }

        if (green.length() == 1) {
            green = "0" + green;
        }

        if (blue.length() == 1) {
            blue = "0" + blue;
        }

        return "#" + alpha + red + green + blue;
    }
    
    /**
	 * For custom purposes. Not used by ColorPickerPreference
	 * @param color
	 * @author Charles Rosaaen
	 * @return A string representing the hex value of color,
	 * without the alpha value
	 */
    public static String convertToRGB(int color) {
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));

        if (red.length() == 1) {
            red = "0" + red;
        }

        if (green.length() == 1) {
            green = "0" + green;
        }

        if (blue.length() == 1) {
            blue = "0" + blue;
        }

        return "#" + red + green + blue;
    }

    /**
     * For custom purposes. Not used by ColorPickerPreferrence
     * @param argb
     * @throws NumberFormatException
     * @author Unknown
     */
    public static int convertToColorInt(String argb) throws NumberFormatException {

    	if (argb.startsWith("#")) {
    		argb = argb.replace("#", "");
    	}

        int alpha = -1, red = -1, green = -1, blue = -1;

        if (argb.length() == 8) {
            alpha = Integer.parseInt(argb.substring(0, 2), 16);
            red = Integer.parseInt(argb.substring(2, 4), 16);
            green = Integer.parseInt(argb.substring(4, 6), 16);
            blue = Integer.parseInt(argb.substring(6, 8), 16);
        }
        else if (argb.length() == 6) {
            alpha = 255;
            red = Integer.parseInt(argb.substring(0, 2), 16);
            green = Integer.parseInt(argb.substring(2, 4), 16);
            blue = Integer.parseInt(argb.substring(4, 6), 16);
        }
        else
        	throw new NumberFormatException("string " + argb + "did not meet length requirements");

        return Color.argb(alpha, red, green, blue);
    }
}
