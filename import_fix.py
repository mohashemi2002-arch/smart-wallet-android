import re
import glob

all_files = glob.glob('app/src/main/java/**/*.kt', recursive=True)

for file in all_files:
    with open(file, 'r') as f:
        content = f.read()

    if '.formatAsToman()' in content and 'import com.example.ui.components.formatAsToman' not in content:
        # insert after package
        content = re.sub(r'(package [^\n]+)', r'\1\n\nimport com.example.ui.components.formatAsToman', content)
        with open(file, 'w') as f:
            f.write(content)

