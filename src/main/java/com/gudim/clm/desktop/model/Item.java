package com.gudim.clm.desktop.model;

import java.io.Serializable;

public class Item implements Serializable {

    private static final long serialVersionUID = -8114615793844935746L;
    private String itemId;
    private String wishNumber;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getWishNumber() {
        return wishNumber;
    }

    public void setWishNumber(String wishNumber) {
        this.wishNumber = wishNumber;
    }

}
