# 🥗 SharePlate: Campus Food Rescue & Sustainability Platform (v1.0.0 Pro)

> *Connecting verified campus food providers with students to eliminate food waste, reduce hunger, and promote campus sustainability through real-time technology and AI.*

---

## 📌 Problem Statement & Solution

### The Problem
Every day, campus canteens, hostel messes, and event organizers generate significant quantities of surplus, safe food. Simultaneously, many students struggle with food access or tight budgets. Traditional food waste management on campuses lacks a real-time, transparent, and frictionless mechanism to redistribute surplus meals before they expire.

### The Solution
**SharePlate** is a production-ready, native Android application built in Kotlin that bridges this gap. It provides a real-time marketplace where providers can list surplus meals, and students can browse, filter, reserve portions securely, and verify pickups using cryptographically unique QR passes.

---

## 🤖 Artificial Intelligence (AI) & Machine Learning Integration

SharePlate features cutting-edge on-device AI capabilities:
1. **✨ AI Smart Description Parser (NLP Entity Extraction)**:
   - Providers can type natural, unstructured sentences (e.g., *"15 veg biryani boxes ready at Canteen A"*). 
   - The on-device AI engine (`FoodListingAssistant.kt`) automatically extracts quantities, cleans up dish titles, determines dietary preferences (Veg/Non-Veg), identifies pickup locations, and computes a dynamic confidence score to **auto-fill form fields**.
2. **🤖 ShareBot (In-App AI Chat Assistant)**:
   - Students can chat with **ShareBot** (`ShareBotService.kt`) to get instant answers regarding food availability, pickup processes, QR passes, cancellation policies, and campus sustainability.
3. **🌱 AI ESG & Carbon Footprint Estimator**:
   - The **Impact Dashboard** calculates real-time environmental metrics (CO₂ emissions prevented and water saved) based on scientific food-rescue formulas (~2.5 kg CO₂ and ~500L water saved per rescued meal).

---

## ⭐ Advanced & Unique Features

- **🎯 First-Launch Onboarding Walkthrough**: 3-step introductory slides cached locally with `SharedPreferences`.
- **⏱️ Food Rescue Timer & Urgency Badges**: Dynamic indicators (`🟢 Fresh`, `🟡 Pickup Soon`, `🔴 Ending Soon`) on listing cards.
- **🏆 Campus Impact Leaderboard**: Real-time ranking of top campus food rescuers.
- **↩️ Cancel & Release Food**: Students can cancel reservations at any time before pickup, instantly returning portions to the live community feed.
- **📊 Provider Analytics**: Real-time tracking of active listings published and meal collection statistics.
- **🥗 Custom Preferences & Profile Editing**: Dietary preference toggles (Veg/All), push notification settings, and display name editing.

---

## 🏗️ Technical Architecture & Tech Stack

- **Language**: 100% Kotlin
- **UI Toolkit**: XML Layouts with Material 3 Design Components (`TextInputLayout`, `MaterialCardView`, `CircularProgressIndicator`, Bottom Navigation)
- **Architecture**: Single-Activity Architecture with Jetpack Navigation Component, ViewModel, and LiveData (MVVM)
- **Backend & Database**: Firebase Authentication (Email/Password with role-based routing) & Cloud Firestore (Real-time `snapshotListener` syncing)
- **Background Processing**: WorkManager (Local pickup reminders)
- **QR Code & Scanning**: ZXing Library (`zxing-android-embedded` for QR generation and camera barcode scanning)
- **Design System**: Strict color palette (Primary Teal `#0F766E`, Dark Navy `#17324D`, Mint Background `#E6FFFA`, Fresh Green `#16A34A`, Alert Amber `#F59E0B`)

---

## 👥 Core User Flows & Roles

### 1. Student Flow
- **Authentication**: Register/Login as a Student.
- **Home Feed**: Browse active surplus food listings in real-time with urgency timers. Filter by All, Veg, or Non-Veg using Material 3 chips.
- **Listing Details & Reservation**: View item details, remaining portions, pickup window, and reserve a portion securely via atomic Firestore transactions (preventing race conditions).
- **QR Pickup Pass & Passes Tab**: Receive a unique QR code pass. Show it at the canteen during pickup, or manage past reservations in the "Passes" tab.
- **ShareBot & Impact**: Chat with ShareBot, track CO₂ saved on the Impact Dashboard, view the Campus Leaderboard, and manage preferences.

### 2. Provider Flow (Canteens / Mess / Events)
- **Authentication**: Register/Login as a verified Provider.
- **Create Listing**: Publish surplus food using manual inputs or the **AI Smart Description Parser**.
- **QR Scanner (`ProviderScanFragment`)**: Use the built-in camera barcode scanner to scan student QR pickup passes and mark reservations as `"collected"` with one tap.
- **Provider Analytics**: Track active and published listings in Profile.

---

## 🔒 Security & Firestore Rules

SharePlate enforces strict role-based security using Cloud Firestore Security Rules (`firestore.rules`):
- **Users (`users/{uid}`)**: Authenticated read, owner-only write.
- **Listings (`listings/{listingId}`)**: Authenticated read, provider-only create/update/delete for their own listings.
- **Reservations (`reservations/{reservationId}`)**: Secure student reservation creation (`studentId == request.auth.uid`) with atomic quantity decrements and cancellation support.
- **Notifications (`notifications/{notifId}`)**: User-specific alert logs.

---

## 🚀 Getting Started & Setup

1. **Clone & Open**: Open the project folder in Android Studio.
2. **Firebase Connection**: Ensure your `google-services.json` file is placed in `app/google-services.json`.
3. **Firestore Rules**: Copy rules from `firestore.rules` into your Firebase Console under **Firestore Database > Rules** and click **Publish**.
4. **Build & Run**: Sync Gradle with project files and run on an emulator or physical Android device.
<img width="295" height="613" alt="Screenshot 2026-10-30 212208" src="https://github.com/user-attachments/assets/5a788a41-3cbe-40bc-af77-56d23cb16f53" />
