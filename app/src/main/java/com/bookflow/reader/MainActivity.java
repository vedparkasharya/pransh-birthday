package com.bookflow.reader;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import java.io.*;

public class MainActivity extends Activity {
  private PdfRenderer renderer;
  private ParcelFileDescriptor descriptor;
  private ImageView pageView;
  private TextView title, pageLabel, empty;
  private int pageIndex=0;
  private LinearLayout root;
  private final int PICK=41;
  public void onCreate(Bundle b){super.onCreate(b); getWindow().setStatusBarColor(Color.rgb(247,245,239)); getWindow().setNavigationBarColor(Color.rgb(247,245,239)); getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR); build();}
  private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density);}
  private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;}
  private void build(){
    root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(20),dp(28),dp(20),dp(12));root.setBackgroundColor(Color.rgb(247,245,239));setContentView(root);
    TextView brand=text("BOOKFLOW",13,Color.rgb(123,105,77));brand.setLetterSpacing(.18f);root.addView(brand);
    title=text("Your reading space.",28,Color.rgb(39,39,36)); 
    root.addView(title);
    TextView sub=text("A quiet place for your PDFs.",14,Color.rgb(120,116,108));root.addView(sub);
    Button open=new Button(this);open.setText("＋   Open a PDF");open.setAllCaps(false);root.addView(open);open.setOnClickListener(v->pick());
    empty=text("Your library is empty.\nChoose a PDF to begin reading.",16,Color.GRAY);empty.setGravity(17);root.addView(empty,new LinearLayout.LayoutParams(-1,dp(160)));
    pageView=new ImageView(this);pageView.setAdjustViewBounds(true);pageView.setScaleType(ImageView.ScaleType.FIT_CENTER);LinearLayout.LayoutParams img=new LinearLayout.LayoutParams(-1,0,1);root.addView(pageView,img);
    LinearLayout controls=new LinearLayout(this);controls.setGravity(Gravity.CENTER);root.addView(controls);
    Button prev=new Button(this);prev.setText("Previous");prev.setAllCaps(false);controls.addView(prev);prev.setOnClickListener(v->showPage(pageIndex-1));
    pageLabel=text("No document",14,Color.DKGRAY);pageLabel.setPadding(dp(12),dp(12),dp(12),dp(12));controls.addView(pageLabel);
    Button next=new Button(this);next.setText("Next");next.setAllCaps(false);controls.addView(next);next.setOnClickListener(v->showPage(pageIndex+1));
  }
  private void pick(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/pdf");startActivityForResult(i,PICK);}
  protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);if(req==PICK&&res==RESULT_OK&&data!=null){try{Uri u=data.getData();getContentResolver().takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION);if(renderer!=null)renderer.close();if(descriptor!=null)descriptor.close();descriptor=getContentResolver().openFileDescriptor(u,"r");renderer=new PdfRenderer(descriptor);pageIndex=0;empty.setVisibility(View.GONE);showPage(0);}catch(Exception e){new AlertDialog.Builder(this).setMessage("Could not open this PDF.").setPositiveButton("OK",null).show();}}}
  private void showPage(int n){if(renderer==null)return;if(n<0||n>=renderer.getPageCount())return;pageIndex=n;PdfRenderer.Page p=renderer.openPage(n);int w=Math.max(1,getResources().getDisplayMetrics().widthPixels-dp(48));int h=Math.max(1,(int)(w*(float)p.getHeight()/p.getWidth()));Bitmap b=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);b.eraseColor(Color.WHITE);p.render(b,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);p.close();pageView.setImageBitmap(b);pageLabel.setText((n+1)+" / "+renderer.getPageCount());title.setText("Reading");}
  protected void onDestroy(){try{if(renderer!=null)renderer.close();if(descriptor!=null)descriptor.close();}catch(Exception ignored){}super.onDestroy();}
}
