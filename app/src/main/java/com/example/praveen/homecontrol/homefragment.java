package com.example.praveen.homecontrol;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class homefragment extends Fragment {
    private Button button;
    private EditText edit;
    private String IP = "192.168.137.146";
    private int PORT1 = 3333;
    private int PORT2=8888;
    private String result;
    private String mssg = "";
    private TextView text;
    private Socket socket1,socket2;
    Thread clientThread, sentThread,appThread;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ((MainActivity) getActivity()).setActionBarTitle("Home Control");

        View view = inflater.inflate(R.layout.homefragment, container, false);


        button = view.findViewById(R.id.button);   // the send button
        edit = view.findViewById(R.id.editText);   // the text view
        text = view.findViewById(R.id.textview);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sentThread = new Thread(new sentMessage());
                sentThread.start();
            }
        });

        clientThread = new Thread(new ClientThread());
        clientThread.start();



        return  view;
    }


    class sentMessage implements Runnable {
        @Override
        public void run() {
            try {
                socket1= new Socket(IP,PORT1);
                DataOutputStream os = new DataOutputStream(socket1.getOutputStream());
                result = edit.getText().toString();
                os.writeUTF(result);
                os.flush();
                os.close();
                socket1.close();
            } catch (IOException ignored) {
            }
        }
    }


    public class ClientThread implements Runnable {
        public void run() {
            try {
                do {
                    //  text.setText("Connecting to socket...");
                    // text.setText("Input stream receiving...");
                    socket2 = new Socket(IP,PORT2);
                    DataInputStream in = new DataInputStream(socket2.getInputStream());
                    //text.setText("Input stream received");
                    mssg = in.readUTF();
                    Log.d("message",mssg);


                    getActivity().runOnUiThread(new Runnable() {
                                      @Override
                                      public void run() {

                                          text.setText(mssg);
                                      }
                                  });

                    in.close();
                    socket2.close();


                } while (!mssg.equals("bye"));
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }






}
