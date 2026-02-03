package team.javaee.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import team.javaee.common.websocket.ChartWebSocketHandler;
import java.util.Objects;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChartWebSocketHandler chartWebSocketHandler;

    public WebSocketConfig(ChartWebSocketHandler chartWebSocketHandler) {
        this.chartWebSocketHandler = chartWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(@org.springframework.lang.NonNull WebSocketHandlerRegistry registry) {
        registry.addHandler(Objects.requireNonNull(chartWebSocketHandler), "/ws/chart")
                .setAllowedOrigins("*");
    }
}
