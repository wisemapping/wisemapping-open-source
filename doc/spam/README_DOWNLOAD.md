# How to Download Maps Manually

Since the API is protected by Vercel's security checkpoint, you can download the maps manually:

## Method 1: Browser Console

1. Open your browser and navigate to: `https://app.wisemapping.com/c/maps/1841477/public`
2. Open Developer Tools (F12)
3. Go to Console tab
4. Run this JavaScript:

```javascript
// Replace 1841477 with the map ID you want to download
const mapId = 1841477;
fetch(`/api/restful/maps/${mapId}/metadata?xml=true`)
  .then(r => r.json())
  .then(data => {
    const blob = new Blob([JSON.stringify(data, null, 2)], {type: 'application/json'});
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `map-${mapId}.json`;
    a.click();
    console.log(`Downloaded map-${mapId}.json`);
  });
```

5. Repeat for each map ID, changing the `mapId` variable

## Method 2: Browser Network Tab

1. Open Developer Tools (F12)
2. Go to Network tab
3. Navigate to: `https://app.wisemapping.com/c/maps/1841477/public`
4. Find the request to `/api/restful/maps/1841477/metadata?xml=true`
5. Right-click → Copy → Copy response
6. Save as `map-1841477.json`

## Method 3: Using curl with cookies (if you're logged in)

If you're logged into the app, you can use cookies:

```bash
# First, get your cookies from browser (copy as curl command from Network tab)
# Then use:
curl -H "Cookie: YOUR_COOKIES_HERE" \
     -H "Accept: application/json" \
     "https://app.wisemapping.com/api/restful/maps/1841477/metadata?xml=true" \
     > map-1841477.json
```

## Map IDs to Download

- 1841477
- 1854919
- 1388292
- 1902545
- 1936562
- 1553987
- 1961176
- 1804233
- 1901495
- 1924052

## After Downloading

Once you have the JSON files, you can analyze them using:

```bash
python3 analyze_maps_precise.py
```

The script will automatically detect and use the downloaded map files.

