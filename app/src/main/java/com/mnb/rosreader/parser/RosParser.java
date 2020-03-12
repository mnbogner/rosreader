package com.mnb.rosreader.parser;

import android.content.Context;
import android.util.Xml;

import com.mnb.rosreader.data.Damage;
import com.mnb.rosreader.data.Force;
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
  protected static final String FORCE = "force";
  protected static final String UNIT = "unit";
  protected static final String SUBUNIT = "subunit";
  protected static final String PSYKER = "psyker";
  protected static final String DAMAGE = "damage";
  protected static final String WEAPON = "weapon";
  protected static final String POWER = "power";
  protected static final String RULE = "rule";

  protected Context context;

  protected XmlPullParser xpp;

  protected ArrayList<Force> forces;
  protected Force currentForce;
  protected Unit currentUnit;
  protected SubUnit currentSubUnit;
  protected Psyker currentPsyker;
  protected Damage currentDamage;
  protected Weapon currentWeapon;
  protected Power currentPower;
  protected Rule currentRule;
  protected int selectionDepth;
  protected Integer tagDepth;
  protected HashMap<Integer, String> tagStack;
  protected Integer numberDepth;
  protected HashMap<Integer, Integer> numberStack;

  protected int currentPl;
  protected int currentPts;
  protected String characteristic1 = "";
  protected String characteristic2 = "";
  protected String characteristic3 = "";

  protected boolean isTheEight;

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
      // assuming zip contains only one file (.rosz -> .ros)
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

  public ArrayList<Force> parseRosFile(String rosFile) {
    forces = new ArrayList<Force>();
    InputStream is = openFile(rosFile);
    if (is == null) {
      System.out.println(TAG + " failed to open input stream");
      return forces;
    }
    xpp = null;
    try {
      // TODO - verify state/encoding options
      xpp = Xml.newPullParser();
      xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
      xpp.setInput(is, null);
    } catch (XmlPullParserException xppe) {
      System.out.println(TAG + " failed to create parser: " + xppe.getMessage());
      return forces;
    }
    if (xpp == null) {
      System.out.println(TAG + " failed to create parser");
      return forces;
    }

    // reset tracking variables
    currentForce = null;
    currentUnit = null;
    currentRule = null;
    currentPsyker = null;
    currentPower = null;
    currentSubUnit = null;
    currentDamage = null;
    currentWeapon = null;
    selectionDepth = 0;
    tagDepth = 0;
    tagStack = new HashMap<Integer, String>();
    numberDepth = 0;
    numberStack = new HashMap<Integer, Integer>();

    try {
      int eventType = xpp.getEventType();
      while (eventType != XmlPullParser.END_DOCUMENT) {
        // single-line tags only trigger a start event, not an end event
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
      return forces;
    } catch (IOException ioe) {
      System.out.println(TAG + " failed to get next element: " + ioe.getMessage());
      return forces;
    }
    return forces;
  }

  private void handleStartTag() {
    String startName = xpp.getName();
    // handle relevant tags, ignore the rest
    switch (startName) {
      case "force":
        pushTag(FORCE);
        pushNumber(null);
        handleForceTag();
        break;
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

  private void handleCost() {
    // accumulate power level and points for current unit
    if (currentUnit != null) {
      String costName = xpp.getAttributeValue(ns, "name");
      if ("pts".equals(costName)) {
        String costValue = xpp.getAttributeValue(ns, "value");
        float f = Float.parseFloat(costValue);
        currentPts += f;
      } else if (" PL".equals(costName)) {
        String costValue = xpp.getAttributeValue(ns, "value");
        float f = Float.parseFloat(costValue);
        currentPl += f;
      }
    }
  }

  private void handleEndTag() {
    String endName = xpp.getName();
    switch (endName) {
      case "force":
        popTag();
        popNumber();
        break;
      case "selection":
        popTag();
        popNumber();
        selectionDepth--;
        if (selectionDepth == 0 || (selectionDepth == 1 && isTheEight)) {
          if (currentUnit != null) {
            currentUnit.pl = currentPl;
            currentUnit.pts = currentPts;
          }
          // sum up detachment pl/pts
          currentForce.pl += currentPl;
          currentForce.pts += currentPts;
          currentPl = 0;
          currentPts = 0;
          if (selectionDepth == 0 && isTheEight) {
            isTheEight = false;
          } else {
            // had to allow "upgrade" selections, so cleanup may be needed
            if (currentUnit.subUnits.size() == 0) {
              // remove units with no units
              currentForce.units.remove(currentUnit);
              if (currentUnit.rules.size() > 0) {
                // if unit had rules, add them to army rule list
                for (Rule r : currentUnit.rules)
                  currentForce.rules.add(r);
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
  }

  private void handleForceTag() {
    String detachment = xpp.getAttributeValue(ns, "name");
    String faction = xpp.getAttributeValue(ns, "catalogueName");
    currentForce = new Force(faction + " " + detachment);
    forces.add(currentForce);
  }

  private void handleSelectionTag() {
    // some units nest unit profile tags inside of additional selection tags
    selectionDepth++;
    String numberString = xpp.getAttributeValue(ns, "number");
    if (numberString != null && !numberString.isEmpty()) {
      reviseNumber(Integer.parseInt(numberString));
    }
    // catch certain multiple weapons that only show a count of 1
    String selectionName = xpp.getAttributeValue(ns, "name");
    if (selectionName != null && selectionName.startsWith("2x")) {
      reviseNumber(2);
    }
    // try assign count to a profile that may already have been parsed
    SubUnit su = lookupSubUnit(selectionName);
    if (su != null && peekNumber() != null && peekNumber() > su.numberOf) {
      su.numberOf = peekNumber();
    }
    // only count a selection as a unit if it's at the proper depth
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
          currentForce.units.add(currentUnit);
          renameTag(UNIT);
          // reset characteristics map
          characteristic1 = "";
          characteristic2 = "";
          characteristic3 = "";
        }
      } else {
        String unknownName = xpp.getAttributeValue(ns, "name");
      }
    }
  }

  private void handleCategoryTag() {
    String categoryName = xpp.getAttributeValue(ns, "name");
    if (currentUnit != null) {
      // "Warlord" shows up in various places, need to make sure we're processing an actual unit
      if ("Warlord".equals(categoryName) && !UNIT.equals(currentTag()) && selectionDepth > 0) {

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
      currentForce.rules.add(currentRule);
    }
    renameTag(RULE);
  }

  private void handleProfileTag() {
    String typeName = xpp.getAttributeValue(ns, "typeName");
    // in general, we only care about these tags while parsing a unit
    switch (typeName) {
      case "Psyker":
        if (currentUnit != null) {
          currentPsyker = new Psyker();
          currentUnit.psyker = currentPsyker;
          renameTag(PSYKER);
        }
        break;
      case "Psychic Power":
        String powerName = xpp.getAttributeValue(ns, "name");
        if (currentUnit != null) {
          currentPower = new Power(powerName);
          currentUnit.powers.add(currentPower);
          renameTag(POWER);
        }
        break;
      case "Unit":
        String unitName = xpp.getAttributeValue(ns, "name");
        if (currentUnit != null) {
          currentSubUnit = new SubUnit(unitName);
          currentSubUnit.numberOf = currentNumber();
          currentUnit.subUnits.add(currentSubUnit);
          renameTag(SUBUNIT);
        }
        break;
      case "Weapon":
        String weaponName = xpp.getAttributeValue(ns, "name");
        if (currentUnit != null) {
          currentWeapon = new Weapon(weaponName);
          currentWeapon.numberOf = currentNumber();
          currentUnit.weapons.add(currentWeapon);
          renameTag(WEAPON);
        }
        break;
      case "Abilities":
        String ruleName = xpp.getAttributeValue(ns, "name");
        if (currentUnit != null) {
          currentRule = new Rule(ruleName);
          currentUnit.rules.add(currentRule);
          renameTag(RULE);
        } else if (isTheEight) {
          currentRule = new Rule(ruleName);
          currentForce.rules.add(currentRule);
          renameTag(RULE);
        }
        break;
      case "Explosion":
        // specific explosion info format
        String explosionName = xpp.getAttributeValue(ns, "name");
        if (currentUnit != null) {
          currentRule = new Rule(explosionName);
          currentUnit.rules.add(currentRule);
          renameTag(RULE);
        }
        break;
      case "Tally":
        // weird epidemius thing
        String tallyName = "Tally " + xpp.getAttributeValue(ns, "name");
        if (currentUnit != null) {
          currentRule = new Rule(tallyName);
          currentUnit.rules.add(currentRule);
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
          renameTag(RULE);
        }
        break;
      case "Power of the C'tan":
        // weird c'tan thing
        String ctanName = xpp.getAttributeValue(ns, "name");
        if (currentUnit != null) {
          currentRule = new Rule(ctanName);
          currentUnit.rules.add(currentRule);
          renameTag(RULE);
        }
        break;
      case "Forge World Dogma":
      case "Dynastic Code":
        // admech/necron thing
        String bonusName = xpp.getAttributeValue(ns, "name");
        currentRule = new Rule(bonusName);
        if (currentUnit != null) {
          currentUnit.rules.add(currentRule);
        } else {
          currentForce.rules.add(currentRule);
        }
        renameTag(RULE);
      default:
        // need to catch inconsistent damage track tags
        if (typeName != null && (typeName.contains("Wound") || typeName.contains("Damage"))) {
          String damageName = xpp.getAttributeValue(ns, "name");
          if (currentUnit != null) {
            currentDamage = new Damage(damageName);
            currentUnit.damages.add(currentDamage);
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
      // parse characteristics based on what type of element is currently in progress
      switch(s) {
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
              break;
            case "Effect":
              // weird epidemius thing
              // weird mutalith thing
              currentRule.description = xpp.nextText();
              break;
            case "Dice Roll":
              // specific explosion info format
              currentRule.roll = xpp.nextText();
              break;
            case "Distance":
              // specific explosion info format
              currentRule.distance = xpp.nextText();
              break;
            case "Mortal Wounds":
              // specific explosion info format
              currentRule.wounds = xpp.nextText();
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

  // go left to right and figure out which stats depend on the damage track
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

  // find the first unassigned characteristic
  private void setCharacteristic(String c) {
    if (characteristic1.isEmpty()) {
      characteristic1 = c;
    } else if (characteristic2.isEmpty()) {
      characteristic2 = c;
    } else if (characteristic3.isEmpty()) {
      characteristic3 = c;
    }
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

  // manage stack of relevant tag types
  private void pushTag(String tag) {
    tagDepth++;
    tagStack.put(tagDepth, tag);
  }

  private void renameTag(String tag) {
    tagStack.put(tagDepth, tag);
  }

  private String popTag() {
    String s = tagStack.remove(tagDepth);
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

  // manage stack of number/count values
  private void pushNumber(Integer number) {
    numberDepth++;
    numberStack.put(numberDepth, number);
  }

  private void reviseNumber(Integer number) {
    numberStack.put(numberDepth, number);
  }

  private Integer popNumber() {
    Integer i = numberStack.remove(numberDepth);
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
}
