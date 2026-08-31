package com.steveomurphy.tasters.rag;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import com.openai.models.embeddings.EmbeddingCreateParams;
import com.openai.models.embeddings.EmbeddingModel;

import java.util.List;

public class EmbeddingService {

    private final OpenAIClient client;

    public EmbeddingService() {
        client = OpenAIOkHttpClient.fromEnv();
    }

    public List<Float> createEmbedding(String text) {

        EmbeddingCreateParams params = EmbeddingCreateParams.builder()
                .input(text)
                .model(EmbeddingModel.TEXT_EMBEDDING_3_SMALL)
                .build();

        CreateEmbeddingResponse response =
                client.embeddings().create(params);

        return response.data()
                .get(0)
                .embedding();
    }
}