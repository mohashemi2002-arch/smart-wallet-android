import re
with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()
    
# Find all "item {"
items = re.findall(r'item\s*\{.*?\}', content, re.DOTALL)
for i in items:
    print(i[:100].replace('\n', ' '))
