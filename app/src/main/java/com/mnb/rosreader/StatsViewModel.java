package com.mnb.rosreader;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StatsViewModel extends ViewModel {

  private MutableLiveData<StatsViewState> currentState;
  private StatsViewState internalState;

  public MutableLiveData<StatsViewState> getCurrentState() {
    if (currentState == null) {
      currentState = new MutableLiveData<StatsViewState>();
    }
    return currentState;
  }

  public void initData() {

    if (internalState == null) {
      internalState = new StatsViewState();
      String[] modStrings = new String[3];
      modStrings[0] = "FOO";
      modStrings[1] = "BAR";
      modStrings[2] = "BAZ";
      internalState.setModifierStrings(modStrings);
      Boolean[] modStates = new Boolean[3];
      modStates[0] = false;
      modStates[1] = false;
      modStates[2] = false;
      internalState.setModifierStates(modStates);
      currentState.setValue(internalState);
    }
  }

  public void handleEvent(ViewEvent event) {

    switch(event.getEventTag()) {
      case StatsViewEvent.TOGGLE_EVENT:
        for (int i = 0; i < internalState.getModifierStrings().length; i++) {
          if (internalState.getModifierStrings()[i].equals(event.getEventData().get(StatsViewEvent.TOGGLE_EVENT_STRING))) {
            internalState.getModifierStates()[i] = !internalState.getModifierStates()[i];
            currentState.setValue(internalState);
            break;
          }
        }
        break;
      default:
        // ???
        break;
    }
  }

}
