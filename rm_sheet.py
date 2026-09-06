import re
with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

start_idx = content.find('    // Dialog: All Transactions Full Modal')
end_idx = content.find('        )', start_idx) + 10

if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + content[end_idx:]
    with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
        f.write(content)
