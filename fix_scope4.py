with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
skip = False
for i, line in enumerate(lines):
    if '// We moved the dialog inside RecycleBinPanel' in line:
        skip = True
        continue
    if skip and '}' in line and len(line.strip()) == 3 and lines[i-1].strip() == ')': # closing bracket of let
        skip = False
        continue
    if not skip:
        new_lines.append(line)

with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'w') as f:
    f.writelines(new_lines)
