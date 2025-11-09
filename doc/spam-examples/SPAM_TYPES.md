# Spam Type Classification & Strategy Design

Based on analysis of 91 spam examples, here are the distinct spam types identified:

## Type 1: Minimal Contact Info Spam (Minimalist Business Listing)
**Characteristics:**
- Very low node count (1-2 topics)
- Contact information only (address, phone, website, email)
- Minimal or no marketing text
- Often just structured contact data

**Examples:**
- `map-1884075.json` (iFix New York - Bay Ridge) - 1 topic, contact info only
- `map-1910128.json` (Evolution AV) - 2 topics, minimal content

**Detection Strategy:** `MinimalContactSpamStrategy`
- Rule: ≤2 topics AND (address + phone + website) OR (address + phone + email)
- Rationale: Legitimate businesses don't create single-topic maps with just contact info

---

## Type 2: Service Directory Spam (Business Listing with Marketing)
**Characteristics:**
- Low node count (2-3 topics typically)
- Complete contact bundle (address + phone + website/email)
- Marketing/service descriptions
- Professional business language

**Examples:**
- `map-1912210.json` (True Millwork) - 2 topics, contact + marketing
- `map-1943823.json` (Face Skin & Body Bar) - 2 topics, contact + marketing
- `map-1931070.json` (Extreme Escape) - 2 topics, contact + marketing
- Most of Bucket A (89 examples)

**Detection Strategy:** `ServiceDirectorySpamStrategy` ✅ (Already implemented)
- Rule 1: Complete contact info (address + phone + website/email) + ≤5 topics
- Rule 2: Contact info + keyword stuffing indicators
- Rule 3: High contact patterns + marketing keywords + ≤5 topics

---

## Type 3: Link Farm/SEO Spam (Extreme Link Stuffing)
**Characteristics:**
- Hundreds of URLs in content
- Link stuffing for SEO purposes
- Often gambling/casino or adult content
- May include contact info but dominated by links

**Examples:**
- `map-1850676.json` (DA88) - 2 topics, 200+ URLs, gambling site

**Detection Strategy:** `LinkFarmSpamStrategy`
- Rule: URL count ≥ 20 OR (URL count ≥ 10 AND node count ≤ 3)
- Rationale: Legitimate content doesn't have excessive URLs in minimal structure

---

## Type 4: Keyword-Stuffed Marketing Spam (Verbose SEO Content)
**Characteristics:**
- Longer marketing descriptions (500+ words)
- Excessive keyword repetition
- Contact info included
- Professional-sounding but SEO-optimized text

**Examples:**
- `map-1858323.json` (Kaco Systems) - 3 topics, 514 words, keyword stuffing
- `map-1848035.json` (iFix New York) - 2 topics, extensive "near me" keyword stuffing

**Detection Strategy:** `KeywordStuffingSpamStrategy`
- Rule 1: Separator count (pipes/commas) ≥ 25 AND contact info present
- Rule 2: "Near me" repetitions ≥ 10 AND contact info present
- Rule 3: Location variants ≥ 8 AND contact info present
- Rule 4: Word count ≥ 500 AND marketing keywords ≥ 3 AND ≤5 topics

---

## Type 5: Franchise/Location Spam (Multi-location Listings)
**Characteristics:**
- Same business name with different locations
- Similar structure across multiple maps
- Contact info for each location
- Often from same spammer account

**Examples:**
- Multiple "Speedpro Signs" locations (Kelowna, Burnaby, Calgary, etc.)
- Multiple "Doctors Implants" locations
- Multiple "Extreme Escape" locations

**Detection Strategy:** `FranchiseSpamStrategy`
- Rule: Same creator + similar title pattern + contact info + ≤5 topics
- Rationale: Legitimate franchises don't create separate maps for each location

---

## Implementation Status

✅ **Implemented:**
1. **ServiceDirectorySpamStrategy** - Catches business directory spam (89 examples)
   - Covers minimal contact info spam (≤5 topics with complete contact bundle)
   - Covers keyword stuffing patterns
   - Covers service directory listings
2. **LinkFarmSpamStrategy** - Catches link farm/SEO spam (NEW)
   - Detects excessive URLs (20+ URLs or 10+ URLs in low-structure maps)

⏳ **Future Enhancements:**
3. **KeywordStuffingSpamStrategy** - Enhanced keyword stuffing detection (partially covered by ServiceDirectorySpamStrategy)
4. **FranchiseSpamStrategy** - Multi-location spam detection (requires account-level analysis)

**Note:** `MinimalContactSpamStrategy` was removed as it duplicated `ServiceDirectorySpamStrategy` functionality. The service directory strategy already covers minimal contact info spam (Rule 1: complete contact info with ≤5 topics).

## Recommended Strategy Execution Order

The strategies are executed in order by `SpamDetectionService`:
1. **LinkFarmSpamStrategy** - Catches extreme cases first (highest confidence, fastest)
2. **ServiceDirectorySpamStrategy** - Catches most common pattern (covers 89 examples, including contact info spam)
3. **Other existing strategies** - DescriptionLength, FewNodesWithContent, etc.

**Note:** `ContactInfoSpamStrategy` was removed as it duplicated `ServiceDirectorySpamStrategy` functionality. The service directory strategy already covers contact info spam patterns (Rule 1: complete contact info with ≤5 topics).

## Strategy Design Principles

- **Pattern-based detection**: Focus on structural/content patterns, not specific examples
- **Configurable thresholds**: All thresholds tunable via `application.yml`
- **Layered approach**: Multiple strategies combine for confidence
- **False positive prevention**: Node count exemptions, multiple signal requirements
- **Performance**: Early exits for obvious cases, efficient pattern matching

