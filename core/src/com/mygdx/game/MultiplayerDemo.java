package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mygdx.game.exceptions.IdNotFoundException;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MultiplayerDemo extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;

	private Socket socket;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
		connectSocket();
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
					String id = data.getString("id");
					Gdx.app.log("SocketIO", "New Player Connect: " + id );
					System.out.println("New Player Connect: " + id );
				} catch (JSONException e) {
						Gdx.app.log("SocketIO", "Errore getting new Player Id");
				}

			}
		})
		;
	}

	@Override
	public void render () {
		ScreenUtils.clear(1, 0, 0, 1);
		batch.begin();
		batch.draw(img, 0, 0);
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}
}
