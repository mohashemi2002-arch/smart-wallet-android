import re

with open('app/src/main/java/com/example/domain/util/AppSecurityManager.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'get() = prefs.getBoolean("biometric_enabled", false)',
    'get() = prefs.getBoolean("biometric_enabled", true)'
)

with open('app/src/main/java/com/example/domain/util/AppSecurityManager.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/screens/LockScreen.kt', 'r') as f:
    content = f.read()

if "LaunchedEffect" not in content:
    content = content.replace('import androidx.compose.runtime.Composable', 'import androidx.compose.runtime.Composable\nimport androidx.compose.runtime.LaunchedEffect')
    
    launched_effect = """
    LaunchedEffect(Unit) {
        onUnlockClick()
    }
    """
    
    content = content.replace('fun LockScreen(onUnlockClick: () -> Unit) {', 'fun LockScreen(onUnlockClick: () -> Unit) {' + launched_effect)
    
    with open('app/src/main/java/com/example/ui/screens/LockScreen.kt', 'w') as f:
        f.write(content)
