# Spam Detection Examples

This folder captures real public map payloads that exposed gaps in the spam-detection heuristics. Each sample includes the raw metadata returned by the WiseMapping public API so the scenarios can be replayed locally during testing.

## Samples

- `map-1919102.json` – large marketing map that previously bypassed detection because of the node-count exemption.
- `map-1905891.json` – small contact-info map that avoided keyword combination checks due to formatting quirks.
- `map-1848035.json` – extensive service-directory style map with bullet-prefixed contact data and keyword stuffing.

Use these payloads to regression-test strategy updates or to craft unit tests around specific spam scenarios.
