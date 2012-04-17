/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.entities;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.google.common.eventbus.Subscribe;
import com.happydroids.droidtowers.TowerAssetManager;
import com.happydroids.droidtowers.TowerConsts;
import com.happydroids.droidtowers.WeatherService;
import com.happydroids.droidtowers.events.GameGridResizeEvent;
import com.happydroids.droidtowers.events.WeatherStateChangeEvent;
import com.happydroids.droidtowers.grid.GameGrid;
import com.happydroids.droidtowers.tween.GameObjectAccessor;
import com.happydroids.droidtowers.tween.TweenSystem;
import com.happydroids.droidtowers.utils.Random;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

public class CloudLayer extends GameLayer {
  public static final int PADDING = TowerConsts.GAME_WORLD_PADDING;
  public static final int CLOUD_SPAWN_DELAY = 2;
  public static final String CLOUDS_ATLAS = "backgrounds/clouds.txt";
  public double CLOUD_SPAWN_MIN = 0.6;
  public double CLOUD_SPAWN_MAX = 0.98;
  public static final int MAX_ACTIVE_CLOUDS = 40;
  private final TextureAtlas textureAtlas;
  private float timeSinceSpawn;
  protected Vector2 worldSize;
  private List<GameObject> cloudsToRemove;
  private final int numberOfCloudTypes;
  private final WeatherService weatherService;

  public CloudLayer(GameGrid gameGrid, WeatherService weatherService) {
    super();
    this.weatherService = weatherService;

    textureAtlas = TowerAssetManager.textureAtlas(CLOUDS_ATLAS);
    numberOfCloudTypes = textureAtlas.getRegions().size();
    cloudsToRemove = new ArrayList<GameObject>(5);

    if (gameGrid != null) {
      gameGrid.events().register(this);
    }

    if (weatherService != null) {
      weatherService.events().register(this);
    }
  }

  @Override
  public void update(float timeDelta) {
    super.update(timeDelta);

    removeDeadClouds();
    spawnCloudWhenItsTime(timeDelta);
  }

  private void spawnCloudWhenItsTime(float timeDelta) {
    timeSinceSpawn += timeDelta;

    if (timeSinceSpawn > CLOUD_SPAWN_DELAY && gameObjects.size() < MAX_ACTIVE_CLOUDS) {
      timeSinceSpawn = 0;

      spawnCloudNow();
    }
  }

  protected void spawnCloudNow() {
    AtlasRegion cloudRegion = textureAtlas.findRegion("cloud", Random.randomInt(1, numberOfCloudTypes));

    float scale = Math.max(0.4f, Random.randomFloat());
    float cloudX = (cloudRegion.getRegionWidth() * scale) + PADDING;

    GameObject cloud = new GameObject(cloudRegion);

    if (weatherService != null) {
      switch (weatherService.currentState()) {
        case RAINING:
          cloud.setColor(Color.DARK_GRAY);
          break;

        case SUNNY:
          cloud.setColor(Color.WHITE);
          break;
      }
    }

    cloud.setPosition(-cloudX, Random.randomInt(worldSize.y * CLOUD_SPAWN_MIN, worldSize.y * CLOUD_SPAWN_MAX));
    cloud.setVelocity(Random.randomInt(5, 25), 0);

    cloud.setOpacity(0);
    Tween.to(cloud, GameObjectAccessor.OPACITY, 2000).target(1.0f).start(TweenSystem.getTweenManager());

    addChild(cloud);
  }

  private void removeDeadClouds() {
    if (cloudsToRemove.size() > 0) {
      gameObjects.removeAll(cloudsToRemove);
      cloudsToRemove.clear();
    }

    for (final GameObject cloud : gameObjects) {
      if (cloud.getX() >= worldSize.x + PADDING) {
        Tween.to(cloud, GameObjectAccessor.OPACITY, 2000)
                .target(0f)
                .setCallback(new TweenCallback() {
                  public void onEvent(int eventType, BaseTween source) {
                    markForRemoval(cloud);
                  }
                })
                .setCallbackTriggers(TweenCallback.COMPLETE)
                .start(TweenSystem.getTweenManager());
      }
    }
  }

  private void markForRemoval(GameObject cloud) {
    cloudsToRemove.add(cloud);
  }

  @Subscribe
  public void GameGrid_onResize(GameGridResizeEvent event) {
    worldSize = event.gameGrid.getWorldSize();
  }

  @Subscribe
  public void WeatherService_onWeatherChange(WeatherStateChangeEvent event) {
    Color cloudColor = null;

    switch (weatherService.currentState()) {
      case RAINING:
        cloudColor = Color.DARK_GRAY;
        break;

      case SUNNY:
        cloudColor = Color.WHITE;
        break;
    }

    if (cloudColor != null) {
      for (GameObject cloud : gameObjects) {
        Tween.to(cloud, GameObjectAccessor.COLOR, TowerConsts.WEATHER_SERVICE_STATE_CHANGE_DURATION)
                .target(cloudColor.r, cloudColor.g, cloudColor.b, cloudColor.a)
                .start(TweenSystem.getTweenManager());
      }
    }
  }

  public void setWorldSize(Vector2 worldSize) {
    this.worldSize = worldSize;
  }
}