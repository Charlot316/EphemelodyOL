package team.javaee.common.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import team.javaee.entity.domain.*;
import team.javaee.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*; // Added for heartbeat scheduler

@Component
public class ChartWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChartWebSocketHandler.class);

    // Heartbeat tracking
    private final Map<WebSocketSession, Long> lastHeartbeat = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public ChartWebSocketHandler() {
        // Heartbeat check every 30 seconds
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            for (WebSocketSession session : lastHeartbeat.keySet()) {
                if (session.isOpen()) {
                    try {
                        // Send PING
                        ChartMessage ping = new ChartMessage();
                        ping.setType("PING");
                        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(ping)));

                        // Check timeout (60s)
                        Long last = lastHeartbeat.get(session);
                        if (last != null && (now - last > 60000)) {
                            session.close(CloseStatus.SESSION_NOT_RELIABLE);
                        }
                    } catch (IOException e) {
                        // Ignore
                    }
                }
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    // Changed from @Autowired to direct instantiation as per provided code edit
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private NoteMapper noteMapper;

    @Autowired
    private TrackMapper trackMapper;

    @Autowired
    private MoveOperationMapper moveOperationMapper;

    @Autowired
    private ChangeWidthOperationMapper changeWidthOperationMapper;

    @Autowired
    private ChangeColorOperationMapper changeColorOperationMapper;

    @Autowired
    private ChangeBackgroundOperationMapper changeBackgroundOperationMapper;

    // Room management: songId -> sessions
    private static final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    private static final Map<WebSocketSession, String> sessionToRoom = new ConcurrentHashMap<>();
    private static final Map<WebSocketSession, String> sessionToUser = new ConcurrentHashMap<>();
    private static final Map<WebSocketSession, String> sessionToUserId = new ConcurrentHashMap<>();

    // Track publishing and reset consensus
    private static final Map<String, PublishConsensus> pendingConsensus = new ConcurrentHashMap<>();
    private static final Map<String, PublishConsensus> pendingResetConsensus = new ConcurrentHashMap<>();

    private static class PublishConsensus {
        WebSocketSession initiator; // Changed from String initiatorSessionId
        String initiatorName;
        Set<String> approvedSessions = new HashSet<>(); // Changed from agreedSessions
        int totalExpected;
    }

    @Data
    public static class ChartMessage {
        private String type; // JOIN, ADD_NOTE, UPDATE_NOTE, DELETE_NOTE, etc.
        private String songId;
        private Object payload;
        private String clientId; // For ghosting resolution
    }

    @Override
    public void afterConnectionEstablished(@org.springframework.lang.NonNull WebSocketSession session)
            throws Exception {
        lastHeartbeat.put(session, System.currentTimeMillis());
    }

    private void broadcastOnlineStatus(String songId) {
        Set<WebSocketSession> sessions = rooms.get(songId);
        if (sessions == null)
            return;

        Map<String, String> uniqueUsers = new HashMap<>(); // userId -> username
        for (WebSocketSession s : sessions) {
            String uid = sessionToUserId.get(s);
            String uname = sessionToUser.get(s);
            if (uid != null && uname != null) {
                uniqueUsers.put(uid, uname);
            }
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("count", uniqueUsers.size());
        payload.put("users", new ArrayList<>(uniqueUsers.values()));
        broadcast(songId, "ONLINE_STATUS", payload, null, null);
    }

    @Override
    protected void handleTextMessage(@org.springframework.lang.NonNull WebSocketSession session,
            @org.springframework.lang.NonNull TextMessage message) throws Exception {
        lastHeartbeat.put(session, System.currentTimeMillis());

        ChartMessage msg = objectMapper.readValue(message.getPayload(), ChartMessage.class);
        if ("PONG".equals(msg.getType())) {
            return;
        }

        String songId = msg.getSongId();

        if ("JOIN".equals(msg.getType())) {
            URI uri = session.getUri();
            String username = "未知用户";
            String userId = UUID.randomUUID().toString(); // Default if not provided

            if (uri != null && uri.getQuery() != null) {
                String query = uri.getQuery();
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("username=")) {
                        username = java.net.URLDecoder.decode(param.substring(9), "UTF-8");
                    } else if (param.startsWith("userId=")) {
                        userId = java.net.URLDecoder.decode(param.substring(7), "UTF-8");
                    }
                }
            }

            rooms.computeIfAbsent(songId, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(session);
            sessionToRoom.put(session, songId);
            sessionToUser.put(session, username);
            sessionToUserId.put(session, userId);
            broadcastOnlineStatus(songId);
            return;
        }

        // Process CRUD
        Object resultPayload = null;
        try {
            switch (msg.getType()) {
                case "PUBLISH_REQUEST":
                    handlePublishRequest(session, songId);
                    return;
                case "PUBLISH_VOTE":
                    handlePublishVote(session, songId, (Boolean) msg.getPayload());
                    return;
                case "RESET_REQUEST":
                    handleResetRequest(session, songId);
                    return;
                case "RESET_VOTE":
                    handleResetVote(session, songId, (Boolean) msg.getPayload());
                    return;
                case "ADD_NOTE":
                    Note n = objectMapper.convertValue(msg.getPayload(), Note.class); // Changed var name
                    n.setSongId(songId);
                    // basedTrack should already be in payload, but verify it exists
                    if (n.getBasedTrack() == null) {
                        throw new IllegalArgumentException("basedTrack is required for ADD_NOTE");
                    }
                    noteMapper.insert(n);
                    resultPayload = n;
                    break;
                case "UPDATE_NOTE":
                    Note un = objectMapper.convertValue(msg.getPayload(), Note.class); // Changed var name
                    noteMapper.updateById(un);
                    resultPayload = un;
                    break;
                case "DELETE_NOTE":
                    // Integer noteId = (Integer) msg.getPayload(); // Original line
                    noteMapper.deleteById(objectMapper.convertValue(msg.getPayload(), Integer.class)); // Simplified
                    resultPayload = msg.getPayload(); // Simplified
                    break;
                case "ADD_TRACK":
                    Track t = objectMapper.convertValue(msg.getPayload(), Track.class); // Changed var name
                    t.setSongId(songId);
                    trackMapper.insert(t);
                    resultPayload = t;
                    break;
                case "UPDATE_TRACK":
                    Track ut = objectMapper.convertValue(msg.getPayload(), Track.class); // Changed var name
                    trackMapper.updateById(ut);
                    resultPayload = ut;
                    break;
                case "DELETE_TRACK":
                    int tid = objectMapper.convertValue(msg.getPayload(), Integer.class); // Changed var name
                    // Cascading delete notes and operations for this track
                    noteMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Note>()
                            .eq("based_track", tid)); // Simplified
                    // The following cascading deletes were removed as per the provided code edit
                    // moveOperationMapper.delete(new
                    // QueryWrapper<MoveOperation>().eq("based_track", trackId));
                    // changeWidthOperationMapper.delete(new
                    // QueryWrapper<ChangeWidthOperation>().eq("based_track", trackId));
                    // changeColorOperationMapper.delete(new
                    // QueryWrapper<ChangeColorOperation>().eq("based_track", trackId));
                    trackMapper.deleteById(tid);
                    resultPayload = tid;
                    break;

                // Move Operations
                case "ADD_MOVE_OP":
                    MoveOperation mo = objectMapper.convertValue(msg.getPayload(), MoveOperation.class);
                    mo.setSongId(songId);
                    moveOperationMapper.insert(mo);
                    resultPayload = mo;
                    break;
                case "UPDATE_MOVE_OP":
                    MoveOperation uMo = objectMapper.convertValue(msg.getPayload(), MoveOperation.class);
                    moveOperationMapper.updateById(uMo);
                    resultPayload = uMo;
                    break;
                case "DELETE_MOVE_OP":
                    moveOperationMapper.deleteById(objectMapper.convertValue(msg.getPayload(), Integer.class));
                    resultPayload = msg.getPayload();
                    break;

                // Width Operations
                case "ADD_WIDTH_OP":
                    ChangeWidthOperation wo = objectMapper.convertValue(msg.getPayload(), ChangeWidthOperation.class);
                    wo.setSongId(songId);
                    changeWidthOperationMapper.insert(wo);
                    resultPayload = wo;
                    break;
                case "UPDATE_WIDTH_OP":
                    ChangeWidthOperation uWo = objectMapper.convertValue(msg.getPayload(), ChangeWidthOperation.class);
                    changeWidthOperationMapper.updateById(uWo);
                    resultPayload = uWo;
                    break;
                case "DELETE_WIDTH_OP":
                    changeWidthOperationMapper.deleteById(objectMapper.convertValue(msg.getPayload(), Integer.class));
                    resultPayload = msg.getPayload();
                    break;

                // Color Operations
                case "ADD_COLOR_OP":
                    ChangeColorOperation co = objectMapper.convertValue(msg.getPayload(), ChangeColorOperation.class);
                    co.setSongId(songId);
                    changeColorOperationMapper.insert(co);
                    resultPayload = co;
                    break;
                case "UPDATE_COLOR_OP":
                    ChangeColorOperation uCo = objectMapper.convertValue(msg.getPayload(), ChangeColorOperation.class);
                    changeColorOperationMapper.updateById(uCo);
                    resultPayload = uCo;
                    break;
                case "DELETE_COLOR_OP":
                    changeColorOperationMapper.deleteById(objectMapper.convertValue(msg.getPayload(), Integer.class));
                    resultPayload = msg.getPayload();
                    break;

                // Background Operations
                case "ADD_BG_OP":
                    ChangeBackgroundOperation bo = objectMapper.convertValue(msg.getPayload(),
                            ChangeBackgroundOperation.class);
                    bo.setSongId(songId);
                    changeBackgroundOperationMapper.insert(bo);
                    resultPayload = bo;
                    break;
                case "UPDATE_BG_OP":
                    ChangeBackgroundOperation uBo = objectMapper.convertValue(msg.getPayload(),
                            ChangeBackgroundOperation.class);
                    changeBackgroundOperationMapper.updateById(uBo);
                    resultPayload = uBo;
                    break;
                case "DELETE_BG_OP":
                    Integer bgOpId = objectMapper.convertValue(msg.getPayload(), Integer.class);
                    log.info("🗑️ [DELETE_BG_OP] 准备删除背景操作 ID: {}, songId: {}", bgOpId, songId);
                    int deletedRows = changeBackgroundOperationMapper.deleteById(bgOpId);
                    log.info("✅ [DELETE_BG_OP] 删除完成，影响行数: {}, ID: {}", deletedRows, bgOpId);
                    resultPayload = msg.getPayload();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendToSession(session, "ERROR", e.getMessage(), msg.getClientId());
            return;
        }

        // Broadcast to all in room
        broadcast(songId, msg.getType() + "_ACK", resultPayload, msg.getClientId(), session);
    }

    private void handleResetRequest(WebSocketSession session, String songId) throws IOException {
        Set<WebSocketSession> sessions = rooms.get(songId);
        if (sessions == null)
            return;

        if (sessions.size() <= 1) {
            sendToSession(session, "RESET_READY_SOLO", null, null);
        } else {
            PublishConsensus consensus = new PublishConsensus();
            consensus.initiator = session;
            consensus.initiatorName = sessionToUser.getOrDefault(session, "匿名");
            consensus.totalExpected = sessions.size() - 1;
            pendingResetConsensus.put(songId, consensus);

            Map<String, String> payload = new HashMap<>();
            payload.put("from", consensus.initiatorName);
            broadcastToOthers(songId, "RESET_PROPOSAL", payload, session);
        }
    }

    private void handleResetVote(WebSocketSession session, String songId, Boolean agree) throws IOException {
        PublishConsensus consensus = pendingResetConsensus.get(songId);
        if (consensus == null)
            return;

        if (!agree) {
            pendingResetConsensus.remove(songId);
            broadcast(songId, "RESET_REJECTED", sessionToUser.getOrDefault(session, "有人"), null, null);
            return;
        }

        consensus.approvedSessions.add(session.getId());
        if (consensus.approvedSessions.size() >= consensus.totalExpected) {
            pendingResetConsensus.remove(songId);
            sendToSession(consensus.initiator, "RESET_PERMITTED", null, null);
            broadcastToOthers(songId, "RESET_STARTING", null, consensus.initiator);
        }
    }

    private void handlePublishRequest(WebSocketSession session, String songId) throws IOException {
        Set<WebSocketSession> sessions = rooms.get(songId);
        if (sessions == null)
            return;

        if (sessions.size() <= 1) {
            sendToSession(session, "PUBLISH_READY_SOLO", null, null);
        } else {
            PublishConsensus consensus = new PublishConsensus();
            consensus.initiator = session;
            consensus.initiatorName = sessionToUser.getOrDefault(session, "匿名");
            consensus.totalExpected = sessions.size() - 1;
            pendingConsensus.put(songId, consensus);

            Map<String, String> payload = new HashMap<>();
            payload.put("from", consensus.initiatorName);
            broadcastToOthers(songId, "PUBLISH_PROPOSAL", payload, session);
        }
    }

    private void handlePublishVote(WebSocketSession session, String songId, Boolean agree) throws IOException {
        PublishConsensus consensus = pendingConsensus.get(songId);
        if (consensus == null)
            return;

        if (!agree) {
            pendingConsensus.remove(songId);
            broadcast(songId, "PUBLISH_REJECTED", sessionToUser.getOrDefault(session, "有人"), null, null);
            return;
        }

        consensus.approvedSessions.add(session.getId());
        if (consensus.approvedSessions.size() >= consensus.totalExpected) {
            pendingConsensus.remove(songId);
            sendToSession(consensus.initiator, "PUBLISH_PERMITTED", null, null);
            broadcastToOthers(songId, "PUBLISH_STARTING", null, consensus.initiator);
        }
    }

    private void broadcast(String songId, String type, Object payload, String clientId, WebSocketSession exclude) {
        Set<WebSocketSession> sessions = rooms.get(songId);
        if (sessions == null)
            return;

        ChartMessage response = new ChartMessage();
        response.setType(type);
        response.setSongId(songId);
        response.setPayload(payload);
        response.setClientId(clientId);

        try {
            String json = objectMapper.writeValueAsString(response);
            if (json != null) {
                TextMessage tm = new TextMessage(json);
                for (WebSocketSession s : sessions) {
                    if (s.isOpen() && s != exclude) { // Added exclude logic
                        s.sendMessage(tm);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastToOthers(String songId, String type, Object payload, WebSocketSession initiator) {
        broadcast(songId, type, payload, null, initiator);
    }

    private void sendToSession(WebSocketSession session, String type, Object payload, String clientId)
            throws IOException {
        ChartMessage response = new ChartMessage();
        response.setType(type);
        response.setPayload(payload);
        response.setClientId(clientId);
        String json = objectMapper.writeValueAsString(response);
        if (json != null) {
            session.sendMessage(new TextMessage(json));
        }
    }

    @Override
    public void afterConnectionClosed(@org.springframework.lang.NonNull WebSocketSession session,
            @org.springframework.lang.NonNull CloseStatus status) {
        lastHeartbeat.remove(session);
        String songId = sessionToRoom.remove(session);
        sessionToUser.remove(session);
        sessionToUserId.remove(session);
        if (songId != null) {
            Set<WebSocketSession> sessions = rooms.get(songId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    rooms.remove(songId);
                    pendingConsensus.remove(songId);
                    pendingResetConsensus.remove(songId);
                } else {
                    broadcastOnlineStatus(songId); // Broadcast status after a user leaves
                }
            }
        }
    }
}
