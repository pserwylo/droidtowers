/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.achievements;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.Subscribe;
import com.happydroids.droidtowers.entities.GridObjectPlacementState;
import com.happydroids.droidtowers.events.GridObjectEvent;
import com.happydroids.droidtowers.gamestate.server.TowerGameService;
import com.happydroids.droidtowers.grid.GameGrid;
import com.happydroids.droidtowers.gui.AchievementNotification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AchievementEngine {
  protected static AchievementEngine instance;
  protected List<Achievement> achievements;
  private GameGrid gameGrid;

  public static AchievementEngine instance() {
    if (instance == null) {
      instance = new AchievementEngine();
    }

    return instance;
  }

  protected AchievementEngine() {
    try {
      ObjectMapper mapper = TowerGameService.instance().getObjectMapper();
      achievements = mapper.readValue(Gdx.files.internal("params/achievements.json").reader(), mapper.getTypeFactory().constructCollectionType(ArrayList.class, Achievement.class));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public List<Achievement> getAchievements() {
    return achievements;
  }

  public void checkAchievements() {
    for (Achievement achievement : achievements) {
      if (!achievement.isCompleted() && achievement.requirementsMet(gameGrid)) {
        complete(achievement);
      }
    }
  }

  public void complete(Achievement achievement) {
    if (achievement.isLocked() || achievement.isCompleted()) {
      return;
    }

    achievement.setCompleted(true);
    achievement.giveReward();

    displayNotification(achievement);
  }

  protected void displayNotification(Achievement achievement) {
    new AchievementNotification(achievement).show();
  }

  public void complete(String achievementId) {
    for (Achievement achievement : achievements) {
      if (achievementId.equalsIgnoreCase(achievement.getId())) {
        complete(achievement);
        return;
      }
    }

    throw new RuntimeException("Could not find achievement called: " + achievementId);
  }

  public void loadCompletedAchievements(List<String> achievementIds) {
    resetState();

    if (achievementIds == null) {
      return;
    }

    for (Achievement achievement : achievements) {
      if (achievementIds.contains(achievement.getId())) {
        achievement.setCompleted(true);
        achievement.giveReward();
      }
    }
  }

  public void add(Achievement achievement) {
    achievements.add(achievement);
  }

  public boolean hasGameGrid() {
    return gameGrid != null;
  }

  public void unregisterGameGrid() {
    if (gameGrid != null) {
      gameGrid.events().unregister(this);
    }

    gameGrid = null;
  }

  public void registerGameGrid(GameGrid gameGrid) {
    this.gameGrid = gameGrid;
    if (gameGrid != null) {
      gameGrid.events().register(this);
    }
  }

  public GameGrid getGameGrid() {
    return gameGrid;
  }

  @Subscribe
  public void GameEvent_handleGridObjectEvent(GridObjectEvent event) {
    if (event.gridObject.getPlacementState().equals(GridObjectPlacementState.PLACED)) {
      checkAchievements();
    }
  }

  public void resetState() {
    for (Achievement completedAchievement : achievements) {
      completedAchievement.setCompleted(false);
    }

    for (Achievement achievement : achievements) {
      achievement.resetState();
    }
  }

  public void completeAll() {
    for (Achievement achievement : achievements) {
      complete(achievement);
    }
  }

  public Achievement findById(String achievementId) {
    for (Achievement achievement : achievements) {
      if (achievement.getId().equalsIgnoreCase(achievementId)) {
        return achievement;
      }
    }

    return null;
  }
}
