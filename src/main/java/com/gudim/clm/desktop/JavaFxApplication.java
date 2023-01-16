package com.gudim.clm.desktop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFxApplication extends Application {
	@Value("${clm.title}")
	private String title;
	
	private ConfigurableApplicationContext applicationContext;
	
	@Override
	public void init() {
		String[] args = getParameters().getRaw().toArray(new String[NumberUtils.INTEGER_ZERO]);
		this.applicationContext = new SpringApplicationBuilder().headless(false).sources(
			SpringBootApplication.class).run(args);
	}
	
	@Override
	public void start(Stage stage) {
		FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
		Parent root = fxWeaver.loadView(CLMController.class);
		Scene scene = new Scene(root);
		stage.setTitle(title);
		stage.setScene(scene);
		stage.show();
	}
	
	@Override
	public void stop() {
		this.applicationContext.close();
		Platform.exit();
	}
	
}
