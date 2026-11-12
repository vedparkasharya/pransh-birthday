package com.bookflow.reader;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import java.io.OutputStream;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;

public class NotebookActivity extends Activity {
  private Pad pad; private String storeKey="bookflow_notebook"; private TextView status; private static final int CREATE_PDF=7413; private int pageIndex=0;
  int dp(float n){return (int)(n*getResources().getDisplayMetrics().density);}
  Button tool(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextSize(12);return b;}
  public void onCreate(Bundle b){super.onCreate(b);setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);getWindow().setFlags(1024,1024);
    LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(8),dp(6),dp(8),dp(6));root.setBackgroundColor(Color.rgb(246,244,238));setContentView(root);
    LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);root.addView(bar,new LinearLayout.LayoutParams(-1,dp(52)));
    TextView title=new TextView(this);title.setText("BOOKFLOW  /  NOTEBOOK");title.setTextSize(14);title.setTextColor(Color.rgb(65,57,44));title.setPadding(dp(8),0,dp(12),0);bar.addView(title,new LinearLayout.LayoutParams(0,-2,1));
    add(bar,"Pen",()->pad.mode="pen");add(bar,"Marker",()->pad.mode="marker");add(bar,"Eraser",()->pad.mode="eraser");add(bar,"Line",()->pad.mode="line");add(bar,"Rect",()->pad.mode="rect");add(bar,"Circle",()->pad.mode="circle");add(bar,"Triangle",()->pad.mode="triangle");add(bar,"Black",()->pad.color=Color.BLACK);add(bar,"Blue",()->pad.color=Color.rgb(35,70,150));add(bar,"Red",()->pad.color=Color.rgb(190,45,55));add(bar,"Green",()->pad.color=Color.rgb(35,130,85));add(bar,"Width",()->chooseWidth());add(bar,"Undo",()->pad.undo());add(bar,"Redo",()->pad.redo());add(bar,"Page −",()->changePage(-1));add(bar,"Page +",()->changePage(1));add(bar,"Clear",()->new AlertDialog.Builder(this).setMessage("Clear this notebook page?").setNegativeButton("Cancel",null).setPositiveButton("Clear",(d,w)->pad.clear()).show());
    Button pdf=tool("PDF ↓");bar.addView(pdf);pdf.setOnClickListener(v->exportPdf());
    Button done=tool("Done ✓");bar.addView(done);done.setOnClickListener(v->{pad.save();finish();});
    pad=new Pad(this);root.addView(pad,new LinearLayout.LayoutParams(-1,0,1));pad.post(()->pad.load());
    status=new TextView(this);status.setText("Saved automatically on this device  ·  Use finger or stylus");status.setTextSize(11);status.setTextColor(Color.GRAY);status.setPadding(dp(8),dp(5),0,0);root.addView(status);
  }
  void add(LinearLayout bar,String label,Runnable action){Button b=tool(label);bar.addView(b);b.setOnClickListener(v->action.run());}
  class Stroke {ArrayList<PointF> points=new ArrayList<>();int color;float width;String mode;}
  class Pad extends View{
    ArrayList<Stroke> strokes=new ArrayList<>();ArrayList<Stroke> undone=new ArrayList<>();Stroke active;String mode="pen";int color=Color.rgb(35,70,150);float penWidth=dp(3);Paint p=new Paint(3);
    Pad(Context c){super(c);setBackgroundColor(Color.WHITE);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
    protected void onDraw(Canvas c){super.onDraw(c);float gap=dp(32);p.setColor(Color.rgb(226,231,235));p.setStrokeWidth(1);for(float y=gap;y<getHeight();y+=gap)c.drawLine(0,y,getWidth(),y,p);p.setColor(Color.rgb(230,190,190));c.drawLine(dp(48),0,dp(48),getHeight(),p);for(Stroke s:strokes)draw(c,s);if(active!=null)draw(c,active);}
    void draw(Canvas c,Stroke s){if(s.points.isEmpty())return;p.setColor(s.color);p.setStrokeWidth(s.width);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);p.setStyle(Paint.Style.STROKE);p.setAlpha(s.mode.equals("marker")?75:255);Path path=new Path();PointF a=s.points.get(0),z=s.points.get(s.points.size()-1);
      if(s.mode.equals("rect")){c.drawRect(a.x,a.y,z.x,z.y,p);return;}
      if(s.mode.equals("circle")){c.drawOval(Math.min(a.x,z.x),Math.min(a.y,z.y),Math.max(a.x,z.x),Math.max(a.y,z.y),p);return;}
      if(s.mode.equals("line")){c.drawLine(a.x,a.y,z.x,z.y,p);return;}
      if(s.mode.equals("triangle")){path.moveTo((a.x+z.x)/2,a.y);path.lineTo(z.x,z.y);path.lineTo(a.x,z.y);path.close();c.drawPath(path,p);return;}
      path.moveTo(a.x,a.y);for(int i=1;i<s.points.size();i++)path.lineTo(s.points.get(i).x,s.points.get(i).y);if(s.points.size()==1)c.drawCircle(a.x,a.y,s.width/2,p);else c.drawPath(path,p);}
    public boolean onTouchEvent(android.view.MotionEvent e){float x=e.getX(),y=e.getY();if(e.getAction()==0){active=new Stroke();active.mode=mode;active.color=mode.equals("marker")?Color.YELLOW:color;active.width=mode.equals("marker")?dp(18):penWidth;active.points.add(new PointF(x,y));invalidate();return true;}if(e.getAction()==2&&active!=null){if(active.mode.equals("line")||active.mode.equals("rect")||active.mode.equals("circle")||active.mode.equals("triangle")){if(active.points.size()>1)active.points.set(1,new PointF(x,y));else active.points.add(new PointF(x,y));}else active.points.add(new PointF(x,y));invalidate();return true;}if(e.getAction()==1&&active!=null){active.points.add(new PointF(x,y));if(mode.equals("eraser")){eraseNearest(x,y);}else{strokes.add(active);undone.clear();}active=null;save();invalidate();return true;}return true;}
    void eraseNearest(float x,float y){int hit=-1;float best=dp(24);for(int i=0;i<strokes.size();i++){Stroke s=strokes.get(i);for(PointF q:s.points){float dist=(float)Math.hypot(q.x-x,q.y-y);if(dist<best){best=dist;hit=i;}}}if(hit>=0)undone.add(strokes.remove(hit));save();invalidate();}
    void undo(){if(!strokes.isEmpty()){undone.add(strokes.remove(strokes.size()-1));save();invalidate();}}
    void redo(){if(!undone.isEmpty()){strokes.add(undone.remove(undone.size()-1));save();invalidate();}}
    void clear(){strokes.clear();undone.clear();save();invalidate();}
    void save(){JSONArray a=new JSONArray();try{for(Stroke s:strokes){JSONObject o=new JSONObject();o.put("c",s.color);o.put("w",s.width);o.put("m",s.mode);JSONArray pts=new JSONArray();for(PointF q:s.points){JSONArray xy=new JSONArray();xy.put(q.x/Math.max(1,getWidth()));xy.put(q.y/Math.max(1,getHeight()));pts.put(xy);}o.put("p",pts);a.put(o);}}catch(Exception ignored){}getPreferences(0).edit().putString(storeKey+"_page_"+pageIndex,a.toString()).apply();}
    void load(){strokes.clear();try{JSONArray a=new JSONArray(getPreferences(0).getString(storeKey+"_page_"+pageIndex,"[]"));for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);Stroke s=new Stroke();s.color=o.getInt("c");s.width=(float)o.getDouble("w");s.mode=o.getString("m");JSONArray pts=o.getJSONArray("p");for(int j=0;j<pts.length();j++){JSONArray xy=pts.getJSONArray(j);s.points.add(new PointF((float)xy.getDouble(0)*getWidth(),(float)xy.getDouble(1)*getHeight()));}strokes.add(s);}}catch(Exception ignored){}postInvalidate();}
  }
  private void exportPdf(){Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/pdf");i.putExtra(Intent.EXTRA_TITLE,"BookFlow-Notebook.pdf");startActivityForResult(i,CREATE_PDF);}
  @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);if(requestCode!=CREATE_PDF||resultCode!=RESULT_OK||data==null||data.getData()==null)return;try{pad.save();PdfDocument pdf=new PdfDocument();PdfDocument.Page pg=pdf.startPage(new PdfDocument.PageInfo.Builder(842,595,1).create());Canvas cv=pg.getCanvas();cv.drawColor(Color.WHITE);float scale=Math.min(800f/Math.max(1,pad.getWidth()),550f/Math.max(1,pad.getHeight()));cv.save();cv.translate(21,22);cv.scale(scale,scale);pad.draw(cv);cv.restore();pdf.finishPage(pg);try(OutputStream out=getContentResolver().openOutputStream(data.getData())){if(out==null)throw new Exception("Cannot open destination");pdf.writeTo(out);}pdf.close();status.setText("Notebook PDF exported ✓");}catch(Exception e){status.setText("PDF export failed: "+e.getMessage());}}
  public void onBackPressed(){pad.save();super.onBackPressed();}
}