package com.sidereal.lumm.architecture.core;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.sidereal.lumm.architecture.LummConfiguration;
import com.sidereal.lumm.architecture.LummModule;

public class Time extends LummModule {

    float timeScale;

    float timeInScene;

    float timeInGame;

    float deltaTime;

    public Time(LummConfiguration cfg) {
        super(cfg);
        timeScale = 1;
        timeInGame = timeInScene = 0;
    }

    public float getDeltaTime() {

        return deltaTime * timeScale;
    }

    public float getRealDeltaTime() {

        return deltaTime;
    }

    public float getTimeScale() {

        return timeScale;
    }

    public void setTimeScale(float timeScale) {
        this.timeScale = timeScale;
    }

    public float getTimeInScene() {

        return 0;
    }

    public float getTimeInGame() {

        return 0;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onUpdate() {
        deltaTime = Gdx.graphics.getRawDeltaTime();
        timeInGame += Gdx.graphics.getRawDeltaTime();
        timeInScene += Gdx.graphics.getRawDeltaTime();
    }

    @Override
    public List<Class<? extends LummModule>> getDependencies() {
        return null;
    }


}
