package lagi.garap.tubes;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //membuat fragment, fragment manager, transaction,dsb
        Fragment fragment = new HomeFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.layout_area, fragment);
        fragmentTransaction.addToBackStack(fragment.toString());
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();
        //inisialisasi navigasi dari bottomnavigationView
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.nav);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    //inisialisasi method jika bottomnavigationview ditekan
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;

            int id = item.getItemId();
            //memeriksa apakah id sama dengan navigation_home
            if (id == R.id.navigation_home) {
                fragment = new HomeFragment();
            } else
                //jika id sama dengan navigation_dashboard
                if (id == R.id.navigation_dashboard) {
                    fragment = new LogOutFrag();
                }

            //jika fragment tidak kosoong
            if (fragment != null) {
                //membuat fragment manager, dan fragmentTransaction untuk memulai transaksi
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                //memanggil method replace untuk mengganti layout saat ini menjadi ke layout_area
                ft.replace(R.id.layout_area, fragment);
                ft.commit();
            }

            return true;
        }
    };

}
