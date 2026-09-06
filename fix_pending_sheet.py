with open('app/src/main/java/com/example/ui/components/PendingReviewBottomSheet.kt', 'r') as f:
    content = f.read()

content = content.replace(
    "            modifier = Modifier\n                .padding(horizontal = 16.dp)\n                .padding(bottom = 32.dp),",
    "            modifier = Modifier\n                .padding(horizontal = 16.dp)\n                .padding(bottom = 32.dp)\n                .verticalScroll(androidx.compose.foundation.rememberScrollState()),"
)

with open('app/src/main/java/com/example/ui/components/PendingReviewBottomSheet.kt', 'w') as f:
    f.write(content)
