# گزارش ممیزی و ارزیابی مهاجرت‌های پایگاه داده Room (Smart Wallet Database Migration Report)

**تاریخ:** نوامبر ۲۰۲۶  
**شاخه گیت:** `security-and-technical-remediation`  
**وضعیت:** اعتبارسنجی‌شده با تست‌های خودکار یکپارچگی SQLite  
**تیم مسئول:** Android Database Engineer (Room / SQLite)  

---

## ۱. ساختار فعلی پایگاه داده محلی (Current Database Architecture)

- **نام پایگاه داده:** `"smart wallet_ai_database"`
- **نسخه فعلی پایگاه داده:** `version = 9`
- **تعداد انتیتی‌ها:** ۱۲ جدول فعال
  1. `Transaction` (جدول اصلی تراکنش‌ها)
  2. `Category` (دسته‌بندی‌های مالی)
  3. `BankAccount` (کارت‌ها و حساب‌های بانکی)
  4. `Asset` (دارایی‌های سرمایه‌ای شامل طلا، ارز و رمزارز)
  5. `AssetTransactionEntity` (سوابق خرید و فروش دارایی‌ها)
  6. `LoanEntity` (وام‌ها و تسهیلات)
  7. `InstallmentEntity` (اقساط هر وام)
  8. `ChequeEntity` (چک‌های صیادی دریافتنی و پرداختنی)
  9. `BankSmsSender` (سرشماره‌های بانکی ثبت‌شده)
  10. `MarketRateEntity` (کش نرخ‌های لحظه‌ای بازار)
  11. `ParsedSmsEntity` (سوابق پیامک‌های پردازش‌شده هوشمند)
  12. `BudgetEntity` (بودجه‌بندی‌های ماهانه دسته‌ها)

---

## ۲. ارزیابی دقیق Migrationهای موجود

| مسیر مهاجرت | جداول یا ستون‌های اضافه‌شده | حفظ داده‌های موجود | وضعیت تست |
| :--- | :--- | :---: | :---: |
| **`MIGRATION_1_2`** | ایجاد جداول `loans`، `installments`، `cheques` و `asset_items` با Foreign Key و ایندکس | ✅ داده‌های `transactions` و `categories` کاملاً حفظ می‌شوند | **FIXED & TESTED** (`AppDatabaseMigrationTest`) |
| **نسخه‌های ۲ تا ۷** | فاقد فایل Schema یا کدهای Migration تاریخی در مخزن | ⚠️ عدم وجود مستندات تاریخی نسخه | **NEEDS EXTERNAL ACTION / DOCUMENTED LIMITATION** |
| **`MIGRATION_7_8`** | ایجاد جدول `market_rates` و افزودن ستون `notes` به جدول `transactions` | ✅ بدون حذف یا رونویسی تراکنش‌های کاربر | **FIXED & TESTED** (`AppDatabaseMigrationTest`) |
| **`MIGRATION_8_9`** | ایجاد جداول `parsed_sms_records` و `budgets`، افزودن ستون‌های `bankName`، `balanceAfterToman` و `senderNumber` به `transactions`، ستون‌های تکمیلی به `assets` و ایندکس‌گذاری جامع | ✅ کلیه تراکنش‌ها، کارت‌ها و دارایی‌ها با مقادیر پیش‌فرض حفظ می‌شوند | **FIXED & TESTED** (`AppDatabaseMigrationTest`) |

---

## ۳. تحلیل علت ریشه‌ای نبود مایگریشن‌های ۲ تا ۷ و راهکار مهندسی

### علت ریشه‌ای (Root Cause):
در کدهای قدیمی پروژه، گزینه `exportSchema = false` در کلاس `AppDatabase.kt` تنظیم شده بود و هیچ فایل JSON از اسکیماهای تاریخی نسخه‌های ۲، ۳، ۴، ۵ و ۶ در مخزن کامیت نشده است.

### انطباق با قوانین غیرقابل‌مذاکره (قانون ۵):
> «از ساخت Migration جعلی یا حدسی برای نسخه‌های قدیمی خودداری کنید. اگر Schema تاریخی یک نسخه قابل بازیابی نیست، آن را به‌عنوان محدودیت گزارش کنید و مسیر امن بازیابی را مشخص کنید.»

تیم مهندسی پایگاه داده اکیداً از ساخت Migrationهای حدسی یا ساختگی برای نسخه‌های ۲ تا ۶ خودداری کرد؛ زیرا هرگونه حدس نادرست در مورد ستون‌ها، نوع داده‌ها یا ایندکس‌های قدیمی می‌تواند باعث کرش مرگبار `IllegalStateException: Room cannot verify the data integrity` در دستگاه کاربر شود.

### راهکار پایداری بلندمدت و حفاظت از داده‌ها:
1. **عدم استفاده از `fallbackToDestructiveMigration`:** پایگاه داده کماکان به صورت کاملاً غیرمخرب عمل کرده و هیچ متد تخریب داده‌ای به دیتابیس تزریق نشده است.
2. **فعال‌سازی Schema Export:** فعال‌سازی خروجی اسکیما در دیتابیس به صورت خودکار تا از این پس تمام تغییرات پایگاه داده در فایل‌های JSON در دایرکتوری `app/schemas` نگهداری شوند.
3. **مکانیزم بازیابی امن:** در صورت نصب مجدد برنامه روی دستگاهی با دیتابیس آسیب‌دیده، کاربر با استفاده از گزینه «بازیابی امن از فضای ابری» (`recoverFromCloud`) بدون از دست دادن حتی یک تراکنش، داده‌های خود را بازگردانی می‌کند.

---

## ۴. نتایج آزمون‌های پایداری و صحت داده‌ها (Migration Tests)
تست‌های خودکار مهاجرت در کلاس `com.example.data.database.AppDatabaseMigrationTest` اجرا و با موفقیت ۱۰۰٪ پاس شدند:
- `testMigration1To2CreatesExpectedTables`: با موفقیت اجرا شد.
- `testMigration7To8AddsMarketRatesAndNotes`: با موفقیت اجرا شد.
- `testMigration8To9AddsBudgetsAndParsedSmsRecords`: با موفقیت اجرا شد.
هیچ رکوردی در حین ارتقا حذف یا مخدوش نشد.
