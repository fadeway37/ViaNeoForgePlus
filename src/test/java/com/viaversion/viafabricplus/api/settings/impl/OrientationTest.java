/*
 * ViaNeoForgePlus - GPL-3.0-or-later.
 */
package com.viaversion.viafabricplus.api.settings.impl;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class OrientationTest {

    @ParameterizedTest
    @CsvSource({
        "320, 180, 98, 20",
        "320, 180, 110, 20",
        "854, 480, 98, 20",
        "854, 480, 110, 20",
        "854, 480, 140, 30",
        "1920, 1080, 110, 20"
    })
    public void keepsFivePixelMargins(final int screenWidth, final int screenHeight, final int buttonWidth, final int buttonHeight) {
        final Button button = Button.builder(Component.literal("ViaNeoForgePlus"), _ -> {}).size(buttonWidth, buttonHeight).build();

        Orientation.LEFT_TOP.getPositioner().setPosition(button, screenWidth, screenHeight);
        assertEquals(5, button.getX());
        assertEquals(5, button.getY());

        Orientation.RIGHT_TOP.getPositioner().setPosition(button, screenWidth, screenHeight);
        assertEquals(screenWidth - 5, button.getX() + button.getWidth());
        assertEquals(5, button.getY());

        Orientation.LEFT_BOTTOM.getPositioner().setPosition(button, screenWidth, screenHeight);
        assertEquals(5, button.getX());
        assertEquals(screenHeight - 5, button.getY() + button.getHeight());

        Orientation.RIGHT_BOTTOM.getPositioner().setPosition(button, screenWidth, screenHeight);
        assertEquals(screenWidth - 5, button.getX() + button.getWidth());
        assertEquals(screenHeight - 5, button.getY() + button.getHeight());
    }

    @Test
    public void repositionsAfterResize() {
        final Button button = Button.builder(Component.literal("ViaNeoForgePlus"), _ -> {}).size(110, 20).build();

        Orientation.RIGHT_BOTTOM.getPositioner().setPosition(button, 854, 480);
        button.setWidth(140);
        Orientation.RIGHT_BOTTOM.getPositioner().setPosition(button, 640, 360);

        assertEquals(635, button.getX() + button.getWidth());
        assertEquals(355, button.getY() + button.getHeight());
    }

    @Test
    public void noneLeavesPositionUnchanged() {
        final Button button = Button.builder(Component.literal("ViaNeoForgePlus"), _ -> {}).bounds(17, 23, 110, 20).build();

        Orientation.NONE.getPositioner().setPosition(button, 854, 480);

        assertEquals(17, button.getX());
        assertEquals(23, button.getY());
    }
}
