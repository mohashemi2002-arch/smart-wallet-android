with open('app/build.gradle.kts', 'r') as f:
    content = f.read()
    
import re
content = re.sub(
    r'buildConfigField\("String", "GITHUB_TOKEN", ".*?"\)', 
    'buildConfigField("String", "GITHUB_TOKEN", "\\"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\\"")', 
    content
)

with open('app/build.gradle.kts', 'w') as f:
    f.write(content)
