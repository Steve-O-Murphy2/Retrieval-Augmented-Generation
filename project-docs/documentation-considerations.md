# Documentation Considerations

## Don't make the reader infer information that the text could state explicitly.

Humans are surprisingly good at resolving things like:

"These are enforced automatically."

An embedding/retrieval system has a much easier job with:

"API rate limits are enforced automatically."

This is one reason clear technical writing is increasingly valuable in AI systems.

## Headings become particularly important

Consider:

```
## Authentication

API keys are required.
```
versus:

```
## Authentication Methods for the REST API

The REST API requires an API key for authentication.
```

The second gives the retrieval system more semantic information.

But here's the wrinkle:

You don't want to keyword-stuff documentation for an embedding model.

Don't write:

`API authentication REST API authentication API key authentication`

That's terrible technical writing and doesn't magically become good because an embedding model is watching.

Instead, write naturally and explicitly.

## Tables are another interesting case

Suppose you have:


| Status | Meaning               |
|--------|-----------------------|
| 400    | Invalid request       |
| 401    | Authentication railed |
| 404    | Resource not found    |


A human can understand that instantly.

A RAG pipeline can handle tables, but depending on how your document parser/chunker processes Markdown, you can end up with awkward chunks.

For example, the retrieval system might encounter:
```
400 | Invalid request
401 | Authentication failed
404 | Resource not found
```

That's still reasonably useful.

But a poorly extracted table can become much less useful.

For important information, prose can sometimes be more robust:

"The API returns HTTP 401 when authentication fails. It returns HTTP 404 when the requested resource cannot be found."

Again, this isn't about writing for the machine. It's about making the information explicit.

## Lists have a similar issue

This:
```
## Authentication Requirements

- API key
- HTTPS
- Authorization header
- Valid account
```
is perfectly readable.

But this:
```
## Authentication Requirements

Requests to the REST API must use HTTPS and include a valid API
key in the Authorization header. The API key must belong to an
active account.
```
contains stronger semantic relationships.

For RAG, I'd generally favor the second form for important conceptual information, while still using lists when lists genuinely improve human usability.

## Documentation format and RAG ingestion

RAG doesn't require documentation to be written in Markdown.

Technical documentation might come from Markdown, Word documents, PDFs, HTML pages, knowledge bases, wikis, or other sources. The ingestion pipeline is responsible for extracting and normalizing that content before it is chunked and embedded.

A useful way to think about the pipeline is:

```text
Source documents
      │
      ▼
Source-specific parser
      │
      ▼
Normalized document
      │
      ▼
Chunking
      │
      ▼
Embeddings
      │
      ▼
Vector store
```

This means the chunking stage shouldn't need to know whether its input originally came from Markdown, DOCX, or PDF. Ideally, each source has a parser that converts it into a common document representation first.

### File format is not the same thing as content quality

A document can be technically compatible with a RAG pipeline and still be difficult to retrieve from effectively.

For example, a DOCX parser might successfully extract text from a table, but the extraction process may not preserve all of the relationships that were obvious in the original document. PDFs can present similar challenges with columns, headings, tables, and other layout-dependent information.

So there are really two separate questions:

1. **Can the ingestion pipeline extract the content?**
2. **Is the extracted content structured clearly enough to support useful retrieval?**

The first is an ingestion problem. The second is a documentation problem.

This distinction matters because changing the source format isn't necessarily the solution. A well-structured document in almost any common format can be more useful than a poorly structured document in Markdown.

### Write for people first, but make important relationships explicit

The goal isn't to write documentation in some unnatural format designed to please an embedding model.

Instead, good technical-writing practices become even more valuable.

For example:

```text
## Authentication

API keys are required.
```

is understandable, but:

```text
## Authentication Methods for the REST API

The REST API requires an API key for authentication.
```

provides more context without adding much length.

Likewise, keyword stuffing is not the answer:

```text
API authentication REST API authentication API key authentication
```

That's poor technical writing. It doesn't become better because an embedding model is reading it.

Instead, use natural language and state important relationships explicitly.

### Headings provide useful context

Headings help both humans and retrieval systems understand what a chunk is about.

A heading such as:

```text
## Authentication Methods for the REST API
```

provides more context than:

```text
## Authentication
```

especially if the heading becomes part of the chunk that is embedded.

This doesn't mean every heading needs to be long or packed with keywords. The goal is to make the subject and scope clear.

### Tables need care

Tables are excellent for humans when information naturally fits a tabular structure.

For example:

```text
| Status | Meaning               |
|--------|-----------------------|
| 400    | Invalid request       |
| 401    | Authentication failed |
| 404    | Resource not found    |
```

A parser might extract that as:

```text
400 | Invalid request
401 | Authentication failed
404 | Resource not found
```

That can still be useful, but extraction quality depends on the parser and source format.

For information where the relationship between the values is particularly important, prose can sometimes be more robust:

> The API returns HTTP 401 when authentication fails. It returns HTTP 404 when the requested resource cannot be found.

The point isn't to eliminate tables. Use tables when they improve human comprehension, but consider whether the important relationships will survive extraction and chunking.

### Lists need context too

Lists are also useful when the information is genuinely a list.

For example:

```text
## Authentication Requirements

- API key
- HTTPS
- Authorization header
- Valid account
```

is perfectly readable.

But if the relationships between those items matter, prose may communicate them more explicitly:

```text
## Authentication Requirements

Requests to the REST API must use HTTPS and include a valid
API key in the Authorization header. The API key must belong
to an active account.
```

For RAG, favor prose when it makes an important relationship clearer. Keep lists when a list is the clearest format for the reader.

### The larger principle

RAG-friendly documentation is not a new genre of technical writing.

The strongest approach is still to write clear, accurate documentation for human readers. Then consider how that documentation will be parsed, chunked, embedded, retrieved, and presented to an LLM.

In other words:

```text
Good technical writing
        +
Clear semantic relationships
        +
Thoughtful document structure
        =
Documentation that works well for both
humans and RAG systems
```
