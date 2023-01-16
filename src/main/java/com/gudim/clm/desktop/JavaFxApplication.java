package com.gudim.clm.desktop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFxApplication extends Application {
	
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
		scene.getStylesheets().add(this.getClass().getResource("/main.css").toExternalForm());
		stage.getIcons().add(new Image(this.getClass().getResourceAsStream("/clmLogo.png")));
		stage.setTitle("Consul Loot Master v0.0.1");
		stage.setScene(scene);
		stage.show();
	}
	
	@Override
	public void stop() {
		this.applicationContext.close();
		Platform.exit();
	}
	
}
