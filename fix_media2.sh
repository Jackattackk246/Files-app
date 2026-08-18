sed -i 's/val translucentBg = Color.Black.copy(alpha = 0.7f)/val translucentBg = Color.Black.copy(alpha = 0.4f)/g' app/src/main/java/com/example/ui/MediaPlayerStudio.kt
sed -i 's/\.clip(RoundedCornerShape(24.dp))/.clip(RoundedCornerShape(24.dp)).blur(16.dp)/g' app/src/main/java/com/example/ui/MediaPlayerStudio.kt
sed -i 's/\.background(bgCharcoal)/.background(Color(0xFF1C1D22).copy(alpha = 0.7f))/g' app/src/main/java/com/example/ui/MediaPlayerStudio.kt
