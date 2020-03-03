package com.mnb.rosreader.data;

import java.util.ArrayList;

public class Unit {

  public String name;

  public int pl;
  public int pts;
  public Boolean warlord;
  public ArrayList<String> categories;
  public ArrayList<SubUnit> subUnits;
  public Psyker psyker;
  public ArrayList<Damage> damages;
  public ArrayList<Weapon> weapons;
  public ArrayList<Power> powers;
  public ArrayList<Rule> rules;

  public Unit (String name) {
    this.name = name;
    this.pl = 0;
    this.pts = 0;
    this.warlord = false;
    this.categories = new ArrayList<String>();
    this.subUnits = new ArrayList<SubUnit>();
    this.psyker = null;
    this.damages = new ArrayList<Damage>();
    this.weapons = new ArrayList<Weapon>();
    this.powers = new ArrayList<Power>();
    this.rules = new ArrayList<Rule>();
  }
}
