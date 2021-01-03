package com.example.icandoit.adapter;

import com.example.icandoit.model.drawing;

public class DragData {

    public final drawing item;
    public final int width;
    public final int height;

    public DragData(drawing item, int width, int height) {
        this.item= item;
        this.width = width;
        this.height = height;
    }

}
