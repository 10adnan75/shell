const socket = io();
const term = new Terminal({
  cursorBlink: true,
  cursorStyle: 'block',
  theme: {
    background: '#181818',
    foreground: '#e0e0e0',
    cursor: '#4fc3f7',
    cursorAccent: '#ff4fd8'
  },
  fontFamily: 'JetBrains Mono, Fira Mono, Consolas, monospace',
  fontSize: 17,
  letterSpacing: 1.1,
});
term.open(document.getElementById('terminal-inner'));

let inputBuffer = '';
let sessionHistory = [];

// Helper for writing the prompt
function writePrompt() {
  // Use black for $ in light mode, cyan in dark mode
  if (document.body.classList.contains('light-mode')) {
    term.write('\x1b[1;30m$\x1b[0m '); // Bright black (gray) $
    sessionHistory.push({ type: 'prompt', theme: 'light' });
  } else {
    term.write('\x1b[1;96m$\x1b[0m '); // Bright cyan $
    sessionHistory.push({ type: 'prompt', theme: 'dark' });
  }
}

// Show the prompt at startup
writePrompt();

// Handle terminal key input
term.onKey(e => {
  const ev = e.domEvent;
  const printable = !ev.altKey && !ev.ctrlKey && !ev.metaKey;

  if (ev.key === 'Enter') {
    sessionHistory.push({ type: 'input', value: inputBuffer });
    socket.emit('input', inputBuffer + '\n');
    inputBuffer = '';
  } else if (ev.key === 'Backspace') {
    if (inputBuffer.length > 0) {
      term.write('\b \b');
      inputBuffer = inputBuffer.slice(0, -1);
    }
  } else if (printable && ev.key.length === 1) {
    inputBuffer += ev.key;
    term.write(ev.key);
  }
});

// Display output from server
socket.on('output', data => {
  const output = data.replace(/\n/g, '\r\n');
  term.write(output);
  sessionHistory.push({ type: 'output', value: output });
  if (!output.endsWith('\r\n')) {
    term.write('\r\n');
    sessionHistory.push({ type: 'output', value: '\r\n' });
  }
  writePrompt();
});

// Faster cursor blink effect
let blinkVisible = true;
// Removed manual setInterval for cursor blinking. xterm.js will handle blinking automatically.

// --- BUTTON FUNCTIONALITY (FIXED) ---
const btnClear = document.getElementById('btn-clear');
const btnCopy = document.getElementById('btn-copy');
const btnTheme = document.getElementById('btn-theme');

btnClear.addEventListener('click', () => {
  term.reset(); // Full reset (clears scrollback and state)
  sessionHistory = [];
  writePrompt();
});

btnCopy.addEventListener('click', () => {
  let text = term.getSelection();
  if (!text) {
    // If nothing is selected, copy all visible buffer lines
    const buffer = term.buffer.active;
    text = '';
    for (let i = 0; i < buffer.length; i++) {
      const line = buffer.getLine(i);
      if (line) text += line.translateToString(true) + '\n';
    }
  }
  navigator.clipboard.writeText(text.trim());
  // Show notification
  showCopyNotification();
});

function showCopyNotification() {
  let note = document.getElementById('copy-note');
  if (!note) {
    note = document.createElement('div');
    note.id = 'copy-note';
    note.textContent = 'Copied!';
    note.style.position = 'fixed';
    note.style.top = '24px';
    note.style.right = '32px';
    note.style.background = 'rgba(30,40,60,0.92)';
    note.style.color = '#00ffb3';
    note.style.fontSize = '1.1rem';
    note.style.padding = '10px 22px';
    note.style.borderRadius = '12px';
    note.style.boxShadow = '0 0 12px 2px #4fc3f7cc, 0 0 4px 1px #ff4fd8cc';
    note.style.zIndex = 9999;
    note.style.opacity = '0.98';
    note.style.transition = 'opacity 0.3s';
    document.body.appendChild(note);
  }
  note.style.opacity = '0.98';
  note.style.display = 'block';
  setTimeout(() => {
    note.style.opacity = '0';
    setTimeout(() => { note.style.display = 'none'; }, 350);
  }, 1200);
}

const darkTheme = {
  background: '#181818',
  foreground: '#e0e0e0',
  cursor: '#4fc3f7',
  cursorAccent: '#ff4fd8'
};
const lightTheme = {
  background: '#fff',
  foreground: '#222',
  cursor: '#111', // black cursor in light mode
  cursorAccent: '#fff'
};
let isDark = true;
btnTheme.addEventListener('click', () => {
  isDark = !isDark;
  term.options.theme = isDark ? darkTheme : lightTheme;
  document.body.style.background = isDark
    ? 'linear-gradient(120deg, #101014 0%, #0f2027 40%, #2c5364 100%)'
    : 'linear-gradient(120deg, #f7f7fa 0%, #e0e0e0 40%, #b3c6e0 100%)';
  document.body.classList.toggle('light-mode', !isDark);
  // Redraw the session history with correct prompt color
  term.reset();
  for (const entry of sessionHistory) {
    if (entry.type === 'prompt') {
      if (document.body.classList.contains('light-mode')) {
        term.write('\x1b[1;30m$\x1b[0m ');
      } else {
        term.write('\x1b[1;96m$\x1b[0m ');
      }
    } else if (entry.type === 'input') {
      term.write(entry.value + '\r\n');
    } else if (entry.type === 'output') {
      term.write(entry.value);
    }
  }
}); 