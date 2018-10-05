package com.example.detch.projectjinan;

import android.content.Context;
import android.content.DialogInterface;
//import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.CheckBox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.widget.SimpleCursorAdapter;
import android.widget.TwoLineListItem;
//import android.R;


public class MainActivity extends AppCompatActivity {
    Button adder;
    TextView title;
    EditText editTitle;
    EditText editDes;
    ListView listView;
    List<String> listStrings;
    ArrayAdapter<String> arrayAdapter;
    SimpleAdapter simpleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adder = (Button) findViewById(R.id.addButton);
        title = (TextView) findViewById(R.id.appTitle);
        editTitle = (EditText) findViewById(R.id.newTaskTitle);
        editDes = (EditText) findViewById(R.id.newTaskContent);
        listView = (ListView) findViewById(R.id.listView);
        final List<Map<String, String>> listmaps = new ArrayList<Map<String, String>>();
        //simpleAdapter = new SimpleAdapter(MainActivity.this, listmaps, android.R.layout.simple_expandable_list_item_2, new String[]{"title", "description"}, new int[]{android.R.id.text1, android.R.id.text2});
        final MyAdapter myAdapter =new MyAdapter(MainActivity.this,listmaps,R.id.deleter,R.layout.listview_item, new String[]{"title", "description"}, new int[]{R.id.title, R.id.description});
        listView.setAdapter(myAdapter);
        myAdapter.notifyDataSetChanged();

        adder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String insertTitle = editTitle.getText().toString();
                String insertContent = editDes.getText().toString();
                Map<String, String> map = new HashMap<String, String>() {
                };
                map.put("title", insertTitle);
                map.put("description", insertContent);
                listmaps.add(map);
                myAdapter.notifyDataSetChanged();
                editTitle.setText("");
                editDes.setText("");
                /*
                //write to txt
                try{
                    File file = new File(Environment.getExternalStorageDirectory(),"ToDoListDebug.txt");
                    if(file.exists()){file.delete();}
                    file.createNewFile();
                    FileOutputStream outStream = new FileOutputStream(file);
                    OutputStreamWriter writer = new OutputStreamWriter(outStream,"gb2312");
                    for(int i=0;i<listmaps.size();i++){
                        map=listmaps.get(i);
                        writer.write(map.get("title"));
                        writer.write("\n");
                        writer.write(map.get("description"));
                        writer.write("\n\n\n");
                        writer.flush();
                    }
                    writer.close();
                }
                catch (Exception e){e.printStackTrace();}
                */
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(MainActivity.this).setTitle("Alert").setMessage("Delete one todo?").setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listmaps.remove(position);
                        myAdapter.notifyDataSetChanged();
                    }
                }).setNegativeButton("Cancel", null).show();
                return true;
            }
        });
    }

    protected void myRefreshView() {

    }

    public class MyAdapter extends SimpleAdapter {
        List<Map<String, String>> listmaps;
        private int myCheckBoxId;

        public MyAdapter(Context context, List<Map<String, String>> list,int checkBoxId, int resource, String[] from, int[] to) {
            super(context, list, resource, from, to);
            this.listmaps = list;
            this.myCheckBoxId=checkBoxId;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            CheckBox deleter = (CheckBox) view.findViewById(R.id.deleter);
            deleter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listmaps.remove(position);
                    notifyDataSetChanged();
                }
            });
            return view;
        }

        @Override
        public int getCount() {
            return listmaps.size();
        }
    }

}




