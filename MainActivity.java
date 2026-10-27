package com.gtstarek.aimguide;

import android.app.*;
import android.os.*;
import android.content.*;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.provider.Settings;
import android.graphics.Color;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {
    private static final int REQ_OVERLAY=10, REQ_CAPTURE=11;
    private MediaProjectionManager projectionManager;
    private int captureResultCode=0;
    private Intent captureData;
    private LinearLayout root;
    private TextView status;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        projectionManager=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
        buildUi();
    }
    private void buildUi() {
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22),dp(28),dp(22),dp(20)); root.setBackgroundColor(Color.rgb(12,20,28));
        ImageView logo=new ImageView(this); logo.setImageResource(com.gtstarek.aimguide.R.drawable.gts_logo); logo.setScaleType(ImageView.ScaleType.CENTER_CROP); logo.setAdjustViewBounds(true); LinearLayout.LayoutParams lpLogo=new LinearLayout.LayoutParams(-1,dp(240)); root.addView(logo,lpLogo);
        TextView title=new TextView(this); title.setText("GTS TAREK AIM GUIDE"); title.setTextColor(Color.WHITE); title.setTextSize(25); title.setTypeface(null,1);
        root.addView(title);
        TextView subtitle=new TextView(this); subtitle.setText("Manual Carrom Aim Guide • Screen Markers"); subtitle.setTextColor(Color.rgb(170,195,207)); subtitle.setTextSize(15);
        LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2); sp.topMargin=dp(8); root.addView(subtitle,sp);
        Button start=button("Enable Floating Aim Guide");
        start.setOnClickListener(v->enableOverlay());
        Button auto=button("Test Experimental Screen Markers");
        auto.setOnClickListener(v->requestCapture());
        Button game=button("Play Carrom Pool (Password Required)");
        game.setOnClickListener(v->showPasswordDialog());
        Button telegram=button("Join Official Telegram Channel");
        telegram.setOnClickListener(v->{ try { startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://t.me/gtstarek"))); } catch(Exception e) { Toast.makeText(this,"Could not open Telegram.",Toast.LENGTH_SHORT).show(); } });
        Button stop=button("Stop Aim Guide");
        stop.setOnClickListener(v->stopService(new Intent(this,AimOverlayService.class)));
        root.addView(start); root.addView(auto); root.addView(game); root.addView(telegram); root.addView(stop);
        status=new TextView(this); status.setTextColor(Color.LTGRAY); status.setTextSize(13);
        status.setText("Manual mode: use the floating panel buttons to move the aim point and target.\nScreen markers are experimental and do not reliably identify every coin or predict exact stopping points. This app does not auto-play matches.");
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.topMargin=dp(18); root.addView(status,p);
        ScrollView scroll=new ScrollView(this); scroll.addView(root); setContentView(scroll);
    }
    private Button button(String s) {
        Button b=new Button(this); b.setText(s); b.setAllCaps(false);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.topMargin=dp(12); b.setLayoutParams(p); return b;
    }
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
    private void enableOverlay() {
        if(!Settings.canDrawOverlays(this)) {
            Intent i=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()));
            startActivityForResult(i,REQ_OVERLAY); return;
        }
        Intent s=new Intent(this,AimOverlayService.class); s.setAction(AimOverlayService.ACTION_MANUAL); startService(s);
        status.setText("Manual Aim Line enabled. Open Carrom Pool and use the floating controls.");
    }
    private void requestCapture() {
        if(!Settings.canDrawOverlays(this)) {
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())),REQ_OVERLAY);
            Toast.makeText(this,"Allow display over other apps, then tap Automatic Detection again.",Toast.LENGTH_LONG).show(); return;
        }
        startActivityForResult(projectionManager.createScreenCaptureIntent(),REQ_CAPTURE);
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        if(request==REQ_OVERLAY) {
            if(Settings.canDrawOverlays(this)) enableOverlay();
        } else if(request==REQ_CAPTURE) {
            if(result==RESULT_OK && data!=null) {
                Intent s=new Intent(this,AimOverlayService.class);
                s.setAction(AimOverlayService.ACTION_AUTO);
                s.putExtra(AimOverlayService.EXTRA_RESULT,result); s.putExtra(AimOverlayService.EXTRA_DATA,data);
                if(Build.VERSION.SDK_INT>=26) startForegroundService(s); else startService(s);
                status.setText("Screen-marker mode requested. Markers are experimental and may be inaccurate.");
            } else Toast.makeText(this,"Screen capture permission was not granted.",Toast.LENGTH_SHORT).show();
        }
    }
    private void showPasswordDialog() {
        EditText input=new EditText(this); input.setSingleLine(true); input.setHint("Enter app password");
        input.setInputType( android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD );
        new AlertDialog.Builder(this).setTitle("GTS Tarek Access").setMessage("Enter the password to unlock Play.")
            .setView(input).setNegativeButton("Cancel",(d,w)->d.dismiss())
            .setPositiveButton("Unlock",(d,w)->{
                if("GTSTAREK141".equals(input.getText().toString().trim())) openGame();
                else Toast.makeText(this,"Incorrect password.",Toast.LENGTH_LONG).show();
            }).show();
    }
    private void openGame() {
        Intent i=getPackageManager().getLaunchIntentForPackage("com.miniclip.carrom");
        if(i!=null) startActivity(i);
        else {
            try { startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://play.google.com/store/apps/details?id=com.miniclip.carrom"))); }
            catch(Exception e){ Toast.makeText(this,"Could not open Play Store.",Toast.LENGTH_SHORT).show(); }
        }
    }
}