<div dir="rtl" align="right">

# 💳 Smart Wallet — کیف پول هوشمند

**مدیریت ساده‌تر امور مالی شخصی، با کمک هوش مصنوعی**

Smart Wallet یک اپلیکیشن اندرویدی برای ثبت و مدیریت تراکنش‌ها، حساب‌های بانکی، بودجه، دارایی‌ها و تعهدات مالی است. هدف پروژه کاهش ورود دستی اطلاعات و فراهم‌کردن یک نمای یکپارچه از وضعیت مالی کاربر است.

</div>

<div align="center">

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/compose)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24-informational)](https://developer.android.com/tools/releases/platforms)
[![Compile SDK](https://img.shields.io/badge/Compile%20SDK-35-informational)](https://developer.android.com/tools/releases/platforms)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

</div>

---

## ✨ امکانات اصلی

- **پردازش هوشمند پیامک بانکی:** استخراج اطلاعات تراکنش از پیامک با استفاده از Google Gemini و Firebase AI.
- **دریافت پیامک در اندروید:** استفاده از SMS Receiver برای دریافت پیامک‌های بانکی، با رعایت مجوزهای لازم.
- **مدیریت تراکنش‌ها:** ثبت و پیگیری درآمدها و هزینه‌ها.
- **مدیریت حساب‌ها:** نگهداری اطلاعات حساب‌های بانکی.
- **دسته‌بندی و بودجه‌بندی:** سازمان‌دهی تراکنش‌ها و بررسی وضعیت هزینه‌ها.
- **مدیریت دارایی و بدهی:** ثبت دارایی‌های غیرنقدی، بدهی‌ها و وام‌ها.
- **مدیریت چک‌ها:** پیگیری چک‌ها و تعهدات مالی.
- **گزارش و خروجی:** امکان خروجی گرفتن از داده‌های مالی.
- **ذخیره‌سازی محلی:** استفاده از Room برای نگهداری داده‌ها روی دستگاه.

## 🧠 جریان پردازش پیامک

<div align="center">

```text
       پیامک بانکی دریافتی
                │
                ▼
       Android SMS Receiver
                │
                ▼
      پردازش و آماده‌سازی متن
                │
                ▼
          Gemini / Firebase AI
                │
                ▼
       داده ساختاریافته تراکنش
                │
                ▼
        Finance Repository
                │
                ▼
           Room Database
                │
                ▼
       نمایش در بخش‌های مالی اپ
```

</div>

> پردازش هوشمند پیامک به تنظیم صحیح سرویس AI و دسترسی‌های موردنیاز وابسته است. رفتار برنامه را پیش از استفاده با اطلاعات واقعی بانکی آزمایش کنید.

## 🛠️ فناوری‌ها

| بخش | فناوری |
|---|---|
| زبان برنامه‌نویسی | Kotlin 2.2.10 |
| رابط کاربری | Jetpack Compose، Material 3 |
| معماری داده | Repository pattern |
| پایگاه داده محلی | Room |
| تولید کد | KSP |
| پردازش ناهمگام | Kotlin Coroutines |
| هوش مصنوعی | Firebase AI / Google Gemini |
| ارتباط شبکه | Retrofit، OkHttp |
| JSON | Moshi |
| محافظت از سرویس‌ها | Firebase App Check / reCAPTCHA |
| تست | JUnit، Robolectric، Espresso، Roborazzi |

## 🧱 ساختار کلی پروژه

ساختار زیر نمایی خلاصه از بخش‌های اصلی کد است؛ ممکن است با توسعه پروژه تغییر کند.

```text
smart-wallet-android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   └── java/com/example/
│   │   │       ├── MainActivity.kt
│   │   │       ├── data/
│   │   │       │   ├── FinanceRepository.kt
│   │   │       │   ├── SmsAiParser.kt
│   │   │       │   ├── SmsReceiver.kt
│   │   │       │   ├── Exporter.kt
│   │   │       │   ├── dao/
│   │   │       │   ├── database/
│   │   │       │   └── model/
│   │   │       └── domain/
│   │   │           └── util/
│   │   └── androidTest/
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml
├── .env.example
├── build.gradle.kts
└── settings.gradle.kts
```

### اجزای مهم

- `SmsReceiver`: دریافت رویداد پیامک ورودی اندروید.
- `SmsAiParser`: آماده‌سازی و تحلیل پیامک برای استخراج اطلاعات تراکنش.
- `FinanceRepository`: نقطه دسترسی لایه داده برای عملیات مالی.
- `dao/` و `database/`: اجزای ذخیره‌سازی و دسترسی به داده با Room.
- `Exporter`: ابزارهای خروجی گرفتن از اطلاعات.

## 📋 پیش‌نیازهای توسعه

تنظیمات فعلی پروژه:

| مورد | مقدار |
|---|---|
| Android Gradle Plugin | 9.1.1 |
| Gradle Wrapper | 9.3.1 |
| Kotlin | 2.2.10 |
| compileSdk | 35 |
| targetSdk | 35 |
| minSdk | 24 (Android 7.0) |
| Java source / target | 11 |
| Kotlin JVM target | 11 |

برای توسعه، Android Studio سازگار با نسخه‌های Gradle و Android Gradle Plugin پروژه و Android SDK Platform 35 را نصب کنید.

## 🚀 دریافت و اجرای پروژه

ابتدا مخزن را دریافت کنید:

```bash
git clone https://github.com/mohashemi2002-arch/smart-wallet-android.git
cd smart-wallet-android
```

سپس پروژه را در Android Studio باز کنید و منتظر بمانید Gradle Sync کامل شود.

### تنظیم کلید Gemini

پروژه از Secrets Gradle Plugin استفاده می‌کند و فایل نمونه `.env.example` را در ریشه مخزن دارد.

1. از فایل `.env.example` یک کپی با نام `.env` در ریشه پروژه بسازید.
2. مقدار نمونه را با کلید معتبر Gemini جایگزین کنید:

```env
GEMINI_API_KEY=YOUR_GEMINI_API_KEY
```

3. فایل `.env` و کلید واقعی را در Git Commit نکنید.

> فایل `.env.example` فقط برای نمایش نام متغیر و قالب تنظیمات است؛ مقدار نمونه داخل آن یک کلید معتبر نیست.

### Build نسخه Debug

در Linux یا macOS:

```bash
./gradlew assembleDebug
```

در Windows PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

فایل APK نسخه Debug پس از Build موفق، معمولاً در مسیر زیر قرار می‌گیرد:

```text
app/build/outputs/apk/debug/
```

## 🧪 اجرای تست‌ها

برای اجرای تست‌های Unit:

```bash
./gradlew test
```

برای اجرای تست‌های Instrumentation روی دستگاه یا Emulator متصل:

```bash
./gradlew connectedAndroidTest
```

در Windows از `gradlew.bat` استفاده کنید.

## 🔐 حریم خصوصی و امنیت

این برنامه ممکن است با پیامک‌ها و اطلاعات مالی کاربر سروکار داشته باشد. هنگام توسعه، آزمایش و انتشار به نکات زیر توجه کنید:

- دسترسی به SMS حساس است؛ فقط در صورت نیاز واقعی و با اطلاع و رضایت کاربر درخواست شود.
- از داده‌های بانکی واقعی در Logها، Issueها، Screenshotها و مخزن عمومی استفاده نکنید.
- کلید Gemini، فایل‌های محرمانه Firebase و سایر Credentialها را در Git قرار ندهید.
- پیش از ارسال متن پیامک به سرویس AI، ضرورت ارسال و میزان داده موردنیاز را بررسی کنید.
- فایل‌های خروجی مالی را داده حساس تلقی کنید و در نگهداری یا اشتراک‌گذاری آن‌ها احتیاط کنید.
- **پیش از انتشار نسخه Release، تنظیمات امضای برنامه را بازبینی کنید.** رمزها و اطلاعات Keystore باید فقط از طریق متغیرهای محیطی امن یا Secretهای CI تأمین شوند؛ از نگهداری Credential ثابت در کد یا فایل‌های قابل Commit خودداری کنید.
- مجوزهای SMS و سایر دسترسی‌ها ممکن است تابع محدودیت‌های نسخه اندروید و سیاست‌های Google Play باشند. پیش از انتشار، الزامات روز را بررسی کنید.

## 🗺️ ایده‌های توسعه آینده

موارد زیر پیشنهادهایی برای توسعه‌های بعدی هستند و لزوماً به معنی پیاده‌سازی‌شدن آن‌ها در نسخه فعلی نیستند:

- افزایش پوشش بانک‌ها و قالب‌های پیامکی
- بهبود تشخیص و دسته‌بندی تراکنش‌ها
- گزارش‌ها و تحلیل‌های مالی پیشرفته‌تر
- پشتیبان‌گیری و بازیابی امن اطلاعات
- بهبود قابلیت استفاده در حالت آفلاین
- افزایش پوشش تست‌های خودکار

## 🤝 مشارکت

از گزارش خطا، پیشنهاد قابلیت و مشارکت در توسعه استقبال می‌شود.

1. مخزن را Fork کنید.
2. یک Branch برای تغییر خود بسازید:

```bash
git checkout -b feature/your-feature
```

3. تغییرات را پیاده‌سازی و تست‌های مرتبط را اجرا کنید.
4. یک Commit واضح ایجاد کنید:

```bash
git commit -m "feat: describe your change"
```

5. Branch را Push کرده و Pull Request ارسال کنید.

برای گزارش مشکلات امنیتی، لطفاً جزئیات آسیب‌پذیری یا اطلاعات قابل سوءاستفاده را در فضای عمومی منتشر نکنید.

## 📄 مجوز

این پروژه تحت مجوز **MIT** منتشر شده است. برای جزئیات، فایل [LICENSE](LICENSE) را ببینید.

## 👨‍💻 توسعه‌دهنده

**Mohammad Hashemi**

- GitHub: [@mohashemi2002-arch](https://github.com/mohashemi2002-arch)
- Repository: [smart-wallet-android](https://github.com/mohashemi2002-arch/smart-wallet-android)

---

<div align="center">

**ساخته‌شده با Kotlin، Jetpack Compose و Gemini AI** ❤️

</div>
