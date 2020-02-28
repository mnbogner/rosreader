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
import java.util.Arrays;

public class FileFragment extends DialogFragment {

  private RosSelector selector;
  private ArrayList<String> rosList;

  public FileFragment (RosSelector selector, ArrayList<String> rosList) {
    this.selector = selector;
    this.rosList = rosList;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_file, container, false);

    LinearLayout ll = view.findViewById(R.id.file_list);

    for (String s : rosList) {
      View v = inflater.inflate(R.layout.item_file, container, false);
      TextView tv = v.findViewById(R.id.file_name);
      tv.setText(s);
      tv.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          selector.loadRos(((TextView)v).getText().toString());
          dismiss();
        }
      });
      System.out.println("BAR - adding item for " + s);
      ll.addView(v);
    }

    //System.out.println("BAR - FILES: " + Arrays.toString(rosList));

    return view;

  }

  @Override
  public void onResume() {
    super.onResume();
    ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
    getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
  }

}
