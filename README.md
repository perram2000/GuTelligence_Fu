ESP32CamApp
A Smart Toilet Camera Health Monitoring System
ESP32-CAM + Android Client + Google Vision AI Integration

Project Overview
ESP32CamApp is an integrated solution featuring an ESP32-CAM module, an Android mobile application, and Google Vision AI cloud image analysis.
The system allows users to configure the camera’s Wi-Fi, remotely view live streams, capture and sync images, and automatically analyze images for health insights using Google’s AI services.

Features
Wi-Fi Configuration: Easily configure the ESP32-CAM’s Wi-Fi connection from the Android app.
Live Streaming: View real-time video streams from the ESP32-CAM directly within the app.
Capture & Download: Take snapshots and save images to your phone or synchronize image batches.
Automatic Health Analysis: After images are saved, they are automatically uploaded to Google Vision AI for analysis (e.g., label detection).
Health Feedback: Analysis results are displayed in the app for user reference.
UDP Device Discovery: The app can automatically find the ESP32-CAM on your network for seamless connection.
System Architecture


ESP32-CAM <--Wi-Fi--> Android App <--HTTPS--> Google Vision AI
Screenshots
(Add screenshots of your app and workflow here if available.)

Getting Started
Prerequisites
ESP32-CAM module with custom firmware supporting HTTP API endpoints (e.g., /capture, /list.json, etc.)
Android device (API 23+ recommended)
Google Cloud account with Vision API enabled
Service Account JSON key (key.json)
Setup Steps
Clone this repository

Open the project in Android Studio

Add your Google Cloud Vision API key:

Place your key.json file into the app/src/main/assets/ directory.
Configure dependencies:

Ensure the following dependencies are in your app/build.gradle(.kts):
kotlin


implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")
implementation("com.squareup.okhttp3:okhttp:4.9.3")
implementation("org.json:json:20210307")
Connect your phone to the ESP32-CAM’s Wi-Fi hotspot and use the app’s config page to set the correct Wi-Fi credentials.

Build and run the app on your Android device.

Usage
Configure the ESP32-CAM:
Use the app’s Wi-Fi setup screen to send your router credentials to the ESP32-CAM.

View and Capture:
See the camera’s live feed in the app. Capture images or synchronize batches as needed.

Automatic Analysis:
After saving an image, the app uploads it to Google Vision AI and displays the recognition results.

Review Results:
Health analytics and labels are shown in the app’s status area.

Security & Privacy
All health-related images and data are processed securely. Your Google Cloud service key should be kept private and never committed to public repositories.
