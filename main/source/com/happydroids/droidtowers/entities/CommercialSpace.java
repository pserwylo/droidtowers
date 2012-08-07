/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.entities;

import com.badlogic.gdx.math.MathUtils;
import com.google.common.collect.Lists;
import com.happydroids.droidtowers.achievements.AchievementEngine;
import com.happydroids.droidtowers.employee.JobCandidate;
import com.happydroids.droidtowers.events.EmployeeFiredEvent;
import com.happydroids.droidtowers.events.EmployeeHiredEvent;
import com.happydroids.droidtowers.grid.GameGrid;
import com.happydroids.droidtowers.gui.CommercialSpacePopOver;
import com.happydroids.droidtowers.gui.GridObjectPopOver;
import com.happydroids.droidtowers.types.CommercialType;
import com.happydroids.droidtowers.utils.Random;

import java.util.List;

public class CommercialSpace extends Room {
  private int jobsFilled;
  private long lastJobUpdateTime;
  protected List<JobCandidate> employees;

  public CommercialSpace(CommercialType commercialType, GameGrid gameGrid) {
    super(commercialType, gameGrid);
    employees = Lists.newArrayListWithCapacity(commercialType.getJobsProvided());
  }

  @Override
  public GridObjectPopOver makePopOver() {
    return new CommercialSpacePopOver(this);
  }

  public void updateJobs() {
    jobsFilled = 0;
    if (isConnectedToTransport()) {
      CommercialType commercialType = (CommercialType) getGridObjectType();

      if (Player.instance().getTotalPopulation() > commercialType.getPopulationRequired()) {
        int jobsProvided = commercialType.getJobsProvided();
        if (jobsProvided > 0) {
          jobsFilled = Random.randomInt(jobsProvided / 2, jobsProvided);
        }
      }
    }
  }

  public int getJobsFilled() {
    return employees.size();
  }

  @Override
  public float getNoiseLevel() {
    if (jobsFilled > 0) {
      return gridObjectType.getNoiseLevel() * ((float) jobsFilled / ((CommercialType) gridObjectType).getJobsProvided());
    }

    return 0;
  }

  @Override
  public int getCoinsEarned() {
    if (jobsFilled > 0 && isConnectedToTransport()) {
      return (int) Math.ceil(gridObjectType.getCoinsEarned() * getDesirability()) + getUpkeepCost();
    }

    return 0;
  }

  @Override
  public int getUpkeepCost() {
    if (employees.isEmpty()) {
      return 0;
    }

    int totalSalaries = 0;
    for (JobCandidate employee : employees) {
      totalSalaries += employee.getSalary();
    }

    return totalSalaries;
  }

  @Override
  public float getDesirability() {
    if (canEmployDroids() && getEmployees().isEmpty()) {
      return 0f;
    }

    return super.getDesirability();
  }

  public float getEmploymentLevel() {
    int jobsProvided = ((CommercialType) gridObjectType).getJobsProvided();

    if (jobsProvided > 0) {
      return MathUtils.clamp(employees.size() / (float) jobsProvided, 0, 1);
    }

    return 0;
  }

  public int getJobsProvided() {
    return ((CommercialType) gridObjectType).getJobsProvided();
  }

  @Override
  protected void checkDecals() {
    super.checkDecals();

    if (canEmployDroids()) {
      if (employees.size() == 0) {
        decalsToDraw.add(DECAL_NEEDS_DROIDS);
      } else {
        decalsToDraw.remove(DECAL_NEEDS_DROIDS);
      }
    }

    boolean unlockedJanitors = AchievementEngine.instance().findById("build5commercialspaces").hasGivenReward();
    boolean unlockedMaids = AchievementEngine.instance().findById("build8hotelroom").hasGivenReward();
    if (unlockedJanitors && unlockedMaids && getDirtLevel() >= 0.95f && !getEmployees().isEmpty()) {
      decalsToDraw.add(DECAL_DIRTY);
    } else {
      decalsToDraw.remove(DECAL_DIRTY);
    }
  }

  protected boolean canEmployDroids() {
    return true;
  }

  public void addEmployee(JobCandidate selectedCandidate) {
    employees.add(selectedCandidate);
    gameGrid.events().post(new EmployeeHiredEvent(this, selectedCandidate));
  }

  public List<JobCandidate> getEmployees() {
    return employees;
  }

  public void setEmployees(List<JobCandidate> employees) {
    this.employees.clear();

    JobCandidate employee;
    for (int i = 0, employeesSize = employees.size(); i < employeesSize; i++) {
      employee = employees.get(i);
      addEmployee(employee);
    }
  }

  public void fireAllEmployees() {
    JobCandidate employee;
    for (int i = 0, employeesSize = employees.size(); i < employeesSize; i++) {
      employee = employees.get(i);
      gameGrid.events().post(new EmployeeFiredEvent(this, employee));
    }

    employees.clear();
  }

  public void removeEmployee(JobCandidate employee) {
    employees.remove(employee);
    gameGrid.events().post(new EmployeeFiredEvent(this, employee));
  }

  @Override
  public float getDirtLevel() {
    if (canEmployDroids() && getEmployees().isEmpty()) {
      return 0;
    }

    return super.getDirtLevel();
  }
}
