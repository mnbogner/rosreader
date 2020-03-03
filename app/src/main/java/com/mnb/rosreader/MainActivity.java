package com.mnb.rosreader;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.mnb.rosreader.data.Power;
import com.mnb.rosreader.data.Rule;
import com.mnb.rosreader.data.Unit;
import com.mnb.rosreader.parser.RosAssetParser;
import com.mnb.rosreader.parser.RosDownloadParser;
import com.mnb.rosreader.parser.RosParser;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends FragmentActivity implements RosSelector {

  protected static final String TAG = "MNB.ROS";

  private static final int PERMISSION_REQUEST_CODE = 100;

  private ArrayList<Unit> units = null;
  private HashMap<String, Integer> index = new HashMap<String, Integer>();

  //private ArrayList<Fragment> fragments = null;

  private FragmentManager fm;

  private ViewPager vp;
  private PagerAdapter pa;

  private RosParser parser;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // check permissions before trying to get file list
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        + ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
      System.out.println(TAG + " need permissions");
      ActivityCompat.requestPermissions(this,
          new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE },
          PERMISSION_REQUEST_CODE);

    } else {
      System.out.println(TAG + " permissions ok");
      init();
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == PERMISSION_REQUEST_CODE) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        System.out.println(TAG + " got permissions");
        init();
      } else {
        System.out.println(TAG + " permissions denied");
      }
    } else {
      System.out.println(TAG + " wrong request code");
    }
  }

  private void init() {

    parser = new RosAssetParser(this);
    //parser = new RosDownloadParser(this);

    fm = getSupportFragmentManager();

    vp = findViewById(R.id.fragment_pager);
    pa = new MyPagerAdapter(fm);
    vp.setAdapter(pa);

    showDialog();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
  }

  @Override
  protected void onRestoreInstanceState(Bundle inState) {
    super.onRestoreInstanceState(inState);
  }

  @Override
  public void onBackPressed() {
    int i = vp.getCurrentItem();
    if (i == 0) {
      super.onBackPressed();
    } else {
      vp.setCurrentItem(i - 1);
    }
  }

  @Override
  public void showInfo(ArrayList<Power> powers, ArrayList<Rule> rules, int pl, int pts) {

    FragmentTransaction ft = fm.beginTransaction();
    Fragment prev = fm.findFragmentByTag("info");
    if (prev != null) {
      ft.remove(prev);
    }
    ft.addToBackStack(null);

    //String[] rosFiles = getRosFiles();
    DialogFragment d = new InfoFragment(powers, rules, pl, pts);

    d.show(ft, "info");

  }

  boolean foo = false;
  boolean bar = false;

  @Override
  public void showMenu(Context c, View v) {
    final PopupMenu popup = new PopupMenu(c, v);
    popup.inflate(R.menu.popup_menu);
    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
          case R.id.load_item:
            showSelector();return true;
          case R.id.count_item:
            foo = !foo;
            item.setChecked(foo);
            return true;
          case R.id.points_item:
            bar = !bar;
            item.setChecked(bar);
            return true;
          default:
            return false;
        }
      }
    });
    popup.getMenu().findItem(R.id.count_item).setChecked(foo);
    popup.getMenu().findItem(R.id.points_item).setChecked(bar);
    popup.show();
  }

  @Override
  public void showSelector() {

    showDialog();

  }

  @Override
  public void showItems() {
    showOtherDialog();
  }

  @Override
  public void loadRos(String rosFile) {

    System.out.println(rosFile);

    units = parser.parseRosFile(rosFile);

    int i = 0;
    for (Unit u : units) {
      index.put(u.name, i);
      i++;
    }

    pa.notifyDataSetChanged();
    vp.setCurrentItem(0);

  }

  @Override
  public void goToItem(String itemName) {

    System.out.println("BAZ - go to item " + itemName);

    int target = index.get(itemName);

    System.out.println("BAZ - go to index " + target);

    vp.setCurrentItem(target);

  }

  private class MyPagerAdapter extends FragmentStatePagerAdapter {

    public MyPagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public Fragment getItem(int i) {



      if (units == null || units.isEmpty()) {
        return null;
      }

      Unit u = units.get(i);

      Fragment f = fm.findFragmentByTag(u.name);

      if (f == null) {
        if (i == 0) {
          f = new RulesFragment(MainActivity.this, u);
        } else {
          f = new UnitFragment(MainActivity.this, u);
        }
      }

      return f;

    }

    @Override
    public int getCount() {
      if (units == null) {
        return 0;
      } else {
        return units.size();
      }
    }
  }




  void showDialog() {

    // DialogFragment.show() will take care of adding the fragment
    // in a transaction.  We also want to remove any currently showing
    // dialog, so make our own transaction and take care of that here.
    FragmentTransaction ft = fm.beginTransaction();
    Fragment prev = fm.findFragmentByTag("dialog");
    if (prev != null) {
      ft.remove(prev);
    }
    ft.addToBackStack(null);

    ArrayList<String> rosFiles = parser.getRosFileList();
    DialogFragment d = new FileFragment(this, rosFiles);

    d.show(ft, "dialog");
  }

  void showOtherDialog() {

    // DialogFragment.show() will take care of adding the fragment
    // in a transaction.  We also want to remove any currently showing
    // dialog, so make our own transaction and take care of that here.
    FragmentTransaction ft = fm.beginTransaction();
    Fragment prev = fm.findFragmentByTag("dialog");
    if (prev != null) {
      ft.remove(prev);
    }
    ft.addToBackStack(null);

    String[] stuff = new String[units.size()];
    int i = 0;
    for (Unit u : units) {
      stuff[i] = u.name;
      i++;
    }
    DialogFragment d = new SelectionFragment(this, stuff);

    d.show(ft, "dialog");
  }

}
