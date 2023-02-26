package com.gudim.clmdesktop.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserInfoDTO {

    String nickname;
    List<ItemInfoDTO> items = new ArrayList<>();
}
