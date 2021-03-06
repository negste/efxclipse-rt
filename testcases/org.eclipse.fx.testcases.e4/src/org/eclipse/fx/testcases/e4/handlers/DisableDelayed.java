package org.eclipse.fx.testcases.e4.handlers;

import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;

public class DisableDelayed {
	@Execute
	public void execute(MApplication application, IEventBroker broker, @Optional MPart part) {
		Timer t = new Timer(true);
		t.schedule(new TimerTask() {
			
			@Override
			public void run() {
				Platform.runLater(new Runnable() {
					
					@Override
					public void run() {
						new Handler0().execute(application, part);
						new Handler1().execute(application, broker, part);
					}
				});
			}
		}, 3000);
	}
}
