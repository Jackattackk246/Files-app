sed -i 's/\.blur(16\.dp)//g' app/src/main/java/com/example/ui/Sidebar.kt
sed -i 's/\.testTag("sidebar_navigation_panel"),/.testTag("sidebar_navigation_panel").blur(16.dp),/g' app/src/main/java/com/example/ui/Sidebar.kt
sed -i 's/color = Color.Black.copy(alpha = 0.7f),/color = Color(0xFF1C1D22).copy(alpha = 0.7f),/g' app/src/main/java/com/example/ui/Sidebar.kt
