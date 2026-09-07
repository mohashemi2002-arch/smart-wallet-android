with open('app/src/main/java/com/example/updater/GitHubUpdateService.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'val url: String = ""',
    'val url: String = "",\n    val browser_download_url: String = ""'
)

with open('app/src/main/java/com/example/updater/GitHubUpdateService.kt', 'w') as f:
    f.write(content)
