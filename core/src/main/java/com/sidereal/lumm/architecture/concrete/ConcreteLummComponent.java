package com.sidereal.lumm.architecture.concrete;

import com.sidereal.lumm.architecture.LummComponent;
import com.sidereal.lumm.architecture.LummObject;

/**
 * Conveniance class to be extended when the user does not want to have the
 * entire {@link LummComponent} API visible, the user being able to override
 * certain parts of the API if necessary.
 * 
 * @author Claudiu Bele
 */
public class ConcreteLummComponent extends LummComponent {

	public ConcreteLummComponent(LummObject obj) {
		super(obj);
	}

	@Override
	protected void onUpdate() {

	}

	@Override
	protected void onRender() {

	}

	@Override
	protected void onDebug() {

	}


}
