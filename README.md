# Spendly

An Android expense tracking application built in Kotlin that automates receipt scanning, categorizes daily spending, and exports formatted PDF financial reports.

## Key Features
- **Receipt OCR Scanning:** Uses Google ML Kit Text Recognition to scan physical bills via the device camera and extract expense details automatically.
- **Local Expense Management:** Stores and organizes categorized transactions offline using Room Database with reactive UI updates.
- **Visual Spending Analytics:** Displays interactive category-wise expense breakdowns using MPAndroidChart pie charts.
- **PDF Report Export:** Generates and exports formatted expense summaries directly to the device's `Documents` folder.

## Tech Stack
- **Mobile App:** Kotlin, XML Layouts, Android SDK
- **Local Storage:** Room Persistence Library (SQLite), KSP
- **Machine Learning & Charts:** Google ML Kit Text Recognition, MPAndroidChart
- **Reporting:** Android PdfDocument API

## Download & Testing
An installable debug APK (`Spendly-v1.0.apk`) is available under the [Releases](https://github.com/Bushra130/Spendly/releases) section of this repository for quick evaluation on Android devices.

## Author
- **Bushra Shaikh** ([@Bushra130](https://github.com/Bushra130)) — Developed as part of the Android Development Internship at Codec Technologies.
