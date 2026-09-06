import glob

files = glob.glob("app/src/main/java/com/example/ui/**/*.kt", recursive=True)

for file in files:
    with open(file, 'r') as f:
        content = f.read()
    
    if "LazyColumn" in content and "Scaffold(" in content and "FloatingActionButton" in content:
        if "item { Spacer(modifier = Modifier.height(96.dp)) }" not in content:
            # Let's just find the last closing brace of LazyColumn and inject it right before
            # This is hard to do with regex, but we can do a simple string replacement if we find a specific pattern.
            pass
