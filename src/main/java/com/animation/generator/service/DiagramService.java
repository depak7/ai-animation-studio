package com.animation.generator.service;

import com.animation.generator.dtos.UserRequest;
import com.animation.generator.objects.Diagram;
import com.animation.generator.repository.DiagramRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DiagramService {

    @Autowired
    private DiagramRepository diagramRepo;
    @Autowired
    private AIService aiService;

    public Diagram createDiagram(UserRequest promptRequest) {
        Diagram diagram = aiService.generateDiagramFromPrompt(promptRequest);
        diagram.setPrompt(promptRequest.getPrompt());
        diagram.setUserId(promptRequest.getUserId());
        diagramRepo.save(diagram);
        return diagram;
    }

}
