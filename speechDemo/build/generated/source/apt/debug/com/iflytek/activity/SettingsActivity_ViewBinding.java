// Generated code from Butter Knife. Do not modify!
package com.iflytek.activity;

import android.annotation.SuppressLint;
import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.iflytek.voicedemo.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class SettingsActivity_ViewBinding implements Unbinder {
  private SettingsActivity target;

  private View view7f09019a;

  private View view7f09019d;

  private View view7f09019c;

  private View view7f09019f;

  private View view7f0901a3;

  private View view7f0901a0;

  @UiThread
  public SettingsActivity_ViewBinding(SettingsActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  @SuppressLint("ClickableViewAccessibility")
  public SettingsActivity_ViewBinding(final SettingsActivity target, View source) {
    this.target = target;

    View view;
    view = Utils.findRequiredView(source, R.id.tvBeg, "field 'tvBeg' and method 'bindViewOnTouch'");
    target.tvBeg = Utils.castView(view, R.id.tvBeg, "field 'tvBeg'", TextView.class);
    view7f09019a = view;
    view.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View p0, MotionEvent p1) {
        return target.bindViewOnTouch(p0, p1);
      }
    });
    view = Utils.findRequiredView(source, R.id.tvInt, "field 'tvInt' and method 'bindViewOnTouch'");
    target.tvInt = Utils.castView(view, R.id.tvInt, "field 'tvInt'", TextView.class);
    view7f09019d = view;
    view.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View p0, MotionEvent p1) {
        return target.bindViewOnTouch(p0, p1);
      }
    });
    view = Utils.findRequiredView(source, R.id.tvExp, "field 'tvExp' and method 'bindViewOnTouch'");
    target.tvExp = Utils.castView(view, R.id.tvExp, "field 'tvExp'", TextView.class);
    view7f09019c = view;
    view.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View p0, MotionEvent p1) {
        return target.bindViewOnTouch(p0, p1);
      }
    });
    target.scBeg = Utils.findRequiredViewAsType(source, R.id.scBeg, "field 'scBeg'", Switch.class);
    target.scInt = Utils.findRequiredViewAsType(source, R.id.scInt, "field 'scInt'", Switch.class);
    target.scExp = Utils.findRequiredViewAsType(source, R.id.scExp, "field 'scExp'", Switch.class);
    view = Utils.findRequiredView(source, R.id.tvLongClickVibration, "field 'tvLongClickVibration' and method 'bindViewOnTouch'");
    target.tvLongClickVibration = Utils.castView(view, R.id.tvLongClickVibration, "field 'tvLongClickVibration'", TextView.class);
    view7f09019f = view;
    view.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View p0, MotionEvent p1) {
        return target.bindViewOnTouch(p0, p1);
      }
    });
    view = Utils.findRequiredView(source, R.id.tvWinVibration, "field 'tvWinVibration' and method 'bindViewOnTouch'");
    target.tvWinVibration = Utils.castView(view, R.id.tvWinVibration, "field 'tvWinVibration'", TextView.class);
    view7f0901a3 = view;
    view.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View p0, MotionEvent p1) {
        return target.bindViewOnTouch(p0, p1);
      }
    });
    view = Utils.findRequiredView(source, R.id.tvLoseVibration, "field 'tvLoseVibration' and method 'bindViewOnTouch'");
    target.tvLoseVibration = Utils.castView(view, R.id.tvLoseVibration, "field 'tvLoseVibration'", TextView.class);
    view7f0901a0 = view;
    view.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View p0, MotionEvent p1) {
        return target.bindViewOnTouch(p0, p1);
      }
    });
    target.scLongClickVibration = Utils.findRequiredViewAsType(source, R.id.scLongClickVibration, "field 'scLongClickVibration'", Switch.class);
    target.scWinVibration = Utils.findRequiredViewAsType(source, R.id.scWinVibration, "field 'scWinVibration'", Switch.class);
    target.scLoseVibration = Utils.findRequiredViewAsType(source, R.id.scLoseVibration, "field 'scLoseVibration'", Switch.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    SettingsActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.tvBeg = null;
    target.tvInt = null;
    target.tvExp = null;
    target.scBeg = null;
    target.scInt = null;
    target.scExp = null;
    target.tvLongClickVibration = null;
    target.tvWinVibration = null;
    target.tvLoseVibration = null;
    target.scLongClickVibration = null;
    target.scWinVibration = null;
    target.scLoseVibration = null;

    view7f09019a.setOnTouchListener(null);
    view7f09019a = null;
    view7f09019d.setOnTouchListener(null);
    view7f09019d = null;
    view7f09019c.setOnTouchListener(null);
    view7f09019c = null;
    view7f09019f.setOnTouchListener(null);
    view7f09019f = null;
    view7f0901a3.setOnTouchListener(null);
    view7f0901a3 = null;
    view7f0901a0.setOnTouchListener(null);
    view7f0901a0 = null;
  }
}
