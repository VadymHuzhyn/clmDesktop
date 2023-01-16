package com.gudim.clm.desktop.model;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Item implements Serializable {
	
	static final long serialVersionUID = -8114615793844935746L;
	String itemId;
	String wishNumber;
	
}
