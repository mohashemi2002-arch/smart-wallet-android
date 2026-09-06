with open('app/src/main/java/com/example/data/SmsReceiver.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for i, line in enumerate(lines):
    if i >= 35 and i <= 39:
        if line.strip() == "}":
            continue # skip the dangling ones
    new_lines.append(line)

with open('app/src/main/java/com/example/data/SmsReceiver.kt', 'w') as f:
    f.writelines(new_lines)

