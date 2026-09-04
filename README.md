# Retrieval-Augmented Generation

> Current milestone: In-memory vector database
  
A hands-on exploration of RAG pipelines using Java.

## What is RAG?

RAG (Retrieval-Augmented Generation) combines information retrieval with an LLM: it retrieves relevant content from a knowledge source and provides it to the model as context for generating an answer.

In short: retrieve relevant information first, then generate an answer grounded in that information.


## What This Project Does

This project builds a small RAG pipeline from the ground up. It
loads Markdown documentation, chunks the content, generates
embeddings, retrieves relevant chunks, and eventually uses an LLM
to generate an answer from the retrieved content.

![RAG pipeline](images/pipeline.png)

## The RAG Pipeline

1. Load documents
2. Chunk documents
3. Generate embeddings
4. Store embeddings
5. Retrieve relevant chunks
6. Generate an answer

## Project Goals

- Understand the components of a RAG pipeline
- Explore how document structure affects retrieval
- Learn how embeddings enable semantic search
- Implement the pipeline in Java

## Documentation

- [RAG Concepts](project-docs/rag-concepts.md)
- [Java Implementation Walkthrough](project-docs/java-walkthrough.md)
- [Documentation Considertions](project-docs/documentation-considerations.md)
