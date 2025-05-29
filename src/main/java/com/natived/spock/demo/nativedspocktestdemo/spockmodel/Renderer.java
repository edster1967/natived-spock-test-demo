package com.natived.spock.demo.nativedspocktestdemo.spockmodel;

public class Renderer {
    private final Palette palette;

    public Renderer(Palette palette) {
        this.palette = palette;
    }

    public void drawLine() {
    }

    public Colour getForegroundColour() {
        return palette.getPrimaryColour();
    }
}
