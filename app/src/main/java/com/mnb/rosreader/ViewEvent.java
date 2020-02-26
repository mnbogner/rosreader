package com.mnb.rosreader;

import java.util.HashMap;

public class ViewEvent {

  private String eventTag;
  private HashMap<String, Object> eventData;

  public ViewEvent(String eventTag) {
    this.eventTag = eventTag;
    this.eventData = new HashMap<String, Object>();
  }

  public String getEventTag() {
    return eventTag;
  }

  public HashMap<String, Object> getEventData() {
    return eventData;
  }

}
