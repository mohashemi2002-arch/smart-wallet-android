with open('app/src/main/java/com/example/updater/AppUpdateManager.kt', 'r') as f:
    content = f.read()

import re

# Fix syntax errors in the string literal (escape characters)
# First we find the body string and fix it
content = re.sub(
    r'body = "این یک پیغام تستی برای بررسی سلامت رابط کاربری آپدیتر است\.\\nبرای تست، یک فایل موقت دانلود می‌شود\."',
    'body = "این یک پیغام تستی برای بررسی سلامت رابط کاربری آپدیتر است.\\\\nبرای تست، یک فایل موقت دانلود می‌شود."',
    content
)

content = content.replace('body = "این یک پیغام تستی برای بررسی سلامت رابط کاربری آپدیتر است.\\nبرای تست، یک فایل موقت دانلود می‌شود."', 'body = "این یک پیغام تستی برای بررسی سلامت رابط کاربری آپدیتر است.\\nبرای تست، یک فایل موقت دانلود می‌شود."')

# Fix okhttp request builder
content = content.replace(
    '.apply { if (token.isNotBlank()) header("Authorization", "Bearer $token") }',
    ''
)
content = content.replace(
    '.url(asset.url)',
    '.url(asset.url)\n                .apply { if (token.isNotBlank()) header("Authorization", "Bearer $token") }'
)


with open('app/src/main/java/com/example/updater/AppUpdateManager.kt', 'w') as f:
    f.write(content)
