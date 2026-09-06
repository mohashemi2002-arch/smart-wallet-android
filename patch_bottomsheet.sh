#!/bin/bash
awk '
/^    if \(showAllTransactionsDialog\) \{/ {
    print "    selectedPendingTx?.let { tx ->"
    print "        com.example.ui.components.PendingReviewBottomSheet("
    print "            transaction = tx,"
    print "            onDismiss = { selectedPendingTx = null },"
    print "            onConfirm = { updatedTx ->"
    print "                viewModel.approvePendingTransaction(updatedTx)"
    print "                selectedPendingTx = null"
    print "            },"
    print "            onIgnore = {"
    print "                viewModel.rejectPendingTransaction(tx)"
    print "                selectedPendingTx = null"
    print "            }"
    print "        )"
    print "    }"
    print ""
}
{ print }
' app/src/main/java/com/example/ui/screens/DashboardScreen.kt > temp2.kt && mv temp2.kt app/src/main/java/com/example/ui/screens/DashboardScreen.kt
