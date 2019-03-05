package com.example.praveen.homecontrol;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.support.annotation.NonNull;
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
import com.google.firebase.database.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static android.content.Context.MODE_PRIVATE;
import static android.support.constraint.Constraints.TAG;


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
    String room = "room01";
    LinearLayout linearlayout;
    FirebaseDatabase database;

     int temperature ;
     int door_cond ;
     Handler handler = null;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)  {
        ((MainActivity) getActivity()).setActionBarTitle("Room1");
        Ids = new int[8];
        data = new int[8];
        ids = new HashSet<String>();
        obj = new JSONObject();


        for (int f = 0; f < 7; f++) {
            data[f] = 3;
            try {
                obj.put(String.valueOf(f + 1), 3);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
//        ids.add(String.valueOf(linearlayout.getId())+10);

        view = inflater.inflate(R.layout.room1fragment, container, false);
        relativeLayout = ((RelativeLayout) view.findViewById(R.id.room1frg));
        linearlayout = ((LinearLayout) view.findViewById(R.id.linearlayout));
        preferences = getContext().getSharedPreferences("Light_Info", MODE_PRIVATE);
        ids = preferences.getStringSet("lights_ids", ids);
        preferences2 = getContext().getSharedPreferences("data_info", MODE_PRIVATE);
         database = FirebaseDatabase.getInstance();



       density = getContext().getResources().getDisplayMetrics().density;
        add = view.findViewById(R.id.addview);
        temp = view.findViewById(R.id.temp);
        door = view.findViewById(R.id.door);
        j = linearlayout.getId();
        i = linearlayout.getId();
        textView = new TextView[17];
        if (ids.size() > 0) {
            try {
                showTextview(k);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        add.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if (ids.size() < 6) {
                    try {
                        addTextview();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getContext(), "You can add max 6 lights only",Toast.LENGTH_SHORT).show();
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
    private void showTextview(int t) throws JSONException {

        int[] id_array = makearray();


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

                        try {
                            showTextview(k);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    data[s-1] =3;
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
    public void addTextview() throws JSONException {

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
                data[k] =3;

                if (ids.size() > 0) {
                    i = linearlayout.getId();

                    try {
                        showTextview(k);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
    private int findValue() throws JSONException {

      int[] id_array = makearray();
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

            for(int i=1;i<8;i++) {
                DatabaseReference myRef = database.getReference(room + "/L" + i);
                myRef.setValue(data[i-1]);


            }


//                socket1.close();
        }
    }


    public class ClientThread implements Runnable {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @SuppressLint("SetTextI18n")
        public void run() {
            try {
                do {





//                    socket2 = new Socket(IP, PORT2);
//                    DataInputStream in = new DataInputStream(socket2.getInputStream());
//                     mssg = in.readUTF();
//                    Log.d("mssg",mssg);


//                    JSONObject obj = new JSONObject(mssg);

                    int[] ids = new int[7];

                      ids = makearray();



                         DatabaseReference ref1 = database.getReference(room + "/L1");

                        ref1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                data[0] = dataSnapshot.getValue(int.class);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }});

//                         Log.d("val1", String.valueOf(data[0]));

                        boolean found1 = Arrays.stream(ids).anyMatch(x -> x == 1 + j);

                        if (found1) {

                            ChangeBackground changebackground = new ChangeBackground();
                            changebackground.onProgressUpdate(0,data[0]);

                        }


                    DatabaseReference ref2 = database.getReference(room + "/L2");

                    ref2.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            data[1] = dataSnapshot.getValue(int.class);


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }});

//                    Log.d("val2", String.valueOf(data[1]));

                    boolean found2 = Arrays.stream(ids).anyMatch(x -> x == 2 + j);

                    if (found2) {

                        ChangeBackground changebackground = new ChangeBackground();
                        changebackground.onProgressUpdate(1,data[1]);

                    }

                    DatabaseReference ref3 = database.getReference(room + "/L3");

                    ref3.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            data[2] = dataSnapshot.getValue(int.class);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }});

//                    Log.d("val3", String.valueOf(data[2]));

                    boolean found3 = Arrays.stream(ids).anyMatch(x -> x == 3 + j);

                    if (found3) {

                        ChangeBackground changebackground = new ChangeBackground();
                        changebackground.onProgressUpdate(2,data[2]);

                    }

                    DatabaseReference ref4 = database.getReference(room + "/L4");

                    ref4.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            data[3] = dataSnapshot.getValue(int.class);


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }});

//                    Log.d("val4", String.valueOf(data[3]));

                    boolean found4 = Arrays.stream(ids).anyMatch(x -> x == 4 + j);

                    if (found4) {

                        ChangeBackground changebackground = new ChangeBackground();
                        changebackground.onProgressUpdate(3,data[3]);

                    }

                    DatabaseReference ref5 = database.getReference(room + "/L5");

                    ref5.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            data[4] = dataSnapshot.getValue(int.class);


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }});

//                    Log.d("val5", String.valueOf(data[4]));

                    boolean found5 = Arrays.stream(ids).anyMatch(x -> x == 5 + j);

                    if (found5) {

                        ChangeBackground changebackground = new ChangeBackground();
                        changebackground.onProgressUpdate(4,data[4]);

                    }


                    DatabaseReference ref6 = database.getReference(room + "/L6");

                    ref6.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            data[5] = dataSnapshot.getValue(int.class);


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }});

//                    Log.d("val6", String.valueOf(data[5]));

                    boolean found6 = Arrays.stream(ids).anyMatch(x -> x == 6 + j);

                    if (found6) {

                        ChangeBackground changebackground = new ChangeBackground();
                        changebackground.onProgressUpdate(5,data[5]);

                    }



                    DatabaseReference ref7 = database.getReference(room + "/L7");

                    ref1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            data[6] = dataSnapshot.getValue(int.class);


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }});

//                    Log.d("val7", String.valueOf(data[6]));

                    boolean found7 = Arrays.stream(ids).anyMatch(x -> x == 7 + j);

                    if (found7) {

                        ChangeBackground changebackground = new ChangeBackground();
                        changebackground.onProgressUpdate(6,data[6]);

                    }




                    DatabaseReference temper = database.getReference(room+"/temp");

                    temper.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                             temperature = dataSnapshot.getValue(int.class);


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }});

                        DatabaseReference door_con = database.getReference(room+"/door");

                    door_con.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange (DataSnapshot dataSnapshot){
                                 door_cond = dataSnapshot.getValue(int.class);


                            }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                     setTemperature(temperature);
                     setDoor(door_cond);









                    editor2 = preferences2.edit();
                    editor2.clear();
                    editor2.commit();
                    editor2.putString("datas", String.valueOf(obj));
                    editor2.commit();
//
//                    in.close();
//                    socket2.close();

                Thread.sleep(200);
                } while (!mssg.equals("bye"));
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    public void setTemperature(int temperature){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                temp.setText("Temp:- " + temperature + "°C");
            }
        });
    }

    public void setDoor(int door_cond){


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

    }


    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.N)


    private int[] makearray() throws JSONException {
        ids = preferences.getStringSet("lights_ids", ids);
        int[] id_array =new int[8];

        if(ids.size()==0){
            return id_array;
        }
        JSONArray jsonArray = new JSONArray(ids);
        String[] str = new String[jsonArray.length()];

        for (int i = 0; i < jsonArray.length(); i++) {
            str[i] = jsonArray.getString(i);
        }

         id_array = Arrays.stream(str).mapToInt(Integer::parseInt).toArray();

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

            Resources res = getResources();


            if (values[1] == 1) {
                getActivity().runOnUiThread(new Runnable() { @Override public void run() {
                    Drawable design = res.getDrawable(R.drawable.click_textview_design);
                    textView[values[0] + 1].setBackground(design);


//                Log.d(String.valueOf(values[0] + 1), String.valueOf(textView[values[0] + 1].getId()));
            }});}
            else if(values[1]==0) {
              getActivity().runOnUiThread(new Runnable() { @Override public void run() {
                  Drawable design = res.getDrawable(R.drawable.click_textview_design_red);
                  textView[values[0] + 1].setBackground(design);
              }});
//                Log.d(String.valueOf(values[0] + 1), String.valueOf(textView[values[0] + 1].getId()));

            }
        }
    }
}




