package com.rusdelphi.timer;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rusdelphi.timer.models.Stopwatch;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link StopwatchWidgetConfigureActivity
 * StopwatchWidgetConfigureActivity}
 */
public class StopwatchWidget extends AppWidgetProvider {
  private static final String APPWIDGET_CONFIGURE = "android.appwidget.action.APPWIDGET_CONFIGURE";

  static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
    int stopwatchId = StopwatchWidgetConfigureActivity.getStopwatch(context, appWidgetId);
    Stopwatch stopwatch = getStopwatch(context, stopwatchId);

    Intent configIntent = new Intent(context, MainActivity.class);
    PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);

    // Construct the RemoteViews object
    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stopwatch_widget);
    views.setInt(R.id.WidgetRoot, "setBackgroundResource", R.drawable.layout_corners);

    if (stopwatch != null) {
      if (stopwatch.isPaused()) {
        views.setInt(R.id.start_button_widget, "setBackgroundResource", R.drawable.button_pause);
        views.setInt(R.id.start_button_widget, "setText", R.string.pause);
      } else {
        views.setInt(R.id.start_button_widget, "setBackgroundResource", R.drawable.button_start);
        views.setInt(R.id.start_button_widget, "setText", R.string.start);
      }

      views.setTextViewText(R.id.name_stopwatch_widget, stopwatch.getName());
      views.setTextViewText(R.id.tv_digital_clock_widget, longToTime(stopwatch.getCurrentPeriod()));

      Intent intent = new Intent(context, StopwatchService.class);

      if (stopwatch.isPaused()) {
        stopwatch.setPaused(false);
        stopwatch.setRunning(false);
        intent.setAction(StopwatchService.PAUSE_STOPWATCH);
        intent.putExtra("Stopwatch", stopwatch);
      } else {
        stopwatch.setPaused(true);
        stopwatch.setRunning(true);
        intent.setAction(StopwatchService.START_STOPWATCH);
        intent.putExtra("Stopwatch", stopwatch);
      }

      //            PendingIntent startIntent = PendingIntent.getService(context, appWidgetId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
      //            views.setOnClickPendingIntent(R.id.start_button_widget, startIntent);
    }

    views.setOnClickPendingIntent(R.id.WidgetRoot, configPendingIntent);

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views);
  }

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    // There may be multiple widgets active, so update all of them
    //        for (int appWidgetId : appWidgetIds) {
    //            updateAppWidget(context, appWidgetManager, appWidgetId);
    //        }
    Toast.makeText(context, "Update", Toast.LENGTH_SHORT).show();
    ComponentName thisWidget = new ComponentName(context, StopwatchWidget.class);
    int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

    //        Intent intent = new Intent(context, UpdateService.class);
    //        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, allWidgetIds);
    //        context.startService(intent);
  }

  @Override public void onReceive(Context context, Intent intent) {
    //        if (intent.getAction().equals(StopwatchService.WIDGET_UPDATE)) {
    //            AppWidgetManager gm = AppWidgetManager.getInstance(context);
    //            int[] ids = gm.getAppWidgetIds(new ComponentName(context, StopwatchWidget.class));
    //            onUpdate(context, gm, ids);
    //        }

    super.onReceive(context, intent);
  }

  @Override public void onDeleted(Context context, int[] appWidgetIds) {
    // When the user deletes the widget, delete the preference associated with it.
    for (int appWidgetId : appWidgetIds) {
      //            StopwatchWidgetConfigureActivity.deleteIconPref(context, appWidgetId);
    }
  }

  @Override public void onEnabled(Context context) {
    // Enter relevant functionality for when the first widget is created
  }

  @Override public void onDisabled(Context context) {
    // Enter relevant functionality for when the last widget is disabled
  }

  private static Stopwatch getStopwatch(Context context, int id) {
    Gson gson = new Gson();
    Type type = new TypeToken<List<Stopwatch>>() {
    }.getType();
    List<Stopwatch> tempList = gson.fromJson(SP.getString(context, SP.STOPWATCH_LIST, ""), type);

    if (tempList == null) {
      tempList = new ArrayList<>();
    }

    for (Stopwatch stopwatch : tempList) {
      if (stopwatch.getId() == id) {
        return stopwatch;
      }
    }

    return null;
  }

  private static String longToTime(long i) {
    if (i < 0) return "00:00:00";

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    return sdf.format(new Date(i));
  }

  private static class UpdateService extends Service {
    @Nullable @Override public IBinder onBind(Intent intent) {
      return null;
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
      Toast.makeText(getApplicationContext(), "Service", Toast.LENGTH_SHORT).show();
      AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
      int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_ID);
      ComponentName thisWidget = new ComponentName(getApplicationContext(), StopwatchWidget.class);

      for (int widgetId : allWidgetIds) {
        int stopwatchId =
            StopwatchWidgetConfigureActivity.getStopwatch(getApplicationContext(), widgetId);
        Stopwatch stopwatch = getStopwatch(getApplicationContext(), stopwatchId);

        Intent configIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent configPendingIntent =
            PendingIntent.getActivity(getApplicationContext(), 0, configIntent, 0);

        // Construct the RemoteViews object
        RemoteViews views =
            new RemoteViews(getApplicationContext().getPackageName(), R.layout.stopwatch_widget);
        views.setInt(R.id.WidgetRoot, "setBackgroundResource", R.drawable.layout_corners);

        if (stopwatch != null) {
          if (stopwatch.isPaused()) {
            views.setInt(R.id.start_button_widget, "setBackgroundResource",
                R.drawable.button_pause);
            views.setInt(R.id.start_button_widget, "setText", R.string.pause);
          } else {
            views.setInt(R.id.start_button_widget, "setBackgroundResource",
                R.drawable.button_start);
            views.setInt(R.id.start_button_widget, "setText", R.string.start);
          }

          views.setTextViewText(R.id.name_stopwatch_widget, stopwatch.getName());
          views.setTextViewText(R.id.tv_digital_clock_widget,
              longToTime(stopwatch.getCurrentPeriod()));

          Intent clickIntent = new Intent(getApplicationContext(), StopwatchService.class);

          if (stopwatch.isPaused()) {
            stopwatch.setPaused(false);
            stopwatch.setRunning(false);
            clickIntent.setAction(StopwatchService.PAUSE_STOPWATCH);
            clickIntent.putExtra("Stopwatch", stopwatch);
          } else {
            stopwatch.setPaused(true);
            stopwatch.setRunning(true);
            clickIntent.setAction(StopwatchService.START_STOPWATCH);
            clickIntent.putExtra("Stopwatch", stopwatch);
          }

          PendingIntent startIntent =
              PendingIntent.getService(getApplicationContext(), widgetId, clickIntent,
                  PendingIntent.FLAG_CANCEL_CURRENT);
          views.setOnClickPendingIntent(R.id.start_button_widget, startIntent);
        }

        views.setOnClickPendingIntent(R.id.WidgetRoot, configPendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(widgetId, views);
      }

      stopSelf();
      return super.onStartCommand(intent, flags, startId);
    }
  }
}

