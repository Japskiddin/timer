package com.rusdelphi.timer;

import android.os.CountDownTimer;

/**
 * Created by Nikita on 02.06.2017.
 */

public class CustomCountdown {
  private CountDownTimer countDownTimer;
  private OnTickListener onTickListener;
  private OnFinishListener onFinishListener;
  private static final int TIME_INTERVAL = 1000;
  private int id;

  public interface OnFinishListener {
    void onFinish(int id);
  }

  public interface OnTickListener {
    void onTick(long millis, int id);
  }

  public CustomCountdown(int id, OnTickListener onTickListener, OnFinishListener onFinishListener) {
    this.id = id;
    this.onTickListener = onTickListener;
    this.onFinishListener = onFinishListener;
  }

  public void start(long millis) {
    countDownTimer = new CountDownTimer(millis, TIME_INTERVAL) {
      @Override public void onTick(long l) {
        onTickListener.onTick(l, id);
      }

      @Override public void onFinish() {
        onFinishListener.onFinish(id);
      }
    }.start();
  }

  public void stop() {
    countDownTimer.cancel();
  }

  public int getId() {
    return id;
  }
}
