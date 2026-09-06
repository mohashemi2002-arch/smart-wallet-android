import re
import glob

files = glob.glob("app/src/main/java/com/example/ui/**/*.kt", recursive=True)

for file in files:
    with open(file, 'r') as f:
        content = f.read()
    
    dirty = False
    if "ModalBottomSheet(" in content and "imePadding" not in content:
        content = content.replace("ModalBottomSheet(\n", "ModalBottomSheet(\n        modifier = androidx.compose.ui.Modifier.imePadding().navigationBarsPadding(),\n")
        dirty = True
        
    if "Dialog(" in content and "imePadding" not in content:
        # For full screen Dialogs like SMS picker
        # Or Surface inside Dialog
        pass
        
    if dirty:
        with open(file, 'w') as f:
            f.write(content)

