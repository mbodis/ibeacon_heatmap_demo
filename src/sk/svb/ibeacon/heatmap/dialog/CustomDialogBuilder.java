package sk.svb.ibeacon.heatmap.dialog;

import sk.svb.ibeacon.heatmap.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * dialog builder helper
 * 
 * @author svab
 *
 */
public class CustomDialogBuilder {

	public AlertDialog alertDialog;
	public Context context;
	boolean version = false;

	private TextView titleText;
	private TextView bodyMessage;

	public CustomDialogBuilder(Context context) {

		version = android.os.Build.VERSION.SDK_INT >= 11;
		this.context = context;

		alertDialog = new AlertDialog(context) {

			@Override
			protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
			}

		};

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.custom_dialog_body, null);
		alertDialog.setView(layout);
		bodyMessage = (TextView) layout
				.findViewById(R.id.custom_dialog_body_message);		

		View customTitle = View.inflate(context, R.layout.custom_dialog_head,
				null);
		titleText = (TextView) customTitle
				.findViewById(R.id.custom_dialog_head_title);		
		if (!version) {
			alertDialog.setCustomTitle(customTitle);
		}
	}

	public CustomDialogBuilder setTitle(int res) {
		if (!version) {
			titleText.setText(res);
		} else {
			alertDialog.setTitle(res);
		}
		return this;
	}

	public CustomDialogBuilder setTitle(String title) {
		if (!version) {
			titleText.setText(title);
		} else {
			alertDialog.setTitle(title);
		}
		return this;
	}

	public AlertDialog create() {
		alertDialog.getWindow().setBackgroundDrawableResource(
				android.R.color.transparent);
		return alertDialog;
	}

	public CustomDialogBuilder setMessage(int resId) {
		if (!version) {
			bodyMessage.setText(context.getString(resId));
		} else {
			alertDialog.setMessage(context.getString(resId));
		}
		return this;
	}

	public CustomDialogBuilder setMessage(String message) {
		if (!version) {
			bodyMessage.setText(message);
		} else {
			alertDialog.setMessage(message);
		}
		return this;
	}

	public CustomDialogBuilder setPositiveButton(int res,
			DialogInterface.OnClickListener listener) {
		alertDialog.setButton(Dialog.BUTTON_POSITIVE, context.getString(res),
				listener);
		return this;
	}

	public CustomDialogBuilder setNegativeButton(int res,
			DialogInterface.OnClickListener listener) {
		alertDialog.setButton(Dialog.BUTTON_NEGATIVE, context.getString(res),
				listener);
		return this;
	}

	public CustomDialogBuilder setNeutralButton(int res,
			DialogInterface.OnClickListener listener) {
		alertDialog.setButton(Dialog.BUTTON_NEUTRAL, context.getString(res),
				listener);
		return this;
	}

	public CustomDialogBuilder setCancelable(boolean cancelable) {
		alertDialog.setCancelable(cancelable);
		return this;
	}

	public CustomDialogBuilder setDissmissListener(
			DialogInterface.OnDismissListener listener) {
		alertDialog.setOnDismissListener(listener);
		return this;
	}

	public CustomDialogBuilder setView(final View v) {
		alertDialog.setView(v);
		return this;
	}

	public AlertDialog show() {
		alertDialog.getWindow().setBackgroundDrawableResource(
				android.R.color.transparent);
		alertDialog.show();
		return alertDialog;
	}

	public CustomDialogBuilder setOnKeyListener(
			DialogInterface.OnKeyListener onKeyListener) {
		alertDialog.setOnKeyListener(onKeyListener);
		return this;
	}

	public void dismiss() {
		alertDialog.dismiss();
	}


}
