# گزارش پیاده‌سازی و اصلاحات امنیتی پروژه Smart Wallet (پولکی)

**تاریخ اجرا:** نوامبر ۲۰۲۶  
**شاخه گیت:** `security-and-technical-remediation`  
**وضعیت پیاده‌سازی:** کامل و اعتبارسنجی‌شده با تست‌های خودکار  
**تیم مسئول:** Application Security Specialist & Backend Lead  

---

## ۱. آسیب‌پذیری‌های برطرف‌شده (Remediated Vulnerabilities)

### ۱. آسیب‌پذیری SEC-P0-01: افشای رمز عبور و پین کد در پاسخ بازیابی ابری (Restore)
- **محل مشکل:** `scripts/google_apps_script.js` (متد `handleRestore`) و `app/src/main/java/com/example/data/sync/GoogleSheetsSyncManager.kt`
- **علت ریشه‌ای:** در کد قبلی، فیلدهای `passwordHash` و `pinHash` مستقیماً از شیت خوانده شده و در خروجی JSON ارسال می‌شدند. هر کلاینت با ارسال درخواست می‌توانست هش‌های کاربری را استخراج کند.
- **راهکار پیاده‌سازی‌شده:**
  1. حذف قطعی فیلدهای `passwordHash` و `pinHash` از پاسخ JSON در سرور Google Apps Script.
  2. حذف فیلدهای `passwordHash` و `pinHash` از کلاس داده `GoogleSheetsRestoreResult`.
  3. در سمت کلاینت اندروید، فرایند بازیابی حساب (`recoverFromCloud`) صرفاً اطلاعات عمومی پروفایل، کارت‌ها و تراکنش‌ها را بازیابی می‌کند و اعتبارسنجی/تولید هش امن به صورت کاملاً محلی انجام می‌پذیرد.
- **وضعیت:** **FIXED** (تست شده در `GoogleSheetsSyncTest.testRestoreSecurityOmitsCredentialHashes`)

---

### ۲. آسیب‌پذیری SEC-P0-02: افشای اطلاعات بدون احراز هویت در وب‌سرویس ابری (Broken Object Level Authorization)
- **محل مشکل:** `scripts/google_apps_script.js` (خطوط ۳۵-۷۰)
- **علت ریشه‌ای:** اندپوینت‌های `restore` و `sync` بدون هیچ‌گونه بررسی پسورد یا امضا، صرفاً با یک شماره موبایل یا نام کاربری داده‌ها را ارائه می‌کردند.
- **راهکار پیاده‌سازی‌شده:**
  1. طراحی پروتکل امضای دیجیتال بر پایه `HMAC-SHA256`:
     `authSignature = HMAC_SHA256(phone + ":" + action + ":" + timestamp, userPasswordHash)`
  2. بررسی پنجره اعتبارسنجی زمانی (Time-window) تا حداکثر ۱۵ دقیقه جهت جلوگیری از Replay Attack.
  3. در صورتی که امضای دریافتی با امضای محاسبه‌شده در سرور تطابق نداشته باشد، پاسخ با وضعیت `401 Unauthorized` برگردانده شده و دسترسی مسدود می‌گردد.
  4. حذف کامل اندپوینت‌های حساس از متد `doGet` و انتقال اجباری به `doPost` رمزگذاری‌شده.
- **وضعیت:** **FIXED**

---

### ۳. آسیب‌پذیری SEC-P0-03: تخریب و دستکاری داده‌های مالی کاربران دیگر در Sync
- **محل مشکل:** `scripts/google_apps_script.js` (متد `handleSync`)
- **علت ریشه‌ای:** متد `removeRowsForUserKey` تمام تراکنش‌های کاربر ثبت‌شده را قبل از درج رکوردهای جدید پاک می‌کرد؛ بدون اینکه تأیید هویت ارسال‌کننده سنجیده شود.
- **راهکار پیاده‌سازی‌شده:**
  1. الزام وجود `authSignature` معتبر برای هرگونه به‌روزرسانی حساب موجود در شیت.
  2. ایجاد محدودیت حداکثر رکوردها (حداکثر ۱۰۰ کارت و ۲۵۰۰ تراکنش) و برش رشته‌های ورودی جهت جلوگیری از حملات Denial of Service و Buffer Flooding در Google Sheets.
- **وضعیت:** **FIXED**

---

### ۴. آسیب‌پذیری SEC-P0-04: الگوریتم ضعیف هش PIN با سالت ثابت و ریسک Rainbow Table
- **محل مشکل:** `app/src/main/java/com/example/domain/util/AuthManager.kt`
- **علت ریشه‌ای:** استفاده از SHA-256 با یک سالت ثابت متنی (`"SmartWallet_Auth_Salt_2026_Secured#"`) برای پین کدهای ۴ رقمی.
- **راهکار پیاده‌سازی‌شده:**
  1. ایجاد کلاس مستقل و استاندارد `PasswordHasher.kt`.
  2. پیاده‌سازی استاندارد `PBKDF2WithHmacSHA256` با ۱۰,۰۰۰ دور (Iterations) و کلید ۲۵۶ بیتی.
  3. تولید سالت تصادفی ۱۶ بایتی کاملاً مستقل و یکتا برای هر پین کد یا رمز عبور با `SecureRandom`.
  4. ذخیره با ساختار استاندارد قابل ارتقا: `pbkdf2$<iterations>$<saltHex>$<hashHex>`.
  5. استفاده از `MessageDigest.isEqual` برای مقایسه با زمان ثابت (Constant-Time) جهت محافظت در برابر حملات Timing Side-Channel.
  6. پیاده‌سازی مهاجرت خودکار و بدون زحمت (Transparent Migration): کاربران دارای هش قدیمی به محض ورود با پین یا رمز صحیح، به صورت خودکار به هش امن PBKDF2 ارتقا می‌یابند.
- **وضعیت:** **FIXED** (تست شده در `PasswordHasherTest`)

---

## ۲. خلاصه وضعیت امنیتی

| شناسه | وضعیت | تست خودکار | فایل متأثر |
| :--- | :---: | :---: | :--- |
| حذف افشای هش‌ها در Restore | **FIXED** | `GoogleSheetsSyncTest` | `google_apps_script.js`, `GoogleSheetsSyncManager.kt` |
| احراز هویت امضامحور در سرور ابری | **FIXED** | `GoogleSheetsSyncTest` | `google_apps_script.js`, `GoogleSheetsSyncManager.kt` |
| ارتقای رمزنگاری پین و پسورد به PBKDF2 | **FIXED** | `PasswordHasherTest` | `PasswordHasher.kt`, `AuthManager.kt` |
| مقایسه امن در برابر حملات زمانی | **FIXED** | `PasswordHasherTest` | `PasswordHasher.kt` |
| مهاجرت شفاف کاربران قدیمی | **FIXED** | `PasswordHasherTest` | `AuthManager.kt` |
