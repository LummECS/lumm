package com.sidereal.lumm.architecture.concrete;

import java.util.List;

import com.sidereal.lumm.architecture.LummConfiguration;
import com.sidereal.lumm.architecture.LummModule;

/**
 * Conveniance class to be extended when the user does not want to have the
 * entire {@link LummModule} API visible, the user being able to override
 * certain parts of the API if necessary.
 *
 * @author Claudiu Bele
 */
public class ConcreteLummModule extends LummModule {

    public ConcreteLummModule(LummConfiguration config) {
        super(config);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onUpdate() {

    }

    @Override
    public List<Class<? extends LummModule>> getDependencies() {
        return null;
    }


}
