package com.bridger.momentum;

/** Small drawing boundary: native Minecraft in game, an SVG recorder in previews. */
public interface UiCanvas {
    void rect(int x,int y,int width,int height,int color);
    void text(String value,int x,int y,int color);
    int width(String value);
    void textScaled(String value,int x,int y,int color,float scale);
}
