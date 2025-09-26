/*
 *    Copyright [2022] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.wisemapping.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

/**
 * WebSocket security configuration for STOMP messaging.
 * Defines security rules for WebSocket connections and message handling.
 */
@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        // Configure security for different message destinations
        
        // Allow authenticated users to subscribe to mind map updates
        messages
            .simpDestMatchers("/topic/mindmap/*/updates").authenticated()
            .simpDestMatchers("/topic/mindmap/*/presence").authenticated()
            .simpDestMatchers("/topic/mindmap/*/cursor").authenticated()
            .simpDestMatchers("/topic/mindmap/*/typing").authenticated()
            
            // Allow authenticated users to send messages to mind map channels
            .simpDestMatchers("/app/mindmap/*/update").authenticated()
            .simpDestMatchers("/app/mindmap/*/presence").authenticated()
            .simpDestMatchers("/app/mindmap/*/cursor").authenticated()
            .simpDestMatchers("/app/mindmap/*/typing").authenticated()
            
            // Allow authenticated users to send private messages
            .simpDestMatchers("/app/private").authenticated()
            
            // Allow users to subscribe to their own private message queue
            .simpDestMatchers("/user/queue/private").authenticated()
            
            // Require authentication for all other message destinations
            .anyMessage().authenticated();
    }

    @Override
    protected boolean sameOriginDisabled() {
        // Disable same origin policy for WebSocket connections
        // This should be configured properly in production based on your CORS requirements
        return true;
    }
}
