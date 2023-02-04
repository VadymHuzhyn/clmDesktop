package com.gudim.clm.desktop.dto.wishlists;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CLMUser {

    String nickname;
    List<CLMUserItem> items = new ArrayList<>();
}
