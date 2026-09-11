with open('app/src/main/java/com/example/updater/UpdatePromptDialog.kt', 'r') as f:
    content = f.read()

import re

new_click_logic = """                                        val apkAsset = release.assets.find { it.name.endsWith(".apk") }
                                        if (apkAsset != null) {
                                            scope.launch { updateManager.downloadUpdate(apkAsset) }
                                        } else {
                                            android.widget.Toast.makeText(context, "فایل نصب در گیت‌هاب یافت نشد (فایل آپلود نشده است)", android.widget.Toast.LENGTH_LONG).show()
                                        }"""

content = re.sub(
    r'val apkAsset = release\.assets\.find \{ it\.name\.endsWith\("\.apk"\) \}\s+if \(apkAsset != null\) \{\s+scope\.launch \{ updateManager\.downloadUpdate\(apkAsset\) \}\s+\}',
    new_click_logic,
    content
)

with open('app/src/main/java/com/example/updater/UpdatePromptDialog.kt', 'w') as f:
    f.write(content)
