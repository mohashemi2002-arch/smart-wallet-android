with open('app/src/main/java/com/example/updater/AppUpdateManager.kt', 'r') as f:
    content = f.read()

import re

# Correcting the OkHttp Request.Builder token part
content = content.replace(
    'if (token.isNotBlank()) header("Authorization", "Bearer $token") else this',
    'apply { if (token.isNotBlank()) header("Authorization", "Bearer $token") }'
)

with open('app/src/main/java/com/example/updater/AppUpdateManager.kt', 'w') as f:
    f.write(content)
