package com.mygdx.game.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameUI {
    private Stage stage;
    private Skin skin;
    private Label label;

    boolean buttonsVisible;

    public GameUI() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Initial visibility state
        buttonsVisible = false;


        // Load the built-in skin
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Create UI elements
        label = new Label("Hello, LibGDX!", skin);
        TextButton menuBtn = new TextButton("Menu", skin);
        TextButton button1 = new TextButton("Inventory", skin);
        TextButton button2 = new TextButton("Exit", skin);

        button1.setVisible(false);
        button2.setVisible(false);


        // Create a SelectBox
        /*
        SelectBox<Object> selectBox = new SelectBox<>(skin);
        selectBox.setItems("Option 1", "Option 2", "Option 3");

         */

        // Add listener to the button
        button1.addListener(event -> {
            if (event.toString().equals("touchDown")) {
                label.setText("Button clicked!");
                return true;
            }
            return false;
        });




        menuBtn.addListener(event -> {
            if (event.toString().equals("touchDown")) {
                buttonsVisible = !buttonsVisible;
                button1.setVisible(buttonsVisible);
                button2.setVisible(buttonsVisible);
                return true;
            }
            return false;
        });



        // Create a table to organize UI elements
        Table table = new Table();
        table.setFillParent(true);
        table.bottom().right();

        // Add UI elements to the table
        table.add(label).padBottom(20).row();
        table.add(button1).padBottom(20).padRight(20).width(100).height(50);
        table.row();
        table.add(button2).padBottom(20).padRight(20).width(100).height(50);
        table.row();
        table.add(menuBtn).padBottom(20).width(100).height(50).left();

        // Add the table to the stage
        stage.addActor(table);
    }

    public void render(float delta) {
        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
    }

    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
