package com.unhappyrobot.input;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.unhappyrobot.entities.GameGrid;
import com.unhappyrobot.entities.GameLayer;

import java.util.List;

public class ToolBase implements GestureDetector.GestureListener {
  protected final OrthographicCamera camera;
  protected final List<GameLayer> gameLayers;

  public ToolBase(OrthographicCamera camera, List<GameLayer> gameLayers) {
    this.camera = camera;
    this.gameLayers = gameLayers;
  }

  public GameGrid getGameGrid() {
    for (GameLayer gameLayer : gameLayers) {
      if (gameLayer instanceof GameGrid) {
        return (GameGrid) gameLayer;
      }
    }

    return null;
  }

  public boolean touchDown(int x, int y, int pointer) {
    return false;
  }

  public boolean tap(int x, int y, int count) {
    return false;
  }

  public boolean longPress(int x, int y) {
    return false;
  }

  public boolean fling(float velocityX, float velocityY) {
    return false;
  }

  public boolean pan(int x, int y, int deltaX, int deltaY) {
    return false;
  }

  public boolean zoom(float originalDistance, float currentDistance) {
    return false;
  }

  public void cleanup() {

  }
}
