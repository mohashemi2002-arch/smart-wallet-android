with open('app/src/main/java/com/example/updater/UpdatePromptDialog.kt', 'r') as f:
    lines = f.readlines()

with open('app/src/main/java/com/example/updater/UpdatePromptDialog.kt', 'w') as f:
    for line in lines:
        if "Toast.makeText" in line:
            continue
        f.write(line)
