---
title: "AI Commits IntelliJ Plugin"
date: 2025-04-01T19:57:00+02:00
lastmod: 2025-07-23T18:32:00+02:00

categories:
  - posts
  - articles

tags:
  - plugin
  - intellij
---

[AI Commits IntelliJ Plugin](https://github.com/Blarc/ai-commits-intellij-plugin) is
an [IntelliJ IDEA](https://www.jetbrains.com/idea/) plugin that generates commit messages using `git diff` and
large language models (LLMs). The idea came to me at the start of the LLM hype when I noticed my colleague's commit
messages at work were completely non-descriptive—usually just one word. That's when I thought maybe an LLM could write
better commit messages, and I did what I usually do: I googled for existing solutions.

## Motivation

At the time, a popular project
for generating commits with LLM help was [AI commits](https://github.com/Nutlope/aicommits). This seemed promising, but
I wanted something that better integrated with our development environment—specifically, something that integrated with
the IDE we use: [IntelliJ IDEA](https://www.jetbrains.com/idea/). I checked the issues on the AI commits project and
found an open issue for an IntelliJ plugin. Since I had some experience building IntelliJ plugins already and because
other people showed interest in the issue, I started the project myself.

[//]: # (// @formatter:off)
{{< dynamic-image title="Open issue on AI commits project." alt="Open issue on AI commits project." dark="images/issue_dark.png" light="images/issue_light.png" noResize="true" >}}

[//]: # (// @formatter:on)

## First implementation

At the time, the main LLM provider was [Open AI](). Applications could integrate with it via a REST API. I didn't want
to implement the client from scratch (like I did with [GitLab Template Lint Plugin]()) and found a promising client
implementation written in Kotlin: [openai-kotlin](https://github.com/aallam/openai-kotlin). I've implemented a simple
settings UI where the users could set the OpenAI token and the locale and added an action to the commit dialog that ran
the command `git diff` and sent the request to the LLM. The prompt for the LLM was hardcoded to:

```text
Write an insightful but concise Git commit message
in a complete sentence in present tense for the 
following diff without prefacing it with anything,
the response must be in the language {locale}:
{diff}
```

[//]: # (// @formatter:off)
{{< dynamic-image title="Demo of the early version." alt="Demo of the early version." dark="images/demo_dark.gif" light="images/demo_light.gif" noResize="true" >}}

[//]: # (// @formatter:on)

## IntelliJ IDEA vs Git diff

In IntelliJ IDEA the commit dialog allows users to decide what they would like to commit. The user can decide for each
file and even for each line of code that was changed, whether to commit it or not. The `git diff` approach I used,
always retrieved the whole diff, even if the user didn't want to commit all the changes.

So I've dug into IntelliJ API and found a way to retrieve only the changes that were selected by the user in the commit
dialog. The solution was in getting the `commitWorkflowHandler` from the action data context which could then be used to
retrieve the included changes.

{{< highlight kotlin "linenos=table, hl_lines=4-5" >}}
{{< readfile-rel file="resources/commitWorkflowHandler.kt" >}}
{{< /highlight >}}

## Prompts

Hardcoding the prompt was not ideal, since users can have different preferences how the commit message should look. That
is why the next step was adding a table to settings for configuring different prompts.

[//]: # (// @formatter:off)
{{< dynamic-image title="Prompts table." alt="Prompts table." dark="images/prompts_dark.png" light="images/prompts_light.png" noResize="true" >}}

[//]: # (// @formatter:on)

It was soon clear that the prompts should support more variables that are dynamically evaluated before the request is
sent to the LLM. The first addition to existing `{locale}` and `{diff}` was `{branch}`, which resolves to the current
Git branch and in the case of multiple repositories to the most common Git branch. Later on, I've added:

- `hint`: resolves to the text that was in the commit message text field before running the action
- `taskId`: ID of the task from
  the [IntelliJ tasks tracker](https://www.jetbrains.com/help/idea/managing-tasks-and-context.html#work-with-tasks)
- `taskSummary`: summary of the task
- `taskDescription`: description of the task
- `taskTimeSpent`: time spent on the task

[//]: # (// @formatter:off)
{{< dynamic-image title="Prompt edit." alt="Prompt edit." dark="images/prompt_edit_dark.png" light="images/prompt_edit_light.png" noResize="true" >}}

[//]: # (// @formatter:on)

## LangChain4j :parrot:

After the first release of OpenAI, new LLM providers soon began to appear. For some providers, the same API client
worked since their API was the same as OpenAI, but for many this was not true. This is when I learned
about [langchain4j](https://docs.langchain4j.dev/) a Java library of LLM provider clients that is an alternative
to the Python library [langchain](https://github.com/langchain-ai/langchain).

The library contains clients for a lot of different LLM providers and offers a unified interface for all of them. This
allowed me to refactor the code in a way that makes adding new clients much easier. For each client I need to add four
classes:

- `configuration`: extends the `LLMClientConfiguration` class and contains the configuration properties for the client.
- `panel`: extends the `LLMClientPanel` class and implements the UI for configuring the client.
- `service`: extends the `LLMClientService` class and provides methods for building the client.
- `sharedState`: extends the `LLMClientSharedState` class and enables persisting data shared between the clients
  of the same type.

I've also added a new settings table where the users can configure LLM client configurations:

[//]: # (// @formatter:off)
{{< dynamic-image title="Configurations table." alt="Configurations table." dark="images/configurations_dark.png" light="images/configurations_light.png" noResize="true" >}}

{{< dynamic-image title="Add or edit configuration." alt="Add or edit configuration." dark="images/add_configuration_dark.png" light="images/add_configuration_light.png" noResize="true" >}}

[//]: # (// @formatter:on)

## Conclusion

The AI Commits IntelliJ plugin has been fun to build. I've learned a lot of new stuff regarding IntelliJ platform,
Gradle build system, LLMs, Kotlin coroutines, Kotlin UI DSL and plugin design. The plugin has a decent user base that
provides feedback and ideas for new features. This way the plugin is constantly evolving and improving. If you'd like to
check it out, you can find it on [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/21335-ai-commits) and
if you like it, you can star it on [GitHub](https://github.com/Blarc/ai-commits-intellij-plugin).
