package com.example.praveen.homecontrol;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class room2fragment extends Fragment {
    TextView light,fan;
    TextView light_on,light_off,fan_on,fan_off;

    private String IP = "192.168.137.123";
    private int PORT1 = 3333;
    private int PORT2=8888;
    private String result;
    private String mssg = "";
    private TextView text;
    private Socket socket1,socket2;
    int fn=0;
    int lght=0;
    Thread clientThread, sentThread,appThread;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setActionBarTitle("Room2");

        View view =inflater.inflate(R.layout.room2fragment, container, false);

        light = view.findViewById(R.id.light_room2);
        fan =   view.findViewById(R.id.fan_room2);
        light_on = view.findViewById(R.id.r2light_on);
        light_off = view.findViewById(R.id.r2light_off);
        fan_on = view.findViewById(R.id.r2fan_on);
        fan_off = view.findViewById(R.id.r2fan_off);

        light.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sentThread = new Thread(new setLight());
                sentThread.start();
            }
        });


        fan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sentThread = new Thread(new setLight());
                sentThread.start();
            }
        });
        clientThread = new Thread(new ClientThread());
        clientThread.start();


        return view;
    }




    class setLight implements Runnable {
        @Override
        public void run() {
            try {
                socket1= new Socket(IP,PORT1);
                DataOutputStream os = new DataOutputStream(socket1.getOutputStream());
                JSONObject obj = new JSONObject();

                obj.put("light",new Integer(lght));
                obj.put("fan",new Integer(fn));
                os.writeUTF(String.valueOf(obj));
                os.flush();
                os.close();
                socket1.close();
            } catch (IOException ignored) {
            } catch (JSONException e) {
                e.printStackTrace();
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



                    JSONObject obj = new JSONObject(mssg);
                    String light = obj.getString("light");
                    String  fan = obj.getString("fan");

                    Log.d("light",light );
                    Log.d("fan",fan);


                    lght= Integer.parseInt(light);
                    fn= Integer.parseInt(fan);


                    if (lght ==1){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                light_on.setVisibility(View.VISIBLE);
                                light_off.setVisibility(View.INVISIBLE);
                            }
                        });

                    }
                    else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                light_off.setVisibility(View.VISIBLE);
                                light_on.setVisibility(View.INVISIBLE);
                            }

                        });
                    }

                    if(fn==1){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                fan_on.setVisibility(View.VISIBLE);
                                fan_off.setVisibility(View.INVISIBLE);
                            }

                        });
                    }
                    else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fan_off.setVisibility(View.VISIBLE);
                                fan_on.setVisibility(View.INVISIBLE);
                            }

                        });
                    }


                    in.close();
                    socket2.close();


                } while (!mssg.equals("bye"));
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void setlight(){


    }

    public void setfan(){


    }

}
