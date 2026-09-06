def clean(file):
    with open(file, 'r') as f:
        lines = f.readlines()
    
    out_lines = []
    skip = False
    for line in lines:
        if "// private fun detectIranianBank" in line or "private fun detectIranianBank" in line:
            skip = True
        
        if not skip:
            out_lines.append(line)
            
        if skip and line.strip() == "}":
            # Very fragile, let's just find the first "{" and the matching "}"
            pass # Actually, better way:
    
    # Just do a regex to remove the whole function block.
import re
def clean_regex(file):
    with open(file, 'r') as f:
        content = f.read()
    
    # regex to remove from "// private fun detectIranianBank" to the end of the file, assuming it's the last thing.
    # Actually, it IS the last thing based on the tail output.
    content = re.sub(r'// private fun detectIranianBank.*\}', '', content, flags=re.DOTALL)
    content = re.sub(r'private fun detectIranianBank.*\}', '', content, flags=re.DOTALL)
    
    with open(file, 'w') as f:
        f.write(content)

clean_regex('app/src/main/java/com/example/ui/components/AddBankAccountDialog.kt')
clean_regex('app/src/main/java/com/example/ui/components/EditAccountBottomSheet.kt')
