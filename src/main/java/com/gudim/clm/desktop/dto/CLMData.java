package com.gudim.clm.desktop.dto;

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
public class CLMData {

    Map<String, List<Itemlist>> items = new HashMap<>();
    List<Wishlist> wishlists = new ArrayList<>();
}
