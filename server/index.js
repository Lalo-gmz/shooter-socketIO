const express = require('express');
const http = require('http');
const socketIo = require('socket.io');

const app = express();
const server = http.createServer(app);
const io = socketIo(server);
let players = [];
// Middleware para servir archivos estáticos
app.use(express.static(__dirname + '/public'));

// Ruta principal
app.get('/', (req, res) => {
  res.sendFile(__dirname + '/public/index.html');
});

// Manejo de conexiones de Socket.io
io.on('connection', (socket) => {
  console.log('Nuevo usuario conectado');
  socket.emit('SocketID', { id: socket.id })
  console.table(players);
  socket.emit('getPlayers', players)
  socket.broadcast.emit('newPlayer', { id: socket.id } )
  players.push(new Player(socket.id, 0, 0 ));
  console.log(players);


  // Manejo de Jugador cuando se mueve

  socket.on('playerMoved', (data) => {
    data.id = socket.id;
    socket.broadcast.emit('playerMoved', data);
      for (let i = 0; i < players.length; i++) {
        if(players[i].id == data.id){
          players[i].x = data.x;
          players[i].y = data.y;
        }
        
      }
  })

  // Manejo de mensajes enviados por el cliente
  socket.on('mensaje', (data) => {
    console.log('Mensaje recibido:', data);
    // Reenviar el mensaje a todos los clientes conectados
    io.emit('mensaje', data);
  });

  // Manejo de desconexiones
  socket.on('disconnect', () => {
    console.log('Usuario desconectado');
    socket.broadcast.emit("playerDisconnected", {id: socket.id });
    players = players.filter(e => e.id == socket.id);
  });
});

const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
  console.log(`Servidor Express escuchando en el puerto ${PORT}`);
});

class Player {
  constructor(id, x, y) {
    this.id = id;
    this.x = x;
    this.y = y;
  }
}
