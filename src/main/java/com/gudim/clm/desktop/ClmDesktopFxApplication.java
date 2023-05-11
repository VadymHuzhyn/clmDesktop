package com.gudim.clm.desktop;

import com.gudim.clm.desktop.util.CLMConstant;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;
import net.rgielen.fxweaver.core.FxWeaver;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Objects;

@Log4j2
public class ClmDesktopFxApplication extends Application {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        String[] args = getParameters().getRaw().toArray(new String[NumberUtils.INTEGER_ZERO]);
        this.applicationContext = new SpringApplicationBuilder().headless(false).sources(
                ClmDesktopApplication.class).run(args);
    }

    @Override
    public void start(Stage stage) {
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(CLMController.class);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                Objects.requireNonNull(this.getClass().getResource("/static/css/main.css"))
                        .toExternalForm());
        stage.getIcons().add(new Image(Objects.requireNonNull(
                this.getClass().getResourceAsStream("/static/image/clmLogo.png"))));
        stage.setTitle(CLMConstant.TITLE);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        this.applicationContext.close();
        Platform.exit();
    }
}
