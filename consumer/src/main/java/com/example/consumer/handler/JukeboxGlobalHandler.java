

package com.example.consumer.handler;

import com.example.common_lib.model.kafka.annotations.EventApiHandler;
import com.example.common_lib.model.kafka.annotations.EventApiHandlerClass;
import com.example.common_lib.model.kafka.model.ListenerEvent;
import com.example.consumer.dto.JukeboxEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@EventApiHandlerClass
public class JukeboxGlobalHandler {


    @EventApiHandler(
            eventNames = {"PLAY_REQUEST_EVENT"},
            payloadType = JukeboxEventPayload.class,
            eventVersions = {1})
    public void handleCreatePlayRequestEvent(ListenerEvent<JukeboxEventPayload> event) {
        System.out.println("🎶 Received event success: " + event.getName());

    }
}

