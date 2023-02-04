package com.gudim.clm.desktop.dto;

import com.gudim.clm.desktop.dto.items.CLMItem;
import com.gudim.clm.desktop.dto.wishlists.CLMCharType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CLMWishlist {

    Map<String, List<CLMItem>> items = new HashMap<>();
    List<CLMCharType> wishlists = new ArrayList<>();
}
