import re

with open('app/src/main/java/com/example/ui/components/BankLogoImage.kt', 'r') as f:
    content = f.read()

target = r'''fun BankLogoImage\(
    bankName: String\?,
    cardNumber: String\? = null,
    size: Dp = 40\.dp,
    containerColor: Color = Color\.White,
    modifier: Modifier = Modifier
\) \{
    val context = LocalContext\.current
    val resourceId = BankLogoHelper\.getLogoResourceId\(context, bankName, cardNumber\)
    
    Box\(
        modifier = modifier
            \.size\(size\)
            \.clip\(RoundedCornerShape\(12\.dp\)\)
            \.background\(containerColor\),
        contentAlignment = Alignment\.Center
    \) \{
        if \(resourceId != null\) \{
            Image\(
                painter = painterResource\(id = resourceId\),
                contentDescription = bankName \?: "لوگوی بانک",
                modifier = Modifier
                    \.fillMaxSize\(\)
                    \.padding\(4\.dp\)
            \)
        \} else \{
            // Monogram fallback
            val brandColor = BankLogoHelper\.getBrandColor\(bankName, cardNumber\)
            val initial = \(bankName \?: "🏦"\)\.trim\(\)\.take\(2\)
            Box\(
                modifier = Modifier
                    \.fillMaxSize\(\)
                    \.background\(brandColor\),
                contentAlignment = Alignment\.Center
            \) \{
                Text\(
                    text = initial,
                    color = Color\.White,
                    fontSize = \(size\.value \* 0\.35f\)\.sp,
                    fontWeight = FontWeight\.Bold
                \)
            \}
        \}
    \}
\}'''

replacement = r'''fun BankLogoImage(
    bankName: String?,
    cardNumber: String? = null,
    modifier: Modifier = Modifier,
    containerSize: Dp = 56.dp,
    logoSize: Dp = 46.dp,
    containerColor: Color = Color.White
) {
    val context = LocalContext.current
    val resourceId = BankLogoHelper.getLogoResourceId(context, bankName, cardNumber)
    
    Box(
        modifier = modifier
            .size(containerSize)
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        if (resourceId != null) {
            Image(
                painter = painterResource(id = resourceId),
                contentDescription = bankName ?: "لوگوی بانک",
                modifier = Modifier
                    .size(logoSize)
                    .padding(4.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
        } else {
            // Monogram fallback
            val brandColor = BankLogoHelper.getBrandColor(bankName, cardNumber)
            val initial = (bankName ?: "🏦").trim().take(2)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brandColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = Color.White,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(
                        fontSize = (containerSize.value * 0.35f).sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}'''

content = re.sub(target, replacement, content)

with open('app/src/main/java/com/example/ui/components/BankLogoImage.kt', 'w') as f:
    f.write(content)
