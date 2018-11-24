package com.example.duskpiper.projjieyang_wifi_chatroom;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {
    private EditText inputMessage;
    private Button sendButton;
    private TextView debugWindow;
    private ArrayList<String> debugInfo;
    private int debugMaxLength = 12;
    private StringBuffer output;
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        inputMessage = (EditText)findViewById(R.id.message_input);
        sendButton = (Button)findViewById(R.id.send_button);
        debugWindow = (TextView)findViewById(R.id.debug_window);

        debugInfo = new ArrayList<String>();
        output = new StringBuffer();
        updateDebugWindow("> System initialized.");

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // COLLECT AND SEND MESSAGE
                message = inputMessage.getText().toString();
                if (message.length() < 1) {
                    updateDebugWindow("> Aborted: message too short.");
                    Toast.makeText(ChatActivity.this, "Message should not be empty!", Toast.LENGTH_LONG).show();
                } else {
                    inputMessage.setText("");
                    // SEND THROUGH SOCKET
                }
            }
        });
    }

    private void updateDebugWindow(String info) {
        while (debugInfo.size() >= debugMaxLength) {
            debugInfo.remove(0);
        }
        debugInfo.add(info);

        output = new StringBuffer();
        for (String infoLine : debugInfo) {
            output.append(info + "\n");
        }
        debugWindow.setText(output);
    }
}
