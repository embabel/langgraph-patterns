package com.embabel.patterns.routing;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@Agent(description = "Perform sentiment analysis")
public class ClassificationAgent {

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.SIMPLE_NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type"
    )
    public sealed interface Sentiment {
    }

    public static final class Positive implements Sentiment {
    }

    public static final class Negative implements Sentiment {
    }

    public record Response(String message) {
    }

    private enum SentimentType {
        POSITIVE,
        NEGATIVE;

        public Sentiment toSentiment() {
            return switch (this) {
                case POSITIVE -> new Positive();
                case NEGATIVE -> new Negative();
            };
        }
    }

    @Action
    public Sentiment classify(UserInput userInput, Ai ai) {
        var type = ai.withAutoLlm()
                .createObject("""
                                Determine if the sentiment of the following text is positive or negative.
                                Text: "%s"
                                """.formatted(userInput.getContent()),
                        SentimentType.class);
        return type.toSentiment();
    }

    @Action
    public Response encourage(
            UserInput userInput,
            Positive sentiment,
            Ai ai) {
        return ai.withAutoLlm()
                .createObject("""
                        Generate an encouraging response to the following positive text:
                        %s
                        """.formatted(userInput.getContent()), Response.class);
    }

    @Action
    public Response help(
            UserInput userInput,
            Negative sentiment,
            Ai ai) {
        return ai.withAutoLlm()
                .createObject("""
                        Generate a supportive response to the following negative text:
                        %s
                        """.formatted(userInput.getContent()), Response.class);
    }

    @AchievesGoal(
            description = "Generate a response based on discerning sentiment in user input")
    @Action
    public Response done(Response response) {
        return response;
    }

}
