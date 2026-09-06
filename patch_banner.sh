#!/bin/bash
awk '
/^\s*\/\/ 2\. Date & Quick Actions Card/ {
    print "            // 1.5 Pending Transactions Banner"
    print "            if (pendingTransactions.isNotEmpty()) {"
    print "                item {"
    print "                    PaddingBox {"
    print "                        Card("
    print "                            modifier = Modifier.fillMaxWidth().clickable { selectedPendingTx = pendingTransactions.first() },"
    print "                            colors = CardDefaults.cardColors(containerColor = Color(0xFF5A3036)),"
    print "                            shape = RoundedCornerShape(16.dp)"
    print "                        ) {"
    print "                            Row("
    print "                                modifier = Modifier.fillMaxWidth().padding(16.dp),"
    print "                                verticalAlignment = Alignment.CenterVertically,"
    print "                                horizontalArrangement = Arrangement.SpaceBetween"
    print "                            ) {"
    print "                                Text("
    print "                                    text = \"${PersianFormatters.toPersianDigits(pendingTransactions.size.toString())} تراکنش خوانده شده از پیامک نیازمند تایید شماست\","
    print "                                    color = Color(0xFFFFD8E4),"
    print "                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)"
    print "                                )"
    print "                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = \"Review\", tint = Color(0xFFFFD8E4))"
    print "                            }"
    print "                        }"
    print "                    }"
    print "                }"
    print "            }"
    print ""
}
{ print }
' app/src/main/java/com/example/ui/screens/DashboardScreen.kt > temp.kt && mv temp.kt app/src/main/java/com/example/ui/screens/DashboardScreen.kt
