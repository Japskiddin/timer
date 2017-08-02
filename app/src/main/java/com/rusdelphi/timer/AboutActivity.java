package com.rusdelphi.timer;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

public class AboutActivity extends Activity {
  float fromPosition;
  float toPosition;
  String Text_to_vote, URL_to_vote, Market_url,// Link_to_vote,
      Choose_email_client, Share_via;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_about);
    Resources res = getResources();
    // Link_to_vote = res.getString(R.string.Link_to_vote);
    Text_to_vote = res.getString(R.string.Text_to_vote);
    URL_to_vote = res.getString(R.string.URL_to_vote);
    Market_url = res.getString(R.string.Market_url);
    Choose_email_client = res.getString(R.string.Choose_email_client);
    Share_via = res.getString(R.string.Share_via);
    TextView text_url = (TextView) findViewById(R.id.Link_to_vote);
    // showHtml(text_url,Link_to_vote);
    SpannableString ss = new SpannableString(Text_to_vote);
    ss.setSpan(new URLSpan(URL_to_vote), 0, Text_to_vote.length(),
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    text_url.setText(ss);
  }

  private boolean MyStartActivity(Intent aIntent) {
    try {
      startActivity(aIntent);
      return true;
    } catch (ActivityNotFoundException e) {
      return false;
    }
  }

  public void onShareButtonClick(View v) {
    Intent share = new Intent(Intent.ACTION_SEND);
    share.putExtra(Intent.EXTRA_TEXT, URL_to_vote);
    share.setType("text/plain");
    if (canShareText(true, share)) {
      startActivityForResult(Intent.createChooser(share, Share_via), 0);
    } else {
      Toast.makeText(this, R.string.Share_error, Toast.LENGTH_LONG).show();
    }
  }

  public void onVoteLinkClick(View v) {

    Intent intent = new Intent(Intent.ACTION_VIEW);
    // Try Google play
    intent.setData(Uri.parse(Market_url));
    if (!MyStartActivity(intent)) {
      // Market (Google play) app seems not
      // installed, let's try to open a webbrowser
      intent.setData(Uri.parse(URL_to_vote));
      if (!MyStartActivity(intent)) {
        // Well if this also fails, we have run
        // out of options, inform the user.
        Toast.makeText(this, R.string.Market_error, Toast.LENGTH_SHORT).show();
      }
    }
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    int count = event.getPointerCount();
    if (count == 1) {
      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          fromPosition = (int) event.getX();
          return true;
        case MotionEvent.ACTION_MOVE:
          toPosition = (int) event.getX();
          if ((fromPosition + 150) < toPosition) {
            // right
            fromPosition = toPosition;
            finish();
          }
          return true;
      }
    }
    return super.onTouchEvent(event);
  }

  boolean canShareText(boolean allowSmsMms, Intent intent) {
    PackageManager manager = getPackageManager();
    List<ResolveInfo> list = manager.queryIntentActivities(intent, 0);

    if (list != null && list.size() > 0) {
      if (allowSmsMms) return true;
      int handlersCount = 0;
      for (ResolveInfo li : list) {
        if (li != null && li.activityInfo != null && li.activityInfo.packageName != null && li.activityInfo.packageName.equals("com.android.mms")) {
        } else {
          ++handlersCount;
        }
      }
      if (handlersCount > 0) return true;
    }
    return false;
  }

  public static void showHtml(TextView textView, String text) {
    textView.setText(Html.fromHtml(text));
    textView.setMovementMethod(LinkMovementMethod.getInstance());
  }

  public void Blog_View(View v) {
    Uri address = Uri.parse("http://rusdelphi.com/app");
    Intent openlink = new Intent(Intent.ACTION_VIEW, address);
    startActivity(openlink);
  }

  public void Send_Email(View v) {
    Intent sendMail = new Intent(android.content.Intent.ACTION_SEND);
    sendMail.setType("plain/text");
    sendMail.putExtra(Intent.EXTRA_EMAIL, new String[] { "burdic.lite@gmail.com" });
    sendMail.putExtra(Intent.EXTRA_SUBJECT, "flashlight");
    startActivity(Intent.createChooser(sendMail, Choose_email_client));
  }
}
