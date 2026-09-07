with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    lines = f.readlines()

if lines[0].startswith('import androidx.compose.material.icons.filled.History'):
    lines.pop(0)
    lines.insert(2, 'import androidx.compose.material.icons.filled.History\n')
    
with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.writelines(lines)
