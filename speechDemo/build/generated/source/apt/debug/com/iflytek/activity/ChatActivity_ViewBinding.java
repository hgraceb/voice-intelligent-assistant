// Generated code from Butter Knife. Do not modify!
package com.iflytek.activity;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.iflytek.voicedemo.R;
import com.iflytek.widget.RecordButton;
import com.iflytek.widget.StateButton;
import java.lang.IllegalStateException;
import java.lang.Override;

public class ChatActivity_ViewBinding implements Unbinder {
  private ChatActivity target;

  private View view7f090037;

  private View view7f09004d;

  private View view7f09012c;

  private View view7f09012d;

  private View view7f09012b;

  private View view7f09012a;

  @UiThread
  public ChatActivity_ViewBinding(ChatActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public ChatActivity_ViewBinding(final ChatActivity target, View source) {
    this.target = target;

    View view;
    target.mLlContent = Utils.findRequiredViewAsType(source, R.id.llContent, "field 'mLlContent'", LinearLayout.class);
    target.mRvChat = Utils.findRequiredViewAsType(source, R.id.rv_chat_list, "field 'mRvChat'", RecyclerView.class);
    target.mEtContent = Utils.findRequiredViewAsType(source, R.id.et_content, "field 'mEtContent'", EditText.class);
    target.mRlBottomLayout = Utils.findRequiredViewAsType(source, R.id.bottom_layout, "field 'mRlBottomLayout'", RelativeLayout.class);
    target.mIvAdd = Utils.findRequiredViewAsType(source, R.id.ivAdd, "field 'mIvAdd'", ImageView.class);
    target.mIvEmo = Utils.findRequiredViewAsType(source, R.id.ivEmo, "field 'mIvEmo'", ImageView.class);
    view = Utils.findRequiredView(source, R.id.btn_send, "field 'mBtnSend' and method 'onViewClicked'");
    target.mBtnSend = Utils.castView(view, R.id.btn_send, "field 'mBtnSend'", StateButton.class);
    view7f090037 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.mIvAudio = Utils.findRequiredViewAsType(source, R.id.ivAudio, "field 'mIvAudio'", ImageView.class);
    target.mBtnAudio = Utils.findRequiredViewAsType(source, R.id.btnAudio, "field 'mBtnAudio'", RecordButton.class);
    target.mLlEmotion = Utils.findRequiredViewAsType(source, R.id.rlEmotion, "field 'mLlEmotion'", LinearLayout.class);
    target.mLlAdd = Utils.findRequiredViewAsType(source, R.id.llAdd, "field 'mLlAdd'", LinearLayout.class);
    target.mSwipeRefresh = Utils.findRequiredViewAsType(source, R.id.swipe_chat, "field 'mSwipeRefresh'", SwipeRefreshLayout.class);
    view = Utils.findRequiredView(source, R.id.common_toolbar_settings, "method 'settings' and method 'longClick'");
    view7f09004d = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.settings();
      }
    });
    view.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View p0) {
        return target.longClick();
      }
    });
    view = Utils.findRequiredView(source, R.id.rlPhoto, "method 'onViewClicked'");
    view7f09012c = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.rlVideo, "method 'onViewClicked'");
    view7f09012d = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.rlLocation, "method 'onViewClicked'");
    view7f09012b = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.rlFile, "method 'onViewClicked'");
    view7f09012a = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    ChatActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mLlContent = null;
    target.mRvChat = null;
    target.mEtContent = null;
    target.mRlBottomLayout = null;
    target.mIvAdd = null;
    target.mIvEmo = null;
    target.mBtnSend = null;
    target.mIvAudio = null;
    target.mBtnAudio = null;
    target.mLlEmotion = null;
    target.mLlAdd = null;
    target.mSwipeRefresh = null;

    view7f090037.setOnClickListener(null);
    view7f090037 = null;
    view7f09004d.setOnClickListener(null);
    view7f09004d.setOnLongClickListener(null);
    view7f09004d = null;
    view7f09012c.setOnClickListener(null);
    view7f09012c = null;
    view7f09012d.setOnClickListener(null);
    view7f09012d = null;
    view7f09012b.setOnClickListener(null);
    view7f09012b = null;
    view7f09012a.setOnClickListener(null);
    view7f09012a = null;
  }
}
