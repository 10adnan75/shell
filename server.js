const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const { spawn } = require('child_process');
const path = require('path');

const app = express();
const server = http.createServer(app);
const io = new Server(server);

const PORT = process.env.PORT || 3000;

// Serve static files from web/
app.use(express.static(path.join(__dirname, 'web')));

io.on('connection', (socket) => {
  console.log('New user connected:', socket.id);

  // Spawn a new Java shell process for each user
  const shell = spawn('java', ['-cp', 'target/classes', 'core.Main'], {
    env: { ...process.env, WEB_SHELL: 'true' }
  });

  // Send shell output to client
  shell.stdout.on('data', (data) => {
    socket.emit('output', data.toString());
  });
  shell.stderr.on('data', (data) => {
    socket.emit('output', data.toString());
  });

  // Send client input to shell
  socket.on('input', (input) => {
    shell.stdin.write(input);
  });

  // Handle disconnect
  socket.on('disconnect', () => {
    shell.kill();
    console.log('User disconnected:', socket.id);
  });
});

server.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
}); 