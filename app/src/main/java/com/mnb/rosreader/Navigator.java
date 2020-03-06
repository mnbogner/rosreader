package com.mnb.rosreader;

import android.content.Context;
import android.view.View;

public interface Navigator {

  String COUNT_PREF = "show_count_pref";
  String POINTS_PREF = "show_points_pref";

  void showPopupMenu(Context c, View v);
  void showFileSelector();
  void openFile(String fileName);
  void showItemSelector();
  void goToItem(String itemName);
  void showItemInfo(String itemName);
  void toggleOption(String optionName);
  boolean checkOption(String optionName);
}
