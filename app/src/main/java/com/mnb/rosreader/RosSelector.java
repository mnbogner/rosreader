package com.mnb.rosreader;

import android.content.Context;
import android.view.View;

import com.mnb.rosreader.data.Power;
import com.mnb.rosreader.data.Rule;

import java.util.ArrayList;

public interface RosSelector {
  public void showSelector();
  public void loadRos(String rosFile);
  public void showItems();
  public void goToItem(String item);
  public void showInfo(ArrayList<Power> powers, ArrayList<Rule> rules, int pl, int pts);
  public void showMenu(Context c, View v);
}
