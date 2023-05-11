package com.gudim.clm.loader.repository.impl;


import com.gudim.clm.loader.entity.Item;
import com.gudim.clm.loader.repository.ItemRepository;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@AllArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {
    private static final String FIND_BY_EXPANSION_ID = "select i.item_id as itemId from items i join item_item_sources iis on i.item_id = iis.item_id join item_sources s on s.id = iis.item_source_id join instances i2 on s.instance_id = i2.id where i2.expansion_id=?";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Item> findByExpansionId(Integer expansionId) {
        return jdbcTemplate.query(FIND_BY_EXPANSION_ID, (rs, rowNum) -> new Item(rs.getInt("itemId")), expansionId);
    }
}
