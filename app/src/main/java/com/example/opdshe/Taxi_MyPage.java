package com.example.opdshe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Taxi_MyPage extends Taxi  {
    private DatabaseReference mPostReference;
    static String MYPAGE_NICKNAME;
    static String MYPAGE_ID;
    static String MYPAGE_IMAGE;
    static String MYPAGE_EMAIL;
    CircularImageView image_profile;
    String REMOVE_LIST;

    
    TextView txt_profile;
    TextView txt_email;
    ListAdapter listAdapter;
    //static ArrayAdapter<String> listAdapter;
    static ArrayList<String> listIndex =  new ArrayList<String>();
    static ArrayList<String> listData = new ArrayList<String>();
    Toolbar toolbar;
    Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_mypage);
        getListDatabase();
        getList();
        toolbar =findViewById(R.id.toolbar);
        toolbar.setTitle("My List");
        toolbar.setTitleTextColor(Color.WHITE);
        txt_profile=findViewById(R.id.txt_name);
        txt_profile.setText(MYPAGE_NICKNAME);
        txt_email=findViewById(R.id.txt_email);
        txt_email.setText(MYPAGE_EMAIL);
        image_profile=findViewById(R.id.img_profile);



        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try{
                    final CircularImageView iv = findViewById(R.id.img_profile);
                    URL url = new URL(MYPAGE_IMAGE);
                    InputStream is = url.openStream();
                    final Bitmap bm = BitmapFactory.decodeStream(is);
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            iv.setImageBitmap(bm);
                        }
                    });
                    iv.setImageBitmap(bm);
                } catch(Exception e){

                }

            }
        });

        t.start();


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar ab = getSupportActionBar();
        image_profile=findViewById(R.id.img_profile);
        listAdapter = new ListAdapter();
        ListView listView = (ListView) findViewById(R.id.mp_list_view);
        listView.setAdapter(listAdapter);
        listView.setOnItemLongClickListener(longClickListener);


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_taxi, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //getFirebaseDatabase();
                finish();
                return true;
            case R.id.menu_refresh:
                getFirebaseDatabase();
                return true;
            case R.id.menu_mypage:
                Toast.makeText(getApplicationContext(), " press My page", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_logout:
                UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
                    @Override
                    public void onCompleteLogout() {
                        Intent intent =new Intent(Taxi_MyPage.this, Login.class);
                        startActivity(intent);
                    }
                });
                Toast.makeText(getApplicationContext(), " press logout", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Log.d("Long Click", "position = " + position);
            final String nowListData = listData.get(position);
            REMOVE_LIST=nowListData;
            Log.d("String temp", "String temp:" + nowListData.toString());
            android.app.AlertDialog.Builder dialog = new AlertDialog.Builder(Taxi_MyPage.this);
            dialog.setTitle("데이터 삭제")
                    .setMessage("신청을 취소하시겠습니까?" + "\n" )
                    .setPositiveButton("네", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                                POST_LIST.remove(nowListData);
                                postDataList(false);
                                getListDatabase();
                                removeList();
                                Toast.makeText(Taxi_MyPage.this, "신청을 취소했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .create()
                    .show();
            return true;
        }
    };

    public void postDataList(boolean add) {

        mPostReference = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> childUpdates = new HashMap<>();
        Map<String, Object> postValues = null;
        User user = new User(USER_ID,POST_LIST);
        postValues = user.toMap();

        childUpdates.put("/LIST/" + USER_ID, postValues);
        mPostReference.updateChildren(childUpdates);
    }

    public void getListDatabase() {

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listData.clear();
                listIndex.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String listkey = postSnapshot.getKey();
                    User user = postSnapshot.getValue(User.class);
                    //Log.d("temp",user.post_list.toString());
                    if(user.post_list!=null){
                        for(int i=0;i<user.post_list.size();i++){
                            listData.add(user.post_list.get(i));
                            listIndex.add(listkey);
                        }
                    }
                    Log.d("getFirebaseDatabase", "key: " + listkey);

                }
                listAdapter.clear();
                listAdapter.addAll(listData);
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("getFirebaseDatabase", "loadPost:onCancelled", databaseError.toException());
            }
        };
        Query sortbyAge = FirebaseDatabase.getInstance().getReference().child("LIST").orderByChild(sort);
        sortbyAge.addListenerForSingleValueEvent(postListener);
    }

    public void removeList() {

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String key = postSnapshot.getKey();
                    Post get = postSnapshot.getValue(Post.class);
                    if(REMOVE_LIST.equals(get.title)){

                        TITLE=get.title;
                        SOURCE=get.source;
                        DEST=get.dest;
                        TIME=get.time;
                        PERSONNEL=get.personnel;
                        int temp= Integer.parseInt(get.current_personnel);
                        temp=temp-1;
                        CURRENT_PERSONNEL=Integer.toString(temp);
                        PASSWORD=get.password;
                        EDITOR_ID=get.editor_id;
                    }

                }
                postFirebaseDatabase(true);
                getFirebaseDatabase();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("getFirebaseDatabase", "loadPost:onCancelled", databaseError.toException());
            }
        };
        Query sortbyAge = FirebaseDatabase.getInstance().getReference().child("POST").orderByChild(sort);
        sortbyAge.addListenerForSingleValueEvent(postListener);
    }




}
