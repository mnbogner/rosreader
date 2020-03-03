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

  private RosSelector selector;
  private Unit unit;

  private View view;

  // private ArrayList<String> units = new ArrayList<String>();

  private HashMap<String, SubUnit> unitCounts = new HashMap<String, SubUnit>();
  private HashMap<String, Weapon> weaponCounts = new HashMap<String, Weapon>();
  private ArrayList<String> unitNames = new ArrayList<String>();
  private ArrayList<String> weaponNames = new ArrayList<String>();

  public UnitFragment (RosSelector selector, Unit unit) {
    this.selector = selector;
    this.unit = unit;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_unit, container, false);

    if (unit == null) {
      System.out.println("BAR - EMPTY FRAGMENT");
      return view;
    }

    TextView t = view.findViewById(R.id.unit_name);
    if (unit.warlord) {
      t.setText(unit.name + "(Warlord)");
    } else {
      t.setText(unit.name);
    }

    Button ub = view.findViewById(R.id.unit_button);
    ub.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Context c = getContext();
        selector.showMenu(c, v);
      }
    });

    Button ib = view.findViewById(R.id.info_button_open);
    ib.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        System.out.println(unit.name + " - " + unit.pl + " pl / " + unit.pts + " points");
        selector.showInfo(unit.powers, unit.rules, unit.pl, unit.pts);
      }
    });

    TextView nt = view.findViewById(R.id.unit_name);
    nt.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        selector.showItems();
      }
    });

    LinearLayout unitView = view.findViewById(R.id.unit_list);

    View v = inflater.inflate(R.layout.item_unit, container, false);
    unitView.addView(v);

    // need to merge counts
    for (SubUnit su : unit.subUnits) {
      System.out.println("MERGE - found " + su.numberOf + "x " + su.name);
      if (su.numberOf < 1) {
        su.numberOf = 1;
      }
      if (unitCounts.containsKey(su.name)) {
        SubUnit suPlus = unitCounts.get(su.name);
        System.out.println("MERGE - combine " + suPlus.numberOf + "x " + suPlus.name);
        su.numberOf += suPlus.numberOf;
      }
      unitCounts.put(su.name, su);
    }

    for (SubUnit su : unit.subUnits) {
      if (!unitNames.contains(su.name)) {
        unitNames.add(su.name);
        v = inflater.inflate(R.layout.item_unit, container, false);
        t = v.findViewById(R.id.item_unit_name);

        Integer count = unitCounts.get(su.name).numberOf;
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
      //} else {
      //  System.out.println("OOPS: DUPLICATE " + su.name);
      //}
    }

    LinearLayout psykerView = view.findViewById(R.id.psyker);
    if (unit.psyker != null) {
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
      // t = v.findViewById(R.id.item_psyker_other);
      // t.setText(unit.psyker.other);
      psykerView.addView(v);
    } else {
      psykerView.setVisibility(View.GONE);
    }

    LinearLayout damageView = view.findViewById(R.id.damage_list);
    if (unit.damages.size() > 0) {
      v = inflater.inflate(R.layout.item_damage, container, false);
      damageView.addView(v);

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
      damageView.setVisibility(View.GONE);
    }

    LinearLayout weaponView = view.findViewById(R.id.weapon_scroll);
    if (unit.weapons.size() > 0) {
      v = inflater.inflate(R.layout.item_weapon, container, false);
      weaponView.addView(v);

      // need to merge counts
      for (Weapon w : unit.weapons) {
        System.out.println("MERGE - found " + w.numberOf + "x " + w.name);
        if (w.numberOf < 1) {
          w.numberOf = 1;
        }
        if (weaponCounts.containsKey(w.name)) {
          Weapon wPlus = weaponCounts.get(w.name);
          System.out.println("MERGE - combine " + wPlus.numberOf + "x " + wPlus.name);
          w.numberOf += wPlus.numberOf;
        }
        weaponCounts.put(w.name, w);
      }

      for (Weapon w : unit.weapons) {
        if (!weaponNames.contains(w.name)) {
          weaponNames.add(w.name);
          v = inflater.inflate(R.layout.item_weapon, container, false);
          t = v.findViewById(R.id.item_weapon_name);

          Integer count = weaponCounts.get(w.name).numberOf;
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
          String shortType = w.type.replace("Rapid Fire", "R")
              .replace("Assault", "A")
              .replace("Heavy", "H")
              .replace("Pistol", "P")
              .replace("Grenade", "G");
          t.setText(shortType);
          t = v.findViewById(R.id.item_weapon_s);
          if ("User".equals(w.s)) {
            t.setText("n/a");
          } else {
            t.setText(w.s);
          }
          t = v.findViewById(R.id.item_weapon_ap);
          t.setText(w.ap);
          t = v.findViewById(R.id.item_weapon_d);
          t.setText(w.d);
          t = v.findViewById(R.id.item_weapon_abilities);
          t.setText(w.abilities);
          weaponView.addView(v);
        } else {
          System.out.println("OOPS: DUPLICATE " + w.name);
        }
      }
    } else {
      weaponView = view.findViewById(R.id.weapon_list);
      weaponView.setVisibility(View.GONE);
    }

    return view;
  }
}
