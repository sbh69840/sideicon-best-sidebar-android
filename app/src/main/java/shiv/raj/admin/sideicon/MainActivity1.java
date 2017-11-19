package shiv.raj.admin.sideicon;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.example.admin.sideicon.R;

import shiv.raj.admin.sideicon.adapter.TabsPagerAdapter;

/**
 * Created by shivraj on 27/01/2017.
 */

public class MainActivity1 extends FragmentActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager1);
        TabsPagerAdapter mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);



    }
    public void sendMessage(View view){
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    public void firstLaunch(View view) {
        Intent intent=new Intent(this,FirstLaunch.class);
        startActivity(intent);
    }
}
