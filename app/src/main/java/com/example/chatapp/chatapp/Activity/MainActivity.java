package com.example.chatapp.chatapp.Activity;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.chatapp.chatapp.Adapter.SectionPagerAdapter;
import com.example.chatapp.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {


    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private ViewPager viewPager;
    private SectionPagerAdapter sectionPagerAdapter;
    private Toolbar toolbar;
    private TabLayout tabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMain();
    }

    public void setUpMain(){
        tabLayout = (TabLayout) findViewById(R.id.mainTab);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        sectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(sectionPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        toolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ChatApp");
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if(user != null){
            databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = auth.getCurrentUser();
        if(user == null){
            sendToStart();
        }else{
            databaseReference.child("online").setValue("true");
        }
    }

    private void sendToStart() {
        Intent intent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menuLogOut:
                auth.signOut();
                sendToStart();
                break;
            case R.id.menuAccountSetings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.menuAllUsers:
                startActivity(new Intent(MainActivity.this, UsersActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
