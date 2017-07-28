package com.rusdelphi.timer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.rusdelphi.timer.models.Stopwatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Nikita on 09.06.2017.
 */

public class TimerService extends Service
        implements CustomCountdown.OnFinishListener, CustomCountdown.OnTickListener {
    public static final String TIMER_UPDATE = "TIMER_UPDATE";
    public static final String START_TIMER = "StartTimer";
    public static final String PAUSE_TIMER = "PauseTimer";
    private List<Stopwatch> timerList = new ArrayList<>();
    private List<CustomCountdown> customCountdownList = new ArrayList<>();
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private static final int NOTIFICATION_ID = 1338;

    @Override
    public void onCreate() {
        super.onCreate();
        runAsForeground();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case START_TIMER: {
                    Log.d("Status", "Start");
                    Stopwatch object = (Stopwatch) intent.getSerializableExtra("Timer");
                    timerList.add(object);
                    CustomCountdown timer = new CustomCountdown(object.getId(), this, this);
                    customCountdownList.add(timer);
                    timer.start(object.getCurrentPeriod());
                    break;
                }
                case PAUSE_TIMER: {
                    int timerId = intent.getIntExtra("id", -1);
                    deleteTimer(timerId);
                    break;
                }
            }

//            if (timerList.size() == 0) {
//                closeService();
//            }
        }
        return START_REDELIVER_INTENT;
    }

    private void runAsForeground() {
        builder = new NotificationCompat.Builder(this);
        startForeground(NOTIFICATION_ID, builder.build());
    }

    private void updateNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(TIMER_UPDATE);
        notificationIntent.putExtra("Items", (ArrayList<Stopwatch>) timerList);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.mActions.clear();

        String content = "";
        for (Stopwatch timer : timerList) {
            if (timer.getCurrentPeriod() == 0) {
                if (!SP.getString(getApplicationContext(), SP.MELODY, "").equals("")) {
                    builder.setSound(Uri.parse(SP.getString(getApplicationContext(), SP.MELODY, ""))); // TODO: в O ругается на setSound
                }

                if (SP.getBoolean(getApplicationContext(), SP.VIBRATE, false)) {
                    if (SP.getBoolean(getApplicationContext(), SP.VIBRATE_SHORT, false))
                        builder.setVibrate(new long[]{0, 100, 1000, 100, 1000, 100, 1000});
                    if (SP.getBoolean(getApplicationContext(), SP.VIBRATE_LONG, false))
                        builder.setVibrate(new long[]{0, 100, 1000, 100, 1000, 100, 1000, 0, 100, 1000, 100, 1000, 100, 1000, 0, 100, 1000, 100, 1000, 100, 1000});
                }
            }

            content = content + timer.getName() + getString(R.string.timer_left) + longToTime(timer.getCurrentPeriod()) + "\n";
        }

        builder.setSmallIcon(R.drawable.ic_timer_10_white_24dp);
        builder.setContentTitle(getString(R.string.timer_running));
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(content));
        builder.setContentIntent(pendingIntent);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void deleteTimer(int id) {
        for (Stopwatch timer : timerList) {
            if (timer.getId() == id) {
                for (CustomCountdown customCountdown : customCountdownList) {
                    if (customCountdown.getId() == timer.getId()) {
                        customCountdown.stop();
                        customCountdownList.remove(customCountdownList.indexOf(customCountdown));
                        break;
                    }
                }
                timerList.remove(timerList.indexOf(timer));
                break;
            }
        }

        if (timerList.size() == 0) {
            closeService();
        }
    }

    private void closeService() {
        stopForeground(true);
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void sendToReceiver() {
        Intent timerInfoIntent = new Intent(TIMER_UPDATE);
        timerInfoIntent.putExtra("Items", (ArrayList<Stopwatch>) timerList);
        LocalBroadcastManager.getInstance(this).sendBroadcast(timerInfoIntent);
    }

    @Override
    public void onTick(long millis, int id) {
        Log.d("Status", "millis - " + millis + " id - " + id);

        for (Stopwatch timer : timerList) {
            if (timer.getId() == id) {
                timer.setCurrentPeriod(millis);
                sendToReceiver();
                break;
            }
        }

        updateNotification();
    }

    @Override
    public void onFinish(int id) {
        Log.d("Status", "Finish");

        for (Stopwatch timer : timerList) {
            if (timer.getId() == id) {
                timer.setCurrentPeriod(0);
                timer.setPaused(false);
                sendToReceiver();
                break;
            }
        }

//        deleteTimer(id);
        updateNotification();
//        final int idTimer = id;
//
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                deleteTimer(idTimer);
//                updateNotification();
//            }
//        }, 5000);
    }

    private String longToTime(long i) {
        if (i < 0)
            return "00:00:00";

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(i));
    }
}
