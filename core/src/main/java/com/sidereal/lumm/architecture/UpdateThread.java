package com.sidereal.lumm.architecture;

import com.badlogic.gdx.utils.ObjectMap.Entry;

public class UpdateThread extends Thread {

	@Override
	public void run() {

		if (!Lumm.disposed) {

			try {

				// update all modules
				for (Entry<Class<? extends LummModule>, LummModule> entry : Lumm.modules.entries()) {
					entry.value.onUpdateInternal();
				}

				
				Lumm.getScene().onUpdateInternal();
				
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				Lumm.net.logThrowable(e);
			}
		}

	}

}
