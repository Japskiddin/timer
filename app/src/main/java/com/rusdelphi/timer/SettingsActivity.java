package com.rusdelphi.timer;

import android.app.Activity;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;


public class SettingsActivity extends AppCompatActivity {
    private static final int REQ_MUSIC = 111;
    private TextView tv_melody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tv_melody = (TextView) findViewById(R.id.tv_melody);
        final TextView tv_vibrate_long = (TextView) findViewById(R.id.tv_vibrate_long);
        final TextView tv_vibrate_short = (TextView) findViewById(R.id.tv_vibrate_short);
//        Ringtone ringtone = RingtoneManager.getRingtone(this, MainActivity.mMelody);
        Ringtone ringtone = RingtoneManager.getRingtone(this, Uri.parse(SP.getString(getApplicationContext(), SP.MELODY, "")));
        String title = ringtone.getTitle(this);

        tv_melody.setText(title);
        CheckBox cb_vibrate = (CheckBox) findViewById(R.id.cb_vibrate);
        final CheckBox cb_vibrate_long = (CheckBox) findViewById(R.id.cb_vibrate_long);
        final CheckBox cb_vibrate_short = (CheckBox) findViewById(R.id.cb_vibrate_short);
//        cb_vibrate.setChecked(MainActivity.mVibrate);
        cb_vibrate.setChecked(SP.getBoolean(getApplicationContext(), SP.VIBRATE, false));
//        tv_vibrate_long.setEnabled(MainActivity.mVibrate);
        tv_vibrate_long.setEnabled(SP.getBoolean(getApplicationContext(), SP.VIBRATE, false));
//        tv_vibrate_short.setEnabled(MainActivity.mVibrate);
        tv_vibrate_short.setEnabled(SP.getBoolean(getApplicationContext(), SP.VIBRATE, false));
//        cb_vibrate_long.setEnabled(MainActivity.mVibrate);
        cb_vibrate_long.setEnabled(SP.getBoolean(getApplicationContext(), SP.VIBRATE, false));
//        cb_vibrate_short.setEnabled(MainActivity.mVibrate);
        cb_vibrate_short.setEnabled(SP.getBoolean(getApplicationContext(), SP.VIBRATE, false));

//        cb_vibrate_long.setChecked(MainActivity.mVibrateLong);
        cb_vibrate_long.setChecked(SP.getBoolean(getApplicationContext(), SP.VIBRATE_LONG, false));
//        cb_vibrate_short.setChecked(MainActivity.mVibrateShort);
        cb_vibrate_short.setChecked(SP.getBoolean(getApplicationContext(), SP.VIBRATE_SHORT, false));

        cb_vibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                MainActivity.mVibrate = isChecked;
                SP.setBoolean(getApplicationContext(), SP.VIBRATE, isChecked);
                if (isChecked) {
                    cb_vibrate_short.setChecked(true);
//                    MainActivity.mVibrateShort = true;
                    SP.setBoolean(getApplicationContext(), SP.VIBRATE_SHORT, true);
                } else {
                    cb_vibrate_short.setChecked(false);
                    cb_vibrate_long.setChecked(false);
//                    MainActivity.mVibrateShort = false;
                    SP.setBoolean(getApplicationContext(), SP.VIBRATE_SHORT, false);
//                    MainActivity.mVibrateLong = false;
                    SP.setBoolean(getApplicationContext(), SP.VIBRATE_LONG, false);
                }
                tv_vibrate_long.setEnabled(isChecked);
                tv_vibrate_short.setEnabled(isChecked);
                cb_vibrate_long.setEnabled(isChecked);
                cb_vibrate_short.setEnabled(isChecked);
            }
        });
        cb_vibrate_long.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (!MainActivity.mVibrate)
                if (!SP.getBoolean(getApplicationContext(), SP.VIBRATE, false))
                    return;
                cb_vibrate_short.setChecked(!isChecked);
//                MainActivity.mVibrateShort = !isChecked;
                SP.setBoolean(getApplicationContext(), SP.VIBRATE_SHORT, !isChecked);
//                MainActivity.mVibrateLong = isChecked;
                SP.setBoolean(getApplicationContext(), SP.VIBRATE_LONG, isChecked);

            }
        });
        cb_vibrate_short.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (!MainActivity.mVibrate)
                if (!SP.getBoolean(getApplicationContext(), SP.VIBRATE, false))
                    return;
                cb_vibrate_long.setChecked(!isChecked);
//                MainActivity.mVibrateShort = isChecked;
                SP.setBoolean(getApplicationContext(), SP.VIBRATE_SHORT, isChecked);
//                MainActivity.mVibrateLong = !isChecked;
                SP.setBoolean(getApplicationContext(), SP.VIBRATE_LONG, !isChecked);

            }
        });


        tv_melody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(RingtoneManager.ACTION_RINGTONE_PICKER), REQ_MUSIC);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_MUSIC && resultCode == Activity.RESULT_OK) {
            if (data.getData() == null) {
//                MainActivity.mMelody = (Uri) data.getExtras().get(
//                        RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                Uri extras = (Uri) data.getExtras().get(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                SP.setString(getApplicationContext(), SP.MELODY, extras.toString());
            } else
                SP.setString(getApplicationContext(), SP.MELODY, data.getData().toString());
//                MainActivity.mMelody = data.getData();
            //Log.d("main", "onActivityResult MainActivity.mMelody=" + MainActivity.mMelody);
//                Ringtone ringtone = RingtoneManager.getRingtone(this, MainActivity.mMelody);
            Ringtone ringtone = RingtoneManager.getRingtone(this, Uri.parse(SP.getString(getApplicationContext(), SP.MELODY, "")));
            String title = ringtone.getTitle(this);
            tv_melody.setText(title);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}
