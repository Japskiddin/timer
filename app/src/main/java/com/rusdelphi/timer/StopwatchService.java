package com.rusdelphi.timer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.rusdelphi.timer.models.Stopwatch;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Nikita on 15.05.2017.
 */

public class StopwatchService extends Service {
  public static final String STOPWATCH_UPDATE = "STOPWATCH_UPDATE";
  public static final String START_STOPWATCH = "StartStopwatch";
  public static final String PAUSE_STOPWATCH = "PauseStopwatch";
  private List<Stopwatch> stopwatchList = new ArrayList<>();
  private NotificationCompat.Builder builder;
  private NotificationManager notificationManager;
  private static final int NOTIFICATION_ID = 1337;
  private Handler handler = new Handler();
  private Runnable runnable = new Runnable() {
    @Override public void run() {
      for (Stopwatch item : stopwatchList) {
        item.setCurrentPeriod(
            System.currentTimeMillis() - item.getStartTime() - item.getTimeDelay());
        Log.d("Status", "currentPeriod - "
            + item.getCurrentPeriod()
            + " Name - "
            + item.getName()
            + " Id - "
            + item.getId()
            + " Running - "
            + item.isRunning());
      }
      if (stopwatchList.size() > 0) {
        updateNotification();
        sendToReceiver();
      }
      handler.postDelayed(this, 150);
    }
  };

  @Override public void onCreate() {
    super.onCreate();
    runAsForeground();
    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
  }

  private void runAsForeground() {
    builder = new NotificationCompat.Builder(this, getString(R.string.app_name));
    startForeground(NOTIFICATION_ID, builder.build());
  }

  private void updateNotification() {
    Intent notificationIntent = new Intent(this, MainActivity.class);
    notificationIntent.setAction(STOPWATCH_UPDATE);
    notificationIntent.putExtra("Items", (ArrayList<Stopwatch>) stopwatchList);
    PendingIntent pendingIntent =
        PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    String content = "";
    for (Stopwatch stopwatch : stopwatchList) {
      content = content + stopwatch.getName() + getString(R.string.stopwatch_time) + longToTime(
          stopwatch.getCurrentPeriod()) + "\n";
    }

    builder.mActions.clear();
    builder.setSmallIcon(R.drawable.ic_timer_white_48dp);
    builder.setContentTitle(getString(R.string.stopwatch_running));
    builder.setStyle(new NotificationCompat.BigTextStyle().bigText(content));
    builder.setContentIntent(pendingIntent);
    notificationManager.notify(NOTIFICATION_ID, builder.build());
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    String action = intent.getAction();
    if (action != null) {
      switch (action) {
        case START_STOPWATCH: {
          Log.d("Status", "Start");
          Stopwatch stopwatch = (Stopwatch) intent.getSerializableExtra("Stopwatch");

          if (stopwatch.getStartTime() == 0) { //SystemClock.elapsedRealtime()
            stopwatch.setStartTime(System.currentTimeMillis());
          } else {
            stopwatch.setTimeDelay(System.currentTimeMillis() - (stopwatch.getStartTime()
                + stopwatch.getCurrentPeriod()));
          }

          stopwatchList.add(stopwatch);
          handler.postDelayed(runnable, 0);
          break;
        }
        case PAUSE_STOPWATCH: {
          Log.d("Status", "Pause");
          int stopwatchId = intent.getIntExtra("id", -1);
          for (Stopwatch item : stopwatchList) {
            if (item.getId() == stopwatchId) {
              stopwatchList.remove(stopwatchList.indexOf(item));
              break;
            }
          }
          break;
        }
      }

      if (stopwatchList.size() == 0) {
        closeService();
      }
    }
    return START_REDELIVER_INTENT;
  }

  private void closeService() {
    handler.removeCallbacks(runnable);
    stopForeground(true);
    stopSelf();
  }

  @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public void onDestroy() {
    super.onDestroy();
  }

  private void sendToReceiver() {
    Intent timerInfoIntent = new Intent(STOPWATCH_UPDATE);
    timerInfoIntent.putExtra("Items", (ArrayList<Stopwatch>) stopwatchList);
    LocalBroadcastManager.getInstance(this).sendBroadcast(timerInfoIntent);
  }

  private String longToTime(long i) {
    if (i < 0) return "00:00:00";

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    return sdf.format(new Date(i));
  }
}
