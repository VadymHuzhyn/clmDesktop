package com.gudim.clm.desktop.dto.wishlists;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CLMUserItem {

    String itemId;
    String wishNumber;
    Boolean marker;
}
