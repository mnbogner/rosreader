package com.mnb.rosreader;

public class StatsViewEvent extends ViewEvent {

  public final static String TOGGLE_EVENT = "toggle_event";
  public final static String TOGGLE_EVENT_STRING = "toggle_event_string";

  public StatsViewEvent(String eventTag) {
    super(eventTag);
  }

}
