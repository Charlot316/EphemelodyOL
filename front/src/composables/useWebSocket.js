import { ref, onMounted, onBeforeUnmount } from 'vue';

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
    const userStr = localStorage.getItem('user');
    let username = '未知用户';
    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        username = user.username || '未知用户';
      } catch (e) { /* ignore */ }
    }

    const url = `${protocol}//${window.location.hostname}:8090/ws/chart?songId=${songId}&username=${encodeURIComponent(username)}`;
    
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
      songId: parseInt(songId),
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
