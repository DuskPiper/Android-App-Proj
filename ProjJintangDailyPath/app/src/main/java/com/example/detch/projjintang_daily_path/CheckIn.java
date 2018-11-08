package com.example.detch.projjintang_daily_path;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class CheckIn extends AppCompatActivity {
    private TextView showCheckinLongitude;
    private TextView showCheckinLatitude;
    private ListView showCheckedInPlaces;
    private EditText addNewNameForCheckin;
    private Button addButtonToCheckin;

    private String checkinLongitude;
    private String checkinLatitude;
    private String checkinAddress;
    private String checkinName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);

        this.showCheckinLongitude = (TextView)findViewById(R.id.add_txt_longitude_show);
        this.showCheckinLatitude = (TextView)findViewById(R.id.add_txt_latitude_show);
        this.showCheckedInPlaces = (ListView)findViewById(R.id.add_list_history_show);
        this.addNewNameForCheckin = (EditText)findViewById(R.id.add_input_name);
        this.addButtonToCheckin = (Button)findViewById(R.id.add_btn_add_location);

        // ToDo add intent receivers
    }
}
