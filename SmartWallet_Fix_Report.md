# گزارش جامع اصلاحات فنی و مهندسی پروژه Smart Wallet (پولکی)

**تاریخ اجرا:** نوامبر ۲۰۲۶  
**شاخه گیت:** `security-and-technical-remediation`  
**تیم مهندسی:** Senior Android Architect, Application Security Engineer, Database Engineer, Backend Developer, QA Lead  
**وضعیت بیلد:** کامپایل و تست ۱۰۰٪ موفق (`BUILD SUCCESSFUL`)  

---

## ۱. جدول ماتریس اصلاحات و وضعیت نهایی (Remediation Status Matrix)

| ردیف | شناسه مشکل | عنوان مشکل | شدت | فایل‌های تغییریافته | راهکار اجراشده | وضعیت |
| :---: | :--- | :--- | :---: | :--- | :--- | :---: |
| ۱ | **SEC-P0-01** | افشای `passwordHash` و `pinHash` در پاسخ Restore | **بحرانی (P0)** | `scripts/google_apps_script.js`<br>`GoogleSheetsSyncManager.kt`<br>`AuthManager.kt` | حذف قطعی فیلدهای هش هویتی از سرور و کلاینت در فرایند Restore | **FIXED** |
| ۲ | **SEC-P0-02** | دسترسی و دستکاری بدون احراز هویت در Google Apps Script | **بحرانی (P0)** | `scripts/google_apps_script.js`<br>`GoogleSheetsSyncManager.kt` | پیاده‌سازی امضای دیجیتال HMAC-SHA256 و اعتبارسنجی پنجره زمانی در سرور | **FIXED** |
| ۳ | **SEC-P0-03** | هشینگ ضعیف PIN با سالت ثابت و ریسک Rainbow Table | **بحرانی (P0)** | `PasswordHasher.kt`<br>`AuthManager.kt` | ارتقا به PBKDF2-HMAC-SHA256 با سالت تصادفی ۱۶ بایتی یکتا و مقایسه زمان ثابت | **FIXED** |
| ۴ | **SEC-P0-04** | خطرات تخریب دیتابیس در هنگام ارتقای نسخه‌ها | **بحرانی (P0)** | `AppDatabaseMigrations.kt`<br>`AppDatabaseMigrationTest.kt` | راستی‌آزمایی و تست خودکار مایگریشن‌های ۱ به ۲، ۷ به ۸ و ۸ به ۹ و مستندسازی دقیق | **FIXED** |
| ۵ | **SEC-P1-01** | بررسی محاسبات مالی و واحد پول | **بالا (P1)** | `Transaction.kt`<br>`GoogleSheetsSyncManager.kt` | اعتبارسنجی نوع داده `Long` برای `amountToman` و پرهیز از Float/Double در محاسبات | **FIXED** |
| ۶ | **SEC-P1-02** | مهاجرت امن کاربران قبلی بدون از دست رفتن حساب | **بالا (P1)** | `AuthManager.kt`<br>`PasswordHasher.kt` | پیاده‌سازی مکانیزم Transparent Migration جهت تبدیل خودکار هش‌های قدیمی در اولین لاگین | **FIXED** |
| ۷ | **SEC-P2-01** | محدودسازی حجم Payload و جلوگیری از DoS در Google Sheets | **متوسط (P2)** | `scripts/google_apps_script.js` | اعمال محدودیت حداکثر رکوردها (حداکثر ۲۵۰۰ تراکنش و ۱۰۰ کارت) و برش ورودی‌ها | **FIXED** |
| ۸ | **SEC-P2-02** | نشر اسکریپت Google Apps Script در سرور گوگل شیت کاربر | **متوسط (P2)** | `scripts/google_apps_script.js` | استقرار نسخه جدید اسکریپت در گوگل درایو / شیت توسط مدیر مخزن | **NEEDS EXTERNAL ACTION** |

---

## ۲. شرح تفصیلی مشکلات، علل ریشه‌ای و راهکارهای پیاده‌سازی‌شده

### مشکل ۱: حذف افشای هش‌ها در Restore (SEC-P0-01)
- **فایل و محل مشکل:** `scripts/google_apps_script.js` (خطوط ۲۵۰ تا ۳۴۰) و `app/src/main/java/com/example/data/sync/GoogleSheetsSyncManager.kt` (خطوط ۲۵ تا ۳۵ و ۳۵۰ تا ۳۶۵).
- **علت ریشه‌ای:** فیلدهای `passwordHash` و `pinHash` به اشتباه جزو فیلدهای قابل بازیابی در نظر گرفته شده بودند و هر کس با فراخوانی وب‌سرویس می‌توانست هش رمز و پین کاربر را دریافت کند.
- **راهکار انتخاب‌شده:** حذف قطعی هرگونه ارسال هش از سرور به کلاینت در پاسخ Restore؛ کلاس `GoogleSheetsRestoreResult` بازنویسی شد و فرایند بازیابی حساب در کلاینت (`AuthManager.recoverFromCloud`) بر پایه ورود مستقل و اعتبارسنجی کلاینتی ایمن تنظیم گردید.
- **تست اجراشده:** `GoogleSheetsSyncTest.testRestoreSecurityOmitsCredentialHashes`
- **نتیجه تست:** موفقیت‌آمیز (تأیید شد که هیچ فیلد هشی در مدل و پاسخ بازگردانده نمی‌شود).
- **وضعیت:** **FIXED**

---

### مشکل ۲: احراز هویت و جلوگیری از دستکاری در Sync و Restore (SEC-P0-02)
- **فایل و محل مشکل:** `scripts/google_apps_script.js` و `GoogleSheetsSyncManager.kt`
- **علت ریشه‌ای:** متدهای `handleSync` و `handleRestore` بدون احراز هویت کار می‌کردند و هر شخصی با شماره موبایل کاربر دیگری می‌توانست داده‌های او را بخواند یا با ارسال درخواست `sync` پاک کند.
- **راهکار انتخاب‌شده:** 
  1. پیاده‌سازی مکانیزم امضای دیجیتال HMAC-SHA256 بر اساس کلید احراز هویت کاربر و برچسب زمانی:
     `authSignature = HMAC_SHA256(phone + ":" + action + ":" + timestamp, key)`
  2. بررسی پنجره اعتبارسنجی زمانی تا حداکثر ۱۵ دقیقه در سرور جهت پیشگیری از Replay Attack.
  3. در صورتی که امضا اشتباه باشد، سرور پاسخ `401 Unauthorized` برمی‌گرداند.
- **تست اجراشده:** `GoogleSheetsSyncTest.testSyncJsonStructureMatchesSpecification` و `testRestoreJsonRequestStructureWithAuth`
- **نتیجه تست:** موفقیت‌آمیز.
- **وضعیت:** **FIXED**

---

### مشکل ۳: الگوریتم استاندارد PBKDF2 برای PIN و رمز عبور (SEC-P0-03)
- **فایل و محل مشکل:** `app/src/main/java/com/example/domain/util/AuthManager.kt`
- **علت ریشه‌ای:** پین کدهای ۴ رقمی با سالت متنی ثابت هش می‌شدند و فضای کوچک ۱۰,۰۰۰ حالت پین کد در برابر حمله جدول رنگین‌کمانی کاملاً آسیب‌پذیر بود.
- **راهکار انتخاب‌شده:**
  1. ایجاد کلاس تخصصی `PasswordHasher.kt`.
  2. استفاده از `PBKDF2WithHmacSHA256` با ۱۰,۰۰۰ دور محاسباتی.
  3. تولید سالت تصادفی ۱۶ بایتی یکتا به ازای هر کاربر/پین با `SecureRandom`.
  4. مقایسه زمان ثابت با `MessageDigest.isEqual` برای دفع حملات زمانی.
  5. مهاجرت خودکار: در متدهای `loginWithPassword` و `loginWithPin`، اگر هش موجود مربوط به نسخه قدیمی باشد، پس از تطبیق موفق، بلافاصله هش جدید PBKDF2 تولید و جایگزین می‌شود.
- **تست اجراشده:** `PasswordHasherTest`
- **نتیجه تست:** موفقیت‌آمیز (تمام ۴ تست پاس شدند).
- **وضعیت:** **FIXED**

---

### مشکل ۴: حفظ پایداری پایگاه داده و مهاجرت‌ها (SEC-P0-04)
- **فایل و محل مشکل:** `app/src/main/java/com/example/data/database/AppDatabaseMigrations.kt`
- **علت ریشه‌ای:** در نسخه‌های ابتدایی به دلیل غیرفعال بودن Schema Export، فایل‌های اسکیما برای نسخه‌های ۲ تا ۷ ذخیره نشده بودند.
- **راهکار انتخاب‌شده:**
  1. پرهیز از ساخت Migration حدسی یا تخیلی طبق قانون شماره ۵.
  2. صحه‌گذاری و نوشتن تست‌های جامع SQLite برای مایگریشن‌های ۱ به ۲، ۷ به ۸ و ۸ به ۹.
  3. اثبات حفظ کامل تراکنش‌ها و کارت‌های کاربران در طول تمام مراحل ارتقا.
- **تست اجراشده:** `AppDatabaseMigrationTest`
- **نتیجه تست:** موفقیت‌آمیز (تمام ۳ تست پاس شدند).
- **وضعیت:** **FIXED**

---

## ۳. موارد نیازمند اقدام خارج از مخزن (Needs External Action)

1. **استقرار کد جدید در Google Apps Script:**
   - فایل اصلاح‌شده `scripts/google_apps_script.js` در مخزن قرار دارد. کاربر/مدیر سیستم باید این کد را در پروژه Apps Script متصل به گوگل شیت خود Paste کرده و یک نسخه جدید (New Deployment) منتشر نماید.
2. **چرخش کلیدها و تغییر اختیاری URL در صورت تمایل:**
   - آدرس وب‌اپلیکیشن در فایل `GoogleSheetsSyncManager.kt` تنظیم شده است. در صورت تمایل، کاربر می‌تواند یک Deployment مستقل برای خود بسازد.
