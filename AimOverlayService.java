package com.gtstarek.aimguide;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.hardware.display.*;
import android.media.*;
import android.media.projection.*;
import android.os.*;
import android.view.*;
import android.util.DisplayMetrics;
import android.widget.*;
import java.util.*;

public class AimOverlayService extends Service {
    public static final String ACTION_MANUAL="manual", ACTION_AUTO="auto", EXTRA_RESULT="result", EXTRA_DATA="data";
    private WindowManager wm;
    private AimView aimView;
    private LinearLayout panel;
    private WindowManager.LayoutParams aimParams, panelParams;
    private MediaProjection projection;
    private ImageReader imageReader;
    private VirtualDisplay virtualDisplay;
    private Handler handler;
    private boolean autoMode=false;
    private int screenW,screenH,density;
    private long lastFrame=0;
    private final Runnable frameTask=new Runnable(){public void run(){ if(autoMode && imageReader!=null){ analyzeFrame(); handler.postDelayed(this,450); } }};

    @Override public void onCreate(){super.onCreate(); wm=(WindowManager)getSystemService(WINDOW_SERVICE); handler= new Handler(Looper.getMainLooper());}
    @Override public int onStartCommand(Intent intent,int flags,int startId){
        if(intent!=null && ACTION_AUTO.equals(intent.getAction())){
            autoMode=true;
            startForeground(41,notification());
            int result=intent.getIntExtra(EXTRA_RESULT,0);
            Intent data=intent.getParcelableExtra(EXTRA_DATA);
            try {
                MediaProjectionManager m=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
                projection=m.getMediaProjection(result,data);
                startCapture();
            } catch(Exception e){ autoMode=false; }
        } else { autoMode=false; }
        showOverlay();
        return START_NOT_STICKY;
    }
    private Notification notification(){
        String channel="gts_aim_capture";
        if(Build.VERSION.SDK_INT>=26){NotificationChannel c=new NotificationChannel(channel,"Aim Guide screen capture",NotificationManager.IMPORTANCE_LOW); ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);}
        return new Notification.Builder(this,channel).setContentTitle("GTS Aim Guide").setContentText("Automatic screen detection is active").setSmallIcon(android.R.drawable.ic_menu_search).build();
    }
    private void startCapture(){
        DisplayMetrics dm=new DisplayMetrics(); wm.getDefaultDisplay().getRealMetrics(dm);
        screenW=dm.widthPixels; screenH=dm.heightPixels; density=dm.densityDpi;
        imageReader=ImageReader.newInstance(screenW,screenH,PixelFormat.RGBA_8888,2);
        virtualDisplay=projection.createVirtualDisplay("GTSAimCapture",screenW,screenH,density,DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,imageReader.getSurface(),null,handler);
        handler.postDelayed(frameTask,700);
    }
    private void showOverlay(){
        if(aimView==null){
            aimView=new AimView(this);
            aimParams=new WindowManager.LayoutParams(-1,-1, overlayType(),WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
            try{wm.addView(aimView,aimParams);}catch(Exception ignored){}
        }
        if(panel==null){
            panel=new LinearLayout(this); panel.setOrientation(LinearLayout.VERTICAL); panel.setPadding(10,6,10,6); panel.setBackgroundColor(Color.argb(220,16,27,38));
            TextView head=new TextView(this); head.setText("GTS AIM • drag this panel"); head.setTextColor(Color.WHITE); head.setTextSize(12); head.setPadding(5,4,5,4); panel.addView(head);
            Button moveLine=new Button(this); moveLine.setText("Move Aim Point ↗"); moveLine.setAllCaps(false); moveLine.setOnClickListener(v->{aimView.manualX=Math.min(screenWidth()-20,aimView.manualX+25);aimView.manualY=Math.max(80,aimView.manualY-25);aimView.invalidate();}); panel.addView(moveLine);
            Button moveLeft=new Button(this); moveLeft.setText("Aim Point ←"); moveLeft.setAllCaps(false); moveLeft.setOnClickListener(v->{aimView.manualX=Math.max(20,aimView.manualX-25);aimView.invalidate();}); panel.addView(moveLeft);
            Button moveRight=new Button(this); moveRight.setText("Aim Point →"); moveRight.setAllCaps(false); moveRight.setOnClickListener(v->{aimView.manualX=Math.min(screenWidth()-20,aimView.manualX+25);aimView.invalidate();}); panel.addView(moveRight);
            Button moveUp=new Button(this); moveUp.setText("Aim Point ↑"); moveUp.setAllCaps(false); moveUp.setOnClickListener(v->{aimView.manualY=Math.max(80,aimView.manualY-25);aimView.invalidate();}); panel.addView(moveUp);
            Button moveDown=new Button(this); moveDown.setText("Aim Point ↓"); moveDown.setAllCaps(false); moveDown.setOnClickListener(v->{aimView.manualY=Math.min(getResources().getDisplayMetrics().heightPixels-40,aimView.manualY+25);aimView.invalidate();}); panel.addView(moveDown);
            Button target=new Button(this); target.setText("Next Target"); target.setAllCaps(false); target.setOnClickListener(v->{aimView.targetX=Math.min(screenWidth()-30,aimView.targetX+45);aimView.targetY=Math.max(100,aimView.targetY+25);aimView.invalidate();}); panel.addView(target);
            Button mode=new Button(this); mode.setText("Toggle Screen Markers"); mode.setAllCaps(false); mode.setOnClickListener(v->{aimView.autoMode=!aimView.autoMode;aimView.invalidate();}); panel.addView(mode);
            Button close=new Button(this); close.setText("Close Overlay"); close.setAllCaps(false); close.setOnClickListener(v->stopSelf()); panel.addView(close);
            panelParams=new WindowManager.LayoutParams(-2,-2,overlayType(),WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
            panelParams.gravity=Gravity.TOP|Gravity.LEFT; panelParams.x=20; panelParams.y=100;
            panel.setOnTouchListener(new View.OnTouchListener(){float sx,sy;int ox,oy; public boolean onTouch(View v,android.view.MotionEvent e){ if(e.getAction()==MotionEvent.ACTION_DOWN){sx=e.getRawX();sy=e.getRawY();ox=panelParams.x;oy=panelParams.y;return true;} if(e.getAction()==MotionEvent.ACTION_MOVE){panelParams.x=ox+(int)(e.getRawX()-sx);panelParams.y=oy+(int)(e.getRawY()-sy);wm.updateViewLayout(panel,panelParams);return true;} return e.getAction()==MotionEvent.ACTION_UP; }});
            try{wm.addView(panel,panelParams);}catch(Exception ignored){}
        }
    }
    private int overlayType(){return Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE;}
    private int screenWidth(){return getResources().getDisplayMetrics().widthPixels;}
    private void analyzeFrame(){
        Image image=null;
        try{
            image=imageReader.acquireLatestImage(); if(image==null)return;
            Image.Plane p=image.getPlanes()[0]; ByteBufferAccess access=new ByteBufferAccess(p.getBuffer());
            int w=image.getWidth(),h=image.getHeight(),pixelStride=p.getPixelStride(),rowStride=p.getRowStride();
            Bitmap b=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
            int[] pixels=new int[w*h]; byte[] row=new byte[rowStride];
            // Sample every 3 pixels; only identify bright/white candidates, never assume perfect game-specific recognition.
            for(int y=0;y<h;y+=3){
                p.getBuffer().position(y*rowStride);
                int max=Math.min(rowStride,p.getBuffer().remaining()); p.getBuffer().get(row,0,max);
                for(int x=0;x<w;x+=3){
                    int off=x*pixelStride;if(off+2>=max)continue;
                    int r=row[off]&255,g=row[off+1]&255,bl=row[off+2]&255;
                    if(r>210&&g>210&&bl>205){ int idx=y*w+x; pixels[idx]=Color.WHITE; }
                }
            }
            b.setPixels(pixels,0,w,0,0,w,h);
            // Coarse connected-component search for bright clusters in the central screen area.
            int bestX=-1,bestY=-1,bestCount=0;
            int x0=w/5,x1=w*4/5,y0=h/5,y1=h*4/5;
            for(int gy=y0;gy<y1;gy+=18) for(int gx=x0;gx<x1;gx+=18){
                int count=0;
                for(int yy=gy;yy<Math.min(h,gy+18);yy+=3) for(int xx=gx;xx<Math.min(w,gx+18);xx+=3) if(Color.red(pixels[yy*w+xx])>0)count++;
                if(count>bestCount){bestCount=count;bestX=gx+9;bestY=gy+9;}
            }
            if(bestCount>=3 && aimView!=null){aimView.detectedX=bestX;aimView.detectedY=bestY;aimView.hasDetection=true;aimView.postInvalidate();}
            else if(aimView!=null){aimView.hasDetection=false;aimView.postInvalidate();}
            b.recycle();
        }catch(Exception ignored){} finally {if(image!=null)image.close();}
    }
    private static class ByteBufferAccess { ByteBufferAccess(java.nio.ByteBuffer b){} }
    @Override public void onDestroy(){
        handler.removeCallbacks(frameTask);
        if(virtualDisplay!=null)virtualDisplay.release();
        if(imageReader!=null)imageReader.close();
        if(projection!=null)projection.stop();
        if(aimView!=null){try{wm.removeView(aimView);}catch(Exception ignored){}aimView=null;}
        if(panel!=null){try{wm.removeView(panel);}catch(Exception ignored){}panel=null;}
        stopForeground(true);super.onDestroy();
    }
    @Override public android.os.IBinder onBind(Intent i){return null;}

    private class AimView extends View {
        Paint p=new Paint(3); float manualX,manualY,targetX,targetY; float detectedX,detectedY; boolean hasDetection=false,autoMode=false;
        float startX,startY;
        AimView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null); DisplayMetrics d=getResources().getDisplayMetrics(); manualX=d.widthPixels*.28f;manualY=d.heightPixels*.65f;targetX=d.widthPixels*.70f;targetY=d.heightPixels*.40f;}
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            p.setStrokeWidth(4);p.setStyle(Paint.Style.STROKE);p.setColor(Color.argb(230,70,220,255));
            c.drawLine(manualX,manualY,targetX,targetY,p);
            p.setStrokeWidth(3);p.setColor(Color.argb(210,255,210,60)); c.drawLine(targetX,targetY,targetX+(targetX-manualX)*.7f,targetY+(targetY-manualY)*.7f,p);
            p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);c.drawCircle(manualX,manualY,9,p);
            p.setColor(Color.rgb(255,190,40));c.drawCircle(targetX,targetY,8,p);
            if(autoMode){
                p.setColor(Color.RED);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);
                if(hasDetection){c.drawCircle(detectedX,detectedY,18,p);p.setColor(Color.argb(220,255,70,70));c.drawLine(manualX,manualY,detectedX,detectedY,p);}
                else {p.setStyle(Paint.Style.FILL);p.setTextSize(30);c.drawText("Auto: looking for bright objects",30,getHeight()-60,p);}
            }
        }
        @Override public boolean onTouchEvent(android.view.MotionEvent e){
            if(e.getAction()==MotionEvent.ACTION_DOWN){startX=e.getX();startY=e.getY();return true;}
            if(e.getAction()==MotionEvent.ACTION_MOVE){manualX=e.getX();manualY=e.getY();invalidate();return true;}
            if(e.getAction()==MotionEvent.ACTION_UP){targetX=e.getX();targetY=e.getY();invalidate();return true;}
            return true;
        }
    }
}
