package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mygdx.game.exceptions.IdNotFoundException;
import com.mygdx.game.sprites.Starship;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MultiplayerDemo extends ApplicationAdapter {
	private final float UPDATE_TIME = 1/60f;
	float timer;
	SpriteBatch batch;
	private Socket socket;
	String id;
	Starship player;
	Texture playerShip;
	Texture friendlyShip;

	Map<String, Starship> friendlyPlayers;
	
	@Override
	public void create () {

		batch = new SpriteBatch();
		playerShip = new Texture("space1.png");
		friendlyShip = new Texture("space2.png");
		friendlyPlayers = new HashMap<>();
		Gdx.gl.glClearColor(0.2f, 0.6f, 1f, 1f);

		connectSocket();
		configSocketEvents();
	}



	private void connectSocket() {
		try {
			socket = IO.socket("http://localhost:3000");
			socket.connect();

		} catch (Exception e){
			System.out.println(e);
		}
	}

	private void configSocketEvents(){
		socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				Gdx.app.log("SocketIO", "Connected");
				player = new Starship(playerShip);
			}
		}).on("SocketID", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
                try {
                    String id = data.getString("id");
					Gdx.app.log("SocketIO", "My ID: " + id );
					System.out.println("My ID: " + id );
                } catch (JSONException e) {
                    try {
                        throw new IdNotFoundException("Client with Out Id from Server", e);
                    } catch (IdNotFoundException ex) {
                        throw new RuntimeException(ex);
                    }
                }

            }
		}).on("newPlayer", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try {
					String playerId = data.getString("id");
					Gdx.app.log("SocketIO", "New Player Connect: " + playerId );
					friendlyPlayers.put(playerId, new Starship(friendlyShip));
				} catch (JSONException e) {
						Gdx.app.log("SocketIO", "Errore getting new Player Id");
				}

			}
		}).on("playerMoved", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try {
					String playerId = data.getString("id");
					double x = data.getDouble("x");
					double y = data.getDouble("y");
					if(friendlyPlayers.get(playerId) != null ){
						friendlyPlayers.get(playerId).setPosition((float) x, (float) y);
					}

				} catch (JSONException e) {
				}

			}
		}).on("playerDisconnected", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try {
					String id = data.getString("id");
					friendlyPlayers.remove(id);
				} catch (JSONException e) {
					Gdx.app.log("SocketIO", "Errore getting new Player Disconected");
				}

			}
		}).on("getPlayers", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				Gdx.app.log("SocketIO" ,"Obteniendo Data de Players");
				JSONArray objects = (JSONArray) args[0];
				try {
					for (int i = 0; i < objects.length(); i++) {
						Starship coopPlayer = new Starship(friendlyShip);
						Vector2 position = new Vector2();
						position.x = ((Double) objects.getJSONObject(i).getDouble("x")).floatValue();
						position.y = ((Double) objects.getJSONObject(i).getDouble("y")).floatValue();
						coopPlayer.setPosition(position.x, position.y);

						friendlyPlayers.put(objects.getJSONObject(i).getString("id"), coopPlayer );

					}

				} catch (JSONException e) {
					Gdx.app.log("SocketIO", "Error getFriendlyPlayer");
				}

			}
		})
		;
	}

	public void updateServer(float detaTime){
		timer += detaTime;
		if (timer >= UPDATE_TIME && player != null && player.hasMove()){
			JSONObject data = new JSONObject();
			try {
				data.put("x", player.getX());
				data.put("y", player.getY());
				socket.emit("playerMoved", data);
			} catch (JSONException e) {
				Gdx.app.log("SocketIO", "Error updatePlayer");
			}
		}
	}

	@Override
	public void render () {
		ScreenUtils.clear(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		handleInput(Gdx.graphics.getDeltaTime());
		updateServer(Gdx.graphics.getDeltaTime());


		batch.begin();
		if(player != null ){
			player.draw(batch);
		}
		for(Map.Entry<String, Starship> entry : friendlyPlayers.entrySet()){
			entry.getValue().draw(batch);
		}
		batch.end();
	}

	private void handleInput(float deltaTime) {
		if (player != null){
			if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
				player.setPosition(player.getX() + (-200 * deltaTime), player.getY() );
			}else if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
				player.setPosition(player.getX() + (+200 * deltaTime), player.getY() );
			}
		}
	}

	@Override
	public void dispose () {
		batch.dispose();
		super.dispose();
		playerShip.dispose();
		friendlyShip.dispose();
	}
}
