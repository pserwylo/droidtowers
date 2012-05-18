/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.utils.Logger;
import com.happydroids.HappyDroidConsts;
import com.happydroids.droidtowers.gamestate.server.TowerGameService;
import com.happydroids.droidtowers.gui.FontManager;
import com.happydroids.droidtowers.pipeline.AssetList;
import com.happydroids.droidtowers.platform.Display;

import java.io.IOException;
import java.util.Map;

import static com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
import static com.badlogic.gdx.graphics.Texture.TextureFilter.MipMapNearestNearest;

public class TowerAssetManager {
  private static final String TAG = TowerAssetManager.class.getSimpleName();
  private static MemoryTrackingAssetManager assetManager;
  public static final String WHITE_SWATCH = "swatches/swatch-white.png";
  public static final String WHITE_SWATCH_TRIANGLE = "swatches/swatch-white-triangle.png";
  private static Skin skin;
  private static AssetList assetList;
  private static Skin customSkin;

  public static MemoryTrackingAssetManager assetManager() {
    if (assetManager == null) {
      assetManager = new MemoryTrackingAssetManager();
      if (HappyDroidConsts.DEBUG) {
        assetManager.getLogger().setLevel(Logger.ERROR);
      }

      Texture.setAssetManager(assetManager);

      try {
        assetList = TowerGameService.instance().getObjectMapper().readValue(Gdx.files.internal("assets.json").read(), AssetList.class);

        addToAssetManager(assetList.preloadFiles, assetList.highDefFiles);
        addToAssetManager(assetList.normalFiles, assetList.highDefFiles);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      TextureLoader.TextureParameter parameter = new TextureLoader.TextureParameter();
      parameter.genMipMaps = true;
      parameter.minFilter = MipMapNearestNearest;
      parameter.magFilter = Linear;
      assetManager.load(checkForHDPI("elevator/shaft.png"), Texture.class, parameter);
      assetManager.load(checkForHDPI("elevator/empty.png"), Texture.class, parameter);

      assetManager.setErrorListener(new AssetErrorListener() {
        public void error(String fileName, Class type, Throwable throwable) {
          throw new RuntimeException("Error loading: " + fileName, throwable);
        }
      });

      TextureAtlas skinAtlas = new TextureAtlas(Gdx.files.internal("hud/skin.txt"));

      customSkin = new Skin();

      CheckBox.CheckBoxStyle checkBoxStyle = new CheckBox.CheckBoxStyle();
      checkBoxStyle.checkboxOn = findRegion(skinAtlas, "checkbox-on");
      checkBoxStyle.checkboxOff = skinAtlas.findRegion("checkbox-off");
      checkBoxStyle.font = FontManager.Default.getFont();
      checkBoxStyle.fontColor = Color.WHITE;
      customSkin.addStyle("default", checkBoxStyle);

      Slider.SliderStyle sliderStyle = new Slider.SliderStyle(new NinePatch(new Texture(Gdx.files.internal(WHITE_SWATCH)), Color.LIGHT_GRAY), findRegion(skinAtlas, "slider-handle"));
      customSkin.addStyle("default", sliderStyle);

    }

    return assetManager;
  }

  private static TextureAtlas.AtlasRegion findRegion(TextureAtlas atlas, String name) {
    if (Display.isHDPIMode()) {
      return atlas.findRegion("hdpi/" + name);
    }

    return atlas.findRegion(name);
  }

  private static void addToAssetManager(Map<String, Class> preloadFiles, Map<String, String> highDefFiles) {
    for (Map.Entry<String, Class> entry : preloadFiles.entrySet()) {
      String fileName = entry.getKey();
      Class clazz = entry.getValue();
      if (Display.isHDPIMode() && highDefFiles.containsKey(fileName)) {
        fileName = highDefFiles.get(fileName);
      }

      assetManager.load(fileName, clazz);
    }
  }

  public static String checkForHDPI(String fileName) {
    if (Display.isHDPIMode() && assetList.highDefFiles.containsKey(fileName)) {
      return assetList.highDefFiles.get(fileName);
    }

    return fileName;
  }

  public static void dispose() {
    assetManager.dispose();
  }

  public static Skin skin(String s) {
    return assetManager().get(s, Skin.class);
  }

  public static BitmapFont bitmapFont(String s) {
    return assetManager().get(s, BitmapFont.class);
  }

  public static Sound sound(String s) {
    return assetManager().get(s, Sound.class);
  }

  public static TextureAtlas textureAtlas(String s) {
    return assetManager().get(checkForHDPI(s), TextureAtlas.class);
  }

  public static Texture texture(String s) {
    return assetManager().get(checkForHDPI(s), Texture.class);
  }

  public static TextureAtlas.AtlasRegion textureFromAtlas(String textureName, String atlasName) {
    return assetManager().get(checkForHDPI(atlasName), TextureAtlas.class).findRegion(textureName);
  }

  public static NinePatch ninePatch(String fileName) {
    return ninePatch(fileName, Color.WHITE);
  }

  public static NinePatch ninePatch(String fileName, Color color) {
    return new NinePatch(texture(fileName), color);
  }

  public static Sprite sprite(String fileName) {
    return new Sprite(texture(fileName));
  }

  public static void setGuiSkin(Skin guiSkin) {
    skin = guiSkin;
  }

  public static Skin getGuiSkin() {
    return skin;
  }

  public static ImageButton imageButton(TextureAtlas.AtlasRegion region) {
    return new ImageButton(region);
  }

  public static AssetList getAssetList() {
    return assetList;
  }

  public static Skin getCustomSkin() {
    return customSkin;
  }
}
