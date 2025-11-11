# Non-Spam Regression Fixtures

This directory contains real-world or representative public maps that should *not* be flagged by the spam-detection heuristics.

## Contents

- `map-1704774.json` – Curated list of links to high-authority platforms (Google, Wikipedia, GitHub, etc.). Previously triggered the link-farm heuristic; now serves as a regression test to prevent false positives when maps aggregate widely trusted destinations.
- `map-1400591.json` – Activity-focused design sprint breakdown containing no outbound links. Ensures content-rich, process-oriented maps without external URLs are never misclassified as spam.
- `map-1972915.json` – Educational mindmap about branches of philosophy ("Ramas de la Filosofía") with rich HTML content in notes. Contains extensive HTML formatting with nested elements, data attributes, and complex structures from AI-generated content. Previously triggered HTML content spam detection due to high HTML element count; serves as a regression test to prevent false positives for legitimate educational content with rich formatting.

Use these payloads in tests to ensure legitimate, content-rich maps remain accessible.
