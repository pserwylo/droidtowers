package com.unhappyrobot.input;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.unhappyrobot.entities.GameLayer;

import java.util.List;

public class PickerTool extends ToolBase {
  public PickerTool(OrthographicCamera camera, List<GameLayer> gameLayers) {
    super(camera, gameLayers);
  }

  @Override
  public boolean longPress(int x, int y) {
    Ray pickRay = camera.getPickRay(x, y);
    Vector3 worldPoint = pickRay.getEndPoint(1);

    for (GameLayer gameLayer : gameLayers) {
      if (gameLayer.isTouchEnabled() && gameLayer.longPress(worldPoint)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean pan(int x, int y, int deltaX, int deltaY) {
    Vector3 worldPoint = camera.getPickRay(x, y).getEndPoint(1);
    Vector3 deltaPoint = camera.getPickRay(x + -deltaX, y + deltaY).getEndPoint(1);

    for (GameLayer gameLayer : gameLayers) {
      if (gameLayer.isTouchEnabled() && gameLayer.pan(worldPoint, deltaPoint)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean tap(int x, int y, int count) {
    Ray pickRay = camera.getPickRay(x, y);
    Vector3 worldPoint = pickRay.getEndPoint(1);

    for (GameLayer gameLayer : gameLayers) {
      if (gameLayer.isTouchEnabled() && gameLayer.tap(worldPoint, count)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean touchDown(int x, int y, int pointer) {
    Ray pickRay = camera.getPickRay(x, y);
    Vector3 worldPoint = pickRay.getEndPoint(1);

    for (GameLayer gameLayer : gameLayers) {
      if (gameLayer.isTouchEnabled() && gameLayer.touchDown(worldPoint, pointer)) {
        return true;
      }
    }

    return false;
  }
}
