package com.mnb.rosreader;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mnb.rosreader.data.Damage;
import com.mnb.rosreader.data.SubUnit;
import com.mnb.rosreader.data.Unit;
import com.mnb.rosreader.data.Weapon;

import java.util.ArrayList;
import java.util.HashMap;

public class UnitFragment extends Fragment {

  private static final String TAG = "MNB.ROS";

  private Navigator navigator;
  private Unit unit;
  private boolean showCounts;

  private ArrayList<String> unitNames;
  private ArrayList<String> weaponNames;
  private HashMap<String, Integer> unitCounts;
  private HashMap<String, Integer> weaponCounts;

  public UnitFragment (Navigator navigator, Unit unit, boolean showCounts) {
    this.navigator = navigator;
    this.unit = unit;
    this.showCounts = showCounts;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_unit, container, false);

    // set up unit menu button
    TextView un = view.findViewById(R.id.unit_name);
    un.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        navigator.showItemSelector();
      }
    });

    // set up option menu button
    Button ub = view.findViewById(R.id.option_button);
    ub.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Context c = getContext();
        navigator.showPopupMenu(c, v);
      }
    });

    // set up unit info button
    Button ib = view.findViewById(R.id.info_button_open);
    ib.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        navigator.showItemInfo(unit.name);
      }
    });

    if (unit == null) {
      System.out.println(TAG + " no unit data to display");
      return view;
    }

    // used to enforce uniqueness
    unitNames = new ArrayList<String>();
    weaponNames = new ArrayList<String>();
    // used to merge counts
    unitCounts = new HashMap<String, Integer>();
    weaponCounts = new HashMap<String, Integer>();

    // add warlord tag to unit header, if necessary
    if (unit.warlord) {
      un.setText(unit.name + " (Warlord)");
    } else {
      un.setText(unit.name);
    }

    LinearLayout unitView = view.findViewById(R.id.unit_list);

    // default unit layout includes headers
    View v = inflater.inflate(R.layout.item_unit, container, false);
    unitView.addView(v);
    TextView t = null;

    // need to merge unit counts
    if (showCounts) {
      for (SubUnit su : unit.subUnits) {
        if (unitCounts.containsKey(su.name)) {
          Integer count = unitCounts.get(su.name);
          count += su.numberOf;
          unitCounts.put(su.name, count);
        } else {
          unitCounts.put(su.name, su.numberOf);
        }
      }
    } else {
      // if there is no value in the hashmap, no count will be displayed
    }

    // show individual unit stats
    for (SubUnit su : unit.subUnits) {
      if (!unitNames.contains(su.name)) {
        unitNames.add(su.name);
        v = inflater.inflate(R.layout.item_unit, container, false);
        t = v.findViewById(R.id.item_unit_name);
        // show count only if > 1
        Integer count = unitCounts.get(su.name);
        if (count != null && count > 1) {
          t.setText(count + "x " + su.name);
        } else {
          t.setText(su.name);
        }
        t = v.findViewById(R.id.item_unit_m);
        t.setText(su.m);
        t = v.findViewById(R.id.item_unit_ws);
        t.setText(su.ws);
        t = v.findViewById(R.id.item_unit_bs);
        t.setText(su.bs);
        t = v.findViewById(R.id.item_unit_s);
        t.setText(su.s);
        t = v.findViewById(R.id.item_unit_t);
        t.setText(su.t);
        t = v.findViewById(R.id.item_unit_w);
        t.setText(su.w);
        t = v.findViewById(R.id.item_unit_a);
        t.setText(su.a);
        t = v.findViewById(R.id.item_unit_ld);
        t.setText(su.ld);
        t = v.findViewById(R.id.item_unit_save);
        t.setText(su.save);
        unitView.addView(v);
      }
    }

    LinearLayout psykerView = view.findViewById(R.id.psyker);
    // show psyker stats, if any
    if (unit.psyker != null) {
      // default psyker layout includes headers
      v = inflater.inflate(R.layout.item_psyker, container, false);
      psykerView.addView(v);
      v = inflater.inflate(R.layout.item_psyker, container, false);
      t = v.findViewById(R.id.item_psyker_name);
      t.setText("Psyker");
      t = v.findViewById(R.id.item_psyker_cast);
      t.setText(unit.psyker.cast);
      t = v.findViewById(R.id.item_psyker_deny);
      t.setText(unit.psyker.deny);
       t = v.findViewById(R.id.item_psyker_known);
       t.setText(unit.psyker.powersKnown);
      psykerView.addView(v);
    } else {
      // hide psyker stats if there's nothing to show
      psykerView.setVisibility(View.GONE);
    }

    LinearLayout damageView = view.findViewById(R.id.damage_list);
    // show damage track, if any
    if (unit.damages.size() > 0) {
      // default damage track layout includes headers
      v = inflater.inflate(R.layout.item_damage, container, false);
      damageView.addView(v);
      // can't guarantee order of damage tracks in unit data
      View v1 = inflater.inflate(R.layout.item_damage, container, false);
      damageView.addView(v1);
      View v2 = inflater.inflate(R.layout.item_damage, container, false);
      damageView.addView(v2);
      View v3 = inflater.inflate(R.layout.item_damage, container, false);
      damageView.addView(v3);
      for (Damage d : unit.damages) {
        View vCurrent;
        if (d.name.endsWith("1") || d.name.endsWith("(1)")) {
          vCurrent = v1;
        } else if (d.name.endsWith("2") || d.name.endsWith("(2)")) {
          vCurrent = v2;
        } else if (d.name.endsWith("3") || d.name.endsWith("(3)")) {
          vCurrent = v3;
        } else {
          continue;
        }
        t = vCurrent.findViewById(R.id.item_damage_remaining);
        t.setText(d.remaining);
        t = vCurrent.findViewById(R.id.item_damage_m);
        t.setText(d.m);
        t = vCurrent.findViewById(R.id.item_damage_ws);
        t.setText(d.ws);
        t = vCurrent.findViewById(R.id.item_damage_bs);
        t.setText(d.bs);
        t = vCurrent.findViewById(R.id.item_damage_s);
        t.setText(d.s);
        t = vCurrent.findViewById(R.id.item_damage_t);
        t.setText(d.t);
        t = vCurrent.findViewById(R.id.item_damage_w);
        t.setText(d.w);
        t = vCurrent.findViewById(R.id.item_damage_a);
        t.setText(d.a);
        t = vCurrent.findViewById(R.id.item_damage_ld);
        t.setText(d.ld);
        t = vCurrent.findViewById(R.id.item_damage_save);
        t.setText(d.save);
      }
    } else {
      // hide damage track if there's nothing to show
      damageView.setVisibility(View.GONE);
    }

    LinearLayout weaponView = view.findViewById(R.id.weapon_scroll);
    // show weapon stats, if any
    if (unit.weapons.size() > 0) {
      // default weapon layout includes headers
      v = inflater.inflate(R.layout.item_weapon, container, false);
      weaponView.addView(v);

      // need to merge weapon counts
      if (showCounts) {
        for (Weapon w : unit.weapons) {
          if (weaponCounts.containsKey(w.name)) {
            Integer count = weaponCounts.get(w.name);
            count += w.numberOf;
            weaponCounts.put(w.name, count);
          } else {
            weaponCounts.put(w.name, w.numberOf);
          }
        }
      } else {
        // if there is no value in the hashmap, no count will be displayed
      }

      for (Weapon w : unit.weapons) {
        if (!weaponNames.contains(w.name)) {
          weaponNames.add(w.name);
          v = inflater.inflate(R.layout.item_weapon, container, false);
          t = v.findViewById(R.id.item_weapon_name);
          // show count if > 1
          Integer count = weaponCounts.get(w.name);
          if (count != null && count > 1) {
            t.setText(count + "x " + w.name);
          } else {
            t.setText(w.name);
          }
          t = v.findViewById(R.id.item_weapon_range);
          if ("Melee".equals(w.range)) {
            t.setText("n/a");
          } else {
            t.setText(w.range);
          }
          t = v.findViewById(R.id.item_weapon_type);
          // truncate weapon types to support smaller displays
          String shortType = w.type.replace("Rapid Fire", "R")
              .replace("Assault", "A")
              .replace("Heavy", "H")
              .replace("Pistol", "P")
              .replace("Grenade", "G");
          t.setText(shortType);
          t = v.findViewById(R.id.item_weapon_s);
          // found some typos
          String s = w.s;
          if (s != null) {
            s = s.toLowerCase().trim();
          }
          if ("user".equals(s)) {
            t.setText("U");
          } else {
            t.setText(s);
          }
          t = v.findViewById(R.id.item_weapon_ap);
          t.setText(w.ap);
          t = v.findViewById(R.id.item_weapon_d);
          t.setText(w.d);
          t = v.findViewById(R.id.item_weapon_abilities);
          t.setText(w.abilities);
          weaponView.addView(v);
        }
      }
    } else {
      // hide weapon stats if there's nothing to show
      weaponView = view.findViewById(R.id.weapon_list);
      weaponView.setVisibility(View.GONE);
    }

    return view;
  }
}
