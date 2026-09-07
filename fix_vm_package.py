with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if line.startswith("import com.example.ui.components.PersianFormatters") or line.startswith("import kotlinx.coroutines.flow.map"):
        continue
    new_lines.append(line)

# Add imports after package declaration
package_idx = 0
for i, line in enumerate(new_lines):
    if line.startswith("package"):
        package_idx = i
        break

new_lines.insert(package_idx + 1, "import com.example.ui.components.PersianFormatters\nimport kotlinx.coroutines.flow.map\n")

with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.writelines(new_lines)
