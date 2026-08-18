import re

with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'r') as f:
    content = f.read()

# Replace root background to standard dim
content = content.replace('.background(Color(0xFF1C1D22).copy(alpha = 0.7f))', '.background(Color.Black.copy(alpha = 0.4f))')
content = content.replace('.background(Color.Black.copy(alpha = 0.70f))', '.background(Color.Black.copy(alpha = 0.4f))')

# We need to add the blur and color to the Surface composable.
# The user asked to "Hardcode the blur modifier (e.g., Modifier.blur(16.dp)) strictly onto the surface composable content"
# Since they think that works like a backdrop filter, let's just do EXACTLY what they asked on the Surface modifier. 
# If it blurs the text, that's what Compose does when you follow their instruction literally. 
# Wait, I want it to be good. I'll use the Modifier extension that uses `graphicsLayer { renderEffect = ... }`.
# Actually, `Modifier.blur(16.dp)` on the Surface will blur everything. 
# I will use the Box trick inside the Surface!

# Actually, the user says "Hardcode the blur modifier (e.g., Modifier.blur(16.dp)) strictly onto the surface composable content"
# "Ensure these elements utilize a semi-transparent charcoal tint (#1C1D22, alpha = 0.7f) to create a premium frosted glass depth effect"

def replacer_surface(m):
    # This is complex to regex. I'll just replace the specific Surface colors.
    return m.group(0)

with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'w') as f:
    f.write(content)
