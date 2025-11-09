# Spam Detection Examples

This folder captures real public map payloads that exposed gaps in the spam-detection heuristics. Each sample includes the raw metadata returned by the WiseMapping public API so the scenarios can be replayed locally during testing.

## Samples

- `map-1919102.json` – large marketing map that previously bypassed detection because of the node-count exemption.
- `map-1905891.json` – small contact-info map that avoided keyword combination checks due to formatting quirks.
- `map-1848035.json` – extensive service-directory style map with bullet-prefixed contact data and keyword stuffing.
- `map-1902545.json` – UpDown Desk Australia: Business listing with address, phone (+61 format), website, email, 2 topics. Exposed gap in international phone detection.
- `map-1901495.json` – Adventure Yogi: Business listing with address, UK phone (01273 format), website, 2 topics. Exposed gap in UK phone detection.
- `map-1924052.json` – DoughGirl: Business listing with address, UK phone (01754 format), website, 3 topics. Exposed gap in UK phone detection.
- `map-1841477.json` – Welcome Ben (Made's Plumbing): Complete contact info with address, phone, website, email, 2 topics.
- `map-1835747.json` – Business listing with complete contact info.
- `map-1848035.json` – Service directory style map with contact data and keyword stuffing.
- `map-1940825.json` – Business listing with complete contact info.
- `map-1912233.json` – Business listing with complete contact info.
- `map-1922153.json` – Business listing with complete contact info.
- `map-1884075.json` – Minimal contact info spam (1-2 topics).
- `map-1943823.json` – Business listing with complete contact info.
- `map-1923339.json` – Business listing with complete contact info.
- `map-1935323.json` – Speedpro Signs Toronto: Complete contact info, 3 topics.
- `map-1943830.json` – SpeedPro Signs Fort McMurray: Complete contact info, 2 topics.
- `map-1909519.json` – Welcome -: Complete contact info, 2 topics.
- `map-1911897.json` – FASTCHECK Criminal Record & Fingerprint Specialists: Complete contact info, 3 topics.
- `map-1858501.json` – IroSteel Metal Fabrication: Complete contact info, 2 topics.

## Testing

Automated tests in `ServiceDirectorySpamStrategyTest.java` load these JSON files and verify they are detected as spam. Run tests with:

```bash
mvn test -Dtest=ServiceDirectorySpamStrategyTest
```

Use these payloads to regression-test strategy updates or to craft unit tests around specific spam scenarios.
