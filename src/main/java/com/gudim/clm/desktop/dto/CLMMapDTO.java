package com.gudim.clm.desktop.dto;

import java.util.HashMap;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CLMMapDTO {
	
	HashMap<String, List<CLMUserInfoDTO>>                  clmItemMap;
	HashMap<String, HashMap<String, List<CLMUserInfoDTO>>> clmWishlistMap;
}
