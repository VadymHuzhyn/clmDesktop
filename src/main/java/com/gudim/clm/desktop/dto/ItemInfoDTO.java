package com.gudim.clm.desktop.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemInfoDTO {
    @JsonProperty("itemId")
    String itemId;
    String bossName;
    String wishNumber;
    @JsonProperty("marker")
    Boolean marker;
}
