package com.brainict.smartwave.model;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class DayCheckListModel implements Serializable {
  private ArrayList<DayCheckModel> checkModels;
}
