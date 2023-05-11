package com.gudim.clm.loader.servise;

import com.gudim.clm.loader.entity.Item;
import com.gudim.clm.loader.repository.ItemRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.StringJoiner;

import static com.gudim.clm.desktop.util.CLMConstant.*;

@Service
@Log4j2
public class ItemService {


    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public StringBuilder getItemsLua(Integer expansionId) {
        List<Item> itemList = itemRepository.findByExpansionId(expansionId);
        log.info("Starting build a item table");
        StringBuilder stringBuilder = new StringBuilder();
        StringJoiner joiner = new StringJoiner(COMMA);
        int i = NumberUtils.INTEGER_ONE;
        for (Item item : itemList) {
            joiner.add(String.format(CONTENT_ITEM_DATA, i++, item.getItemId().toString()));
        }
        joiner.add(String.format(CONTENT_ITEM_DATA, i, "45857"));
        stringBuilder.append(String.format(INIT_ITEM_DATA, joiner));
        log.debug(StringUtils.LF + stringBuilder);
        log.info("Build a item table is completed");
        return stringBuilder;
    }


}
