# AI Interaction Log

## Entry 1: Phase 1 — Project Scaffolding & Build Configuration
- **Phase & Tool:** Phase 1 (Requirements & Setup), Lovable AI Assistant
- **Prompt:** "Set up project directory structure and configure Maven pom.xml with JavaFX 21, SQLite JDBC, and JUnit 5 targeting Java 17 for a 3-layer desktop POS application."
- **Outcome & Judgement:** Accepted with modification. Configured `<maven.compiler.release>17</maven.compiler.release>` to guarantee Java 17 LTS compatibility while building under JDK 25. Created `.gitignore` excluding `config.properties` and `*.db` to strictly satisfy credential and secret protection constraints.
