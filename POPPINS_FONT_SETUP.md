# Poppins Font Setup

## Current Status
The EduTrack app is set up to use the Poppins font family for a more modern, premium look. However, the actual font files need to be downloaded and added to the project.

## Steps to Add Poppins Fonts

### 1. Download Font Files
1. Go to [Google Fonts - Poppins](https://fonts.google.com/specimen/Poppins)
2. Click the **"Download all"** button (or download individual weights)
3. Extract the ZIP file

### 2. Required Font Files
You need these specific font files:
- `Poppins-Regular.ttf` (Normal / 400 weight)
- `Poppins-Medium.ttf` (Medium / 500 weight)
- `Poppins-SemiBold.ttf` (SemiBold / 600 weight)
- `Poppins-Bold.ttf` (Bold / 700 weight)

### 3. Add to Android Project
1. In Android Studio, navigate to `app/src/main/res/`
2. Create a `font` folder (if it doesn't exist - it should already exist)
3. Copy the 4 TTF files and **rename them to lowercase with underscores**:
   - `Poppins-Regular.ttf` → `poppins_regular.ttf`
   - `Poppins-Medium.ttf` → `poppins_medium.ttf`
   - `Poppins-SemiBold.ttf` → `poppins_semibold.ttf`
   - `Poppins-Bold.ttf` → `poppins_bold.ttf`

### 4. Enable in Type.kt
Once the font files are in place, follow these steps to enable Poppins:

1. Open `app/src/main/java/com/example/edutrack/ui/theme/Type.kt`
2. Uncomment the Font imports and PoppinsFamily definition (lines with `// val PoppinsFamily`)
3. Replace all instances of `FontFamily.Default` with `PoppinsFamily` in the Typography definitions
4. Rebuild the project: `./gradlew assembleDebug`

### 5. Verify
After rebuilding, open the app in an emulator or device. All text should now use the Poppins font family instead of the system default.

---

## Font Files Directory Structure
```
app/src/main/res/font/
├── poppins_regular.ttf
├── poppins_medium.ttf
├── poppins_semibold.ttf
└── poppins_bold.ttf
```

## Troubleshooting

### Compilation Error: "Unresolved reference 'font'"
- **Cause**: Font files don't exist in `res/font/`
- **Solution**: Make sure the TTF files are named exactly as specified above (all lowercase with underscores)

### Fonts Not Appearing After Build
- **Cause**: App needs to be redeployed
- **Solution**: 
  1. Clean build: `./gradlew clean assembleDebug`
  2. Uninstall previous app from device/emulator
  3. Reinstall the app

## Additional Notes
- Poppins font is available in 12 weights on Google Fonts, but we only use 4 (Regular, Medium, SemiBold, Bold)
- The font files are relatively small (~15-20 KB each)
- This improves app visual cohesion and creates a more premium feel
- Users on older Android devices will still see the app correctly, just with system default fonts if there's any issue

---

**Status**: Ready to add fonts (implementation prepared, fonts not yet downloaded)
