package com.gudim.clm.desktop;

import com.google.gson.JsonElement;
import com.gudim.clm.desktop.dto.ItemList;
import com.gudim.clm.desktop.dto.Wishlist;
import com.gudim.clm.desktop.service.CLMService;
import com.gudim.clm.desktop.util.CLMConstant;
import com.gudim.clm.desktop.util.CLMUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxmlView;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@FxmlView("CLM.fxml")
public class CLMController {
    private final CLMService clmService;
    @FXML
    public Label statusBar;
    @FXML
    public Button uploadJson;
    @FXML
    public Button downloadJson;
    @FXML
    public Button directoryChooser;
    @FXML
    public Label directoryPath;
    String absolutePath;

    public CLMController(CLMService clmService) {
        this.clmService = clmService;
    }

    @FXML
    public void convertXLSToJSON() {
        String statusCode = clmService.downloadXLSXFromDrive();
        if ("200".equals(statusCode)) {
            List<Wishlist> wishlists = clmService.getWishlist();
            Map<String, List<ItemList>> itemList = clmService.getItemList();
            StringBuilder wishlistTable = clmService.generateWishlistTable(wishlists);
            StringBuilder itemListTable = clmService.generateItemListTable(itemList);
            CLMUtil.saveFile(wishlistTable, directoryPath.getText() + CLMConstant.PATH_CLM_WISHLISTS_LUA);
            CLMUtil.saveFile(itemListTable, directoryPath.getText() + CLMConstant.PATH_CLM_ITEMS_LUA);
            CLMUtil.removeFile(CLMConstant.TEMP_FILE_NAME);
            showPopup(AlertType.INFORMATION, "Data was generated", StringUtils.EMPTY);
        } else {
            showPopup(AlertType.ERROR, "File not found", "Please check the correct account and if you have access to the file. if not, contact the owner of the file and request access.");
        }

    }

    @FXML
    public void convertJSONTonXLS() {
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CLM file", "CLM.lua");
        chooser.getExtensionFilters().add(extFilter);
        Path path = Paths.get(absolutePath + "\\WTF\\Account");
        chooser.setInitialDirectory(path.toFile());
        File file = chooser.showOpenDialog(new Stage());
        if (file != null && Files.exists(file.toPath()) && file.getAbsolutePath().endsWith("SavedVariables\\CLM.lua")) {
            JsonElement jsonElement = CLMUtil.getLuaTable(file.getAbsolutePath(), "CLMUD");
            List<Wishlist> wishlist = clmService.getWishlist(jsonElement);
            String statusCode = clmService.downloadXLSXFromDrive();
            clmService.updateData(wishlist);

            showPopup(AlertType.INFORMATION, "Data was update", StringUtils.EMPTY);
        } else {
            showPopup(AlertType.WARNING, CLMConstant.INCORRECT_DIRECTORY_HEADER, CLMConstant.INCORRECT_DIRECTORY_MESSAGE);
        }
    }


    @FXML
    public void openFile() {
        File selectedDirectory = new DirectoryChooser().showDialog(new Stage());
        if (selectedDirectory != null) {
            absolutePath = selectedDirectory.getAbsolutePath();
            Path path = Paths.get(absolutePath + CLMConstant.ADDON_PATH);
            if (Files.exists(path)) {
                directoryPath.setText(absolutePath);
                downloadJson.setDisable(false);
                uploadJson.setDisable(false);
                directoryChooser.setDisable(true);
            } else {
                showPopup(AlertType.WARNING, CLMConstant.INCORRECT_DIRECTORY_HEADER, CLMConstant.INCORRECT_DIRECTORY_MESSAGE);
            }
        } else {
            showPopup(AlertType.WARNING, CLMConstant.INCORRECT_DIRECTORY_HEADER, CLMConstant.INCORRECT_DIRECTORY_MESSAGE);
        }
    }

    public void showPopup(AlertType warning, String header, String contentText) {
        Alert alert = new Alert(warning);
        alert.setHeaderText(header);
        alert.setContentText(contentText);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(this.getClass().getResource("/static/css/main.css")).toExternalForm());
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/static/image/clmLogo.png"))));
        alert.showAndWait();
    }

}
