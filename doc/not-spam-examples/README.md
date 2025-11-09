# Non-Spam Regression Fixtures

This directory contains real-world or representative public maps that should *not* be flagged by the spam-detection heuristics.

## Contents

- `map-1704774.json` – Curated list of links to high-authority platforms (Google, Wikipedia, GitHub, etc.). Previously triggered the link-farm heuristic; now serves as a regression test to prevent false positives when maps aggregate widely trusted destinations.

Use these payloads in tests to ensure legitimate, content-rich maps remain accessible.
