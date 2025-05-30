/**
 * Copyright (C) 2025 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ancevt.d2d2.engine.norender;

import com.ancevt.d2d2.D2D2;
import com.ancevt.d2d2.engine.DisplayManager;
import com.ancevt.d2d2.engine.Engine;
import com.ancevt.d2d2.engine.SoundManager;
import com.ancevt.d2d2.event.CommonEvent;
import com.ancevt.d2d2.event.core.EventDispatcherImpl;
import com.ancevt.d2d2.input.Mouse;
import com.ancevt.d2d2.scene.Renderer;
import com.ancevt.d2d2.scene.Root;
import com.ancevt.d2d2.scene.text.Font;
import com.ancevt.d2d2.scene.text.TrueTypeFontBuilder;
import com.ancevt.d2d2.time.Timer;
import lombok.Getter;
import lombok.Setter;

//TODO: move it to separate artifact and use pngj instead of BufferedImage
public class NoRenderEngine extends EventDispatcherImpl implements Engine {

    private final int initialWidth;
    private final int initialHeight;
    private Root root;
    private String title;
    private Renderer renderer;
    private boolean running;
    private int frameRate = 60;
    private int frameCounter;
    private int fps = frameRate;
    private long time;
    private long tick;

    @Getter
    private int canvasWidth;

    @Getter
    private int canvasHeight;

    @Getter
    @Setter
    private int timerCheckFrameFrequency = 100;

    private SoundManager soundManager;

    public NoRenderEngine(int initialWidth, int initialHeight, String title) {
        this.initialWidth = initialWidth;
        this.initialHeight = initialHeight;
        D2D2.textureManager().setTextureEngine(new NoRenderTextureEngine());

        System.err.println("D2D2: No render engine is initialized");
    }

    @Override
    public void setCursorXY(int x, int y) {
        Mouse.setXY(x, y);
    }

    @Override
    public void setCanvasSize(int width, int height) {
        canvasWidth = width;
        canvasHeight = height;

        dispatchEvent(CommonEvent.Resize.create(width, height));
    }

    @Override
    public DisplayManager displayManager() {
        return new NoRenderDisplayManagerStub();
    }

    @Override
    public SoundManager soundManager() {
        if (soundManager == null) {
            soundManager = new NoRenderSoundManager();
        }
        return soundManager;
    }

    @Override
    public void setAlwaysOnTop(boolean b) {

    }

    @Override
    public boolean isAlwaysOnTop() {
        return false;
    }

    @Override
    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    @Override
    public int getFrameRate() {
        return frameRate;
    }

    @Override
    public int getActualFps() {
        return fps;
    }

    @Override
    public void create() {
        root = new Root();
        root.setSize(initialWidth, initialHeight);
        renderer = new NoRenderRendererStub(root);
    }

    @Override
    public void start() {
        running = true;
        root.dispatchEvent(CommonEvent.Start.create());
        startNoRenderLoop();
        root.dispatchEvent(CommonEvent.Stop.create());
    }

    @Override
    public Root root() {
        return root;
    }

    @Override
    public Renderer getRenderer() {
        return renderer;
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public void putToClipboard(String string) {

    }

    @Override
    public String getStringFromClipboard() {
        return null;
    }

    private void startNoRenderLoop() {
        while (running) {
            try {
                renderer.renderFrame();
                if (fps > frameRate) {
                    Thread.sleep(1000 / (frameRate + 10));
                } else {
                    Thread.sleep((long) (1000 / (frameRate * 1.5f)));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            frameCounter++;
            final long time2 = System.currentTimeMillis();

            if (time2 - time >= 1000) {
                time = System.currentTimeMillis();
                fps = frameCounter;
                frameCounter = 0;
            }

            tick++;

            if (tick % timerCheckFrameFrequency == 0) Timer.processTimers();
        }
    }


    @Override
    public Font generateBitmapFont(TrueTypeFontBuilder trueTypeFontBuilder) {
        return D2D2.bitmapFontManager().getDefaultFont();
    }


}
