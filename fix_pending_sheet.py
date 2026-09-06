with open('app/src/main/java/com/example/ui/components/PendingReviewBottomSheet.kt', 'r') as f:
    content = f.read()

content = content.replace("ModalBottomSheet(onDismissRequest = onDismiss) {", "ModalBottomSheet(\n        onDismissRequest = onDismiss,\n        modifier = Modifier.imePadding().navigationBarsPadding()\n    ) {")

with open('app/src/main/java/com/example/ui/components/PendingReviewBottomSheet.kt', 'w') as f:
    f.write(content)
