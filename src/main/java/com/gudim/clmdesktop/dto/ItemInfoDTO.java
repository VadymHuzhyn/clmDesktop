package com.gudim.clmdesktop.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemInfoDTO {

    String itemId;
    String bossName;
    String wishNumber;
    Boolean marker;
}
