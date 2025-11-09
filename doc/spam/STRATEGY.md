# Service Directory Spam Detection Strategy

## Overview

The `ServiceDirectorySpamStrategy` detects spam maps that function as business listings with contact information and marketing content. This strategy is designed to catch patterns common to service directory spam without relying on case-by-case examples.

## Detection Rules

The strategy uses multiple detection rules that combine to identify spam:

### Rule 1: Complete Contact Info with Low Node Count
**Triggers when:**
- Map contains: Address + Phone + (Website OR Email)
- AND topic count ≤ 5

**Rationale:** Legitimate business listings typically have more structure. Spam maps are minimal (1-3 topics) with complete contact info.

### Rule 2: Contact Info + Keyword Stuffing
**Triggers when:**
- Map contains contact info (phone, website, or email)
- AND any of:
  - Separator count ≥ 25 (pipes `|` weighted 2x, commas weighted 1x)
  - "Near me" repetitions ≥ 10
  - Location variants ≥ 8

**Rationale:** Keyword stuffing is a common SEO spam technique. Legitimate content doesn't repeat location phrases excessively.

### Rule 3: High Contact Patterns + Marketing Content
**Triggers when:**
- Contact pattern matches ≥ 4
- AND marketing keywords ≥ 3
- AND topic count ≤ 5

**Rationale:** Excessive contact patterns combined with marketing language in a simple structure indicates spam.

### Rule 4: Extreme Keyword Stuffing
**Triggers when:**
- Separator count ≥ 50 (2x threshold)
- AND location variants ≥ 16 (2x threshold)

**Rationale:** Very high thresholds catch extreme keyword stuffing even without contact info.

## Configuration

All thresholds are configurable via `application.yml`:

```yaml
app:
  batch:
    spam-detection:
      service-directory:
        keyword-stuffing-separators: 25
        location-variants: 8
        near-me-repetitions: 10
```

## Integration

The strategy integrates with existing spam detection infrastructure:

- **Uses `SpamContentExtractor`** for content normalization and marketing keyword detection
- **Respects `min-nodes-exemption`** but bypasses it for marketing-heavy content
- **Works alongside other strategies** - runs as part of the spam detection chain
- **Returns `SpamStrategyType.SERVICE_DIRECTORY`** for tracking and metrics

## Pattern-Based Design

The strategy detects **structural and content patterns**, not specific examples:

- ✅ Detects contact information combinations
- ✅ Detects keyword stuffing patterns
- ✅ Detects low-complexity structures with marketing content
- ✅ Configurable thresholds for tuning
- ❌ Does NOT rely on specific business names or domains
- ❌ Does NOT require case-by-case rules

## False Positive Prevention

The strategy includes safeguards to prevent false positives:

- **Node count exemption**: Maps with >15 nodes (configurable) are exempt unless marketing-heavy
- **Multiple signal requirement**: Most rules require multiple indicators
- **High thresholds**: Keyword stuffing thresholds are set conservatively
- **Marketing detection**: Uses existing marketing keyword detection to avoid flagging legitimate content

## Testing

Test cases should use the examples in `doc/spam/` to verify detection. The strategy should catch:

- Maps with complete contact info and low node count
- Maps with keyword stuffing (pipes, "near me" repetitions)
- Maps with excessive location variants
- Maps with marketing content + contact patterns

Legitimate maps should pass:
- High node count maps without marketing indicators
- Maps with contact info but no keyword stuffing
- Maps with marketing content but complex structure

