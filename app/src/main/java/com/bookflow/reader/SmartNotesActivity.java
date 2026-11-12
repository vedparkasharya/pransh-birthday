package com.bookflow.reader;

import android.app.*;
import android.os.*;
import android.graphics.*;
import android.text.*;
import android.text.style.*;
import android.view.*;
import android.widget.*;
import android.content.*;
import com.google.mlkit.vision.digitalink.*;
import com.google.mlkit.vision.digitalink.common.*;
import com.google.android.gms.tasks.*;
import java.util.*;

public class SmartNotesActivity extends Activity {
  private EditText editor;
  private InkCanvas inkCanvas;
  private DigitalInkRecognizer recognizer;
  private DigitalInkRecognitionModel model;
  private DigitalInkRecognitionModelIdentifier modelId;
  private TextView status;
  private final String STORE="bookflow_smart_notes_html";
  private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density);}
  private Button button(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextSize(12);return b;}

  @Override public void onCreate(Bundle b){
    super.onCreate(b);
    getWindow().setStatusBarColor(Color.rgb(247,245,239));
    getWindow().setNavigationBarColor(Color.rgb(247,245,239));
    LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(10),dp(8),dp(10),dp(8));root.setBackgroundColor(Color.rgb(247,245,239));setContentView(root);
    TextView title=new TextView(this);title.setText("BOOKFLOW  /  SMART NOTES");title.setTextSize(14);title.setTextColor(Color.rgb(95,78,52));root.addView(title);
    TextView subtitle=new TextView(this);subtitle.setText("Write naturally, convert to editable English, then format your notes.");subtitle.setTextSize(12);subtitle.setTextColor(Color.DKGRAY);root.addView(subtitle);
    LinearLayout format=new LinearLayout(this);format.setOrientation(LinearLayout.HORIZONTAL);format.setGravity(Gravity.CENTER);root.addView(format);
    add(format,"B",()->apply(new StyleSpan(android.graphics.Typeface.BOLD)));
    add(format,"I",()->apply(new StyleSpan(android.graphics.Typeface.ITALIC)));
    add(format,"U",()->apply(new UnderlineSpan()));
    add(format,"H1",()->apply(new RelativeSizeSpan(1.5f)));
    add(format,"H2",()->apply(new RelativeSizeSpan(1.2f)));
    add(format,"Blue",()->apply(new ForegroundColorSpan(Color.rgb(35,75,160))));
    add(format,"Yellow",()->apply(new BackgroundColorSpan(Color.YELLOW)));
    add(format,"• List",()->{int p=editor.getSelectionStart();editor.getText().insert(Math.max(0,p),"• ");});
    editor=new EditText(this);editor.setGravity(Gravity.TOP|Gravity.START);editor.setTextSize(17);editor.setPadding(dp(12),dp(12),dp(12),dp(12));editor.setHint("Your recognised and typed notes appear here…");editor.setBackgroundColor(Color.WHITE);editor.setMinLines(7);
    LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(-1,0,1);ep.topMargin=dp(8);root.addView(editor,ep);
    String saved=getPreferences(0).getString(STORE,"");if(!saved.isEmpty())editor.setText(android.text.Html.fromHtml(saved,android.text.Html.FROM_HTML_MODE_LEGACY));
    TextView handTitle=new TextView(this);handTitle.setText("HANDWRITING INPUT  ·  Write below, then tap Recognise");handTitle.setTextSize(12);handTitle.setTextColor(Color.DKGRAY);handTitle.setPadding(0,dp(8),0,dp(4));root.addView(handTitle);
    inkCanvas=new InkCanvas(this);inkCanvas.setBackgroundColor(Color.WHITE);LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(170));root.addView(inkCanvas,cp);
    LinearLayout actions=new LinearLayout(this);actions.setGravity(Gravity.CENTER);root.addView(actions);
    Button recognise=button("Recognise English");actions.addView(recognise);recognise.setOnClickListener(v->recognise());
    Button clear=button("Clear ink");actions.addView(clear);clear.setOnClickListener(v->inkCanvas.clear());
    Button save=button("Save notes");actions.addView(save);save.setOnClickListener(v->save());
    status=new TextView(this);status.setText("Handwriting recognition downloads the English model once; internet may be needed.");status.setTextSize(11);status.setTextColor(Color.GRAY);root.addView(status);
    setupRecognizer();
  }
  private void add(LinearLayout bar,String label,Runnable action){Button b=button(label);bar.addView(b,new LinearLayout.LayoutParams(0,dp(44),1));b.setOnClickListener(v->action.run());}
  private void apply(Object span){int a=editor.getSelectionStart(),z=editor.getSelectionEnd();if(a<0||z<=a){Toast.makeText(this,"Select some text first",Toast.LENGTH_SHORT).show();return;}editor.getText().setSpan(span,a,z,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);}
  private void save(){getPreferences(0).edit().putString(STORE,android.text.Html.toHtml(editor.getText(),android.text.Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)).apply();status.setText("Saved on this device ✓");}
  private void setupRecognizer(){
    try{modelId=DigitalInkRecognitionModelIdentifier.fromLanguageTag("en-US");if(modelId==null){status.setText("English handwriting model is unavailable.");return;}model=DigitalInkRecognitionModel.builder(modelId).build();DigitalInkRecognitionModelManager manager=DigitalInkRecognition.getClient().getModelManager();manager.download(model, new DownloadConditions.Builder().build()).addOnSuccessListener(v->{recognizer=DigitalInkRecognition.getClient(DigitalInkRecognizerOptions.builder(modelId).build());status.setText("English handwriting recognition ready ✓");}).addOnFailureListener(e->status.setText("Model download failed. Connect to internet and try again."));}catch(Exception e){status.setText("Handwriting recognition could not start.");}
  }
  private void recognise(){
    if(recognizer==null){status.setText("English model is still downloading. Please wait.");return;}
    Ink ink=inkCanvas.toInk();if(ink==null){status.setText("Write something in the handwriting area first.");return;}
    status.setText("Recognising handwriting…");
    recognizer.recognize(ink).addOnSuccessListener(result->{String value=result.getText();if(value==null||value.trim().isEmpty()){status.setText("No text recognised. Try writing more clearly.");return;}int p=Math.max(0,editor.getSelectionStart());editor.getText().insert(p,value+" ");status.setText("Recognised. You can edit and format the text.");inkCanvas.clear();}).addOnFailureListener(e->status.setText("Recognition failed. Please try again."));
  }
  @Override protected void onPause(){save();super.onPause();}
  class InkCanvas extends View{
    ArrayList<Ink.Stroke> strokes=new ArrayList<>();Ink.Stroke.Builder active;long start;
    Paint paint=new Paint(3);Path path=new Path();float lastX,lastY;
    InkCanvas(Context c){super(c);paint.setColor(Color.rgb(35,72,160));paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(3));paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);}
    public boolean onTouchEvent(android.view.MotionEvent e){float x=e.getX(),y=e.getY();if(e.getAction()==MotionEvent.ACTION_DOWN){active=Ink.Stroke.builder();start=System.currentTimeMillis();active.addPoint(Ink.Point.create(x,y,0));path.reset();path.moveTo(x,y);lastX=x;lastY=y;invalidate();return true;}if(e.getAction()==MotionEvent.ACTION_MOVE&&active!=null){long t=System.currentTimeMillis()-start;for(int i=0;i<e.getHistorySize();i++){float hx=e.getHistoricalX(i),hy=e.getHistoricalY(i);active.addPoint(Ink.Point.create(hx,hy,t));path.lineTo(hx,hy);}active.addPoint(Ink.Point.create(x,y,t));path.lineTo(x,y);invalidate();return true;}if(e.getAction()==MotionEvent.ACTION_UP&&active!=null){strokes.add(active.build());active=null;invalidate();return true;}return true;}
    protected void onDraw(Canvas c){super.onDraw(c);paint.setColor(Color.rgb(230,190,190));paint.setStrokeWidth(1);c.drawLine(0,getHeight()-dp(22),getWidth(),getHeight()-dp(22),paint);paint.setColor(Color.rgb(35,72,160));paint.setStrokeWidth(dp(3));for(Ink.Stroke s:strokes){Path p=new Path();List<Ink.Point> pts=s.getPoints();if(pts.size()>0){p.moveTo(pts.get(0).getX(),pts.get(0).getY());for(int i=1;i<pts.size();i++)p.lineTo(pts.get(i).getX(),pts.get(i).getY());c.drawPath(p,paint);}}if(active!=null)c.drawPath(path,paint);}
    Ink toInk(){if(strokes.isEmpty())return null;Ink.Builder b=Ink.builder();for(Ink.Stroke s:strokes)b.addStroke(s);return b.build();}
    void clear(){strokes.clear();active=null;path.reset();invalidate();}
  }
}