package com.rusdelphi.timer;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rusdelphi.timer.models.Stopwatch;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * The configuration screen for the {@link StopwatchWidget StopwatchWidget} AppWidget.
 */
public class StopwatchWidgetConfigureActivity extends Activity
    implements AdapterView.OnItemClickListener {

  private static final String PREFS_NAME = "com.rusdelphi.timer.StopwatchWidget";
  private static final String PREF_PREFIX_KEY = "appwidget_";
  int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
  List<Stopwatch> stopwatchList;
  // EditText mAppWidgetText;

  public StopwatchWidgetConfigureActivity() {
    super();
  }

  @Override public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    setContentView(R.layout.stopwatch_widget_configure);

    // Set the result to CANCELED.  This will cause the widget host to cancel
    // out of the widget placement if the user presses the back button.
    setResult(RESULT_CANCELED);

    //        setTitle(R.string.labelSelectIcon);

    ListView lv = (ListView) findViewById(R.id.stopwatch_listview);
    stopwatchList = new ArrayList<>();
    readStopwatchList();

    ArrayAdapter<Stopwatch> adapter =
        new ArrayAdapter<Stopwatch>(this, R.layout.select_icon_item, stopwatchList) {
          @Override public int getPosition(@Nullable Stopwatch item) {
            return super.getPosition(item);
          }

          @NonNull @Override
          public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null || !(convertView instanceof LinearLayout)) {
              convertView =
                  View.inflate(StopwatchWidgetConfigureActivity.this, R.layout.select_icon_item,
                      null);
            }

            TextView text = (TextView) convertView.findViewById(R.id.widget_stopwatch_name);
            text.setText(stopwatchList.get(position).getName());
            return convertView;
          }
        };

    lv.setAdapter(adapter);
    lv.setOnItemClickListener(this);

    // Find the widget id from the intent.
    Intent intent = getIntent();
    Bundle extras = intent.getExtras();
    if (extras != null) {
      mAppWidgetId =
          extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    // If this activity was started with an intent without an app widget ID, finish with an error.
    if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
      finish();
    }

    //mAppWidgetText.setText(loadTitlePref(StopwatchWidgetConfigureActivity.this, mAppWidgetId));
  }

  @Override public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    putStopwatch(getApplicationContext(), mAppWidgetId, stopwatchList.get(i).getId());
    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(adapterView.getContext());
    StopwatchWidget.updateAppWidget(adapterView.getContext(), appWidgetManager, mAppWidgetId);
    Intent resultValue = new Intent();
    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
    setResult(RESULT_OK, resultValue);
    finish();
  }

  private void readStopwatchList() {
    Gson gson = new Gson();
    Type type = new TypeToken<List<Stopwatch>>() {
    }.getType();
    List<Stopwatch> tempList = gson.fromJson(SP.getString(this, SP.STOPWATCH_LIST, ""), type);

    if (tempList == null) {
      tempList = new ArrayList<>();
    }

    for (int i = 0; i < tempList.size(); i++) {
      stopwatchList.add(tempList.get(i));
    }
  }

  private static void putStopwatch(Context context, int appWidgetId, int id) {
    SP.setInt(context, SP.WIDGET + appWidgetId, id);
  }

  public static int getStopwatch(Context context, int appWidgetId) {
    return SP.getInt(context, SP.WIDGET + appWidgetId, -1);
  }
}

