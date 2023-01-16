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
public class Wish implements Serializable {
	
	static final long serialVersionUID = 4381251464040467528L;
	String          characterTypeName;
	List<Character> characters = new ArrayList<>();
}
