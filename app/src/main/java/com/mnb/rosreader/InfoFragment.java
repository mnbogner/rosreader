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
import com.mnb.rosreader.data.Unit;

import java.util.ArrayList;

public class InfoFragment extends DialogFragment {

  private static final String TAG = "MNB.ROS";

  private ArrayList<Power> powers;
  private ArrayList<Rule> rules;
  private int pl;
  private int pts;
  private boolean showPoints;

  public InfoFragment (Unit unit, boolean showPoints) {
    this.powers = unit.powers;
    this.rules = unit.rules;
    this.pl = unit.pl;
    this.pts = unit.pts;
    this.showPoints = showPoints;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_info, container, false);

    LinearLayout ll = view.findViewById(R.id.info_list);

    // show power level/point cost, if any (and if option is toggled)
    if (showPoints && (pl > 0 || pts > 0)) {
      View v = inflater.inflate(R.layout.item_rule, container, false);
      TextView t = v.findViewById(R.id.item_rule_name);
      t.setVisibility(View.GONE);
      t = v.findViewById(R.id.item_rule_description);
      t.setText("POWER: " + pl + " / POINTS: " + pts);
      ll.addView(v);
    }

    // show psyker powers, if any
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

    // show unit rules, if any
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

    // set up unit info close button
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
    // need to control size of dialog fragment
    ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
    params.height = ViewGroup.LayoutParams.MATCH_PARENT;
    getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
  }
}
