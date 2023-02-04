package com.gudim.clm.desktop.dto.items;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CLMItem {

    String wishNumber;
    String nickname;
    String characterType;
}
