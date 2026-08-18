sed -i 's/color = Color.Black.copy(alpha = 0.7f),/color = Color(0xFF1C1D22).copy(alpha = 0.7f),/g' app/src/main/java/com/example/ui/DashboardScreen.kt
sed -i 's/color = Color.Black.copy(alpha = 0.70f),/color = Color(0xFF1C1D22).copy(alpha = 0.7f),/g' app/src/main/java/com/example/ui/DashboardScreen.kt
sed -i 's/\.testTag("add_tiles_menu_tray"),/.testTag("add_tiles_menu_tray").blur(16.dp),/g' app/src/main/java/com/example/ui/DashboardScreen.kt
sed -i 's/\.testTag("developer_auth_dialog_frame"),/.testTag("developer_auth_dialog_frame").blur(16.dp),/g' app/src/main/java/com/example/ui/DashboardScreen.kt
sed -i 's/\.testTag("format_partition_dialog_frame"),/.testTag("format_partition_dialog_frame").blur(16.dp),/g' app/src/main/java/com/example/ui/DashboardScreen.kt
sed -i 's/\.testTag("duplicate_inspector_frame"),/.testTag("duplicate_inspector_frame").blur(16.dp),/g' app/src/main/java/com/example/ui/DashboardScreen.kt
