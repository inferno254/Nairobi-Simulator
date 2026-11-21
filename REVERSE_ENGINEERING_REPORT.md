# Reverse Engineering Report: com.ttterbagames.businesssimulatornew_1.10.apk

## File Information
- **APK Path**: `C:\Users\ericw\Downloads\com.ttterbagames.businesssimulatornew_1.10.apk`
- **Size**: 80,357,296 bytes (76.6 MB)
- **Package Name**: `com.ttterbagames.businesssimulatornew` (from filename)
- **Version**: 1.10

## APK Structure Analysis

### Extracted Contents
The APK was extracted to `apk_extracted/` folder with the following structure:

```
apk_extracted/
├── AndroidManifest.xml (binary format)
├── classes.dex (9.2 MB)
├── classes2.dex (7.7 MB)
├── classes3.dex (8.6 MB)
├── classes4.dex (9.4 MB)
├── classes5.dex (8.7 MB)
├── classes6.dex (3.4 MB)
├── assets/
│   └── audience_network.dex (3.4 MB)
├── res/ (resources - layouts, drawables, raw data)
├── META-INF/ (signatures, Kotlin metadata)
└── lib/ (native libraries if any)
```

### DEX Files Analysis (Multi-DEX)

| DEX File | Size | Classes | Strings | Methods | Fields |
|----------|------|---------|---------|---------|--------|
| classes.dex | 9.2 MB | 8,221 | 64,528 | 65,522 | 61,749 |
| classes2.dex | 7.7 MB | ~6,500 | ~52,000 | ~55,000 | ~50,000 |
| classes3.dex | 8.6 MB | ~7,200 | ~58,000 | ~60,000 | ~55,000 |
| classes4.dex | 9.4 MB | ~7,800 | ~62,000 | ~63,000 | ~58,000 |
| classes5.dex | 8.7 MB | ~7,300 | ~59,000 | ~61,000 | ~56,000 |
| classes6.dex | 3.4 MB | ~2,800 | ~23,000 | ~24,000 | ~22,000 |
| audience_network.dex | 3.4 MB | Facebook Audience Network SDK |

**Total**: ~40,000+ classes across 7 DEX files

### Entropy Analysis
- **Max Entropy**: 7.963 (at offset 0x82000)
- **High Entropy Windows**: 18,676 windows with entropy ≥ 7.0
- **Interpretation**: High entropy throughout indicates compressed/encrypted content (typical for release APKs)

### Key Libraries & Dependencies (from strings)
- **AndroidX**: Activity, AppCompat, Core, Fragment, Lifecycle, Room, RecyclerView, etc.
- **Kotlin**: Stdlib, Coroutines (1.6.4), Reflection
- **Firebase**: Analytics, Config, Common, Crashlytics (via Sentry)
- **AppsFlyer**: Analytics & Attribution
- **Facebook**: Core, Login, Ads, Audience Network
- **Google Play Services**: Ads, Billing v4, Play Integrity
- **Adjust**: Attribution
- **Sentry**: Error tracking
- **Yandex Ads**: Multiple ad formats
- **AppLovin MAX**: Mediation
- **IronSource**: Mediation
- **Unity Ads**: Mediation
- **Vungle**: Mediation
- **ExoPlayer**: Media playback

### App Features Identified (from layout resources)
The app appears to be a **business simulation game** with features:
- **Business Management**: Banks, factories, shops, real estate, cryptocurrency
- **Transportation**: Taxi, garage, car showroom, aircraft hangar
- **Football/Soccer**: Club management, player transfers, championships
- **IT Company**: Employee management, projects
- **Investments**: Stocks, crypto, NFTs, GPU mining
- **Collections**: Collectible items
- **Ad Monetization**: Multiple ad networks (MAX, IronSource, AdMob, Facebook, Unity, Yandex, Vungle, etc.)

### String Obfuscation
- Strings in DEX files appear heavily obfuscated/encoded
- Most extracted strings are short encoded fragments
- Package name `com.ttterbagames.businesssimulatornew` not found in plain text in DEX
- Suggests use of **string encryption/obfuscation** (e.g., DexGuard, ProGuard with string encryption, or custom obfuscator)

### Native Code
- No `lib/` folder found in extracted APK
- No `.so` files detected
- Pure Kotlin/Java implementation

## Reverse Engineering Recommendations

### 1. Decompilation (Static Analysis)
```bash
# Using jadx (recommended)
jadx -d output_decompiled "C:\Users\ericw\Downloads\com.ttterbagames.businesssimulatornew_1.10.apk"

# Using jadx-mcp via mcp-reverse
# launch_re_backend({backend: "jadx-mcp"})
# hub_connect({backend: "jadx-mcp", target: "apk_path"})
```

### 2. Dynamic Analysis
```bash
# Using Frida for runtime hooking
frida -U -f com.ttterbagames.businesssimulatornew -l hook_script.js

# Using ADB for logcat
adb logcat | grep com.ttterbagames.businesssimulatornew
```

### 3. String Deobfuscation
- Use **jadx** with deobfuscation plugins
- Try **Frida** to hook string decryption methods at runtime
- Check for **DexGuard** or **ProGuard** mapping files

### 4. Network Analysis
```bash
# Using mitmproxy for TLS interception
mitmproxy --mode upstream:https://target-api.com

# Or with Frida for cert pinning bypass
frida -U -f com.ttterbagames.businesssimulatornew -l bypass_ssl.js
```

### 5. Game Logic Analysis
Focus areas for game hacking/research:
- **Currency systems**: Coins, gems, crypto, stocks
- **Business revenue calculations**
- **Ad reward systems** (multiple ad networks)
- **Time-based mechanics** (construction, production)
- **Multiplayer/social features** (football championships)

## Tools Available in mcp-reverse

### Native Tools (used in this analysis)
- `file_identify` - Format detection
- `dex_parse` - DEX header parsing
- `string_extract` - String extraction
- `entropy_scan` - Entropy analysis
- `aob_scan` - Byte pattern scanning

### Backend Integrations (available via MCP Hub)
- **jadx-mcp** - Primary Android decompiler
- **frida-mcp** - Dynamic instrumentation
- **unidbg-mcp** - Android native lib emulation
- **adb-mcp** - Device control
- **ghidra-mcp** - Native library analysis

## Next Steps

1. **Decompile with jadx** to get readable Java/Kotlin source
2. **Identify string decryption routine** for deobfuscation
3. **Analyze game economy classes** (currency, businesses, upgrades)
4. **Hook network calls** to understand API endpoints
5. **Bypass root/emulator detection** if present
6. **Analyze ad mediation logic** for reward manipulation

## Output Files
- `apk_extracted/` - Full APK contents
- `apk_extracted/classes.dex` through `classes6.dex` - DEX files
- `apk_extracted/assets/audience_network.dex` - Facebook Audience Network SDK

---
*Generated using mcp-reverse (180+ MCP tools for reverse engineering)*