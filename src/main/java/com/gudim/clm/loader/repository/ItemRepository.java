package com.gudim.clm.loader.repository;

import com.gudim.clm.loader.entity.Item;

import java.util.List;

public interface ItemRepository {

    List<Item> findByExpansionId(Integer expansionId);
}
