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


## ⚙️ Installation

### 1️⃣ Clone the repository

```bash
git clone https://github.com/Ducs2005/storyEfun.git
2️⃣ Open project

Open the client project using Android Studio.

3️⃣ Configure Firebase

Create a Firebase project

Add google-services.json to the project

Enable Authentication and Database

4️⃣ Run the app

Run the application on an emulator or Android device.
