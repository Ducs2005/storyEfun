# 📚 Story Reading App

A mobile application that allows users to read online stories with a coin-based unlock system.  
The system includes both **client and backend services**, supporting story management, user authentication, and in-app purchases.

## 🚀 Overview

This project was developed as a university software project.  
It simulates a real-world **digital story platform** where users can read stories, unlock chapters using coins, and interact with content.

The system consists of:

- **Client**: Mobile application for users to read stories
- **Backend / Services**: Handles authentication, data storage, and business logic

## ✨ Features

### User Features
- User registration and login
- Browse story list
- Read stories by chapter
- Unlock locked chapters using coins
- Daily check-in to receive coins
- Add stories to favorites
- Comment and rate stories
- Recharge coins via payment integration

### Admin Features
- Manage stories
- Add / edit / delete chapters
- Lock or unlock chapters
- Manage users
- View statistics

## 🏗️ Architecture

The project follows the **MVVM architecture** to improve maintainability and scalability.

## 🛠️ Technologies Used

**Frontend / Client**
- Android (Java/Kotlin)
- Android Studio
- MVVM Architecture

**Backend / Services**
- Firebase Authentication
- Firebase Realtime Database / Firestore
- Cloudinary (Image storage)

**Payment**
- ZaloPay Sandbox


## ⚙️ Installation Guide

### 1. Clone the repository

```bash
git clone https://github.com/your-username/your-repository.git
cd your-repository
```

### 2. Open the project

Open the project using **Android Studio**.

* Select **Open Project**
* Choose the project folder you just cloned

### 3. Setup Firebase

This project uses **Firebase services**.

1. Go to Firebase Console and create a new project
2. Enable the following services:

   * Firebase Authentication
   * Firestore Database (or Realtime Database)
3. Download the **google-services.json** file
4. Place it inside the **app/** directory

```
app/
 ├── src/
 ├── build.gradle
 └── google-services.json
```

### 4. Configure Cloudinary (optional)

If the project requires image upload:

* Create a Cloudinary account
* Replace the Cloudinary configuration values in the project with your own credentials.

### 5. Run the application

1. Connect an Android device or start an emulator
2. Click **Run** in Android Studio

The application should now build and launch successfully.
