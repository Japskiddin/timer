package com.rusdelphi.timer;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Configuration;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rusdelphi.timer.models.Stopwatch;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener, CardAdapter.adapterClick {
  static final String ASK_SHARE = "AskShare";
  static final String ASK_RATE = "AskRate";
  static boolean mDontAsk = false;
  static final String KEY_NUMBERRUNNING = "NumberRunning";
  private int mNumberRunning;
  protected static boolean mAskRate;
  protected static boolean mAskShare;
  private int mFontSize;
  private RecyclerView mRecyclerView;
  RecyclerView.LayoutManager layoutManager;
  private CardAdapter cardAdapter;
  private List<Stopwatch> stopwatchList;
  private List<Stopwatch> timerList;
  private FloatingActionButton fab;
  private EditText name;
  private boolean isGrid = false;
  public static final String ITEM_TYPE_STOPWATCH = "Stopwatch";
  public static final String ITEM_TYPE_TIMER = "Timer";
  public static final String ACTION_STOPWATCH = "ACTION_STOPWATCH";
  public static final String ACTION_TIMER = "ACTION_TIMER";
  private ServiceReceiver receiver;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d("Status", "OnCreate");
    setContentView(R.layout.activity_main);
    Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(myToolbar);

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer != null) {
      ActionBarDrawerToggle toggle =
          new ActionBarDrawerToggle(this, drawer, myToolbar, R.string.navigation_drawer_open,
              R.string.navigation_drawer_close);
      drawer.addDrawerListener(toggle);
      toggle.syncState();
    }

    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

    mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
    fab = (FloatingActionButton) findViewById(R.id.fab_stopwatch);

    isGrid = SP.getBoolean(this, SP.GRID, false);
    checkLayoutManager();

    mRecyclerView.setLayoutManager(layoutManager);

    //        RecyclerView.ItemAnimator animator = mRecyclerView.getItemAnimator();
    //        if (animator instanceof SimpleItemAnimator) {
    //            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
    //        }

    mRecyclerView.setItemAnimator(null);
    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
    itemTouchHelper.attachToRecyclerView(mRecyclerView);

    checkView();

    receiver = new ServiceReceiver();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
      addShortcuts();
      checkShortcuts();
    }

    checkPendingIntent();
  }

  private void checkPendingIntent() {
    if (getIntent().getAction() != null) {
      Intent intent = getIntent();
      switch (intent.getAction()) {
        case StopwatchService.STOPWATCH_UPDATE: {
          showStopwatch();
          List<Stopwatch> tempList = (ArrayList<Stopwatch>) intent.getSerializableExtra("Items");
          updateStopwatchList(tempList);
          break;
        }
        case TimerService.TIMER_UPDATE: {
          showTimer();
          List<Stopwatch> tempList = (ArrayList<Stopwatch>) intent.getSerializableExtra("Items");
          updateTimerList(tempList);
          break;
        }
      }
    }
  }

  private void checkShortcuts() {
    if (getIntent().getAction() != null) {
      switch (getIntent().getAction()) {
        case ACTION_STOPWATCH: {
          showStopwatch();
          break;
        }
        case ACTION_TIMER: {
          showTimer();
          break;
        }
      }
    }
  }

  @TargetApi(Build.VERSION_CODES.N_MR1) private void addShortcuts() {
    ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

    ShortcutInfo shortcutStopwatch =
        new ShortcutInfo.Builder(this, "idStopwatch").setShortLabel("Stopwatch")
            .setLongLabel("Open stopwatch")
            .setIcon(Icon.createWithResource(MainActivity.this, R.drawable.ic_timer_black_48dp))
            .setIntent(
                new Intent(getApplicationContext(), MainActivity.class).setAction(ACTION_STOPWATCH))
            .build();

    ShortcutInfo shortcutTimer = new ShortcutInfo.Builder(this, "idTimer").setShortLabel("Timer")
        .setLongLabel("Open timer")
        .setIcon(Icon.createWithResource(MainActivity.this, R.drawable.ic_timer_10_black_48dp))
        .setIntent(new Intent(getApplicationContext(), MainActivity.class).setAction(ACTION_TIMER))
        .build();

    shortcutManager.setDynamicShortcuts(Arrays.asList(shortcutStopwatch, shortcutTimer));
  }

  private void checkLayoutManager() {
    if (isGrid) {
      if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
        layoutManager = new GridLayoutManager(this, 2);
      } else {
        layoutManager = new GridLayoutManager(this, 3);
      }
    } else {
      layoutManager = new LinearLayoutManager(this);
    }
  }

  private void checkView() {
    String type = SP.getString(getApplicationContext(), SP.CHECKED, "");
    if (type.equals(ITEM_TYPE_TIMER)) {
      showTimer();
    } else {
      showStopwatch();
    }
  }

  private ItemTouchHelper.SimpleCallback simpleCallback =
      new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
          int position = viewHolder.getAdapterPosition();
          int type = cardAdapter.getItemViewType(position);

          if (type == CardAdapter.TYPE_STOPWATCH && stopwatchList.get(position).isRunning()
              || type == CardAdapter.TYPE_TIMER && timerList.get(position).isRunning()) {
            return 0;
          }

          return super.getSwipeDirs(recyclerView, viewHolder);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
            RecyclerView.ViewHolder target) {
          return false;
        }

        @Override public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
          if (viewHolder instanceof CardAdapter.StopwatchHolder) {
            swipeStopwatch((CardAdapter.StopwatchHolder) viewHolder);
          } else if (viewHolder instanceof CardAdapter.TimerHolder) {
            swipeTimer((CardAdapter.TimerHolder) viewHolder);
          }
        }

        private void swipeStopwatch(CardAdapter.StopwatchHolder holder) {
          Stopwatch item = stopwatchList.get(holder.getAdapterPosition());
          Intent intent = new Intent(MainActivity.this, StopwatchService.class);
          intent.setAction(StopwatchService.PAUSE_STOPWATCH);
          intent.putExtra("id", item.getId());
          startService(intent);

          final int adapterPosition = holder.getAdapterPosition();
          final Stopwatch tempStopwatch = stopwatchList.remove(holder.getAdapterPosition());
          saveToPref(SP.STOPWATCH_LIST);
          cardAdapter.notifyItemRemoved(holder.getAdapterPosition());
          Intent intentUpdate = new Intent(MainActivity.this, StopwatchService.class);
          intentUpdate.setAction(StopwatchService.STOPWATCH_UPDATE);
          intentUpdate.putExtra("start_position", adapterPosition);
          startService(intentUpdate);
          Snackbar snackbar =
              Snackbar.make(mRecyclerView, R.string.item_removed, Snackbar.LENGTH_LONG)
                  .setAction(R.string.undo, new View.OnClickListener() {
                    @Override public void onClick(View view) {
                      stopwatchList.add(adapterPosition, tempStopwatch);
                      saveToPref(SP.STOPWATCH_LIST);
                      cardAdapter.notifyItemInserted(adapterPosition);
                      mRecyclerView.scrollToPosition(adapterPosition);
                    }
                  });
          snackbar.show();
        }

        private void swipeTimer(CardAdapter.TimerHolder holder) {
          Stopwatch item = timerList.get(holder.getAdapterPosition());
          Intent intent = new Intent(MainActivity.this, TimerService.class);
          intent.setAction(TimerService.PAUSE_TIMER);
          intent.putExtra("id", item.getId());
          startService(intent);

          final int adapterPosition = holder.getAdapterPosition();
          final Stopwatch tempTimer = timerList.remove(holder.getAdapterPosition());
          saveToPref(SP.TIMER_LIST);
          cardAdapter.notifyItemRemoved(holder.getAdapterPosition());
          Snackbar snackbar =
              Snackbar.make(mRecyclerView, R.string.item_removed, Snackbar.LENGTH_LONG)
                  .setAction(R.string.undo, new View.OnClickListener() {
                    @Override public void onClick(View view) {
                      timerList.add(adapterPosition, tempTimer);
                      saveToPref(SP.TIMER_LIST);
                      cardAdapter.notifyItemInserted(adapterPosition);
                      mRecyclerView.scrollToPosition(adapterPosition);
                    }
                  });
          snackbar.show();
        }

        @Override public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
            int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
          super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
        }
      };

  private void showStopwatch() {
    SP.setString(getApplicationContext(), SP.CHECKED, ITEM_TYPE_STOPWATCH);

    if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.stopwatch);

    stopwatchList = new ArrayList<>();
    cardAdapter = new CardAdapter(MainActivity.this, stopwatchList, this);
    mRecyclerView.swapAdapter(cardAdapter, false);
    readStopwatchList();
    fab.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        addStopwatch();
      }
    });
  }

  private void readStopwatchList() {
    Gson gson = new Gson();
    Type type = new TypeToken<List<Stopwatch>>() {
    }.getType();
    List<Stopwatch> tempList = gson.fromJson(SP.getString(this, SP.STOPWATCH_LIST, ""), type);

    if (tempList == null) {
      tempList = new ArrayList<>();
    }

    if (tempList.size() == 0) {
      int counter = SP.getInt(MainActivity.this, SP.ID, 0);
      stopwatchList.add(new Stopwatch(
          String.format("%s %s", getResources().getString(R.string.card_note),
              stopwatchList.size() + 1), 0, 0, ITEM_TYPE_STOPWATCH, counter));
      counter++;
      SP.setInt(MainActivity.this, SP.ID, counter);
      saveToPref(SP.STOPWATCH_LIST);
    } else {
      for (int i = 0; i < tempList.size(); i++) {
        stopwatchList.add(tempList.get(i));
      }
    }
  }

  private void addStopwatch() {
    int counter = SP.getInt(MainActivity.this, SP.ID, 0);
    Stopwatch stopwatchObject = new Stopwatch(
        String.format("%s %s", getResources().getString(R.string.card_note),
            stopwatchList.size() + 1), 0, 0, ITEM_TYPE_STOPWATCH, counter);
    stopwatchList.add(stopwatchObject);
    counter++;
    SP.setInt(MainActivity.this, SP.ID, counter);
    saveToPref(SP.STOPWATCH_LIST);
    cardAdapter.notifyItemInserted(stopwatchList.indexOf(stopwatchObject));
  }

  @Override public void clickStopwatch(String type, int position) {
    Stopwatch item = stopwatchList.get(position);
    Intent intent = new Intent(this, StopwatchService.class);

    if (type.equals("StartPause")) {
      if (item.isPaused()) {
        item.setPaused(false);
        item.setRunning(false);
        intent.setAction(StopwatchService.PAUSE_STOPWATCH);
        intent.putExtra("id", item.getId());
        startService(intent);
        cardAdapter.notifyItemChanged(position);
      } else {
        item.setPaused(true);
        item.setRunning(true);
        intent.setAction(StopwatchService.START_STOPWATCH);
        intent.putExtra("Stopwatch", item);
        startService(intent);
      }
      saveToPref(SP.STOPWATCH_LIST);
    }

    if (type.equals("Reset")) {
      item.setPaused(false);
      item.setCurrentPeriod(0);
      item.setTimeDelay(0);
      item.setStartTime(0);

      if (item.isRunning()) {
        item.setRunning(false);
        intent.setAction(StopwatchService.PAUSE_STOPWATCH);
        intent.putExtra("id", item.getId());
        startService(intent);
      }

      saveToPref(SP.STOPWATCH_LIST);
      cardAdapter.notifyItemChanged(stopwatchList.indexOf(item));
    }

    if (type.equals("Remove")) {
      intent.setAction(StopwatchService.PAUSE_STOPWATCH);
      intent.putExtra("id", item.getId());
      startService(intent);
      stopwatchList.remove(position);
      saveToPref(SP.STOPWATCH_LIST);
      cardAdapter.notifyItemRemoved(position);
    }

    if (type.equals("Rename")) {
      final int pos = position;
      final AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(R.string.change_name);
      LayoutInflater inflater =
          (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_view, null);
      name = (EditText) layout.findViewById(R.id.stopwatch_name);
      name.setText(item.getName());
      name.requestFocus();
      InputMethodManager imm =
          (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
      builder.setView(layout);
      builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
        @Override public void onClick(DialogInterface dialogInterface, int i) {
          InputMethodManager inputManager =
              (InputMethodManager) getApplicationContext().getSystemService(
                  Context.INPUT_METHOD_SERVICE);
          inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
          stopwatchList.get(pos).setName(name.getText().toString());
          saveToPref(SP.STOPWATCH_LIST);
          cardAdapter.notifyItemChanged(pos);
        }
      });
      builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
        @Override public void onClick(DialogInterface dialogInterface, int i) {
          InputMethodManager inputManager =
              (InputMethodManager) getApplicationContext().getSystemService(
                  Context.INPUT_METHOD_SERVICE);
          inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
          dialogInterface.dismiss();
        }
      });
      builder.show();
    }
  }

  private void showTimer() {
    SP.setString(getApplicationContext(), SP.CHECKED, ITEM_TYPE_TIMER);

    if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.app_name);

    timerList = new ArrayList<>();
    cardAdapter = new CardAdapter(MainActivity.this, timerList, this);
    mRecyclerView.swapAdapter(cardAdapter, false);
    readTimerList();
    fab.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        addTimer();
      }
    });
  }

  private void readTimerList() {
    Gson gson = new Gson();
    Type type = new TypeToken<List<Stopwatch>>() {
    }.getType();
    List<Stopwatch> tempList = gson.fromJson(SP.getString(this, SP.TIMER_LIST, ""), type);

    if (tempList == null) {
      tempList = new ArrayList<>();
    }

    if (tempList.size() == 0) {
      int counter = SP.getInt(MainActivity.this, SP.ID, 0);
      timerList.add(new Stopwatch(
          String.format("%s %s", getResources().getString(R.string.app_name), timerList.size() + 1),
          60000, 60000, ITEM_TYPE_TIMER, counter));
      counter++;
      SP.setInt(MainActivity.this, SP.ID, counter);
      saveToPref(SP.TIMER_LIST);
    } else {
      for (int i = 0; i < tempList.size(); i++) {
        timerList.add(tempList.get(i));
      }
    }
  }

  private void addTimer() {
    int counter = SP.getInt(MainActivity.this, SP.ID, 0);
    Stopwatch timerObject = new Stopwatch(
        String.format("%s %s", getResources().getString(R.string.app_name), timerList.size() + 1),
        60000, 60000, ITEM_TYPE_TIMER, counter);
    counter++;
    SP.setInt(MainActivity.this, SP.ID, counter);
    timerList.add(timerObject);
    saveToPref(SP.TIMER_LIST);
    cardAdapter.notifyItemInserted(timerList.indexOf(timerObject));
  }

  @Override public void clickTimer(String type, int position) {
    Stopwatch item = timerList.get(position);
    Intent intent = new Intent(MainActivity.this, TimerService.class);

    if (type.equals("StartPause")) {
      if (item.isPaused()) {
        item.setPaused(false);
        item.setRunning(false);
        intent.setAction(TimerService.PAUSE_TIMER);
        intent.putExtra("id", item.getId());
        startService(intent);
        cardAdapter.notifyItemChanged(position);
      } else {
        item.setPaused(true);
        item.setRunning(true);
        intent.setAction(TimerService.START_TIMER);
        intent.putExtra("Timer", item);
        startService(intent);
      }
      saveToPref(SP.TIMER_LIST);
    }

    if (type.equals("Reset")) {
      if (item.isRunning()) {
        item.setRunning(false);
        intent.setAction(TimerService.PAUSE_TIMER);
        intent.putExtra("id", item.getId());
        startService(intent);
      }

      item.setCurrentPeriod(item.getStartTime());
      item.setPaused(false);
      saveToPref(SP.TIMER_LIST);
      cardAdapter.notifyItemChanged(position);
    }

    if (type.equals("Remove")) {
      intent.setAction(TimerService.PAUSE_TIMER);
      intent.putExtra("id", item.getId());
      startService(intent);
      timerList.remove(position);
      saveToPref(SP.TIMER_LIST);
      cardAdapter.notifyItemRemoved(position);
    }

    if (type.equals("Rename")) {
      final int pos = position;
      final AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(R.string.change_name);
      LayoutInflater inflater =
          (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_view, null);
      name = (EditText) layout.findViewById(R.id.stopwatch_name);
      name.setText(item.getName());
      name.requestFocus();
      InputMethodManager imm =
          (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
      builder.setView(layout);
      builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
        @Override public void onClick(DialogInterface dialogInterface, int i) {
          InputMethodManager inputManager =
              (InputMethodManager) getApplicationContext().getSystemService(
                  Context.INPUT_METHOD_SERVICE);
          inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
          timerList.get(pos).setName(name.getText().toString());
          saveToPref(SP.TIMER_LIST);
          cardAdapter.notifyItemChanged(pos);
        }
      });
      builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
        @Override public void onClick(DialogInterface dialogInterface, int i) {
          InputMethodManager inputManager =
              (InputMethodManager) getApplicationContext().getSystemService(
                  Context.INPUT_METHOD_SERVICE);
          inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
          dialogInterface.dismiss();
        }
      });
      builder.show();
    }
  }

  @Override public void updateTime(int position, long millis) {
    timerList.get(position).setStartTime(millis);
    timerList.get(position).setCurrentPeriod(millis);
    cardAdapter.notifyItemChanged(position);
  }

  @Override public void onBackPressed() {
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main, menu);
    MenuItem item = menu.findItem(R.id.action_grid);
    if (isGrid) {
      item.setIcon(R.drawable.ic_grid_off_white_24dp);
    } else {
      item.setIcon(R.drawable.ic_grid_on_white_24dp);
    }
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_grid: {
        if (isGrid) {
          isGrid = false;
          checkLayoutManager();
          item.setIcon(R.drawable.ic_grid_on_white_24dp);
        } else {
          isGrid = true;
          checkLayoutManager();
          item.setIcon(R.drawable.ic_grid_off_white_24dp);
        }

        SP.setBoolean(this, SP.GRID, isGrid);
        mRecyclerView.setLayoutManager(layoutManager);
        cardAdapter.notifyDataSetChanged();
        return true;
      }
      default: {
        return super.onOptionsItemSelected(item);
      }
    }
  }

  public void saveToPref(String type) {
    Gson gson = new Gson();
    String json = "";
    if (type.equals(SP.STOPWATCH_LIST)) {
      json = gson.toJson(stopwatchList);
    } else if (type.equals(SP.TIMER_LIST)) {
      json = gson.toJson(timerList);
    }
    SP.setString(getApplicationContext(), type, json);
  }

  @SuppressWarnings("StatementWithEmptyBody") @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    // Handle navigation view item clicks here.
    int id = item.getItemId();

    switch (id) {
      case R.id.nav_stopwatch: {
        showStopwatch();
        break;
      }
      case R.id.nav_timer: {
        showTimer();
        break;
      }
      case R.id.nav_settings: {
        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
        break;
      }
      case R.id.nav_about: {
        startActivity(new Intent(this, AboutActivity.class));
        break;
      }
      case R.id.nav_share: {
        ShareCompat.IntentBuilder.from(this)
            .setText(getString(R.string.send_message) + " " + getString(R.string.share_text))
            .setType("text/plain")
            .setChooserTitle(R.string.drawer_share)
            .startChooser();
        break;
      }
      case R.id.nav_send: {
        ShareCompat.IntentBuilder.from(this)
            .setType("message/rfc822")
            .setSubject(getString(R.string.app_name))
            .setText(getString(R.string.send_message) + " " + getString(R.string.share_text))
            .setChooserTitle(R.string.drawer_send)
            .startChooser();
        break;
      }
      case R.id.nav_apps: {
        startActivity(
            new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:Владимир Тимофеев")));
        break;
      }
    }

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  private class ServiceReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
      switch (intent.getAction()) {
        case StopwatchService.STOPWATCH_UPDATE: {
          List<Stopwatch> tempList = (ArrayList<Stopwatch>) intent.getSerializableExtra("Items");
          updateStopwatchList(tempList);
          break;
        }
        case TimerService.TIMER_UPDATE: {
          List<Stopwatch> tempList = (ArrayList<Stopwatch>) intent.getSerializableExtra("Items");
          updateTimerList(tempList);
          break;
        }
      }
    }
  }

  private void updateTimerList(List<Stopwatch> tempList) {
    if (timerList == null) {
      return;
    }

    for (Stopwatch item : tempList) {
      for (Stopwatch timer : timerList) {
        if (timer.getId() == item.getId()) {
          timer.setCurrentPeriod(item.getCurrentPeriod());
          timer.setPaused(item.isPaused());
          cardAdapter.notifyItemChanged(timerList.indexOf(timer));
          break;
        }
      }
    }

    saveToPref(SP.TIMER_LIST);
  }

  private void updateStopwatchList(List<Stopwatch> tempList) {
    if (stopwatchList == null) {
      return;
    }

    for (Stopwatch item : tempList) {
      for (Stopwatch stopwatch : stopwatchList) {
        if (stopwatch.getId() == item.getId()) {
          stopwatch.setCurrentPeriod(item.getCurrentPeriod());
          stopwatch.setTimeDelay(item.getTimeDelay());
          stopwatch.setStartTime(item.getStartTime());
          cardAdapter.notifyItemChanged(stopwatchList.indexOf(stopwatch));
          break;
        }
      }
    }

    saveToPref(SP.STOPWATCH_LIST);
  }

  @Override protected void onResume() {
    super.onResume();
    IntentFilter filter = new IntentFilter(StopwatchService.STOPWATCH_UPDATE);
    filter.addAction(TimerService.TIMER_UPDATE);
    LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
  }

  @Override protected void onPause() {
    super.onPause();
    Log.d("Status", "OnPause");
    LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
  }

  @Override protected void onStart() {
    super.onStart();
    Log.d("Status", "OnStart");
    if ((mNumberRunning == 5 && mAskRate && !mDontAsk) || (mNumberRunning == 15
        && mAskRate
        && !mDontAsk) || (mNumberRunning == 25 && mAskRate && !mDontAsk)) {
      AskDialog df = new AskDialog();
      df.mRateDialog = true;
      FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
      transaction.add(df, "AskDialog");
      transaction.commitAllowingStateLoss();
    }
    if ((mNumberRunning == 10 && mAskShare && !mDontAsk) || (mNumberRunning == 20
        && mAskShare
        && !mDontAsk) || (mNumberRunning == 30 && mAskShare && !mDontAsk)) {
      AskDialog df = new AskDialog();
      df.mSharedDialog = true;
      FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
      transaction.add(df, "AskDialog");
      transaction.commitAllowingStateLoss();
    }
  }

  @Override protected void onStop() {
    super.onStop();
    Log.d("Status", "OnStop");
  }
}
