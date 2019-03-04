package com.example.praveen.homecontrol;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.*;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

public class room1fragment extends Fragment {
    TextView[] textView;
    TextView add, temp, door;
    SharedPreferences preferences, preferences2;
    SharedPreferences.Editor editor, editor2;
    private String IP = "192.168.137.177";
    private int PORT1 = 3333;
    private int PORT2 = 8888;
    private String mssg = "";
    private Socket socket1, socket2;
    int i = 1, k = 1, j,i1;
    int[] Ids;
    float density;
    Set<String> ids;
    View view;
    Thread clientThread, sentThread;
    RelativeLayout room1, relativeLayout;
    JSONObject obj;
    int[] data;
    LinearLayout linearlayout;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setActionBarTitle("Room1");
        Ids = new int[7];
        data = new int[7];
        obj = new JSONObject();
        ids = new HashSet<String>();

        for (int f = 0; f < 7; f++) {
            data[f] = 3;
            try {
                obj.put(String.valueOf(f + 1), 3);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        view = inflater.inflate(R.layout.room1fragment, container, false);
        relativeLayout = ((RelativeLayout) view.findViewById(R.id.room1frg));
        linearlayout = ((LinearLayout) view.findViewById(R.id.linearlayout));
        preferences = getContext().getSharedPreferences("Light_Info", MODE_PRIVATE);
        ids = preferences.getStringSet("lights_ids", ids);
        preferences2 = getContext().getSharedPreferences("data_info", MODE_PRIVATE);


        density = getContext().getResources().getDisplayMetrics().density;

        add = view.findViewById(R.id.addview);
        temp = view.findViewById(R.id.temp);
        door = view.findViewById(R.id.door);
        j = linearlayout.getId();
        i = linearlayout.getId();
        textView = new TextView[17];
        if (ids.size() > 0) {
            showTextview(k);
        }

        add.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if (ids.size() < 7) {
                    addTextview();
                } else {
                    Toast.makeText(getContext(), "You can add max 7 lights only", Toast.LENGTH_LONG).show();
                }
            }
        });


        clientThread = new Thread(new ClientThread());
        clientThread.start();

        room1 = view.findViewById(R.id.room1frg);

        return view;
    }

    @TargetApi(Build.VERSION_CODES.N)
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void showTextview(int t) {

        String strg = String.valueOf(ids);
        strg = strg.replace(" ", "");
        strg = strg.replace("[", "");
        strg = strg.replace("]", "");

//        Log.d("new", String.valueOf(ids));

        String[] str = new String[7];
        str = strg.split(",");
        int[] id_array = Arrays.stream(str).mapToInt(Integer::parseInt).toArray();
        Arrays.sort(id_array);

        for (int p = 0; p < id_array.length; p++) {
//            Log.d("array2", String.valueOf(id_array[p]));
            int s = id_array[p] - j;
            RelativeLayout relativeLayout = view.findViewById(R.id.room1frg);
            textView[s] = new TextView(getActivity());
            textView[s].setId(s + j);//Set id to remove in the future.
            textView[s].setLayoutParams(new ViewGroup.LayoutParams(ActionMenuView.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.FILL_PARENT));
            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            RelativeLayout.LayoutParams rlp1 = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);

            textView[s].setHeight((int) (55 * density));
            textView[s].setCompoundDrawablePadding(20);
            rlp.addRule(RelativeLayout.BELOW, i);
            textView[s].setText("Light" + s);
            textView[s].setTextSize(30);
            Resources res = getResources();
            textView[s].setPadding((int) (25 * density), 0, 0, 0);
            Drawable design = res.getDrawable(R.drawable.textview_design);
            textView[s].setBackground(design);
            textView[s].setGravity(Gravity.CENTER_VERTICAL);
            textView[s].setLayoutParams(rlp);

//            Log.d("View", "Start");


            textView[s].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Log.d("button:-", String.valueOf(v.getId()));
                    if (data[s - 1] == 0) {
                        data[s - 1] = 1;
                    } else {
                        data[s - 1] = 0;
                    }

                    try {
                        obj.put(String.valueOf(s), data[s - 1]);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    sentThread = new Thread(new setLight());
                    sentThread.start();


                }
            });

            textView[s].setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ids.remove(String.valueOf(v.getId()));
                    editor = preferences.edit();
                    editor.clear();
                    editor.commit();
                    editor.putInt("No_of_light", k - 1);
                    editor.putStringSet("lights_ids", ids);
                    if (ids.size() > 0) {
                        i = linearlayout.getId();

                        showTextview(k);
                    }
                    ids = preferences.getStringSet("lights_ids", ids);
                    editor.commit();


                    Log.d("longpress", String.valueOf(ids));
                    return true;

                }

            });


            add.setCompoundDrawablePadding(20);
            i = s + j;
            try {
                relativeLayout.addView(textView[s], rlp);
                add.setHeight((int) (55 * density));

                rlp1.addRule(RelativeLayout.BELOW, i);
                add.setLayoutParams(rlp1);
                relativeLayout.addView(add, rlp1);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint({"ResourceType", "NewApi"})
    public void addTextview() {

        RelativeLayout relativeLayout = view.findViewById(R.id.room1frg);
        k = 1;
        if (ids.size() > 0) {
            k = findValue();
        }
//        Log.d("k", String.valueOf(k));

        textView[k] = new TextView(getActivity());
        textView[k].setId(j + k);//Set id to remove in the future.
        textView[k].setLayoutParams(new ViewGroup.LayoutParams(ActionMenuView.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT));
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams rlp1 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        textView[k].setCompoundDrawablePadding(20);

        rlp.addRule(RelativeLayout.BELOW, i);
        textView[k].setHeight((int) (55 * density));

        textView[k].setText("Light" + k);
        textView[k].setTextSize(30);
        textView[k].setPadding((int) (25 * density), 0, 0, 0);
        Resources res = getResources();
        Drawable design = res.getDrawable(R.drawable.textview_design);
        textView[k].setBackground(design);
        textView[k].setGravity(Gravity.CENTER_VERTICAL);
        textView[k].setLayoutParams(rlp);

//        Log.d("View", String.valueOf(ids.size()));

        textView[k].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d("button:-", String.valueOf(v.getId()));
                if (data[k - 1] == 0) {
                    data[k - 1] = 1;
                } else {
                    data[k - 1] = 0;
                }

                try {
                    obj.put(String.valueOf(k), data[k - 1]);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                sentThread = new Thread(new setLight());
                sentThread.start();

            }
        });
        textView[k].setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                ids.remove(String.valueOf(v.getId()));
                editor = preferences.edit();
                editor.clear();
                editor.commit();
                editor.putInt("No_of_light", k - 1);
                editor.putStringSet("lights_ids", ids);
                editor.commit();

                if (ids.size() > 0) {
                    i = linearlayout.getId();

                    showTextview(k);
                }
                return true;
            }

        });

        ids.add(String.valueOf(k + j));
        editor = preferences.edit();
        editor.putInt("No_of_light", k);
        editor.putStringSet("lights_ids", ids);
        editor.commit();
        add.setCompoundDrawablePadding(20);

        i = k + j;

        try {
            relativeLayout.addView(textView[k], rlp);
            add.setHeight((int) (55 * density));

            rlp1.addRule(RelativeLayout.BELOW, i);
            add.setLayoutParams(rlp1);
            relativeLayout.addView(add, rlp1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private int findValue() {
        ids = preferences.getStringSet("lights_ids", ids);
        String strg = String.valueOf(ids);
        strg = strg.replace(" ", "");
        strg = strg.replace("[", "");
        strg = strg.replace("]", "");

        String[] str = new String[7];
        str = strg.split(",");
        int[] id_array = Arrays.stream(str).mapToInt(Integer::parseInt).toArray();
        Arrays.sort(id_array);
        int i = 0;
        for (i = 0; i < id_array.length; i++) {
//            Log.d("i0", String.valueOf(i));

            if (id_array[i] != j + i + 1) {
//                Log.d("i", String.valueOf(i));
                return i + 1;
            } else {
                continue;
            }
        }
        return 1 + i;
    }


    class setLight implements Runnable {
        @Override
        public void run() {

//                socket1 = new Socket(IP, PORT1);
//                DataOutputStream os = new DataOutputStream(socket1.getOutputStream());
//                os.writeUTF(String.valueOf(obj)+"\n");
//                os.flush();
//                os.close();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference();
            myRef.setValue("Hello, World!");
//                socket1.close();
        }
    }


    public class ClientThread implements Runnable {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @SuppressLint("SetTextI18n")
        public void run() {
            try {
                do {
                    mssg = preferences2.getString("datas", mssg);


//                    socket2 = new Socket(IP, PORT2);
//                    DataInputStream in = new DataInputStream(socket2.getInputStream());
//                     mssg = in.readUTF();
//                    Log.d("mssg",mssg);


                    JSONObject obj = new JSONObject(mssg);
                    final int temperature = Integer.parseInt(obj.getString("temp"));
                    final int door_cond = Integer.parseInt(obj.getString("door"));
                    int[] ids = new int[7];
                    ids = makearray();
                    int[] value = new int[7];
                    for (i1 = 0; i1 < 7; i1++) {

                        value[i1] = Integer.parseInt(obj.getString(String.valueOf(i1 + 1)));
                        boolean found = Arrays.stream(ids).anyMatch(x -> x == i1 + 1 + j);

                        if (found) {
                            ChangeBackground changebackground = new ChangeBackground();
                            changebackground.onProgressUpdate(i1,value[i1]);

                        } else {
                            continue;
                        }
                    }



                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            temp.setText("Temp:- " + temperature + "°C");
                        }
                    });


                    if (door_cond == 1) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                door.setText("Door Open");

                            }
                        });
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                door.setText("Door Close");

                            }
                        });

                    }
                    editor2 = preferences2.edit();
                    editor2.clear();
                    editor2.commit();
                    editor2.putString("datas", mssg);
                    editor2.commit();
//
//                    in.close();
//                    socket2.close();


                } while (!mssg.equals("bye"));
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }



    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.N)
    private int[] makearray() {
        ids = preferences.getStringSet("lights_ids", ids);
        String strg = String.valueOf(ids);
        strg = strg.replace(" ", "");
        strg = strg.replace("[", "");
        strg = strg.replace("]", "");
        String[] str = new String[7];
        str = strg.split(",");
        int[] id_array = Arrays.stream(str).mapToInt(Integer::parseInt).toArray();
        Arrays.sort(id_array);
        return id_array;
    }


    public class ChangeBackground extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            for (int k = 1; k <= 100; k++) {

                try {
                    Thread.sleep(1000);
                    publishProgress(k);
                } catch (InterruptedException e) {
                }
            }
            return null;
        }

        protected void onProgressUpdate(Integer... values) {
            if (values[1] == 1) {
                getActivity().runOnUiThread(new Runnable() { @Override public void run() {
                    textView[values[0] + 1].setBackgroundColor(Color.GREEN);
                Log.d(String.valueOf(values[0] + 1), String.valueOf(textView[values[0] + 1].getId()));
            }});}
            else {
              getActivity().runOnUiThread(new Runnable() { @Override public void run() {
                  textView[values[0] + 1].setBackgroundColor(Color.RED);

              }});
                Log.d(String.valueOf(values[0] + 1), String.valueOf(textView[values[0] + 1].getId()));

            }
        }
    }
}



