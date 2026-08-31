<div dir="rtl" align="right">

# 💳 smart-wallet-android

> یک کیف پول هوشمند برای مدیریت امور مالی شخصی در اندروید، طراحی‌شده برای کاربران ایرانی با قابلیت پردازش خودکار پیامک‌های بانکی، ثبت و مدیریت تراکنش‌ها، بودجه‌بندی، مدیریت دارایی‌ها و مدیریت بدهی، وام و چک.

</div>

<div dir="ltr" align="center">

[![Platform](https://img.shields.io/badge/platform-Android-brightgreen)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF?logo=kotlin\&logoColor=white)](#)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose\&logoColor=white)](#)
[![Min SDK](https://img.shields.io/badge/minSdk-24-informational)](#)
[![Target SDK](https://img.shields.io/badge/targetSdk-36-informational)](#)

</div>

<div dir="rtl" align="right">

## ✨ معرفی

**smart-wallet-android** یک کیف پول هوشمند اندرویدی است که با هدف کاهش میزان ورود دستی اطلاعات و ساده‌تر کردن مدیریت امور مالی شخصی طراحی شده است.

به‌جای اینکه کاربر مجبور باشد هر تراکنش بانکی را به‌صورت دستی وارد کند، برنامه می‌تواند پیامک‌های بانکی را دریافت کرده و با استفاده از **Gemini AI** اطلاعات مالی موجود در پیامک را استخراج و به داده‌های ساختاریافته تبدیل کند. سپس تراکنش‌ها به‌صورت محلی ذخیره شده و امکان دسته‌بندی و مدیریت آن‌ها در قالب سوابق مالی وجود دارد.

این پروژه همچنین دارای مدل‌های داده و Repositoryهای اختصاصی برای مدیریت حساب‌های بانکی، تراکنش‌ها، دسته‌بندی‌ها، دارایی‌های غیرنقدی، بدهی‌ها و وام‌ها و فرستندگان پیامک‌های بانکی است.

## 🚀 قابلیت‌ها

- 🤖 **پردازش هوشمند پیامک‌های بانکی با هوش مصنوعی** با استفاده از Google Gemini
- 📩 **تشخیص خودکار تراکنش‌های بانکی** از طریق SMS Receiver اندروید
- 💰 **ثبت و مدیریت درآمد و هزینه‌ها**
- 🏦 **مدیریت حساب‌های بانکی**
- 🏷️ **دسته‌بندی تراکنش‌ها**
- 📊 **نمای کلی وضعیت مالی و پشتیبانی از بودجه‌بندی**
- 💎 **مدیریت دارایی‌های غیرنقدی**
- 🤝 **مدیریت بدهی‌ها و وام‌ها**
- 🧾 **مدیریت چک‌ها و تعهدات مالی**
- 📤 **خروجی گرفتن از اطلاعات مالی**
- 💾 **ذخیره‌سازی اطلاعات به‌صورت محلی با Room**
- 🌐 **ارتباط با API و سرویس‌های شبکه با Retrofit و OkHttp**
- 🔐 **مدیریت امن اطلاعات محرمانه و API Credentialها**
- 🛡️ **یکپارچه‌سازی Firebase App Check / reCAPTCHA**
- 🧪 **پشتیبانی از تست‌های Unit، Instrumentation، رابط کاربری Compose و تست‌های Screenshot**

## 🧠 نحوه پردازش تراکنش‌ها با هوش مصنوعی

</div>

<div dir="ltr" align="center">

```text
پیامک بانکی دریافتی
       │
       ▼
Android SMS Receiver
       │
       ▼
پردازش و نرمال‌سازی پیامک
       │
       ▼
Gemini AI
       │
       ▼
اطلاعات ساختاریافته تراکنش
       │
       ▼
Finance Repository
       │
       ▼
Room Database
       │
       ▼
رابط کاربری کیف پول / گزارش‌ها / بودجه‌بندی
```

</div>

<div dir="rtl" align="right">

برنامه شامل یک کامپوننت اختصاصی با نام `SmsAiParser` و همچنین `SmsReceiver` است که مسئول دریافت و پردازش پیامک‌های بانکی است.

> **نکته حریم خصوصی:** دسترسی به پیامک‌ها یک دسترسی بسیار حساس است. کاربران باید قبل از اعطای این مجوز، به‌طور کامل بدانند برنامه چه اطلاعاتی را می‌خواند و این اطلاعات چگونه پردازش می‌شوند. هرگز کلیدهای واقعی API، اطلاعات بانکی یا داده‌های مالی شخصی را داخل Repository قرار ندهید.

## 🏗️ معماری

ساختار کد پروژه بر اساس جداسازی نسبتاً سبک بین رابط کاربری، لایه داده، ابزارهای Domain و سیستم ذخیره‌سازی طراحی شده است.

</div>

<div dir="ltr" align="left">

```text
app/
└── src/
    └── main/
        └── java/com/example/
            ├── MainActivity.kt
            ├── data/
            │   ├── FinanceRepository.kt
            │   ├── SmsAiParser.kt
            │   ├── SmsReceiver.kt
            │   ├── Exporter.kt
            │   ├── dao/
            │   ├── database/
            │   └── model/
            └── domain/
                └── util/
```

</div>

<div dir="rtl" align="right">

### لایه‌های اصلی

| لایه | مسئولیت |
|---|---|
| `MainActivity` / Compose UI | صفحات برنامه و تعامل با کاربر |
| `data/model` | مدل‌های داده مربوط به حوزه مالی |
| `data/dao` | Data Access Objectهای دیتابیس Room |
| `AppDatabase` | پیکربندی دیتابیس محلی |
| `FinanceRepository` | مدیریت عملیات اصلی داده و Repository |
| `SmsReceiver` | دریافت رویدادهای مربوط به پیامک‌های بانکی اندروید |
| `SmsAiParser` | تبدیل محتوای پیامک بانکی به اطلاعات ساختاریافته با استفاده از هوش مصنوعی |
| `Exporter` | خروجی گرفتن از اطلاعات مالی |
| `domain/util` | ابزارها و توابع کمکی مربوط به منطق کسب‌وکار و Domain |

## 🛠️ تکنولوژی‌های استفاده‌شده

- **Kotlin**
- **Jetpack Compose + Material 3**
- **Android SDK 36**
- **Min SDK 24 (Android 7.0)**
- **Room** برای ذخیره‌سازی محلی
- **KSP** برای تولید کد
- **Retrofit** برای ارتباط با APIهای HTTP
- **OkHttp** برای ارتباطات شبکه
- **Moshi** برای Serialization و Deserialization داده‌های JSON
- **Kotlin Coroutines** برای اجرای عملیات غیرهمزمان
- **Coil** برای بارگذاری تصاویر
- **Google Gemini / Firebase AI** برای تفسیر هوشمند پیامک‌های بانکی
- **Firebase App Check / reCAPTCHA**
- **Robolectric / JUnit / Espresso / Roborazzi** برای تست

## 📋 پیش‌نیازها

قبل از Build کردن پروژه، موارد زیر را آماده کنید:

- Android Studio با یک Android SDK جدید
- JDK 11 یا محیط سازگار با Gradle/JVM
- Android SDK Platform 36
- یک Gemini API Key معتبر برای قابلیت‌های هوش مصنوعی

پیکربندی فعلی Gradle پروژه از **SDK 36** استفاده می‌کند، سازگاری Java 11 را در نظر گرفته و حداقل نسخه پشتیبانی‌شده اندروید را **Android 7.0 / API 24** تعیین کرده است.

## ⚙️ پیکربندی

پروژه از **Secrets Gradle Plugin** استفاده می‌کند و یک فایل نمونه با نام `.env.example` در اختیار قرار داده شده است.

### 1. ساخت فایل `.env`

در محیط توسعه محلی، فایل `.env.example` را به `.env` کپی کنید.

### 2. وارد کردن Gemini API Key

کلید Gemini خود را در فایل قرار دهید:

</div>

<div dir="ltr" align="left">

```env
GEMINI_API_KEY=YOUR_GEMINI_API_KEY
```

</div>

<div dir="rtl" align="right">

### 3. محافظت از اطلاعات محرمانه

فایل `.env` و تمام Credentialهای واقعی را از Version Control خارج نگه دارید.

پیکربندی نمونه Repository، متغیر `GEMINI_API_KEY` را به‌عنوان Credential موردنیاز برای فراخوانی Gemini AI تعریف کرده است.

## ▶️ Build و اجرای پروژه

ابتدا Repository را Clone کنید:

</div>

<div dir="ltr" align="left">

```bash
git clone https://github.com/mohashemi2002-arch/smart-wallet-android.git
cd smart-wallet-android
```

</div>

<div dir="rtl" align="right">

سپس پروژه را در Android Studio باز کرده و اجازه دهید Gradle Sync انجام شود.

بعد از تنظیم Secretهای موردنیاز، Configuration مربوط به `app` را روی یک دستگاه اندرویدی یا Emulator اجرا کنید.

### Build از طریق Command Line

</div>

<div dir="ltr" align="left">

```bash
./gradlew assembleDebug
```

</div>

<div dir="rtl" align="right">

### در Windows PowerShell

</div>

<div dir="ltr" align="left">

```powershell
.\gradlew.bat assembleDebug
```

</div>

<div dir="rtl" align="right">

## 🔐 مجوزهای اندروید

برنامه مجوزهای زیر را در Android Manifest تعریف کرده است:

- دسترسی به اینترنت
- دریافت پیامک
- خواندن پیامک
- ارسال Notification
- زمان‌بندی Alarmهای دقیق

برخی از این Permissionها حساس هستند و ممکن است تحت قوانین و محدودیت‌های Android و Google Play قرار داشته باشند.

اگر قصد انتشار **smart-wallet-android** در Google Play را دارید، حتماً آخرین قوانین مربوط به Permissionها و دسترسی به SMS را بررسی کنید و مطمئن شوید کاربرد برنامه با سیاست‌های Google Play مطابقت دارد.

## 🧪 تست

پروژه زیرساخت لازم برای چندین سطح تست را در اختیار دارد:

- تست‌های Unit با JUnit
- تست‌های Coroutine
- تست‌های Robolectric
- تست‌های رابط کاربری Compose
- تست‌های Instrumentation اندروید
- تست‌های Screenshot با Roborazzi

### نمونه دستورات

</div>

<div dir="ltr" align="left">

```bash
./gradlew test
./gradlew connectedAndroidTest
```

</div>

<div dir="rtl" align="right">

## 📦 ساختار پروژه

</div>

<div dir="ltr" align="left">

```text
smart-wallet-android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   └── java/com/example/
│   │   │       ├── MainActivity.kt
│   │   │       ├── data/
│   │   │       │   ├── dao/
│   │   │       │   ├── database/
│   │   │       │   └── model/
│   │   │       └── domain/
│   │   └── androidTest/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── assets/
├── gradle/
├── .env.example
├── build.gradle.kts
├── gradle.properties
├── metadata.json
└── settings.gradle.kts
```

</div>

<div dir="rtl" align="right">

## 🔒 امنیت

از آنجا که این برنامه با اطلاعات مالی و پیامک‌های بانکی سروکار دارد:

- هرگز API Key یا Service Credentialهای واقعی را Commit نکنید.
- هرگز پیامک‌های واقعی بانکی یا اطلاعات مالی شخصی را داخل Repository قرار ندهید.
- فایل‌های Export شده مالی را به‌عنوان اطلاعات حساس در نظر بگیرید.
- میزان محتوای پیامکی که به سرویس‌های خارجی ارسال می‌شود را تا حد امکان کاهش دهید.
- قبل از استفاده از برنامه با اطلاعات مالی واقعی، نحوه پردازش داده‌ها توسط AI و API را بررسی کنید.
- Credentialهای مربوط به امضای نسخه Release را فقط از طریق Environment Variableهای امن یا یک سیستم مدیریت Secret مناسب استفاده کنید.

## 🗺️ نقشه راه

برخی قابلیت‌های احتمالی برای نسخه‌های آینده:

- [ ] پشتیبانی از فرمت‌های بیشتر پیامک بانکی بانک‌های ایرانی
- [ ] بهبود پردازش AI به‌صورت Offline-First و ایجاد روش‌های جایگزین در صورت عدم دسترسی به AI
- [ ] تحلیل‌های پیشرفته هزینه‌ها و نمودارهای مالی
- [ ] تشخیص خودکار درآمدها و هزینه‌های دوره‌ای
- [ ] پیشنهادهای هوشمندتر برای بودجه‌بندی
- [ ] پشتیبان‌گیری و بازیابی رمزنگاری‌شده
- [ ] همگام‌سازی اطلاعات بین چند دستگاه
- [ ] رمزنگاری قوی‌تر اطلاعات محلی
- [ ] افزایش پوشش تست‌های خودکار و Screenshot
- [ ] ایجاد Pipeline کامل و آماده برای انتشار نسخه Production

## 🤝 مشارکت در پروژه

مشارکت در توسعه پروژه، گزارش Bug و پیشنهاد قابلیت‌های جدید کاملاً استقبال می‌شود.

### مراحل مشارکت

**1.** Repository را Fork کنید.

**2.** یک Branch جدید برای قابلیت موردنظر ایجاد کنید:

</div>

<div dir="ltr" align="left">

```bash
git checkout -b feature/your-feature
```

</div>

<div dir="rtl" align="right">

**3.** تغییرات خود را اعمال کرده و در صورت امکان تست‌های مربوطه را نیز اضافه کنید.

**4.** تغییرات را Commit کنید:

</div>

<div dir="ltr" align="left">

```bash
git commit -m "feat: add your feature"
```

</div>

<div dir="rtl" align="right">

**5.** Branch را Push کرده و یک Pull Request ایجاد کنید.

برای مشکلات امنیتی حساس، از انتشار جزئیات آسیب‌پذیری یا روش Exploit به‌صورت عمومی خودداری کرده و از یک روش خصوصی برای گزارش مشکل امنیتی استفاده کنید.

## 📄 مجوز استفاده (License)

در حال حاضر فایل License مشخصی در Repository تعریف نشده است.

تا زمانی که یک License رسمی به پروژه اضافه نشده باشد، فرض کنید کد پروژه **به‌صورت خودکار مجوز استفاده یا بازنشر نامحدود ندارد**.

## 👤 توسعه‌دهنده

**Mohammad Hashemi**

GitHub: [@mohashemi2002-arch](https://github.com/mohashemi2002-arch)

</div>

<div dir="ltr" align="center">

---

**Built with Kotlin, Jetpack Compose, Room & Gemini AI** ❤️

</div>
