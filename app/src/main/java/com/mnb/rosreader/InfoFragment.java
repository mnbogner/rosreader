package com.mnb.rosreader;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.mnb.rosreader.data.Power;
import com.mnb.rosreader.data.Rule;

import java.util.ArrayList;

public class InfoFragment extends DialogFragment {

  private ArrayList<Power> powers;
  private ArrayList<Rule> rules;

  public InfoFragment (ArrayList<Power> powers, ArrayList<Rule> rules) {
    this.powers = powers;
    this.rules = rules;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_info, container, false);

    LinearLayout ll = view.findViewById(R.id.info_list);

    if (powers != null) {
      for (Power p : powers) {
        View v = inflater.inflate(R.layout.item_rule, container, false);
        TextView nv = v.findViewById(R.id.item_rule_name);
        nv.setText(p.name + " (Warp Charge " + p.warpCharge + ")");
        TextView dv = v.findViewById(R.id.item_rule_description);
        dv.setText(p.details);
        ll.addView(v);
      }
    }

    if (rules != null) {
      for (Rule r : rules) {
        View v = inflater.inflate(R.layout.item_rule, container, false);
        TextView nv = v.findViewById(R.id.item_rule_name);
        nv.setText(r.name);
        TextView dv = v.findViewById(R.id.item_rule_description);
        dv.setText(r.description);
        ll.addView(v);
      }
    }

    Button b = view.findViewById(R.id.info_button_close);
    b.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dismiss();
      }
    });

    return view;

  }

  @Override
  public void onResume() {
    super.onResume();
    ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
    params.height = ViewGroup.LayoutParams.MATCH_PARENT;
    getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
  }



}
