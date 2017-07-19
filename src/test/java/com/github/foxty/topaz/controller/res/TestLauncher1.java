package com.github.foxty.topaz.controller.res;

import com.github.foxty.topaz.annotation.Launcher;

@Launcher
public class TestLauncher1 implements Runnable {

	public boolean executed;

	@Override
	public void run() {
		executed = true;
	}

}
