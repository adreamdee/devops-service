package io.choerodon.devops.api.ws.describe.agent;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.AGENT_DESCRIBE;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;

import io.choerodon.devops.api.ws.AbstractSocketInterceptor;
import io.choerodon.devops.api.ws.WebSocketTool;

/**
 * @author zmf
 * @since 20-5-9
 */
@Component
public class AgentDescribeSocketInterceptor extends AbstractSocketInterceptor {
    @Override
    public String processor() {
        return AGENT_DESCRIBE;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
        HttpServletRequest httpServletRequest = servletRequest.getServletRequest();

        //校验ws连接参数是否正确
        WebSocketTool.checkKey(httpServletRequest);
        WebSocketTool.checkGroup(httpServletRequest);
        WebSocketTool.checkClusterId(httpServletRequest);

        return true;
    }
}
