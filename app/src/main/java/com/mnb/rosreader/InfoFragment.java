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
import java.util.HashMap;

public class InfoFragment extends DialogFragment {

  private static final String TAG = "MNB.ROS";

  private ArrayList<Power> powers;
  private ArrayList<Rule> rules;
  private int pl;
  private int pts;
  private boolean showPoints;

  private ArrayList<String> powerNames;
  private ArrayList<String> ruleNames;
  private HashMap<Integer, Rule> numberedRules;

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

    // set up unit info close button
    Button b = view.findViewById(R.id.info_button_close);
    b.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dismiss();
      }
    });

    // used to enforce uniqueness
    powerNames = new ArrayList<String>();
    ruleNames = new ArrayList<String>();
    // used to organize sets of rules
    numberedRules = new HashMap<Integer, Rule>();

    LinearLayout infoView = view.findViewById(R.id.info_list);

    // show power level/point cost, if any (and if option is toggled)
    if (showPoints && (pl > 0 || pts > 0)) {
      View v = inflater.inflate(R.layout.item_rule, container, false);
      TextView t = v.findViewById(R.id.item_rule_name);
      t.setVisibility(View.GONE);
      t = v.findViewById(R.id.item_rule_description);
      t.setText("PL: " + pl + " / PTS: " + pts);
      infoView.addView(v);
    }

    // show psyker powers, if any
    if (powers != null) {
      for (Power p : powers) {
        if (!powerNames.contains(p.name)) {
          powerNames.add(p.name);
          View v = inflater.inflate(R.layout.item_rule, container, false);
          TextView nv = v.findViewById(R.id.item_rule_name);
          nv.setText(p.name + " (Warp Charge " + p.warpCharge + ")");
          TextView dv = v.findViewById(R.id.item_rule_description);
          dv.setText(p.details);
          infoView.addView(v);
        }
      }
    }

    // show unit rules, if any
    if (rules != null) {
      for (Rule r : rules) {
        if (!ruleNames.contains(r.name)) {
          ruleNames.add(r.name);
          // set aside numbered rules to display in order at the end
          // currently only intended for c'tan powers
          if (Character.isDigit(r.name.charAt(0)) && r.name.charAt(1) == ')') {
            Integer ruleNumber = Integer.parseInt("" + r.name.charAt(0));
            numberedRules.put(ruleNumber, r);
          } else {
            View v = inflater.inflate(R.layout.item_rule, container, false);
            TextView nv = v.findViewById(R.id.item_rule_name);
            nv.setText(r.name);
            TextView dv = v.findViewById(R.id.item_rule_description);


            if (r.description == null || r.description.isEmpty()) {
              if (r.roll.isEmpty() || r.distance.isEmpty() || r.wounds.isEmpty()) {
                dv.setText("(no description found)");
              } else {
                dv.setText("If this model is reduced to 0 wounds, roll a D6 before removing it from the battlefield. On a " + r.roll + " it explodes, and each unit within " + r.distance + " suffers " + r.wounds + " mortal wounds.");
              }
            } else {
              dv.setText(r.description);
            }


            infoView.addView(v);
          }
        }
      }
    }

    // need to fix range, but expected values < 10 and may not be contiguous
    // currently only supports c'tan powers
    for (int i = 0; i < 10; i++) {
      if (numberedRules.containsKey(i)) {
        Rule r = numberedRules.get(i);
        View v = inflater.inflate(R.layout.item_rule, container, false);
        TextView nv = v.findViewById(R.id.item_rule_name);
        nv.setText(r.name);
        TextView dv = v.findViewById(R.id.item_rule_description);
        dv.setText(r.description);
        infoView.addView(v);
      }
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
