with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

content = content.replace('java.net.URL("$baseUrl$slug.png")', 'URL("$baseUrl$slug.png")')

with open('app/build.gradle.kts', 'w') as f:
    f.write(content)
