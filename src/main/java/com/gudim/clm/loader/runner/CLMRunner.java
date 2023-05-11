package com.gudim.clm.loader.runner;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.gudim.clm.desktop.util.CLMUtil;
import com.gudim.clm.loader.servise.ItemService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.InputMismatchException;
import java.util.Scanner;

import static com.gudim.clm.desktop.util.CLMConstant.INTEGER_THREE;

@Component
@Log4j2
public class CLMRunner implements CommandLineRunner {

    private final ItemService itemService;
    @Value("${clm.data.file}")
    String dataFile;
    @Value("${clm.lua.file}")
    String luaFile;
    @Value("${clm.expansion.id}")
    Integer expansionId;
    @Value("${clm.json.file}")
    String jsonFile;

    public CLMRunner(ItemService itemService) {
        this.itemService = itemService;
    }

    @Override
    public void run(String... args) {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean flag = true;
            while (flag) {
                log.info("\n1 - Generate ItemData.lua" +
                        "\n2 - Update Item.json" +
                        "\n3 - Exit" +
                        "\nChoose your option : ");
                try {
                    int menuNumber = scanner.nextInt();
                    if (menuNumber == NumberUtils.INTEGER_ONE) {
                        StringBuilder itemDataFromDB = itemService.getItemsLua(expansionId);
                        CLMUtil.saveFile(itemDataFromDB, dataFile);
                    } else if (menuNumber == NumberUtils.INTEGER_TWO) {
                        JsonElement jsonElement = CLMUtil.getLuaTable(luaFile, "CLMID");
                        String itemInfo = new GsonBuilder().setPrettyPrinting().create().toJson(jsonElement);
                        CLMUtil.saveFile(new StringBuilder(itemInfo), jsonFile);
                    } else if (menuNumber == INTEGER_THREE) {
                        flag = false;
                    }
                } catch (InputMismatchException e) {
                    log.info("\nPlease enter a valid value");
                    scanner.next();
                }
            }
        }
    }
}
