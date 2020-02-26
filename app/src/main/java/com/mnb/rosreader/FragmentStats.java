package com.mnb.rosreader;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

public class FragmentStats extends Fragment implements ModAdapter.ModListInterface {

  public static String TAG = "STATS";

  private StatsViewModel model;

  View statsView;
  ModAdapter adapter;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    model = new ViewModelProvider(this).get(StatsViewModel.class);
    final Observer<StatsViewState> o = new Observer<StatsViewState>() {

      @Override
      public void onChanged(StatsViewState statsViewState) {

        updateState(statsViewState);
      }
    };
    model.getCurrentState().observe(this, o);
    model.initData();

    statsView = inflater.inflate(R.layout.fragment_stats, container, false);
    ListView list = statsView.findViewById(R.id.mod_list);

    /*
    if (savedInstanceState == null) {

      Modifier m1 = new Modifier("FOO", null);
      Modifier m2 = new Modifier("BAR", null);
      Modifier m3 = new Modifier("BAZ", null);
      ArrayList<Modifier> data = new ArrayList<Modifier>();
      data.add(m1);
      data.add(m2);
      data.add(m3);

      adapter = new ModAdapter(getActivity(), data);
    } else {
      ArrayList<Modifier> data = savedInstanceState.getParcelableArrayList(ModAdapter.LIST_TAG);
      boolean[] state = savedInstanceState.getBooleanArray(ModAdapter.STATE_TAG);

      adapter = new ModAdapter(getActivity(), data, state);
    }
    */

    adapter = new ModAdapter(getActivity(), this);
    list.setAdapter(adapter);
    return statsView;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
  }

  @Override
  public void toggleMod(String modName) {

    System.out.println("toggle: " + modName);

    StatsViewEvent event = new StatsViewEvent(StatsViewEvent.TOGGLE_EVENT);
    event.getEventData().put(StatsViewEvent.TOGGLE_EVENT_STRING, modName);
    model.handleEvent(event);
  }

  private void updateState(StatsViewState state) {

    adapter.setStrings(state.getModifierStrings());
    adapter.setStates(state.getModifierStates());
    adapter.notifyDataSetChanged();
  }





  /*
  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelableArrayList(ModAdapter.LIST_TAG, adapter.getList());
    outState.putBooleanArray(ModAdapter.STATE_TAG, adapter.getState());
  }
  */
}