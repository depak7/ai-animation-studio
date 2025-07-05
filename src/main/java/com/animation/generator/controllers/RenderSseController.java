package com.animation.generator.controllers;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/render")
public class RenderSseController {

    private final Map<String, FluxSink<String>> sinks = new ConcurrentHashMap<>();

    @GetMapping(value = "/stream", produces = "text/event-stream;charset=UTF-8")
    public Flux<ServerSentEvent<String>> stream(@RequestParam String conversationId) {
        return Flux.<String>create(sink -> {
            sinks.put(conversationId, sink.onDispose(() -> sinks.remove(conversationId)));
        }).map(message ->
                ServerSentEvent.builder(message).build()
        );
    }

    public void sendLog(String conversationId, String message) {
        FluxSink<String> sink = sinks.get(conversationId);
        if (sink != null) {
            sink.next(message);
        }
    }

    public void complete(String conversationId) {
        FluxSink<String> sink = sinks.get(conversationId);
        if (sink != null) {
            sink.complete();
        }
    }
}
