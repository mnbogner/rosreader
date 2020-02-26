package com.mnb.rosreader;

import java.util.HashMap;

public class CharacterManager {

  private HashMap<String, Modifier> modifiers;
  private CharacterData character;

  public CharacterManager() {
    modifiers = new HashMap<String, Modifier>();
    character = new CharacterData();
  }

  public void addModifier(Modifier modifier) {
    if (modifiers.keySet().contains(modifier.getName())) {
      // mod already applied, no-op
      return;
    }
    character.addModifier(modifier);
    modifiers.put(modifier.getName(), modifier);
  }

  public void removeModifier(Modifier modifier) {
    if (!modifiers.keySet().contains(modifier.getName())) {
      // mod not applied, no-op
      return;
    }
    character.removeModifier(modifier);
    modifiers.put(modifier.getName(), modifier);
  }

  public CharacterView getCharacterView() {
    return new CharacterView(character);
  }

}
