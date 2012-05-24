/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.grid;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.happydroids.droidtowers.TowerConsts;
import com.happydroids.droidtowers.entities.GridObject;
import com.happydroids.droidtowers.events.GameGridResizeEvent;
import com.happydroids.droidtowers.events.GridObjectBoundsChangeEvent;
import com.happydroids.droidtowers.events.GridObjectPlacedEvent;
import com.happydroids.droidtowers.events.GridObjectRemovedEvent;
import com.happydroids.droidtowers.math.GridPoint;

import java.util.HashSet;
import java.util.Set;

public class GridPositionCache {
  private GridPosition[][] gridPositions;
  private GridPoint gridSize;
  private final GameGrid gameGrid;
  private float[][] noiseLevels;

  public GridPositionCache(GameGrid gameGrid) {
    this.gameGrid = gameGrid;
    this.gameGrid.events().register(this);
  }

  @Subscribe
  public void handleGameGridResizeEvent(GameGridResizeEvent event) {
    if (gridSize != null && gridSize.equals(gameGrid.getGridSize())) {
      return;
    }

    boolean copyExisting = false;
    GridPosition[][] oldPositions = null;
    if (event.copyGridPositions) {
      if (gridSize != null && gridPositions != null) {
        oldPositions = gridPositions;
        copyExisting = true;
      }
    }

    gridSize = gameGrid.getGridSize().cpy();
    gridPositions = new GridPosition[gridSize.x + 1][gridSize.y + 1];

    if (copyExisting) {
      for (int x = 0; x < oldPositions.length; x++) {
        System.arraycopy(oldPositions[x], 0, gridPositions[x], 0, oldPositions[x].length);
      }
    }

    for (int x = 0; x <= gridSize.x; x++) {
      for (int y = 0; y <= gridSize.y; y++) {
        if (gridPositions[x][y] == null) {
          gridPositions[x][y] = new GridPosition(x, y);
        }
      }
    }
  }

  private void addGridObjectToPosition(GridObject gridObject) {
    for (int x = gridObject.getPosition().x; x < gridObject.getPosition().x + gridObject.getSize().x; x++) {
      for (int y = gridObject.getPosition().y; y < gridObject.getPosition().y + gridObject.getSize().y; y++) {
        GridPosition gridPosition = getPosition(x, y);
        if (gridPosition != null) {
          getPosition(x, y).add(gridObject);
        }
      }
    }
  }

  private void removeGridObjectFromPosition(GridObject gridObject, GridPoint position, GridPoint size) {
    for (int x = position.x; x < position.x + size.x; x++) {
      for (int y = position.y; y < position.y + size.y; y++) {
        GridPosition gridPosition = getPosition(x, y);
        if (gridPosition != null) {
          gridPosition.remove(gridObject);
        }
      }
    }
  }

  @Subscribe
  public void GameGrid_onGridObjectPlaced(GridObjectPlacedEvent event) {
    GridObject gridObject = event.gridObject;
    if (!gridObject.isPlaced()) {
      return;
    }

    addGridObjectToPosition(gridObject);
  }

  @Subscribe
  public void GameGrid_onGridObjectBoundsChange(GridObjectBoundsChangeEvent event) {
    GridObject gridObject = event.gridObject;
    if (!gridObject.isPlaced()) {
      return;
    }

    removeGridObjectFromPosition(gridObject, event.prevPosition, event.prevSize);

    addGridObjectToPosition(gridObject);
  }

  @Subscribe
  public void GameGrid_onGridObjectRemoved(GridObjectRemovedEvent event) {
    removeGridObjectFromPosition(event.gridObject, event.gridObject.getPosition(), event.gridObject.getSize());
  }

  private GridPosition getObjectSetForPosition(GridPoint gridPoint) {
    return !checkBounds(gridPoint.x, gridPoint.y) ? null : gridPositions[gridPoint.x][gridPoint.y];
  }

  public Set<GridObject> getObjectsAt(GridPoint position, GridPoint size, GridObject... gridObjectsToIgnore) {
    Set<GridObject> objects = new HashSet<GridObject>();

    int maxX = Math.min(gridSize.x, position.x + size.x);
    int maxY = Math.min(gridSize.y, position.y + size.y);

    GridPoint currentPos = position.cpy();
    for (int x = position.x; x < maxX; x++) {
      for (int y = position.y; y < maxY; y++) {
        currentPos.set(x, y);

        GridPosition forPosition = getObjectSetForPosition(currentPos);
        if (forPosition != null) {
          objects.addAll(forPosition.getObjects());
        }
      }
    }

    if (gridObjectsToIgnore != null) {
      objects.removeAll(Lists.newArrayList(gridObjectsToIgnore));
    }

    return objects;
  }

  public Set<GridObject> getObjectsAt(GridPoint gridPoint) {
    return getObjectsAt(gridPoint, TowerConsts.SINGLE_POINT);
  }

  public GridPosition getPosition(GridPoint gridPoint) {
    return getPosition(gridPoint.x, gridPoint.y);
  }

  public GridPosition getPosition(int x, int y) {
    return !checkBounds(x, y) ? null : gridPositions[x][y];
  }

  private boolean checkBounds(int x, int y) {
    if (x >= gridPositions.length || x < 0) {
      return false;
    } else if (y >= gridPositions[x].length || y < 0) {
      return false;
    }
    return true;
  }

  public GridPosition[][] getPositions() {
    return gridPositions;
  }

  public void updateNoiseLevels() {
    for (GridPosition[] row : gridPositions) {
      for (GridPosition position : row) {
        position.findMaxNoise();
      }
    }

    for (GridPosition[] row : gridPositions) {
      for (GridPosition position : row) {
        position.calculateNoise(gridPositions);
      }
    }
  }

  public void normalizeTransitDistances() {
    float minVal = Float.MAX_VALUE;
    float maxVal = Float.MIN_VALUE;
    for (GridPosition[] row : gridPositions) {
      for (GridPosition position : row) {
        minVal = Math.min(position.distanceFromTransit, minVal);
        maxVal = Math.max(position.distanceFromTransit, maxVal);
      }
    }

    if (maxVal != Float.MIN_VALUE) {
      for (GridPosition[] row : gridPositions) {
        for (GridPosition position : row) {
          if (position.distanceFromTransit > 5) {
            position.normalizedDistanceFromTransit = position.distanceFromTransit / maxVal;
          } else {
            position.normalizedDistanceFromTransit = 0f;
          }
        }
      }
    }
  }
}
