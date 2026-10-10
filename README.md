# مرخصی بهار هونی — نسخه 1.3 (اصلاح‌شده)

اپ اندروید (Kotlin + Jetpack Compose) با Firebase Authentication و Firestore و Storage.

## تغییرات نسخه 1.3 نسبت به 1.2
- اضافه شدن صفحه ثبت‌نام با ایمیل و رمز عبور؛ نسخه قبلی فقط صفحه ورود داشت.
- اعتبارسنجی ایمیل و رمز عبور و پیام‌های فارسی برای خطاهای رایج ثبت‌نام.
- اجباری شدن پیوست گواهی پزشکی برای مرخصی استعلاجی.
- بررسی قالب تاریخ و جلوگیری از تاریخ شروع بعد از پایان.

## تغییرات نسخه 1.2 نسبت به 1.1
- رفع خطاهای Build (تم Manifest، وابستگی coroutines، تابع statusColor، فایل gradle.properties)
- اضافه شدن Workflow ساخت خودکار APK در GitHub Actions
- چیدمان راست‌به‌چپ، مخفی‌شدن رمز عبور، کلید Back، اسکرول صفحات
- پیام‌های خطای فارسی و قابل‌فهم
- نمایش نام کاربر در داشبورد، مرتب‌سازی درخواست‌ها بدون نیاز به Index
- اپ اگر Storage فعال نباشد کرش نمی‌کند (فقط آپلود گواهی پزشکی خطا می‌دهد)
- آیکن اپ و فایل‌های Rules آماده (پوشه firebase-rules)

## ساختار Firestore
- `users/{uid}`: name, employeeCode, email
- `leaveBalances/{uid}`: entitlementDays, usedDays, incentiveDays, carriedDays (عدد)
- `leaveRequests/{id}`: userId, leaveType, startDate, endDate, hours, reason, medicalCertificateUrl, status, managerNote, createdAt
- `leaveTransactions/{id}`: userId, type, days, source, createdAt

## نکات مهم
- فایل `app/google-services.json` را باید از Firebase Console بگیرید (نام پکیج: `com.baharhooni.morkhesi`).
- Cloud Storage در Firebase نیاز به پلن Blaze دارد. بدون آن بقیه اپ کار می‌کند ولی آپلود گواهی پزشکی نه.
- خروجی این Workflow نسخه Debug است؛ برای نصب مستقیم روی گوشی مناسب است، نه انتشار در مارکت.


## نکته راه‌اندازی Firebase
- در Firebase Console بخش Authentication، روش ورود **Email/Password** باید فعال باشد.
- ساخت حساب در اپ، حساب Firebase ایجاد می‌کند؛ برای نمایش مانده مرخصی و نام پرسنل، مدیر باید سند `users/{uid}` و `leaveBalances/{uid}` را طبق ساختار بالا در Firestore تنظیم کند.
- این نسخه هنوز پنل مدیریتی داخل اپ ندارد؛ تأیید/رد درخواست‌ها طبق قوانین فعلی از Firebase Console انجام می‌شود.


## نسخه 1.4 — لوگوی شرکت
- فایل لوگوی ارسالی در `app/src/main/res/drawable-nodpi/baharhooni_company_logo.png` اضافه شد.
- لوگو در صفحه ورود برنامه نمایش داده می‌شود.
- این پروژه هنوز نیازمند Build و تست روی دستگاه واقعی است؛ ساخت موفق APK در این مرحله تأیید نشده است.
