package br.com.erombox.nativeapp;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.content.*;
import android.text.InputType;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.util.*;
import org.json.*;

public class MainActivity extends Activity {
  private LinearLayout root, content, tabs;
  private EditText titleField, bodyField, searchField;
  private final int bg=Color.rgb(13,14,18), panel=Color.rgb(27,29,36), white=Color.WHITE, muted=Color.rgb(165,170,183), accent=Color.rgb(184,148,255);
  private final ArrayList<Note> notes=new ArrayList<>();
  private String filter="";
  private int nextId=1;
  private static class Note { int id; String title,body,updated; Note(int i,String t,String b,String u){id=i;title=t;body=b;updated=u;} }
  @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(bg);getWindow().setNavigationBarColor(bg);load();showNotes();}
  private int dp(int x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
  private void shell(String title){root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(18),dp(18),dp(18),dp(8));root.setBackgroundColor(bg);setContentView(root);TextView h=text(title,27,true,white);root.addView(h);ScrollView sc=new ScrollView(this);content=new LinearLayout(this);content.setOrientation(1);sc.addView(content);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));}
  private TextView text(String s,int size,boolean bold,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setPadding(0,dp(6),0,dp(6));return t;}
  private Button button(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextColor(bg);b.setTextSize(15);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(accent));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(48));p.bottomMargin=dp(9);b.setLayoutParams(p);return b;}
  private EditText field(String hint,boolean multiline){EditText e=new EditText(this);e.setHint(hint);e.setTextColor(white);e.setHintTextColor(muted);e.setTextSize(16);e.setPadding(dp(13),dp(12),dp(13),dp(12));e.setBackgroundColor(panel);e.setInputType(multiline? (InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES):(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES));e.setGravity(multiline?Gravity.TOP:Gravity.CENTER_VERTICAL);e.setSingleLine(!multiline);if(multiline)e.setMinLines(8);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(10);e.setLayoutParams(p);return e;}
  private void showNotes(){shell("EROM BOX");content.addView(text("Suas notas, listas e ideias — tudo salvo neste telefone.",14,false,muted));searchField=field("Pesquisar notas…",false);searchField.setSingleLine(true);searchField.setText(filter);content.addView(searchField);searchField.setOnEditorActionListener((v,a,event)->{filter=searchField.getText().toString().trim().toLowerCase(Locale.ROOT);showNotes();return true;});Button add=button("+ Nova nota");content.addView(add);add.setOnClickListener(v->editNote(null));View gap=new View(this);content.addView(gap,new LinearLayout.LayoutParams(1,dp(8)));ArrayList<Note> shown=new ArrayList<>();for(Note n:notes)if((n.title+" "+n.body).toLowerCase(Locale.ROOT).contains(filter))shown.add(n);shown.sort((a,b)->Integer.compare(b.id,a.id));if(shown.isEmpty())content.addView(text(filter.isEmpty()?"Ainda não há notas. Toque em “Nova nota” para começar.":"Nenhuma nota encontrada.",15,false,muted));for(Note n:shown){LinearLayout card=new LinearLayout(this);card.setOrientation(1);card.setPadding(dp(14),dp(12),dp(14),dp(8));card.setBackgroundColor(panel);LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.bottomMargin=dp(10);content.addView(card,cp);card.addView(text(n.title.isEmpty()?"Sem título":n.title,18,true,white));String preview=n.body.replace("\n"," ");if(preview.length()>140)preview=preview.substring(0,140)+"…";card.addView(text(preview,14,false,muted));card.addView(text(n.updated,11,false,muted));Button open=button("Abrir / editar");card.addView(open);open.setOnClickListener(v->editNote(n));}
    Button info=button("Sobre / armazenamento");content.addView(info);info.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("EROM BOX — Notas").setMessage("Aplicativo nativo e offline. As notas ficam salvas somente neste telefone; não há conta nem servidor conectado. Faça cópias de segurança se forem importantes.").setPositiveButton("OK",null).show());
  }
  private void editNote(Note n){shell(n==null?"Nova nota":"Editar nota");content.addView(text("Título",13,true,muted));titleField=field("Título da nota",false);content.addView(titleField);content.addView(text("Texto",13,true,muted));bodyField=field("Escreva aqui…",true);content.addView(bodyField);if(n!=null){titleField.setText(n.title);bodyField.setText(n.body);}Button save=button("Salvar nota");content.addView(save);save.setOnClickListener(v->{String t=titleField.getText().toString().trim(),b=bodyField.getText().toString();if(t.isEmpty()&&b.trim().isEmpty()){toast("Escreva algo antes de salvar.");return;}String now=java.text.DateFormat.getDateTimeInstance().format(new Date());if(n==null){notes.add(new Note(nextId++,t,b,now));}else{n.title=t;n.body=b;n.updated=now;}persist();hideKeyboard();showNotes();toast("Nota salva neste telefone.");});Button cancel=button("Voltar sem salvar");content.addView(cancel);cancel.setOnClickListener(v->showNotes());if(n!=null){Button del=button("Excluir nota");content.addView(del);del.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Excluir nota?").setMessage("Esta ação não pode ser desfeita.").setNegativeButton("Cancelar",null).setPositiveButton("Excluir",(d,w)->{notes.remove(n);persist();showNotes();}).show());}}
  private void load(){String raw=getPreferences(0).getString("notes","[]");try{JSONArray a=new JSONArray(raw);for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);Note n=new Note(o.optInt("id",nextId++),o.optString("title",""),o.optString("body",""),o.optString("updated",""));notes.add(n);nextId=Math.max(nextId,n.id+1);}}catch(Exception ignored){}}
  private void persist(){try{JSONArray a=new JSONArray();for(Note n:notes)a.put(new JSONObject().put("id",n.id).put("title",n.title).put("body",n.body).put("updated",n.updated));getPreferences(0).edit().putString("notes",a.toString()).apply();}catch(Exception e){toast("Não foi possível salvar a nota.");}}
  private void hideKeyboard(){try{((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(root.getWindowToken(),0);}catch(Exception ignored){}}
  private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}
