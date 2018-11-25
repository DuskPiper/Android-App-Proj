package com.example.duskpiper.projjieyang_wifi_chatroom;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {
    private EditText inputMessage;
    private EditText inputIP;
    private EditText inputPort;
    private Button sendButton;
    private Button setAddrButton;
    private TextView debugWindow;
    private ArrayList<String> debugInfo;
    private int debugMaxLength = 12;
    private StringBuffer output;
    private String message;
    private String host;
    private int port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        inputMessage = (EditText)findViewById(R.id.message_input);
        sendButton = (Button)findViewById(R.id.send_button);
        debugWindow = (TextView)findViewById(R.id.debug_window);
        inputIP = (EditText)findViewById(R.id.ip_edit);
        inputPort = (EditText)findViewById(R.id.port_edit);
        setAddrButton = (Button)findViewById(R.id.set_addr_button);

        host = "192.168.0.1";
        port = 999;
        checkWiFiConnection(this);
        output = new StringBuffer();
        inputIP.setText(host);
        inputPort.setText(Integer.toString(port));
        updateDebugWindow("> System initialized.");

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // COLLECT AND SEND MESSAGE
                message = inputMessage.getText().toString();
                if (message.length() < 1) {
                    updateDebugWindow("> Aborted: message too short.");
                    Toast.makeText(ChatActivity.this, "Message should not be empty!", Toast.LENGTH_LONG).show();
                } else if (!checkWiFiConnection(ChatActivity.this)) {
                    // NO WIFI
                } else {
                    inputMessage.setText("");
                    // SEND THROUGH SOCKET
                    send(message);
                }
            }
        });

        setAddrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                host = inputIP.getText().toString();
                port = Integer.valueOf(inputPort.getText().toString());
                updateDebugWindow("> Set new addr = " + host + ":" + Integer.toString(port));
            }
        });
    }

    private void updateDebugWindow(String info) {
        if (debugInfo == null) {
            debugInfo = new ArrayList<String>();
        } else {
            while (debugInfo.size() >= debugMaxLength) {
                debugInfo.remove(0);
            }
        }
        debugInfo.add(info);

        output = new StringBuffer();
        for (String infoLine : debugInfo) {
            output.append(infoLine + "\n");
        }
        debugWindow.setText(output);
    }

    private boolean checkWiFiConnection(Context context){
        ConnectivityManager connectManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(networkInfo.isConnected()){
            updateDebugWindow("> WiFi check: Connected.");
            return true;
        }
        else{
            updateDebugWindow("> WiFi failure: NOT connected to WiFi.");
            Toast.makeText(ChatActivity.this, "Please connect to WiFi!", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public void send(final String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket socket;
                try {
                    // CONNECT
                    updateDebugWindow("> Wrapping message...");
                    socket = new Socket(host, port);
                    updateDebugWindow("> Connection successful.");
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    sendTextMsg(out, message);
                    out.close();
                    socket.close();
                    updateDebugWindow("> Message sent");
                } catch (IOException e) {
                    e.printStackTrace();
                    //updateDebugWindow("> Failed to connect");
                }
            }
        }).start();
    }

    public void sendTextMsg(DataOutputStream out, String msg) throws IOException {
        updateDebugWindow("> Sending message...");
        byte[] bytes = msg.getBytes();
        long len = bytes.length;
        out.writeLong(len);
        out.write(bytes);
    }
}