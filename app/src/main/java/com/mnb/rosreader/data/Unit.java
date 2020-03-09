package com.mnb.rosreader.data;

import java.util.ArrayList;

public class Unit {

  public String name;

  public int pl = 0;
  public int pts = 0;
  public Boolean warlord = false;
  public ArrayList<String> categories;
  public ArrayList<SubUnit> subUnits;
  public Psyker psyker = null;
  public ArrayList<Damage> damages;
  public ArrayList<Weapon> weapons;
  public ArrayList<Power> powers;
  public ArrayList<Rule> rules;

  public Unit (String name) {
    this.name = name;
    this.categories = new ArrayList<String>();
    this.subUnits = new ArrayList<SubUnit>();
    this.damages = new ArrayList<Damage>();
    this.weapons = new ArrayList<Weapon>();
    this.powers = new ArrayList<Power>();
    this.rules = new ArrayList<Rule>();
  }
}
