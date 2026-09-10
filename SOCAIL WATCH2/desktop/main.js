const { app, BrowserWindow, session } = require('electron');
const path = require('path');

const ALLOWED = [
  /^https:\/\/([^/]+\.)?youtube\.com\//i,
  /^https:\/\/([^/]+\.)?youtu\.be\//i,
  /^https:\/\/([^/]+\.)?facebook\.com\//i,
  /^https:\/\/([^/]+\.)?tiktok\.com\//i
];
function allowed(url) {
  try { return ALLOWED.some(rx => rx.test(url)); } catch (_) { return false; }
}
function createWindow() {
  const win = new BrowserWindow({
    width: 1100, height: 800, minWidth: 360, minHeight: 600,
    autoHideMenuBar: true,
    webPreferences: { contextIsolation: true, nodeIntegration: false, sandbox: true }
  });
  win.loadFile(path.join(__dirname, '..', 'src', 'index.html'));
  win.webContents.setWindowOpenHandler(({ url }) => allowed(url) ? { action: 'allow' } : { action: 'deny' });
  win.webContents.on('will-navigate', (event, url) => {
    if (!url.startsWith('file://') && !allowed(url)) event.preventDefault();
  });
}
app.whenReady().then(() => {
  session.defaultSession.setPermissionRequestHandler((_wc, _permission, callback) => callback(false));
  session.defaultSession.setPermissionCheckHandler((_wc, _permission, requestingOrigin) => {
    try { return allowed(requestingOrigin + '/'); } catch (_) { return false; }
  });
  createWindow();
  app.on('activate', () => { if (BrowserWindow.getAllWindows().length === 0) createWindow(); });
});
app.on('window-all-closed', () => { if (process.platform !== 'darwin') app.quit(); });
