package com.steveomurphy.tasters.rag;

import java.util.List;

public class SimilarityService {

    public double cosineSimilarity(
            List<Float> vectorA,
            List<Float> vectorB) {

        if (vectorA.size() != vectorB.size()) {
            throw new IllegalArgumentException(
                    "Vectors must have the same dimensions"
            );
        }

        double dotProduct = 0.0;
        double magnitudeA = 0.0;
        double magnitudeB = 0.0;

        for (int i = 0; i < vectorA.size(); i++) {

            double a = vectorA.get(i);
            double b = vectorB.get(i);

            dotProduct += a * b;
            magnitudeA += a * a;
            magnitudeB += b * b;
        }

        magnitudeA = Math.sqrt(magnitudeA);
        magnitudeB = Math.sqrt(magnitudeB);

        return dotProduct / (magnitudeA * magnitudeB);
    }
}
