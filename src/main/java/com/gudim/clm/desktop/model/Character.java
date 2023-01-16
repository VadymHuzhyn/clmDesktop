package com.gudim.clm.desktop.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Character implements Serializable {
	
	static final long serialVersionUID = -7359448206087698239L;
	String     nickname;
	List<Item> item = new ArrayList<>();
	
	
}
