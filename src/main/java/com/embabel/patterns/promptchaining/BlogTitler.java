package com.embabel.patterns.promptchaining;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.prompt.persona.Actor;
import com.embabel.common.ai.model.LlmOptions;

import java.util.List;

@Agent(description = "Blog Titler Agent")
public class BlogTitler {

    private final Actor<?> techWriter = new Actor<>(
    """
            You are an expert technical writer. Always give clear,
            concise, and straight-to-the-point answers.
            """,
            LlmOptions.withAutoLlm());

    public record Topics(
            List<String> topics
    ) {
    }

    public record TopicTitles(
            String topic,
            List<String> titles
    ) {
    }

    public record BlogTitles(
            List<TopicTitles> topicTitles
    ) {
    }

    @Action
    public Topics extractTopics(UserInput userInput, Ai ai) {
        return techWriter.promptRunner(ai)
                .creating(Topics.class)
                .fromPrompt("""
                        Extract 1-3 key topics from the following text:
                        %s
                        """.formatted(userInput.getContent()));
    }

    @Action
    @AchievesGoal(description="Generate Titles for Topics")
    public BlogTitles generateBlogTitles(Topics topics, OperationContext context) {
        var titles = context.parallelMap(
                topics.topics(),
                10,
                topic -> techWriter.promptRunner(context)
                .creating(TopicTitles.class)
                .fromPrompt("""
                        Generate two catchy blog titles for this topic:
                        %s
                        """.formatted(topic)));
        return new BlogTitles(titles);
    }

}
