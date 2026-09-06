import re
with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

imports = """import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Category"""

content = content.replace("import androidx.compose.material.icons.filled.Add", imports + "\nimport androidx.compose.material.icons.filled.Add")

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
