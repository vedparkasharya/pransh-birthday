package com.bookflow.reader;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.graphics.*;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
  private PdfRenderer renderer;
  private ParcelFileDescriptor descriptor;
  private ImageView pageView;
  private AnnotationView ink;
  private TextView title,pageLabel,empty;
  private int pageIndex=0;
  private final int PICK=41;
  private String tool="pen";
  private LinearLayout root, toolbar;
  private FrameLayout pageFrame;
  private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density);}
  private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;}
  public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(Color.rgb(247,245,239));getWindow().setNavigationBarColor(Color.rgb(247,245,239));getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);build();}
  private Button btn(String label){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextSize(12);return b;}
  private void build(){
    root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(14),dp(18),dp(14),dp(8));root.setBackgroundColor(Color.rgb(247,245,239));setContentView(root);
    TextView brand=text("BOOKFLOW  ·  STUDY MODE",12,Color.rgb(123,105,77));brand.setLetterSpacing(.12f);root.addView(brand);
    title=text("Your reading space.",25,Color.rgb(39,39,36));root.addView(title);
    Button open=btn("＋  Open a PDF");root.addView(open);open.setOnClickListener(v->pick());
    empty=text("Your library is empty.\nChoose a PDF to begin reading.",16,Color.GRAY);empty.setGravity(Gravity.CENTER);root.addView(empty,new LinearLayout.LayoutParams(-1,dp(110)));
    pageFrame=new FrameLayout(this);pageFrame.setBackgroundColor(Color.WHITE);LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,0,1);fp.topMargin=dp(8);fp.bottomMargin=dp(6);root.addView(pageFrame,fp);
    pageView=new ImageView(this);pageView.setAdjustViewBounds(true);pageView.setScaleType(ImageView.ScaleType.FIT_CENTER);pageFrame.addView(pageView,new FrameLayout.LayoutParams(-1,-1));
    ink=new AnnotationView(this);pageFrame.addView(ink,new FrameLayout.LayoutParams(-1,-1));
    toolbar=new LinearLayout(this);toolbar.setOrientation(LinearLayout.HORIZONTAL);toolbar.setGravity(Gravity.CENTER);root.addView(toolbar);
    addTool("Pen","pen");addTool("Highlight","highlight");addTool("Underline","underline");addTool("Eraser","eraser");
    LinearLayout actions=new LinearLayout(this);actions.setGravity(Gravity.CENTER);root.addView(actions);
    Button note=btn("＋ Note");actions.addView(note);note.setOnClickListener(v->editNote());
    Button large=btn("↔ Large writing");actions.addView(large);large.setOnClickListener(v->{setRequestedOrientation(getRequestedOrientation()==ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE?ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED:ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);});
    Button prev=btn("‹");actions.addView(prev);prev.setOnClickListener(v->showPage(pageIndex-1));
    pageLabel=text("No document",13,Color.DKGRAY);pageLabel.setPadding(dp(8),dp(10),dp(8),dp(10));actions.addView(pageLabel);
    Button next=btn("›");actions.addView(next);next.setOnClickListener(v->showPage(pageIndex+1));
    Button clear=btn("Clear");actions.addView(clear);clear.setOnClickListener(v->new AlertDialog.Builder(this).setMessage("Remove marks from this page?").setNegativeButton("Cancel",null).setPositiveButton("Clear",(d,w)->{ink.clear();saveInk();}).show());
  }
  private void addTool(String label,String value){Button b=btn(label);toolbar.addView(b,new LinearLayout.LayoutParams(0,dp(48),1));b.setOnClickListener(v->{tool=value;ink.setTool(value);for(int i=0;i<toolbar.getChildCount();i++)toolbar.getChildAt(i).setAlpha(toolbar.getChildAt(i)==b?1f:.55f);});}
  private void pick(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/pdf");startActivityForResult(i,PICK);}
  protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);if(req==PICK&&res==RESULT_OK&&data!=null){try{Uri u=data.getData();getContentResolver().takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION);if(renderer!=null)renderer.close();if(descriptor!=null)descriptor.close();descriptor=getContentResolver().openFileDescriptor(u,"r");renderer=new PdfRenderer(descriptor);pageIndex=0;empty.setVisibility(View.GONE);showPage(0);}catch(Exception e){new AlertDialog.Builder(this).setMessage("Could not open this PDF.").setPositiveButton("OK",null).show();}}}
  private String key(){return "ink_"+pageIndex;}
  private void showPage(int n){if(renderer==null||n<0||n>=renderer.getPageCount())return;pageIndex=n;PdfRenderer.Page p=renderer.openPage(n);int w=Math.max(1,getResources().getDisplayMetrics().widthPixels-dp(40));int h=Math.max(1,(int)(w*(float)p.getHeight()/p.getWidth()));Bitmap b=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);b.eraseColor(Color.WHITE);p.render(b,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);p.close();pageView.setImageBitmap(b);pageLabel.setText((n+1)+" / "+renderer.getPageCount());title.setText("Reading · Study");ink.load(getPreferences(0).getString(key(),""));}
  private void editNote(){if(renderer==null)return;EditText input=new EditText(this);input.setMinLines(4);input.setGravity(Gravity.TOP);input.setText(getPreferences(0).getString("note_"+pageIndex,""));new AlertDialog.Builder(this).setTitle("Page "+(pageIndex+1)+" notes").setView(input).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->getPreferences(0).edit().putString("note_"+pageIndex,input.getText().toString()).apply()).show();}
  private void saveInk(){getPreferences(0).edit().putString(key(),ink.serialize()).apply();}
  class AnnotationView extends View{
    class Stroke{ArrayList<PointF> pts=new ArrayList<>();int color;float width;String mode;}
    ArrayList<Stroke> strokes=new ArrayList<>();Stroke current;Paint paint=new Paint(3);String active="pen";
    AnnotationView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
    void setTool(String t){active=t;}
    void clear(){strokes.clear();invalidate();}
    protected void onDraw(Canvas c){super.onDraw(c);for(Stroke s:strokes)drawStroke(c,s);if(current!=null)drawStroke(c,current);}
    void drawStroke(Canvas c,Stroke s){if(s.pts.size()==0)return;paint.setColor(s.color);paint.setStrokeWidth(s.width*Math.max(1,getWidth()));paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);paint.setStyle(Paint.Style.STROKE);if(s.mode.equals("highlight"))paint.setAlpha(85);else paint.setAlpha(255);Path path=new Path();path.moveTo(s.pts.get(0).x*getWidth(),s.pts.get(0).y*getHeight());for(int i=1;i<s.pts.size();i++)path.lineTo(s.pts.get(i).x*getWidth(),s.pts.get(i).y*getHeight());c.drawPath(path,paint);if(s.mode.equals("underline")&&s.pts.size()>1){PointF a=s.pts.get(0),z=s.pts.get(s.pts.size()-1);c.drawLine(a.x*getWidth(),a.y*getHeight()+dp(5),z.x*getWidth(),z.y*getHeight()+dp(5),paint);}}
    public boolean onTouchEvent(android.view.MotionEvent e){float x=e.getX()/Math.max(1,getWidth()),y=e.getY()/Math.max(1,getHeight());if(e.getAction()==MotionEvent.ACTION_DOWN){current=new Stroke();current.mode=active;current.color=active.equals("highlight")?Color.YELLOW:active.equals("underline")?Color.RED:Color.rgb(30,75,180);current.width=active.equals("highlight")?.035f:.006f;current.pts.add(new PointF(x,y));invalidate();return true;}if(e.getAction()==MotionEvent.ACTION_MOVE&&current!=null){current.pts.add(new PointF(x,y));invalidate();return true;}if(e.getAction()==MotionEvent.ACTION_UP&&current!=null){current.pts.add(new PointF(x,y));if(active.equals("eraser")){strokes.clear();}else strokes.add(current);current=null;saveInk();invalidate();return true;}return true;}
    String serialize(){JSONArray arr=new JSONArray();try{for(Stroke s:strokes){JSONObject o=new JSONObject();o.put("m",s.mode);o.put("c",s.color);o.put("w",s.width);JSONArray pts=new JSONArray();for(PointF p:s.pts){JSONArray xy=new JSONArray();xy.put(p.x);xy.put(p.y);pts.put(xy);}o.put("p",pts);arr.put(o);}}catch(Exception ignored){}return arr.toString();}
    void load(String raw){strokes.clear();try{JSONArray arr=new JSONArray(raw);for(int i=0;i<arr.length();i++){JSONObject o=arr.getJSONObject(i);Stroke s=new Stroke();s.mode=o.getString("m");s.color=o.getInt("c");s.width=(float)o.getDouble("w");JSONArray pts=o.getJSONArray("p");for(int j=0;j<pts.length();j++){JSONArray xy=pts.getJSONArray(j);s.pts.add(new PointF((float)xy.getDouble(0),(float)xy.getDouble(1)));}strokes.add(s);}}catch(Exception ignored){}invalidate();}
  }
  protected void onDestroy(){try{if(renderer!=null)renderer.close();if(descriptor!=null)descriptor.close();}catch(Exception ignored){}super.onDestroy();}
}