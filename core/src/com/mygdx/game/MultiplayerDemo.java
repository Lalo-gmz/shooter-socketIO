package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mygdx.game.UI.GameUI;
import com.mygdx.game.sprites.Character;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;

public class MultiplayerDemo extends ApplicationAdapter {
	private static final float UPDATE_TIME = 1 / 60f;
	private float timer;
	private SpriteBatch batch;
	private Socket socket;
	private String id;
	private Character player;
	private Texture playerShip;
	private Texture friendlyShip;
	private Map<String, Character> friendlyPlayers;

	private GameUI gameUI;

	@Override
	public void create() {
		batch = new SpriteBatch();
		playerShip = new Texture("playerSprite.png");
		friendlyShip = new Texture("friendSprite.png");
		friendlyPlayers = new HashMap<>();
		Gdx.gl.glClearColor(0.2f, 0.6f, 1f, 1f);
		gameUI = new GameUI();
		connectSocket();
		configSocketEvents();
	}

	private void connectSocket() {
		try {
			socket = IO.socket("http://localhost:3000");
			socket.connect();
		} catch (Exception e) {
			Gdx.app.log("SocketIO", "Error connecting to socket", e);
		}
	}

	private void configSocketEvents() {
		socket.on(Socket.EVENT_CONNECT, args -> {
			Gdx.app.log("SocketIO", "Connected");
			player = new Character(playerShip);
		}).on("SocketID", this::handleSocketID)
				.on("newPlayer", this::handleNewPlayer)
				.on("playerMoved", this::handlePlayerMoved)
				.on("playerDisconnected", this::handlePlayerDisconnected)
				.on("getPlayers", this::handleGetPlayers);
	}

	private void handleSocketID(Object... args) {
		JSONObject data = (JSONObject) args[0];
		try {
			id = data.getString("id");
			Gdx.app.log("SocketIO", "My ID: " + id);
		} catch (JSONException e) {
			Gdx.app.log("SocketIO", "Error getting SocketID", e);
		}
	}

	private void handleNewPlayer(Object... args) {
		JSONObject data = (JSONObject) args[0];
		try {
			String playerId = data.getString("id");
			Gdx.app.log("SocketIO", "New Player Connect: " + playerId);
			friendlyPlayers.put(playerId, new Character(friendlyShip));
		} catch (JSONException e) {
			Gdx.app.log("SocketIO", "Error getting new Player ID", e);
		}
	}

	private void handlePlayerMoved(Object... args) {
		JSONObject data = (JSONObject) args[0];
		try {
			String playerId = data.getString("id");
			float x = (float) data.getDouble("x");
			float y = (float) data.getDouble("y");
			Character character = friendlyPlayers.get(playerId);
			if (character != null) {
				character.setPosition(x, y);
			}
		} catch (JSONException e) {
			Gdx.app.log("SocketIO", "Error updating player position", e);
		}
	}

	private void handlePlayerDisconnected(Object... args) {
		JSONObject data = (JSONObject) args[0];
		try {
			String id = data.getString("id");
			friendlyPlayers.remove(id);
		} catch (JSONException e) {
			Gdx.app.log("SocketIO", "Error handling player disconnection", e);
		}
	}

	private void handleGetPlayers(Object... args) {
		JSONArray objects = (JSONArray) args[0];
		try {
			for (int i = 0; i < objects.length(); i++) {
				JSONObject playerData = objects.getJSONObject(i);
				String playerId = playerData.getString("id");
				float x = (float) playerData.getDouble("x");
				float y = (float) playerData.getDouble("y");
				Character coopPlayer = new Character(friendlyShip);
				coopPlayer.setPosition(x, y);
				friendlyPlayers.put(playerId, coopPlayer);
			}
		} catch (JSONException e) {
			Gdx.app.log("SocketIO", "Error getting players", e);
		}
	}

	public void updateServer(float deltaTime) {
		timer += deltaTime;
		if (timer >= UPDATE_TIME && player != null && player.hasMove()) {
			JSONObject data = new JSONObject();
			try {
				data.put("x", player.getX());
				data.put("y", player.getY());
				socket.emit("playerMoved", data);
				timer = 0; // Reset timer after updating server
			} catch (JSONException e) {
				Gdx.app.log("SocketIO", "Error updating player", e);
			}
		}
	}

	@Override
	public void render() {
		ScreenUtils.clear(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		handleInput(Gdx.graphics.getDeltaTime());
		updateServer(Gdx.graphics.getDeltaTime());

		float delta = Gdx.graphics.getDeltaTime();

		batch.begin();
		if (player != null) {
			player.draw(batch);
		}
		for (Character character : friendlyPlayers.values()) {
			character.draw(batch);
		}
		gameUI.render(delta);
		batch.end();
	}

	private void handleInput(float deltaTime) {
		if (player != null) {
			if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
				player.setPosition(player.getX() - 200 * deltaTime, player.getY());
			} else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
				player.setPosition(player.getX() + 200 * deltaTime, player.getY());
			}
		}
	}

	@Override
	public void dispose() {
		gameUI.dispose();
		batch.dispose();
		playerShip.dispose();
		friendlyShip.dispose();
		if (socket != null) {
			socket.disconnect();
			socket.close();
		}
		super.dispose();
	}
}

