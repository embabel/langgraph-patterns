package com.embabel.patterns.reflection;

import com.embabel.agent.api.common.workflow.loop.RepeatUntilAcceptableBuilder;
import com.embabel.agent.api.common.workflow.loop.TextFeedback;
import com.embabel.agent.core.Agent;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DraftAndRefine {

    public record Draft(
            String content
    ) {
    }

    @Bean
    public Agent draftAndRefineAgent() {
        return RepeatUntilAcceptableBuilder
                .returning(Draft.class)
                .consuming(UserInput.class)
                .withMaxIterations(7)
                .withScoreThreshold(.99)
                .repeating(tac -> {
                    return tac.ai()
                            .withAutoLlm()
                            .withId("draft")
                            .createObject(
                                    """
                                            You are an assistant helping to complete the following task:
                                            
                                            Task:
                                            %s
                                            
                                            Current Draft:
                                            %s
                                            
                                            Feedback:
                                            %s
                                            
                                            Instructions:
                                            - If there is no draft and no feedback, generate a clear and complete response to the task.
                                            - If there is a draft but no feedback, improve the draft as needed for clarity and quality.
                                            - If there is both a draft and feedback, revise the draft by incorporating the feedback directly.
                                            - Always produce a single, improved draft as your output.
                                            """.formatted(
                                            tac.getInput().getContent(),
                                            tac.lastAttemptOr("no draft yet"),
                                            tac.lastFeedbackOr("no feedback yet")),
                                    Draft.class);
                })
                .withEvaluator(tac -> {
                    return tac.ai().withAutoLlm()
                            .withId("evaluate_draft")
                            .createObject("""
                                            Evaluating the following draft, based on the given task.
                                            Score it from 0.0 to 1.0 (best) and provide constructive feedback for improvement.
                                            
                                            Task:
                                            %s
                                            
                                            Draft:
                                            %s
                                            """.formatted(
                                            tac.getInput().getContent(),
                                            tac.getResultToEvaluate()),
                                    TextFeedback.class);
                })
                .buildAgent("draft_and_refine_agent", "An agent that drafts and refines content");
    }

}
