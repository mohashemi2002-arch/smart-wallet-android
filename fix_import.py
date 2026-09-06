with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'r') as f:
    content = f.read()

if "import androidx.room.withTransaction" not in content:
    content = content.replace("import android.content.Context", "import android.content.Context\nimport androidx.room.withTransaction")

with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'w') as f:
    f.write(content)
