# GTS Tarek Aim Guide — Android Source Project

## গুরুত্বপূর্ণ সত্য
এই ZIP-টি Android source project। এটি নিজে APK নয়। এখানে APK build চালিয়ে পরীক্ষা করার Android SDK/Gradle পরিবেশ নেই, তাই Build 100% সফল হবে বলে মিথ্যা নিশ্চয়তা দেওয়া হচ্ছে না। GitHub Actions workflow-টি ঠিক করা হয়েছে যেন `gradlew` না থাকলেও Gradle 8.7 ব্যবহার করে build চালাতে পারে এবং APK artifact হিসেবে আপলোড করে।

## এই ZIP-এ কী আছে
- আপনার দেওয়া GTS TAREK লোগো
- Play বাটনে password `GTSTAREK141` যাচাই (আপনার সোর্স কোডে থাকা পাসওয়ার্ড)
- Carrom Pool ইনস্টল থাকলে সেটি চালু করা, না থাকলে Google Play লিংক খোলা
- Telegram চ্যানেল লিংক: https://t.me/gtstarek
- অন্য অ্যাপের ওপর manual aim-line overlay; aim overlay নিজে গেমের টাচ ইভেন্ট আটকায় না
- স্ক্রিন ক্যাপচার-ভিত্তিক পরীক্ষামূলক bright-object marker (নির্ভুল coin detection নয়)
- GitHub Actions workflow; `gradlew` প্রয়োজন নেই

## ফোন দিয়ে GitHub-এ APK বানানোর নিয়ম
1. এই ZIP ডাউনলোড করে Extract করুন।
2. Extract করা `GTS_Tarek_Aim_Guide_Source` ফোল্ডারের **ভেতরের সব ফাইল ও ফোল্ডার** GitHub repository-র root-এ upload করুন। খেয়াল করুন: `.github` hidden folder-টিও আপলোড হতে হবে। সবচেয়ে সহজে GitHub-এ repository খুলে **Add file → Upload files** ব্যবহার করুন; ফোল্ডারের ভিতরের ফাইলগুলো নির্বাচন করুন।
3. Upload শেষে repository-র মূল পেজে `app`, `.github`, `build.gradle`, `settings.gradle` এবং `gradle.properties` দেখা উচিত। যদি সবকিছু আরেকটি nested project folder-এর ভিতরে থাকে, workflow root-এ project খুঁজে পাবে না।
4. **Actions** ট্যাবে যান → **Build Android APK** workflow নির্বাচন করুন → **Run workflow** চাপুন। প্রথমবার না দেখা গেলে repository-তে একটি commit করুন, তারপর Actions দেখুন।
5. Build শেষ না হওয়া পর্যন্ত অপেক্ষা করুন। সবুজ ✓ মানে workflow সফল। লাল ✗ হলে Build log-এর প্রথম লাল Error-টি পরীক্ষা করতে হবে।
6. সফল হলে ওই run-এর নিচে **Artifacts** অংশে `GTS-Tarek-Aim-Guide-debug-apk` চাপুন। ZIP artifact ডাউনলোড করে Extract করুন; ভেতরে `app-debug.apk` থাকবে। সেটি ফোনে খুলে Install করুন। Android যদি অনুমতি চায়, আপনার সম্মতিতে “Install unknown apps” অনুমতি দিন।

## যদি আবার Build ব্যর্থ হয়
Actions run খুলে `Build debug APK` ধাপটি খুলুন এবং প্রথম Error-এর স্ক্রিনশট বা লেখা পাঠান। অনুমান করে অন্য ফাইল বদলাবেন না।

## ফিচারের সীমাবদ্ধতা
- Manual aim line কেবল হাতে সরানো যায়; গেমের আসল physics বা aim data পড়ে না।
- Automatic detection পরীক্ষামূলক bright-pixel search; এটি গুটি নির্ভুলভাবে শনাক্ত করে না এবং ভুল marker দেখাতে পারে।
- **Auto Play/স্বয়ংক্রিয়ভাবে ম্যাচ খেলা বা ম্যাচ জেতার ফিচার নেই।** Aim Guide কেবল হাতে লক্ষ্য নির্ধারণে সহায়তা করে।
- Source project আগের পরিবেশে build বা বাস্তব ফোনে পরীক্ষা করা হয়নি। সুতরাং APK-র সফল build বা কার্যকারিতা নিশ্চিত নয়।
- পাসওয়ার্ড অ্যাপের কোডে রাখা আছে; এটি নিরাপদ authentication নয় এবং APK বিশ্লেষণ করে দেখা সম্ভব।


## সংশোধিত আচরণ
- পূর্ণ-স্ক্রিন AimView-এ `FLAG_NOT_TOUCHABLE` যোগ করা হয়েছে, যাতে গেমের টাচ সাধারণত ওভারলে দিয়ে ব্লক না হয়। Aim Point সরাতে floating panel-এর বোতাম ব্যবহার করুন।
- Aim Point বাম/ডান/উপর/নিচে সরানোর বোতাম যোগ করা হয়েছে।
- Screen Marker ফিচারকে পরীক্ষামূলক হিসেবে স্পষ্ট করা হয়েছে; এটি প্রতিটি গুটি/স্টিকার শনাক্ত করে না এবং গুটির সঠিক থামার জায়গা গণনা করে না।
- Auto Play যোগ করা হয়নি; সোর্সে থাকা স্ক্রিন-স্যাম্পলিং থেকে নিরাপদ/নির্ভরযোগ্যভাবে স্বয়ংক্রিয় ম্যাচ খেলা তৈরি করা যায় না।
