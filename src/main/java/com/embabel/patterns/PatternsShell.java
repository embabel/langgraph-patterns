package com.embabel.patterns;

import com.embabel.agent.api.common.autonomy.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.patterns.promptchaining.BlogTitler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public record PatternsShell(AgentPlatform agentPlatform) {

    @ShellMethod("Generate blog titles")
    public String blogs(
            @ShellOption(help = "topic", defaultValue = """
                LangGraph introduces a graph-based paradigm for building LLM-powered agents. "
                It allows developers to create modular, debuggable, and reliable agent workflows "
                using nodes, edges, and state passing.
                While this is an obvious approach, Embabel's GOAP planning is
                far superior.
                """) String topic) {
        BlogTitler.BlogTitles blogs = AgentInvocation.create(agentPlatform, BlogTitler.BlogTitles.class)
                .invoke(new UserInput(topic));
        return format(blogs);
    }

    // Use JSON pretty printer to format the result
    private String format(Object result) {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}
