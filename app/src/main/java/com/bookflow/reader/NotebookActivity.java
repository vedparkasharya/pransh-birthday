package com.bookflow.reader;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;

public class NotebookActivity extends Activity {
  private Pad pad; private String storeKey="bookflow_notebook"; private TextView status;
  int dp(float n){return (int)(n*getResources().getDisplayMetrics().density);}
  Button tool(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextSize(12);return b;}
  public void onCreate(Bundle b){super.onCreate(b);setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);getWindow().setFlags(1024,1024);
    LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(8),dp(6),dp(8),dp(6));root.setBackgroundColor(Color.rgb(246,244,238));setContentView(root);
    LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);root.addView(bar,new LinearLayout.LayoutParams(-1,dp(52)));
    TextView title=new TextView(this);title.setText("BOOKFLOW  /  NOTEBOOK");title.setTextSize(14);title.setTextColor(Color.rgb(65,57,44));title.setPadding(dp(8),0,dp(12),0);bar.addView(title,new LinearLayout.LayoutParams(0,-2,1));
    add(bar,"Pen",()->pad.mode="pen");add(bar,"Marker",()->pad.mode="marker");add(bar,"Eraser",()->pad.mode="eraser");add(bar,"Undo",()->pad.undo());add(bar,"Clear",()->new AlertDialog.Builder(this).setMessage("Clear this notebook page?").setNegativeButton("Cancel",null).setPositiveButton("Clear",(d,w)->pad.clear()).show());
    Button done=tool("Done ✓");bar.addView(done);done.setOnClickListener(v->{pad.save();finish();});
    pad=new Pad(this);root.addView(pad,new LinearLayout.LayoutParams(-1,0,1));pad.load();
    status=new TextView(this);status.setText("Saved automatically on this device  ·  Use finger or stylus");status.setTextSize(11);status.setTextColor(Color.GRAY);status.setPadding(dp(8),dp(5),0,0);root.addView(status);
  }
  void add(LinearLayout bar,String label,Runnable action){Button b=tool(label);bar.addView(b);b.setOnClickListener(v->action.run());}
  class Stroke {ArrayList<PointF> points=new ArrayList<>();int color;float width;String mode;}
  class Pad extends View{
    ArrayList<Stroke> strokes=new ArrayList<>();ArrayList<Stroke> undone=new ArrayList<>();Stroke active;String mode="pen";Paint p=new Paint(3);
    Pad(Context c){super(c);setBackgroundColor(Color.WHITE);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
    protected void onDraw(Canvas c){super.onDraw(c);float gap=dp(32);p.setColor(Color.rgb(226,231,235));p.setStrokeWidth(1);for(float y=gap;y<getHeight();y+=gap)c.drawLine(0,y,getWidth(),y,p);p.setColor(Color.rgb(230,190,190));c.drawLine(dp(48),0,dp(48),getHeight(),p);for(Stroke s:strokes)draw(c,s);if(active!=null)draw(c,active);}
    void draw(Canvas c,Stroke s){if(s.points.isEmpty())return;p.setColor(s.color);p.setStrokeWidth(s.width);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);p.setStyle(Paint.Style.STROKE);p.setAlpha(s.mode.equals("marker")?75:255);Path path=new Path();path.moveTo(s.points.get(0).x,s.points.get(0).y);for(int i=1;i<s.points.size();i++)path.lineTo(s.points.get(i).x,s.points.get(i).y);if(s.points.size()==1)c.drawCircle(s.points.get(0).x,s.points.get(0).y,s.width/2,p);else c.drawPath(path,p);}
    public boolean onTouchEvent(android.view.MotionEvent e){float x=e.getX(),y=e.getY();if(e.getAction()==0){active=new Stroke();active.mode=mode;active.color=mode.equals("marker")?Color.YELLOW:Color.rgb(35,70,150);active.width=mode.equals("marker")?dp(18):dp(3);active.points.add(new PointF(x,y));invalidate();return true;}if(e.getAction()==2&&active!=null){active.points.add(new PointF(x,y));invalidate();return true;}if(e.getAction()==1&&active!=null){active.points.add(new PointF(x,y));if(mode.equals("eraser")){if(!strokes.isEmpty())strokes.remove(strokes.size()-1);}else{strokes.add(active);undone.clear();}active=null;save();invalidate();return true;}return true;}
    void undo(){if(!strokes.isEmpty()){undone.add(strokes.remove(strokes.size()-1));save();invalidate();}}
    void clear(){strokes.clear();undone.clear();save();invalidate();}
    void save(){JSONArray a=new JSONArray();try{for(Stroke s:strokes){JSONObject o=new JSONObject();o.put("c",s.color);o.put("w",s.width);o.put("m",s.mode);JSONArray pts=new JSONArray();for(PointF q:s.points){JSONArray xy=new JSONArray();xy.put(q.x/Math.max(1,getWidth()));xy.put(q.y/Math.max(1,getHeight()));pts.put(xy);}o.put("p",pts);a.put(o);}}catch(Exception ignored){}getPreferences(0).edit().putString(storeKey,a.toString()).apply();}
    void load(){strokes.clear();try{JSONArray a=new JSONArray(getPreferences(0).getString(storeKey,"[]"));for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);Stroke s=new Stroke();s.color=o.getInt("c");s.width=(float)o.getDouble("w");s.mode=o.getString("m");JSONArray pts=o.getJSONArray("p");for(int j=0;j<pts.length();j++){JSONArray xy=pts.getJSONArray(j);s.points.add(new PointF((float)xy.getDouble(0)*getWidth(),(float)xy.getDouble(1)*getHeight()));}strokes.add(s);}}catch(Exception ignored){}postInvalidate();}
  }
  public void onBackPressed(){pad.save();super.onBackPressed();}
}