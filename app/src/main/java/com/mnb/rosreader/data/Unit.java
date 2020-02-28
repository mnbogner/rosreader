package com.mnb.rosreader.data;

import com.mnb.rosreader.parser.RosDownloadParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

import static com.mnb.rosreader.parser.RosDownloadParser.ns;

public class Unit {

  public String name;

  public Boolean warlord;
  public ArrayList<String> categories;
  public ArrayList<Rule> rules;
  public Psyker psyker;
  public ArrayList<Power> powers;
  public ArrayList<SubUnit> subUnits;
  public ArrayList<Damage> damages;
  public ArrayList<Weapon> weapons;

  public Unit (String name) {

    this.name = name;
    this.warlord = false;
    this.categories = new ArrayList<String>();
    this.rules = new ArrayList<Rule>();
    this.powers = new ArrayList<Power>();
    this.subUnits = new ArrayList<SubUnit>();
    this.damages = new ArrayList<Damage>();
    this.weapons = new ArrayList<Weapon>();

  }
}
