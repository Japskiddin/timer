package com.rusdelphi.timer;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.rusdelphi.timer.models.Stopwatch;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Nikita on 12.05.2017.
 */

class CardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  private Context mContext;
  private List<Stopwatch> objectList;
  private adapterClick listener;
  static final int TYPE_STOPWATCH = 0;
  static final int TYPE_TIMER = 1;

  interface adapterClick {
    void clickStopwatch(String type, int position);

    void clickTimer(String type, int position);

    void updateTime(int position, long millis);
  }

  static class StopwatchHolder extends RecyclerView.ViewHolder {
    private TextView digital_clock, name;
    private Button startBtn, resetBtn;
    private ImageView menuBtn;

    StopwatchHolder(View view) {
      super(view);
      digital_clock = (TextView) view.findViewById(R.id.tv_digital_clock_card);
      name = (TextView) view.findViewById(R.id.name_stopwatch_card);
      startBtn = (Button) view.findViewById(R.id.start_button_card);
      resetBtn = (Button) view.findViewById(R.id.reset_button_card);
      menuBtn = (ImageView) view.findViewById(R.id.card_menu);
    }
  }

  static class TimerHolder extends RecyclerView.ViewHolder {
    private TextView digital_clock, name;
    private Button startBtn, resetBtn;
    private ImageView menuBtn;
    private MyTimePicker tp;

    TimerHolder(View view) {
      super(view);
      digital_clock = (TextView) view.findViewById(R.id.digital_clock_card_timer);
      name = (TextView) view.findViewById(R.id.name_timer_card);
      startBtn = (Button) view.findViewById(R.id.start_button_card_timer);
      resetBtn = (Button) view.findViewById(R.id.reset_button_card_timer);
      menuBtn = (ImageView) view.findViewById(R.id.card_menu_timer);
      tp = (MyTimePicker) view.findViewById(R.id.timePicker);
      tp.setIs24HourView(true);
      tp.setCurrentSecond(0);
      tp.setCurrentMinute(0);
      tp.setCurrentHour(0);
    }
  }

  CardAdapter(Context mContext, List<Stopwatch> objectList, adapterClick listener) {
    this.mContext = mContext;
    this.objectList = objectList;
    this.listener = listener;
  }

  @Override public int getItemViewType(int position) {
    if (objectList.get(position).getType().equals(MainActivity.ITEM_TYPE_STOPWATCH)) {
      return TYPE_STOPWATCH;
    } else {
      return TYPE_TIMER;
    }
  }

  @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView;
    if (viewType == TYPE_STOPWATCH) {
      itemView =
          LayoutInflater.from(parent.getContext()).inflate(R.layout.card_stopwatch, parent, false);
      return new StopwatchHolder(itemView);
    } else {
      itemView =
          LayoutInflater.from(parent.getContext()).inflate(R.layout.card_timer, parent, false);
      return new TimerHolder(itemView);
    }
  }

  @Override public void onBindViewHolder(final RecyclerView.ViewHolder myHolder, int position) {
    switch (myHolder.getItemViewType()) {
      case TYPE_STOPWATCH: {
        final Stopwatch object = objectList.get(position);
        final StopwatchHolder holder = (StopwatchHolder) myHolder;
        holder.digital_clock.setText(longToTime(object.getCurrentPeriod()));
        holder.digital_clock.setTypeface(
            Typeface.createFromAsset(mContext.getAssets(), "DS-DIGI.TTF"));
        holder.name.setText(object.getName());

        if (object.isPaused()) {
          holder.startBtn.setBackgroundResource(R.drawable.button_pause);
          holder.startBtn.setText(R.string.pause);
          holder.startBtn.invalidate();
        } else {
          holder.startBtn.setBackgroundResource(R.drawable.button_start);
          holder.startBtn.setText(R.string.start);
          holder.startBtn.invalidate();
        }

        holder.menuBtn.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View view) {
            showPopupMenu(holder.menuBtn, holder.getAdapterPosition(), TYPE_STOPWATCH);
          }
        });

        holder.startBtn.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View view) {
            listener.clickStopwatch("StartPause", holder.getAdapterPosition());
            if (!object.isPaused()) {
              holder.startBtn.setBackgroundResource(R.drawable.button_pause);
              holder.startBtn.setText(R.string.pause);
              holder.startBtn.invalidate();
            } else {
              holder.startBtn.setBackgroundResource(R.drawable.button_start);
              holder.startBtn.setText(R.string.start);
              holder.startBtn.invalidate();
            }
          }
        });

        holder.resetBtn.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View view) {
            listener.clickStopwatch("Reset", holder.getAdapterPosition());
            object.setPaused(false);
            holder.startBtn.setBackgroundResource(R.drawable.button_start);
            holder.startBtn.setText(R.string.start);
            holder.startBtn.invalidate();
          }
        });

        holder.resetBtn.setOnTouchListener(touchListener);
        holder.startBtn.setOnTouchListener(touchListener);
        break;
      }
      case TYPE_TIMER: {
        final Stopwatch object = objectList.get(position);
        final TimerHolder holder = (TimerHolder) myHolder;
        holder.digital_clock.setText(longToTime(object.getCurrentPeriod()));
        holder.digital_clock.setTypeface(
            Typeface.createFromAsset(mContext.getAssets(), "DS-DIGI.TTF"));
        holder.name.setText(object.getName());
        holder.tp.setOnTimeChangedListener(new MyTimePicker.OnTimeChangedListener() {
          @Override
          public void onTimeChanged(MyTimePicker view, int hourOfDay, int minute, int seconds) {
            listener.updateTime(holder.getAdapterPosition(), view.getCurrentMillis());
          }
        });

        Log.d("Status", "isPause - " + object.isPaused());
        if (object.isPaused()) {
          holder.tp.setEnabled(false);
          holder.startBtn.setBackgroundResource(R.drawable.button_pause);
          holder.startBtn.setText(R.string.pause);
          holder.startBtn.invalidate();
        } else {
          holder.tp.setEnabled(true);
          holder.startBtn.setBackgroundResource(R.drawable.button_start);
          holder.startBtn.setText(R.string.start);
          holder.startBtn.invalidate();
        }

        holder.menuBtn.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View view) {
            showPopupMenu(holder.menuBtn, holder.getAdapterPosition(), TYPE_TIMER);
          }
        });

        holder.startBtn.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View view) {
            listener.clickTimer("StartPause", holder.getAdapterPosition());
            if (!object.isPaused()) {
              holder.startBtn.setBackgroundResource(R.drawable.button_pause);
              holder.startBtn.setText(R.string.pause);
              holder.startBtn.invalidate();
              Log.d("Status", "Pause true");
            } else {
              holder.startBtn.setBackgroundResource(R.drawable.button_start);
              holder.startBtn.setText(R.string.start);
              holder.startBtn.invalidate();
              Log.d("Status", "Pause false");
            }
          }
        });

        holder.resetBtn.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View view) {
            listener.clickTimer("Reset", holder.getAdapterPosition());
            object.setPaused(false);
            holder.startBtn.setBackgroundResource(R.drawable.button_start);
            holder.startBtn.setText(R.string.start);
            holder.startBtn.invalidate();
          }
        });

        holder.resetBtn.setOnTouchListener(touchListener);
        holder.startBtn.setOnTouchListener(touchListener);
        break;
      }
    }
  }

  private View.OnTouchListener touchListener = new View.OnTouchListener() {
    @Override public boolean onTouch(View view, MotionEvent event) {
      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN: {
          view.getBackground()
              .setColorFilter(0xe0f47521, PorterDuff.Mode.SRC_ATOP); // TODO: подумать над анимацией
          view.invalidate();
          break;
        }
        case MotionEvent.ACTION_UP: {
          view.getBackground().clearColorFilter();
          view.invalidate();
          break;
        }
        case MotionEvent.ACTION_MOVE: {
          view.getBackground().clearColorFilter();
          view.invalidate();
          break;
        }
      }
      return false;
    }
  };

  private void showPopupMenu(ImageView menuBtn, int position, int type) {
    PopupMenu popupMenu = new PopupMenu(mContext, menuBtn);
    MenuInflater inflater = popupMenu.getMenuInflater();
    inflater.inflate(R.menu.card_menu, popupMenu.getMenu());
    popupMenu.setOnMenuItemClickListener(new MenuItemClickListener(position, type));
    popupMenu.show();
  }

  private class MenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
    private int type;
    private int position;

    MenuItemClickListener(int position, int type) {
      this.position = position;
      this.type = type;
    }

    @Override public boolean onMenuItemClick(MenuItem menuItem) {
      switch (menuItem.getItemId()) {
        case R.id.action_card_remove: {
          if (type == TYPE_STOPWATCH) {
            listener.clickStopwatch("Remove", position);
          } else {
            listener.clickTimer("Remove", position);
          }
          break;
        }
        case R.id.action_card_change: {
          if (type == TYPE_STOPWATCH) {
            listener.clickStopwatch("Rename", position);
          } else {
            listener.clickTimer("Rename", position);
          }
          break;
        }
      }
      return true;
    }
  }

  private String longToTime(long i) {
    if (i < 0) return "00:00:00";

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    return sdf.format(new Date(i));
  }

  @Override public int getItemCount() {
    return objectList.size();
  }
}
