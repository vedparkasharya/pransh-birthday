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
  private int readingTheme=0; // 0 paper, 1 sepia, 2 night
  private Uri currentUri;
  private SeekBar progressBar;
  private TextView recentTitle;
  private LinearLayout root, toolbar;
  private FrameLayout pageFrame;
  private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density);}
  private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;}
  public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(Color.rgb(247,245,239));getWindow().setNavigationBarColor(Color.rgb(247,245,239));getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);build();}
  private Button btn(String label){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextSize(12);b.setTextColor(Color.rgb(54,65,58));b.setBackground(makeBg(Color.rgb(237,231,218),dp(16)));b.setPadding(dp(10),dp(4),dp(10),dp(4));return b;}
  private TextView section(String s){TextView t=text(s,12,Color.rgb(116,105,86));t.setTypeface(null,android.graphics.Typeface.BOLD);t.setLetterSpacing(.10f);return t;}
  private LinearLayout panel(){LinearLayout p=new LinearLayout(this);p.setOrientation(1);p.setPadding(dp(15),dp(13),dp(15),dp(13));p.setBackground(makeBg(Color.WHITE,dp(20)));return p;}
  private void openRecent(){String u=getPreferences(0).getString("last_pdf_uri","");if(u.isEmpty()){pick();return;}try{Uri uri=Uri.parse(u);descriptor=getContentResolver().openFileDescriptor(uri,"r");renderer=new PdfRenderer(descriptor);pageIndex=getPreferences(0).getInt("last_pdf_page",0);empty.setVisibility(View.GONE);showPage(pageIndex);}catch(Exception e){pick();}}
  private android.graphics.drawable.GradientDrawable makeBg(int color,int radius){android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();d.setColor(color);d.setCornerRadius(radius);return d;}
  private void applyTheme(){
    int bg=readingTheme==2?Color.rgb(28,29,31):readingTheme==1?Color.rgb(235,224,199):Color.rgb(247,245,239);
    root.setBackgroundColor(bg);pageFrame.setBackgroundColor(readingTheme==2?Color.rgb(20,21,23):readingTheme==1?Color.rgb(235,224,199):Color.WHITE);
    title.setTextColor(readingTheme==2?Color.WHITE:Color.rgb(39,39,36));
    pageLabel.setTextColor(readingTheme==2?Color.LTGRAY:Color.DKGRAY);
    if(pageView.getDrawable()!=null){
      if(readingTheme==2){float[] m={-1,0,0,0,255,0,-1,0,0,255,0,0,-1,0,255,0,0,0,1,0};pageView.setColorFilter(new android.graphics.ColorMatrixColorFilter(m));}
      else if(readingTheme==1){float[] m={.90f,.08f,.02f,0,12,.04f,.88f,.04f,0,8,.02f,.08f,.78f,0,0,0,0,0,1,0};pageView.setColorFilter(new android.graphics.ColorMatrixColorFilter(m));}
      else pageView.clearColorFilter();
    }
    getWindow().setStatusBarColor(bg);getWindow().setNavigationBarColor(bg);
    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
  }
  private void build(){
    root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(18),dp(16),dp(18),dp(10));root.setBackgroundColor(Color.rgb(246,244,237));setContentView(root);
    LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);root.addView(header);
    ImageView logo=new ImageView(this);logo.setImageResource(com.bookflow.reader.R.drawable.bookflow_logo);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(48),dp(48));lp.rightMargin=dp(11);header.addView(logo,lp);
    LinearLayout brandCol=new LinearLayout(this);brandCol.setOrientation(1);header.addView(brandCol,new LinearLayout.LayoutParams(0,-2,1));
    TextView brand=text("BOOKFLOW",15,Color.rgb(35,59,53));brand.setTypeface(null,android.graphics.Typeface.BOLD);brand.setLetterSpacing(.16f);brandCol.addView(brand);
    TextView tagline=text("Your personal reading space",12,Color.rgb(125,116,99));brandCol.addView(tagline);
    Button open=btn("＋ Add book");header.addView(open);open.setOnClickListener(v->pick());
    title=text("Make time for a good book.",25,Color.rgb(39,48,42));title.setTypeface(null,android.graphics.Typeface.BOLD);LinearLayout.LayoutParams ttl=new LinearLayout.LayoutParams(-1,-2);ttl.topMargin=dp(20);root.addView(title,ttl);
    TextView sub=text("Read, highlight, annotate and keep your thoughts together.",13,Color.rgb(119,113,100));LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.topMargin=dp(5);root.addView(sub,sp);
    LinearLayout shelf=panel();LinearLayout.LayoutParams sh=new LinearLayout.LayoutParams(-1,-2);sh.topMargin=dp(17);root.addView(shelf,sh);
    TextView shelfHead=text("MY LIBRARY",11,Color.rgb(143,119,78));shelfHead.setTypeface(null,android.graphics.Typeface.BOLD);shelfHead.setLetterSpacing(.13f);shelf.addView(shelfHead);
    recentTitle=text("Your bookshelf is waiting",18,Color.rgb(48,57,49));recentTitle.setTypeface(null,android.graphics.Typeface.BOLD);LinearLayout.LayoutParams rh=new LinearLayout.LayoutParams(-1,-2);rh.topMargin=dp(7);shelf.addView(recentTitle,rh);
    TextView shelfHint=text("Open a PDF to start your first reading session.",12,Color.rgb(126,121,109));shelf.addView(shelfHint);
    Button recent=btn("Continue reading  →");LinearLayout.LayoutParams rr=new LinearLayout.LayoutParams(-1,dp(44));rr.topMargin=dp(10);shelf.addView(recent,rr);recent.setOnClickListener(v->openRecent());
    String last=getPreferences(0).getString("last_pdf_name","");if(!last.isEmpty()){recentTitle.setText(last);shelfHint.setText("Pick up where you left off.");}
    LinearLayout tools=new LinearLayout(this);tools.setGravity(Gravity.CENTER_VERTICAL);LinearLayout.LayoutParams tps=new LinearLayout.LayoutParams(-1,-2);tps.topMargin=dp(15);root.addView(section("YOUR STUDY TOOLS"),tps);root.addView(tools);
    Button smart=btn("✍  Smart Notes");tools.addView(smart,new LinearLayout.LayoutParams(0,dp(48),1));smart.setOnClickListener(v->startActivity(new Intent(this,SmartNotesActivity.class)));
    View gap=new View(this);tools.addView(gap,new LinearLayout.LayoutParams(dp(8),1));
    Button large=btn("▤  Notebook");tools.addView(large,new LinearLayout.LayoutParams(0,dp(48),1));large.setOnClickListener(v->startActivity(new Intent(this,NotebookActivity.class)));
    LinearLayout.LayoutParams rl=new LinearLayout.LayoutParams(-1,-2);rl.topMargin=dp(14);root.addView(section("NOW READING"),rl);
    empty=text("Your reading page will appear here once you open a PDF.",13,Color.rgb(132,126,113));empty.setGravity(Gravity.CENTER);empty.setPadding(dp(20),dp(18),dp(20),dp(18));empty.setBackground(makeBg(Color.rgb(239,236,228),dp(18)));LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(-1,dp(75));ep.topMargin=dp(7);root.addView(empty,ep);
    pageFrame=new FrameLayout(this);pageFrame.setBackground(makeBg(Color.WHITE,dp(12)));pageFrame.setClipToOutline(true);LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,0,1);fp.topMargin=dp(8);fp.bottomMargin=dp(6);root.addView(pageFrame,fp);
    pageView=new ImageView(this);pageView.setAdjustViewBounds(true);pageView.setScaleType(ImageView.ScaleType.FIT_CENTER);pageFrame.addView(pageView,new FrameLayout.LayoutParams(-1,-1));
    ink=new AnnotationView(this);pageFrame.addView(ink,new FrameLayout.LayoutParams(-1,-1));
    progressBar=new SeekBar(this);progressBar.setMax(1000);progressBar.setProgress(0);root.addView(progressBar,new LinearLayout.LayoutParams(-1,dp(24)));progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int p,boolean u){}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){if(renderer!=null)showPage(Math.round((renderer.getPageCount()-1)*b.getProgress()/1000f));}});
    toolbar=new LinearLayout(this);toolbar.setOrientation(LinearLayout.HORIZONTAL);toolbar.setGravity(Gravity.CENTER);root.addView(toolbar);
    addTool("Pen","pen");addTool("Highlight","highlight");addTool("Underline","underline");addTool("Eraser","eraser");
    LinearLayout actions=new LinearLayout(this);actions.setGravity(Gravity.CENTER);root.addView(actions);
    Button note=btn("＋ Note");actions.addView(note);note.setOnClickListener(v->editNote());
    Button prev=btn("‹");actions.addView(prev);prev.setOnClickListener(v->showPage(pageIndex-1));
    pageLabel=text("No document",12,Color.DKGRAY);pageLabel.setPadding(dp(6),dp(8),dp(6),dp(8));actions.addView(pageLabel);
    Button next=btn("›");actions.addView(next);next.setOnClickListener(v->showPage(pageIndex+1));
    Button theme=btn("◐");actions.addView(theme);theme.setOnClickListener(v->{readingTheme=(readingTheme+1)%3;applyTheme();theme.setText(readingTheme==0?"☼":readingTheme==1?"▤":"☾");});
    Button mark=btn("☆");actions.addView(mark);mark.setOnClickListener(v->toggleBookmark(mark));
    Button jump=btn("Go to");actions.addView(jump);jump.setOnClickListener(v->jumpToPage());
    Button clear=btn("Clear");actions.addView(clear);clear.setOnClickListener(v->new AlertDialog.Builder(this).setMessage("Remove marks from this page?").setNegativeButton("Cancel",null).setPositiveButton("Clear",(d,w)->{ink.clear();saveInk();}).show());
  }
  private void addTool(String label,String value){Button b=btn(label);toolbar.addView(b,new LinearLayout.LayoutParams(0,dp(48),1));b.setOnClickListener(v->{tool=value;ink.setTool(value);for(int i=0;i<toolbar.getChildCount();i++)toolbar.getChildAt(i).setAlpha(toolbar.getChildAt(i)==b?1f:.55f);});}
  private void pick(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/pdf");startActivityForResult(i,PICK);}
  protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);if(req==PICK&&res==RESULT_OK&&data!=null){try{Uri u=data.getData();getContentResolver().takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION);getPreferences(0).edit().putString("last_pdf_uri",u.toString()).putString("last_pdf_name",queryName(u)).putInt("last_pdf_page",0).apply();recentTitle.setText(queryName(u));if(renderer!=null)renderer.close();if(descriptor!=null)descriptor.close();descriptor=getContentResolver().openFileDescriptor(u,"r");renderer=new PdfRenderer(descriptor);currentUri=u;getPreferences(0).edit().putString("last_pdf_uri",u.toString()).apply();pageIndex=getPreferences(0).getInt("progress_"+uriKey(u),0);empty.setVisibility(View.GONE);showPage(pageIndex);}catch(Exception e){new AlertDialog.Builder(this).setMessage("Could not open this PDF.").setPositiveButton("OK",null).show();}}}
  private String key(){return "ink_"+pageIndex;}
  private String uriKey(Uri u){return Integer.toHexString(u.toString().hashCode());}
  private void resumeLast(){try{String raw=getPreferences(0).getString("last_pdf_uri","");if(raw.isEmpty()){pick();return;}Uri u=Uri.parse(raw);getContentResolver().takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION);if(renderer!=null)renderer.close();if(descriptor!=null)descriptor.close();descriptor=getContentResolver().openFileDescriptor(u,"r");renderer=new PdfRenderer(descriptor);currentUri=u;empty.setVisibility(View.GONE);showPage(getPreferences(0).getInt("progress_"+uriKey(u),0));}catch(Exception e){pick();}}
  private void toggleBookmark(Button b){if(renderer==null||currentUri==null)return;String k="bookmarks_"+uriKey(currentUri);Set<String> pages=new HashSet<>(getPreferences(0).getStringSet(k,new HashSet<>()));String p=String.valueOf(pageIndex);if(pages.contains(p)){pages.remove(p);b.setText("☆");}else{pages.add(p);b.setText("★");}getPreferences(0).edit().putStringSet(k,pages).apply();Toast.makeText(this,pages.contains(p)?"Page bookmarked":"Bookmark removed",Toast.LENGTH_SHORT).show();}
  private void jumpToPage(){if(renderer==null)return;EditText input=new EditText(this);input.setInputType(2);input.setHint("1 - "+renderer.getPageCount());new AlertDialog.Builder(this).setTitle("Go to page").setView(input).setNegativeButton("Cancel",null).setPositiveButton("Go",(d,w)->{try{showPage(Integer.parseInt(input.getText().toString().trim())-1);}catch(Exception e){Toast.makeText(this,"Enter a valid page number",Toast.LENGTH_SHORT).show();}}).show();}
  private String queryName(Uri u){String name=u.getLastPathSegment();if(name==null)return "Your recent book";int slash=name.lastIndexOf("/");if(slash>=0)name=name.substring(slash+1);try{name=java.net.URLDecoder.decode(name,"UTF-8");}catch(Exception ignored){}return name.length()>36?name.substring(0,33)+"…":name;}
  private void showPage(int n){if(renderer==null||n<0||n>=renderer.getPageCount())return;pageIndex=n;PdfRenderer.Page p=renderer.openPage(n);int w=Math.max(1,getResources().getDisplayMetrics().widthPixels-dp(40));int h=Math.max(1,(int)(w*(float)p.getHeight()/p.getWidth()));Bitmap b=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);b.eraseColor(Color.WHITE);p.render(b,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);p.close();pageView.setImageBitmap(b);applyTheme();pageLabel.setText((n+1)+" / "+renderer.getPageCount());getPreferences(0).edit().putInt("last_pdf_page",n).apply();title.setText("Reading · Study");ink.load(getPreferences(0).getString(key(),""));}
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