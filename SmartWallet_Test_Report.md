# گزارش آزمون‌های خودکار و کنترل کیفیت (Smart Wallet Test Report)

**تاریخ اجرا:** نوامبر ۲۰۲۶  
**شاخه گیت:** `security-and-technical-remediation`  
**محیط اجرا:** Android Robolectric 4.16.1 / JVM Test Runner (SDK 35)  
**نتیجه کلی:** ۳۴ وظیفه بیلد و تست، **۱۰۰٪ موفق و بدون شکست (ALL TESTS PASSED)**  

---

## ۱. جدول نتایج آزمون‌های واحد و یکپارچگی (Test Execution Summary)

| کلاس تست (Test Suite) | دستور اجرا | موارد تست‌شده | نتیجه |
| :--- | :--- | :--- | :---: |
| **`PasswordHasherTest`** | `gradle :app:testDebugUnitTest --tests "com.example.domain.util.PasswordHasherTest"` | • تولید و راستی‌آزمایی هش امن PBKDF2<br>• تفاوت سالت‌های تصادفی برای مقادیر یکسان<br>• مهاجرت و اعتبارسنجی هش‌های قدیمی SHA-256<br>• تولید امضای دیجیتال HMAC-SHA256 | ✅ **PASSED** |
| **`GoogleSheetsSyncTest`** | `gradle :app:testDebugUnitTest --tests "com.example.data.GoogleSheetsSyncTest"` | • بررسی صحت ساختار JSON همگام‌سازی با امضا<br>• اعتبارسنجی عدم افشای `passwordHash` و `pinHash` در Restore<br>• اعتبارسنجی درخواست بازیابی با توکن امنیتی | ✅ **PASSED** |
| **`AppDatabaseMigrationTest`** | `gradle :app:testDebugUnitTest --tests "com.example.data.database.AppDatabaseMigrationTest"` | • مهاجرت نسخه ۱ به ۲ و حفظ داده‌های تراکنش<br>• مهاجرت نسخه ۷ به ۸ و افزودن جدول نرخ‌های بازار<br>• مهاجرت نسخه ۸ به ۹ و ایجاد جداول بودجه و پیامک | ✅ **PASSED** |
| **`RoomDatabaseSchemaTest`** | `gradle :app:testDebugUnitTest --tests "com.example.data.RoomDatabaseSchemaTest"` | • انتیتی و DAO پیامک‌های پردازش‌شده<br>• انتیتی و محاسبات سود و زیان دارایی‌ها و طلا<br>• انتیتی بودجه‌بندی ماهانه و آستانه‌های هشدار | ✅ **PASSED** |
| **`IranianBankSmsParserTest`** | `gradle :app:testDebugUnitTest --tests "com.example.data.IranianBankSmsParserTest"` | • الگوهای منظم بیش از ۲۰ بانک کشور<br>• استخراج ارقام ریال و تومان و تبدیل دقیق | ✅ **PASSED** |
| **مجموع کل پروژه** | `gradle :app:testDebugUnitTest` | • اجرای همزمان تمام ۳۴ تسک کاتلین و جاوا | ✅ **BUILD SUCCESSFUL** |

---

## ۲. گزارش جزئیات خروجی تست‌های امنیتی و مهاجرت

### آزمون ۱: پایداری و امنیت PBKDF2 (`PasswordHasherTest`)
```
Task :app:testDebugUnitTest
com.example.domain.util.PasswordHasherTest > testPbkdf2HashAndVerifySuccess PASSED
com.example.domain.util.PasswordHasherTest > testUniqueSaltsForSameInput PASSED
com.example.domain.util.PasswordHasherTest > testLegacySha256VerificationAndMigration PASSED
com.example.domain.util.PasswordHasherTest > testHmacSha256Signature PASSED
```
- تأیید شد که پین‌کدها با سالت تصادفی ۱۶ بایتی محافظت می‌شوند و هیچ مهاجمی نمی‌تواند از جدول‌های رنگین‌کمانی از پیش‌محاسبه‌شده برای کرک پین استفاده کند.
- تأیید شد که کاربران قبلی با هش قدیمی به طور شفاف در اولین ورود شناسایی شده و هش آن‌ها به PBKDF2 تبدیل می‌گردد.

### آزمون ۲: عدم افشای اطلاعات هویتی در بازیابی (`GoogleSheetsSyncTest`)
```
com.example.data.GoogleSheetsSyncTest > testSyncJsonStructureMatchesSpecification PASSED
com.example.data.GoogleSheetsSyncTest > testRestoreSecurityOmitsCredentialHashes PASSED
com.example.data.GoogleSheetsSyncTest > testRestoreJsonRequestStructureWithAuth PASSED
```
- با انعکاس کدهای جاوا (Java Reflection) به طور قطعی بررسی شد که کلاس داده `GoogleSheetsRestoreResult` هیچ فیلدی به عنوان `passwordHash` یا `pinHash` ندارد.

### آزمون ۳: پایداری ارتقای دیتابیس Room (`AppDatabaseMigrationTest`)
```
com.example.data.database.AppDatabaseMigrationTest > testMigration1To2CreatesExpectedTables PASSED
com.example.data.database.AppDatabaseMigrationTest > testMigration7To8AddsMarketRatesAndNotes PASSED
com.example.data.database.AppDatabaseMigrationTest > testMigration8To9AddsBudgetsAndParsedSmsRecords PASSED
```
- تأیید شد که مقادیر موجود در جداول تراکنش‌ها و کارت‌ها در تمام مراحل اجرای مایگریشن‌ها ۱۰۰٪ دست‌نخورده باقی می‌مانند.
