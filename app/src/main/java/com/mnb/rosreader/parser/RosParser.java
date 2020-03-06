package com.mnb.rosreader.parser;

import android.content.Context;
import android.util.Xml;

import com.mnb.rosreader.data.Damage;
import com.mnb.rosreader.data.Power;
import com.mnb.rosreader.data.Psyker;
import com.mnb.rosreader.data.Rule;
import com.mnb.rosreader.data.SubUnit;
import com.mnb.rosreader.data.Unit;
import com.mnb.rosreader.data.Weapon;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public abstract class RosParser {

  protected static final String TAG = "MNB.ROS";

  protected static final String ns = null;  // ignore namespaces?

  protected static final String NONE = "none";
  protected static final String UNIT = "unit";
  protected static final String SUBUNIT = "subunit";
  protected static final String PSYKER = "psyker";
  protected static final String DAMAGE = "damage";
  protected static final String WEAPON = "weapon";
  protected static final String POWER = "power";
  protected static final String RULE = "rule";

  protected Context context;

  protected XmlPullParser xpp;

  protected ArrayList<Unit> units;

  protected Unit currentUnit;
  protected SubUnit currentSubUnit;
  protected Psyker currentPsyker;
  protected Damage currentDamage;
  protected Weapon currentWeapon;
  protected Power currentPower;
  protected Rule currentRule;
  protected String inProgress;
  protected int selectionDepth;

  protected Integer tagDepth;
  protected HashMap<Integer, String> tagStack;

  protected Integer numberDepth;
  protected HashMap<Integer, Integer> numberStack;

  protected Unit rulesUnit;

  protected boolean isTheEight;

  protected String characteristic1 = "";
  protected String characteristic2 = "";
  protected String characteristic3 = "";

  public abstract ArrayList<String> getRosFileList();

  protected abstract InputStream openRosFile(String rosFile);
  protected abstract InputStream openRoszFile(String roszFile);

  public RosParser(Context context) {
    this.context = context;
  }

  public InputStream openFile(String file) {

    if (file.endsWith("ros")) {
      return openRosFile(file);
    } else if (file.endsWith("rosz")) {
      return openRoszFile(file);
    } else {
      System.out.println(TAG + " unknown file type: " + file);
      return null;
    }
  }

  protected InputStream unzipFile(InputStream rawInputStream) {

    try {
      String targetFolder = context.getExternalFilesDir(null).getAbsolutePath();

      ZipInputStream zis = new ZipInputStream(rawInputStream);
      ZipEntry ze = zis.getNextEntry();

      // assuming only one file (.rosz -> .ros)
      if (ze != null) {
        String unzippedName = ze.getName();
        File unzippedFile = new File(targetFolder + File.separator + unzippedName);
        File unzippedDir = unzippedFile.getParentFile();
        try {
          if (!unzippedDir.exists()) {
            unzippedDir.mkdirs();
          }
          if (!unzippedFile.exists()) {
            unzippedFile.createNewFile();
          }
        } catch (IOException ioe) {
          System.out.println(TAG + " failed to create file/dir: " + ioe.getMessage());
          return null;
        }

        try {
          FileOutputStream fos = new FileOutputStream(unzippedFile);
          byte[] buffer = new byte[4096];
          int length = 0;
          while ((length = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
            fos.flush();
          }
          fos.close();

          return new FileInputStream(unzippedFile);
        } catch (IOException ioe) {
          System.out.println(TAG + " failed to read/write file: " + ioe.getMessage());
          return null;
        }
      } else {
        System.out.println(TAG + " no zipped files found");
        return null;
      }
    } catch (IOException ioe) {
      System.out.println(TAG + " failed to read zip entries: " + ioe.getMessage());
      return null;
    }
  }

  public ArrayList<Unit> parseRosFile(String rosFile) {

    units = new ArrayList<Unit>();

    InputStream is = openFile(rosFile);

    if (is == null) {
      System.out.println(TAG + " failed to open input stream");
      return units;
    }

    xpp = null;

    try {
      // TODO - verify state/encoding options
      xpp = Xml.newPullParser();
      xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
      xpp.setInput(is, null);
    } catch (XmlPullParserException xppe) {
      System.out.println(TAG + " failed to create parser: " + xppe.getMessage());
      return units;
    }

    if (xpp == null) {
      System.out.println(TAG + " failed to create parser");
      return units;
    }

    // reset tracking variables
    currentUnit = null;
    currentRule = null;
    currentPsyker = null;
    currentPower = null;
    currentSubUnit = null;
    currentDamage = null;
    currentWeapon = null;
    inProgress = null;
    selectionDepth = 0;

    tagDepth = 0;
    tagStack = new HashMap<Integer, String>();

    numberDepth = 0;
    numberStack = new HashMap<Integer, Integer>();

    // create a unit to hold unattached rules
    rulesUnit = new Unit("Army Rules");
    units.add(rulesUnit);

    try {
      int eventType = xpp.getEventType();

      while (eventType != XmlPullParser.END_DOCUMENT) {

        switch (eventType) {
          case XmlPullParser.START_TAG:
            handleStartTag();
            break;
          case XmlPullParser.END_TAG:
            handleEndTag();
            break;
        }

        eventType = xpp.next();
      }
    } catch (XmlPullParserException xppe) {
      System.out.println(TAG + " failed to get next element: " + xppe.getMessage());
      return units;
    } catch (IOException ioe) {
      System.out.println(TAG + " failed to get next element: " + ioe.getMessage());
      return units;
    }

    return units;
  }

  private void handleStartTag() {

    String startName = xpp.getName();
    switch (startName) {
      case "selection":
        pushTag(NONE);
        pushNumber(null);
        handleSelectionTag();
        break;
      case "category":
        handleCategoryTag();
        break;
      case "rule":
        pushTag(NONE);
        pushNumber(null);
        handleRuleTag();
        break;
      case "profile":
        pushTag(NONE);
        pushNumber(null);
        handleProfileTag();
        break;
      case "description":
        handleDescriptionTag();
        break;
      case "characteristic":
        handleCharacteristicTag();
        break;
      case "cost":
        handleCost();
        break;
    }
  }

  int currentPl;
  int currentPts;

  private void handleCost() {
    // if (currentTag().equals(UNIT)) {
    if (currentUnit != null) {
      String costName = xpp.getAttributeValue(ns, "name");
      if ("pts".equals(costName)) {
        String costValue = xpp.getAttributeValue(ns, "value");
        float f = Float.parseFloat(costValue);
        System.out.println(TAG + " updating unit " + currentUnit.name + " points: " + f); // inProgress);
        currentPts += f;
      } else if (" PL".equals(costName)) {
        String costValue = xpp.getAttributeValue(ns, "value");
        float f = Float.parseFloat(costValue);
        System.out.println(TAG + " updating unit " + currentUnit.name + " PL: " + f); // inProgress);
        currentPl += f;
      }
    }
  }

  private void handleEndTag() {

    String endName = xpp.getName();
    switch (endName) {
      case "selection":
        String s = popTag();
        popNumber();

        /* we are exiting the depth at which this number was valid
        if (numberDepth == selectionDepth) {
          numberOf = 0;
        }
        */

        selectionDepth--;
        if (selectionDepth == 0 || (selectionDepth == 1 && isTheEight)) {
          System.out.println(TAG + " closing out unit " + currentUnit.name + " points: " + currentPts); // inProgress);
          currentUnit.pl = currentPl;
          currentUnit.pts = currentPts;
          currentPl = 0;
          currentPts = 0;
          inProgress = NONE;
          if (selectionDepth == 0 && isTheEight) {
            isTheEight = false;
          } else {
            // had to allow "upgrade" selections, so cleanup may be needed
            if (currentUnit.subUnits.size() == 0) {
              // remove units with no units
              units.remove(currentUnit);
              if (currentUnit.rules.size() > 0) {
                // if unit had rules, add them to army rule list
                for (Rule r : currentUnit.rules)
                  rulesUnit.rules.add(r);
              }
            }
            currentUnit = null;
          }
        }
        break;
      case "category":
        break;
      case "rule":
        popTag();
        popNumber();
        break;
      case "profile":
        popTag();
        popNumber();
        break;
      case "description":
        break;
      case "characteristic":
        break;
    }

    if ("selection".equals(endName)) {

    }
  }

  private void handleSelectionTag() {

    // some units nest unit profile tags inside of additional selection tags
    selectionDepth++;

    /*
    // need to grab the count here, it isn't attached to weapons
    String numberString = xpp.getAttributeValue(ns, "number");
    if (numberString != null && !numberString.isEmpty()) {
      numberOf = Integer.valueOf(numberString);
      numberDepth = selectionDepth;
    }

    // just in case...
    String selectionName = xpp.getAttributeValue(ns, "name");
    if (selectionName != null && selectionName.startsWith("2x")) {
      numberOf = 2;
      numberDepth = selectionDepth;
    }

    // sigh...
    if (currentUnit != null) {
      for (SubUnit su : currentUnit.subUnits) {
        if (su.name.equals(selectionName)) {
          if (numberOf > 0) {
            su.numberOf += numberOf;
          } else {
            su.numberOf += 1;
          }
        }
      }
    }
    */

    String numberString = xpp.getAttributeValue(ns, "number");
    if (numberString != null && !numberString.isEmpty()) {
      reviseNumber(Integer.parseInt(numberString));
    }

    // just in case...
    String selectionName = xpp.getAttributeValue(ns, "name");
    if (selectionName != null && selectionName.startsWith("2x")) {
      reviseNumber(2);
    }

    // try to intelligently assign counts
    SubUnit su = lookupSubUnit(selectionName);
    if (su != null && peekNumber() != null && peekNumber() > su.numberOf) {
      System.out.println("COUNTING - updating count of " + su.name + " to " + peekNumber());
      su.numberOf = peekNumber();
    }

    if ((selectionDepth == 1 && !isTheEight) ||
        (selectionDepth == 2 && isTheEight)) {
      String selectionType = xpp.getAttributeValue(ns, "type");
      if ("model".equals(selectionType) ||
          "unit".equals(selectionType) ||
          ("upgrade".equals(selectionType) && currentUnit == null)) {

        // units in the eight are nested one level deeper
        if ("The Eight".equals(selectionName)) {
          isTheEight = true;
        } else {
          currentUnit = new Unit(selectionName);
          units.add(currentUnit);
          renameTag(UNIT);

          // reset characteristics map
          characteristic1 = "";
          characteristic2 = "";
          characteristic3 = "";
        }
      } else {
        String unknownName = xpp.getAttributeValue(ns, "name");
        System.out.println(TAG + " NON-UNIT UPGRADE " + unknownName);
      }
    } else if ((selectionDepth > 1 && !isTheEight) ||
        (selectionDepth > 2 && isTheEight)) {
      String selectionType = xpp.getAttributeValue(ns, "type");
      if ("model".equals(selectionType) ||
          "unit".equals(selectionType)) {
        String unitName = xpp.getAttributeValue(ns, "name");
        //renameTag(unitName);
      }
    }
  }

  private void handleCategoryTag() {

    String categoryName = xpp.getAttributeValue(ns, "name");
    if (currentUnit != null) {
      // "Warlord" shows up in various places, need to make sure we're processing an actual unit
      if ("Warlord".equals(categoryName) && !NONE.equals(inProgress) && selectionDepth > 0) {
        currentUnit.warlord = true;
      }
      currentUnit.categories.add(categoryName);
    }
  }

  private void handleRuleTag() {

    String ruleName = xpp.getAttributeValue(ns, "name");
    currentRule = new Rule(ruleName);
    // if we're not parsing a unit, collect rules to display separately
    if (isTheEight && currentUnit != null && selectionDepth > 1) {
      currentUnit.rules.add(currentRule);
    } else if (!isTheEight && currentUnit != null && selectionDepth > 0) {
      currentUnit.rules.add(currentRule);
    } else {
      rulesUnit.rules.add(currentRule);
    }
    inProgress = RULE;

    renameTag(RULE);

  }

  private void handleProfileTag() {

    String typeName = xpp.getAttributeValue(ns, "typeName");
    // in general, we only care about these tags while parsing a unit
    switch (typeName) {
      case "Psyker":
        String psykerName = xpp.getAttributeValue(ns, "name");
        if (currentUnit != null) {
          currentPsyker = new Psyker();
          currentUnit.psyker = currentPsyker;
          inProgress = PSYKER;

          renameTag(PSYKER);

        }
        break;
      case "Psychic Power":
        String powerName = xpp.getAttributeValue(ns, "name");
        if (currentUnit != null) {
          currentPower = new Power(powerName);
          currentUnit.powers.add(currentPower);
          inProgress = POWER;

          renameTag(POWER);

        }
        break;
      case "Unit":
        String unitName = xpp.getAttributeValue(ns, "name");
        if (currentUnit != null) {
          currentSubUnit = new SubUnit(unitName);

          currentSubUnit.numberOf = currentNumber();

          //if (numberOf > 0) {
          //  currentSubUnit.numberOf = numberOf;
          //} else {
          //  currentSubUnit.numberOf = 1;
          //}


          currentUnit.subUnits.add(currentSubUnit);

          System.out.println("MERGE - adding " + unitName);

          inProgress = SUBUNIT;

          renameTag(SUBUNIT);

        } else {
          System.out.println("MERGE - not adding " + unitName);
        }
        break;
      case "Weapon":
        String weaponName = xpp.getAttributeValue(ns, "name");
        System.out.println("PARSING WEAPON " + currentNumber() + "x " + weaponName + " OWNED BY " + currentTag());
        if (currentUnit != null) {
          currentWeapon = new Weapon(weaponName);

          currentWeapon.numberOf = currentNumber();

          /*
          if (numberOf > 0) {
            if (smartPeek().equals(UNIT)) {
              if (numberOf == 1 && maxSubUnitCount() > 1) {
                System.out.println("PARSING WEAPON (" + totalSubUnitCount() + "x) " + weaponName + " OWNED BY " + currentUnit.name);
                currentWeapon.numberOf = totalSubUnitCount();
              } else {
                System.out.println("PARSING WEAPON " + numberOf + "x " + weaponName + " OWNED BY " + currentUnit.name);
                currentWeapon.numberOf = numberOf;
              }
            } else {
              System.out.println("PARSING WEAPON " + numberOf + "x " + weaponName + " OWNED BY " + smartPeek());
              currentWeapon.numberOf = numberOf;
            }
          } else {
            currentWeapon.numberOf = 1;
            if (smartPeek().equals(UNIT)) {
              System.out.println("PARSING WEAPON " + "(???) " + weaponName + " OWNED BY " + currentUnit.name);
              currentWeapon.numberOf = 999;
            } else {
              System.out.println("PARSING WEAPON (" + 1 + "x) " + weaponName + " OWNED BY " + smartPeek());
              currentWeapon.numberOf = 1;
            }
          }
          */

          currentUnit.weapons.add(currentWeapon);
          inProgress = WEAPON;

          renameTag(WEAPON);

        } else {
          System.out.println("UNATTACHED WEAPON " + currentNumber() + "x " + weaponName + " OWNED BY " + currentTag());
        }
        break;
      case "Abilities":
        String ruleName = xpp.getAttributeValue(ns, "name");
        if (currentUnit != null) {
          if (isTheEight) {
            System.out.println("EIGHT - UNIT: " + currentUnit.name);
          }
          currentRule = new Rule(ruleName);
          currentUnit.rules.add(currentRule);
          inProgress = RULE;

          renameTag(RULE);

        } else if (isTheEight) {
          System.out.println("EIGHT - ABILITY: " + ruleName);
          currentRule = new Rule(ruleName);
          rulesUnit.rules.add(currentRule);
          inProgress = RULE;

          renameTag(RULE);

        }
        break;
      case "Tally":
        // weird epidemius thing
        String tallyName = "Tally " + xpp.getAttributeValue(ns, "name");
        if (currentUnit != null) {
          currentRule = new Rule(tallyName);
          currentUnit.rules.add(currentRule);
          inProgress = RULE;

          renameTag(RULE);

        }
        break;
      case "Warp Vortex - D6 Roll":
        // weird mutalith thing
        String vortexName = xpp.getAttributeValue(ns, "name");
        if (vortexName.equals("-")) {
          vortexName = "Warp Vortex";
        }
        if (currentUnit != null) {
          currentRule = new Rule(vortexName);
          currentUnit.rules.add(currentRule);
          inProgress = RULE;

          renameTag(RULE);

        }
        break;
      case "Forge World Dogma":
        // less weird admech thing
        String dogmaName = xpp.getAttributeValue(ns, "name");
        currentRule = new Rule(dogmaName);
        if (currentUnit != null) {
          currentUnit.rules.add(currentRule);
        } else {
          rulesUnit.rules.add(currentRule);
        }
        inProgress = RULE;
        renameTag(RULE);
      default:
        // need to catch inconsistent damage track tags
        if (typeName != null && (typeName.contains("Wound") || typeName.contains("Damage"))) {
          String damageName = xpp.getAttributeValue(ns, "name");
          if (currentUnit != null) {
            currentDamage = new Damage(damageName);
            currentUnit.damages.add(currentDamage);
            inProgress = DAMAGE;

            renameTag(DAMAGE);

          }
        }
    }
  }

  private void handleDescriptionTag() {

    try {
      String ruleDescription = xpp.nextText();
      currentRule.description = ruleDescription;
    } catch (XmlPullParserException xppe) {
      System.out.println(TAG + " failed to get next element: " + xppe.getMessage());
    } catch (IOException ioe) {
      System.out.println(TAG + " failed to get next element: " + ioe.getMessage());
    }
  }

  private void handleCharacteristicTag() {

    String s = currentTag();

    String characteristicName = xpp.getAttributeValue(ns, "name");
    try {
      switch(s) {
      //switch (inProgress) {
        case SUBUNIT:
          switch (characteristicName) {
            case "M":
              currentSubUnit.m = xpp.nextText();
              break;
            case "WS":
              currentSubUnit.ws = xpp.nextText();
              break;
            case "BS":
              currentSubUnit.bs = xpp.nextText();
              break;
            case "S":
              currentSubUnit.s = xpp.nextText();
              break;
            case "T":
              currentSubUnit.t = xpp.nextText();
              break;
            case "W":
              currentSubUnit.w = xpp.nextText();
              break;
            case "A":
              currentSubUnit.a = xpp.nextText();
              break;
            case "Ld":
              currentSubUnit.ld = xpp.nextText();
              break;
            case "Save":
              currentSubUnit.save = xpp.nextText();
              break;
          }
          break;
        case PSYKER:
          switch (characteristicName) {
            case "Cast":
              currentPsyker.cast = xpp.nextText();
              break;
            case "Deny":
              currentPsyker.deny = xpp.nextText();
              break;
            case "Powers Known":
              currentPsyker.powersKnown = xpp.nextText();
              break;
            case "Other":
              currentPsyker.other = xpp.nextText();
              break;
          }
          break;
        case DAMAGE:
          handleDamageTrack(characteristicName);
          break;
        case WEAPON:
          switch (characteristicName) {
            case "Range":
              currentWeapon.range = xpp.nextText();
              break;
            case "Type":
              currentWeapon.type = xpp.nextText();
              break;
            case "S":
              currentWeapon.s = xpp.nextText();
              break;
            case "AP":
              currentWeapon.ap = xpp.nextText();
              break;
            case "D":
              currentWeapon.d = xpp.nextText();
              break;
            case "Abilities":
              currentWeapon.abilities = xpp.nextText();
              break;
          }
          break;
        case POWER:
          switch (characteristicName) {
            case "Warp Charge":
              currentPower.warpCharge = xpp.nextText();
              break;
            case "Range":
              currentPower.range = xpp.nextText();
              break;
            case "Details":
              currentPower.details = xpp.nextText();
              break;
          }
          break;
        case RULE:
          switch (characteristicName) {
            case "Description":
              currentRule.description = xpp.nextText();
              if (isTheEight) {
                System.out.println("EIGHT - DESCRIPTION: " + currentRule.description);
              }
              break;
            case "Effect":
              // weird epidemius thing
              // weird mutalith thing
              currentRule.description = xpp.nextText();
              break;
          }
          break;
      }
    } catch (XmlPullParserException xppe) {
      System.out.println(TAG + " failed to get next element: " + xppe.getMessage());
    } catch (IOException ioe) {
      System.out.println(TAG + " failed to get next element: " + ioe.getMessage());
    }
  }

  private void handleDamageTrack(String characteristicName) {

    System.out.println(TAG + " - DMG - raw: " + characteristicName);

    String s = "";
    // damage tracks are a mess, try to match characteristic names to unit stats or missing values in unit data
    try {
      switch (characteristicName) {
        case "Characteristic 1":
          if (characteristic1.isEmpty()) {
            s = xpp.nextText();
            if (Character.isDigit(s.charAt(0))) {
              // got track values with no map, need to build one
              buildCharacteristicMap(currentSubUnit);
              characteristicName = characteristic1;
            } else {
              characteristic1 = s;
            }
          } else {
            characteristicName = characteristic1;
          }
          break;
        case "Characteristic 2":
          if (characteristic2.isEmpty()) {
            s = xpp.nextText();
            if (Character.isDigit(s.charAt(0))) {
              // got track values with no map, need to build one
              buildCharacteristicMap(currentSubUnit);
              characteristicName = characteristic2;
            } else {
              characteristic2 = s;
            }
          } else {
            characteristicName = characteristic2;
          }
          break;
        case "Characteristic 3":
          if (characteristic3.isEmpty()) {
            s = xpp.nextText();
            if (Character.isDigit(s.charAt(0))) {
              // got track values with no map, need to build one
              buildCharacteristicMap(currentSubUnit);
              characteristicName = characteristic3;
            } else {
              characteristic3 = s;
            }
          } else {
            characteristicName = characteristic3;
          }
          break;
      }

      if (s.isEmpty()) {
        s = xpp.nextText();
      }

      System.out.println(TAG + " - DMG - mapped: " + characteristicName);

      switch (characteristicName) {
        case "Movement":
        case "M":
          currentDamage.m = s;
          break;
        case "WS":
          currentDamage.ws = s;
          break;
        case "BS":
          currentDamage.bs = s;
          break;
        case "S":
          currentDamage.s = s;
          break;
        case "T":
          currentDamage.t = s;
          break;
        case "W":
          currentDamage.w = s;
          break;
        case "Attacks":
        case "A":
          currentDamage.a = s;
          break;
        case "Ld":
          currentDamage.ld = s;
          break;
        case "Save":
          currentDamage.save = s;
          break;
        case "Relics":
          // weird st. katherine thing
          currentDamage.remaining = currentDamage.remaining + ", " + s + " Relics";
          break;
        case "Additional attacks":
          // weird disco lord thing
          currentDamage.a = "+" + s;
          break;
        case "Snapping Claws":
          // weird keeper of secrets thing
          currentDamage.remaining = currentDamage.remaining + ", " + s + " Claws";
          break;
        case "Psychic Test Bonus":
          // weird lord of change thing
          if (s.length() > 1) {
            currentDamage.remaining = currentDamage.remaining + ", " + s + " Psychic";
          } else {
            currentDamage.remaining = currentDamage.remaining + ", +" + s + " Psychic";
          }
          break;
        case "Void Shield":
          // weird titan thing
          currentDamage.remaining = currentDamage.remaining + ", " + s + " Void";
          break;
        case "Host of Plagues":
          // weird mortarion thing
          currentDamage.remaining = currentDamage.remaining + ", " + s + " Plague";
          break;
        case "Vortex Power":
          // weird mutalith thing
          currentDamage.remaining = currentDamage.remaining + ", " + s + " Vortex";
          break;
        case "Psychic Overload":
          // weird maleceptor thing
          currentDamage.remaining = currentDamage.remaining + ", " + s;
          break;
        default:
          // need to catch inconsistent remaining wounds tags
          if (characteristicName.contains("Remaining")) {
            currentDamage.remaining = s;
          }
      }
    } catch (XmlPullParserException xppe) {
      System.out.println(TAG + " failed to get next element: " + xppe.getMessage());
    } catch (IOException ioe) {
      System.out.println(TAG + " failed to get next element: " + ioe.getMessage());
    }
  }

  private void buildCharacteristicMap (SubUnit su) {
    if (su.m.equals("*")) {
      setCharacteristic("M");
    }
    if (su.ws.equals("*")) {
      setCharacteristic("WS");
    }
    if (su.bs.equals("*")) {
      setCharacteristic("BS");
    }
    if (su.s.equals("*")) {
      setCharacteristic("S");
    }
    if (su.a.equals("*")) {
      setCharacteristic("A");
    }
  }

  private void setCharacteristic(String c) {
    if (characteristic1.isEmpty()) {
      characteristic1 = c;
      System.out.println(TAG + " - DMG - build map, c1: " + c);
    } else if (characteristic2.isEmpty()) {
      characteristic2 = c;
      System.out.println(TAG + " - DMG - build map, c2: " + c);
    } else if (characteristic3.isEmpty()) {
      characteristic3 = c;
      System.out.println(TAG + " - DMG - build map, c3: " + c);
    }
  }

  private void pushTag(String tag) {
    tagDepth++;
    tagStack.put(tagDepth, tag);
    //System.out.println("STACK - push " + tag + ", depth " + tagDepth);
  }

  private void renameTag(String tag) {
    tagStack.put(tagDepth, tag);
    //System.out.println("STACK - rename " + tag + ", depth " + tagDepth);
  }

  private String popTag() {
    String s = tagStack.remove(tagDepth);
    //System.out.println("STACK - pop " + s + ", depth " + tagDepth);
    tagDepth--;
    return s;
  }

  private String peekTag() {
    String s = tagStack.get(tagDepth);
    return s;
  }

  private String currentTag() {
    int i = tagDepth;
    String s = tagStack.get(i);
    while (s != null && s.equals(NONE)) {
      i--;
      s = tagStack.get(i);
    }
    if (s == null) {
      s = "???";
    }
    return s;
  }

  private void pushNumber(Integer number) {
    numberDepth++;
    numberStack.put(numberDepth, number);
    //System.out.println("STACK - push " + number + ", depth " + numberDepth);
  }

  private void reviseNumber(Integer number) {
    numberStack.put(numberDepth, number);
    //System.out.println("STACK - revise " + number + ", depth " + numberDepth);
  }

  private Integer popNumber() {
    Integer i = numberStack.remove(numberDepth);
    //System.out.println("STACK - pop " + i + ", depth " + numberDepth);
    numberDepth--;
    return i;
  }

  private Integer peekNumber() {
    Integer i = numberStack.get(numberDepth);
    return i;
  }

  private Integer currentNumber() {
    int d = numberDepth;
    Integer i = numberStack.get(d);
    while (i == null && d >= 0) {
      d--;
      i = numberStack.get(d);
    }
    if (i == null) {
      i = 999;
    }
    return i;
  }

  private int maxSubUnitCount() {
    int max = 1;
    for (SubUnit su : currentUnit.subUnits) {
      if (su.numberOf > max) {
        max = su.numberOf;
      }
    }
    return max;
  }

  private int totalSubUnitCount() {
    int total = 0;
    for (SubUnit su : currentUnit.subUnits) {
      total += su.numberOf;
    }
    return total;
  }

  private SubUnit lookupSubUnit(String name) {
    if (currentUnit == null) {
      return null;
    } else {
      for (SubUnit su : currentUnit.subUnits) {
        if (su.name.equals(name)) {
          return su;
        }
      }
    }
    return null;
  }
}
