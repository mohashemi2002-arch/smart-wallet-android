import re

with open('app/src/main/java/com/example/ui/components/BankCardItem.kt', 'r') as f:
    content = f.read()

# Replace size=42.dp with no size arg (use defaults)
content = re.sub(r'size\s*=\s*42\.dp,\n\s*', r'', content)
# Increase height from 130.dp to 145.dp
content = re.sub(r'\.height\(130\.dp\)', r'.height(145.dp)', content)

with open('app/src/main/java/com/example/ui/components/BankCardItem.kt', 'w') as f:
    f.write(content)

