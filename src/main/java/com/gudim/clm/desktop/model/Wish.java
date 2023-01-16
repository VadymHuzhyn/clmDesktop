package com.gudim.clm.desktop.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Wish implements Serializable {

    private static final long serialVersionUID = 4381251464040467528L;
    private String characterTypeName;
    private List<Character> characters = new ArrayList<>();

    public String getCharacterTypeName() {
        return characterTypeName;
    }

    public void setCharacterTypeName(String characterTypeName) {
        this.characterTypeName = characterTypeName;
    }

    public List<Character> getCharacters() {
        return characters;
    }

    public void setCharacters(List<Character> characters) {
        this.characters = characters;
    }

}
