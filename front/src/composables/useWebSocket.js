import { ref, onMounted, onBeforeUnmount } from 'vue';
import store from '../store';

export function useWebSocket(songId, onMessageReceived) {
  const socket = ref(null);
  const isConnected = ref(false);
  const reconnectCount = ref(0);
  const maxReconnect = 5;

  const connect = () => {
    if (socket.value || !songId) return;

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    // Use the same host as the current page, or a specific port if needed.
    // Assuming backend runs on 8090 based on previous context.
    // Use Vuex store directly for reliable user info retrieval
    const user = store.state.user;
    let username = '未知用户';
    // Default to a random guest ID
    let userId = 'guest_' + Math.random().toString(36).substr(2, 9);
    
    if (user && user.username) {
        username = user.username;
        // Use ID if available, otherwise fallback to username to dedupe same user tabs
        userId = user.id || user.userId || user.username; 
    }

    let socketUrl = '';
    const hostname = window.location.hostname;
    
    if (hostname === 'localhost' || hostname === '127.0.0.1') {
        // 本地环境，直接连后端端口 8090
        socketUrl = `${protocol}//${hostname}:8090/ws/chart`;
    } else {
        // 远程环境（如 Cloudflare Tunnel），统一种过 /api 路径转发到后端
        // 路径由原来的 /ws/chart 改为 /api/ws/chart
        socketUrl = `${protocol}//${window.location.host}/api/ws/chart`;
    }

    const url = `${socketUrl}?songId=${songId}&username=${encodeURIComponent(username)}&userId=${userId}`;
    
    socket.value = new WebSocket(url);

    socket.value.onopen = () => {
      console.log('Connected to Chart WebSocket as', username);
      isConnected.value = true;
      reconnectCount.value = 0;
      // Join room
      send('JOIN', null);
    };

    socket.value.onmessage = (event) => {
      const msg = JSON.parse(event.data);
      if (msg.type === "PING") {
        send("PONG", null);
        return;
      }
      onMessageReceived(msg);
    };

    socket.value.onclose = () => {
      isConnected.value = false;
      socket.value = null;
      if (reconnectCount.value < maxReconnect) {
        reconnectCount.value++;
        setTimeout(connect, 3000);
      }
    };

    socket.value.onerror = (err) => {
      console.error('WebSocket Error:', err);
    };
  };

  const send = (type, payload, clientId = null) => {
    if (!socket.value || socket.value.readyState !== WebSocket.OPEN) {
      console.warn('Socket not connected, cannot send:', type);
      return;
    }
    const msg = {
      type,
      songId: songId,
      payload,
      clientId
    };
    socket.value.send(JSON.stringify(msg));
  };

  onMounted(() => {
    connect();
  });

  onBeforeUnmount(() => {
    if (socket.value) {
      socket.value.close();
    }
  });

  return {
    isConnected,
    send
  };
}
