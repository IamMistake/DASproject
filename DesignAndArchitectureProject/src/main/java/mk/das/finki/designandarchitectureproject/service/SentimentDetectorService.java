package mk.das.finki.designandarchitectureproject.service;

public interface SentimentDetectorService {

    // Analyzes the sentiment of the given text and returns a sentiment label (e.g., positive, negative, neutral).
    // Currently, this method is static and returns null by default. It needs to be implemented or overridden for functionality.
    static String analyzeSentiment(String text) {
        return null;
    }
}

