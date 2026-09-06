#!/bin/bash
awk '
/override fun onCreate/ {
    print "    private lateinit var securityManager: com.example.domain.util.AppSecurityManager"
    print ""
    print "    override fun onCreate(savedInstanceState: Bundle?) {"
    print "        super.onCreate(savedInstanceState)"
    print "        enableEdgeToEdge()"
    print "        checkAndRequestPermissions()"
    print "        securityManager = com.example.domain.util.AppSecurityManager(this)"
    print "        securityManager.applyScreenSecurity(window)"
    print ""
    print "        setContent {"
    print "            val isUnlocked by securityManager.isUnlocked.collectAsState()"
    print "            PoolakiTheme {"
    print "                if (isUnlocked) {"
    print "                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {"
    print "                        PoolakiApp()"
    print "                    }"
    print "                } else {"
    print "                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {"
    print "                        com.example.ui.screens.LockScreen("
    print "                            onUnlockClick = { securityManager.promptBiometric(this@MainActivity) }"
    print "                        )"
    print "                    }"
    print "                }"
    print "            }"
    print "        }"
    print "    }"
    print ""
    next
}
/setContent \{/,/        \}/ {
    if (in_set_content) {
        next
    } else {
        in_set_content = 1
        next
    }
}
{ print }
' app/src/main/java/com/example/MainActivity.kt > temp_main.kt && mv temp_main.kt app/src/main/java/com/example/MainActivity.kt
