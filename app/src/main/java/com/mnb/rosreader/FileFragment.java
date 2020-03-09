package com.mnb.rosreader;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

public class FileFragment extends DialogFragment {

  private static final String TAG = "MNB.ROS";

  private Navigator navigator;
  private ArrayList<String> rosFileList;

  public FileFragment (Navigator navigator, ArrayList<String> rosFileList) {
    this.navigator = navigator;
    this.rosFileList = rosFileList;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_file, container, false);
    LinearLayout ll = view.findViewById(R.id.file_list);

    // show list of rosz/ros files
    for (String s : rosFileList) {
      View v = inflater.inflate(R.layout.item_file, container, false);
      TextView tv = v.findViewById(R.id.file_name);
      tv.setText(s);
      tv.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          navigator.openFile(((TextView)v).getText().toString());
          dismiss();
        }
      });
      ll.addView(v);
    }

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    // need to control size of dialog fragment
    ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
    params.height = ViewGroup.LayoutParams.MATCH_PARENT;
    getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
  }
}
