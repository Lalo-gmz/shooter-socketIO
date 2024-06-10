const express = require('express');
const http = require('http');
const socketIo = require('socket.io');

const app = express();
const server = http.createServer(app);
const io = socketIo(server);

// Middleware para servir archivos estáticos
app.use(express.static(__dirname + '/public'));

// Ruta principal
app.get('/', (req, res) => {
  res.sendFile(__dirname + '/public/index.html');
});

// Manejo de conexiones de Socket.io
io.on('connection', (socket) => {
  console.log('Nuevo usuario conectado');
  socket.emit('socketId', { id: socket.id })
  socket.broadcast('newPlayer', { id: socket.id } )

  // Manejo de mensajes enviados por el cliente
  socket.on('mensaje', (data) => {
    console.log('Mensaje recibido:', data);
    // Reenviar el mensaje a todos los clientes conectados
    io.emit('mensaje', data);
  });

  // Manejo de desconexiones
  socket.on('disconnect', () => {
    console.log('Usuario desconectado');
  });
});

const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
  console.log(`Servidor Express escuchando en el puerto ${PORT}`);
});
