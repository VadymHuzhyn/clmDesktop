package com.gudim.clm.desktop.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Character implements Serializable {

    private static final long serialVersionUID = -7359448206087698239L;
    private String nickname;
    private List<Item> item = new ArrayList<>();

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public List<Item> getItem() {
        return item;
    }

    public void setItem(List<Item> item) {
        this.item = item;
    }

}
