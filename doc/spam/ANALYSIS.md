# Spam Detection Pattern Analysis

## Common Patterns Identified

### 1. Contact Information Structure (100% of examples)
- **Street address** with postal/zip codes
- **Phone number** (various international formats)
- **Website URL** (https://domain.com)
- **Email address** (often generic providers like @hyperdrivemail.com)
- **Formatting**: Often uses bullets (•), line breaks, or structured sections

### 2. Content Structure
- **Very few nodes**: Typically 1-3 topics total
- **Central topic**: Business/service name
- **Child topics**: Contact information + marketing description
- **Low complexity**: Minimal branching structure

### 3. Content Characteristics
- **Marketing language**: Service descriptions, business pitches
- **Keyword stuffing**: Some examples contain extensive "near me" variations, location keywords
- **Geographic references**: City names, neighborhoods, states/provinces
- **Professional tone**: Business/service descriptions

### 4. Email Domain Patterns
- Generic email services: `@hyperdrivemail.com`, `@outlook.com`, `@gmail.com`
- Business domain emails: Less common in spam examples

### 5. Title Patterns
- Business/service names
- Often includes location: "Business Name - Location"

## Detection Strategy Requirements

A comprehensive strategy should detect:

1. **Contact Info Combination**: Address + Phone + (Website OR Email)
2. **Low Node Count**: Maps with ≤3 topics containing contact info
3. **Marketing Content**: Heavy use of marketing keywords with contact info
4. **Keyword Stuffing**: Excessive repetition of location/service keywords
5. **Structure Pattern**: Central topic (business name) + child topic(s) with contact info

## Strategy Design Principles

- **Pattern-based, not case-by-case**: Detect structural and content patterns
- **Configurable thresholds**: Allow tuning without code changes
- **Layered detection**: Multiple signals combine for confidence
- **False positive prevention**: Exempt legitimate content (high node count, no marketing indicators)

