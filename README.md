# QuickMart Food Delivery App 🍔📦

QuickMart is a complete food delivery app with two modules:
- **User App** for customers to browse, order, and track deliveries  
- **Admin App** for managing food items, orders, and dashboard analytics

---

## 🚀 Features

### 👨‍🍳 User App
- 🔐 OTP-based authentication using Firebase Auth  
- 🛍 Browse menus, view product details  
- 🛒 Add to cart and complete orders  
- 📍 Set delivery location via Google Maps API  

### 🧑‍💼 Admin App
- 🧾 CRUD operations for food items  
- 📦 View and manage user orders  
- 📸 Upload product images to Firebase Storage  
- 📊 Live order & product dashboard powered by Firebase Realtime DB  

---

## 📲 Screenshots

### User App Workflow  
<p float="left">
  <img src="https://raw.githubusercontent.com/AtulGupta8097/QuickMart/main/screenshots/authentication.jpg" width="250"/>
  <img src="https://raw.githubusercontent.com/AtulGupta8097/QuickMart/main/screenshots/home.jpg" width="250"/>
  <img src="https://raw.githubusercontent.com/AtulGupta8097/QuickMart/main/screenshots/address.jpg" width="250"/>
</p>
<p float="left">
  <img src="https://raw.githubusercontent.com/AtulGupta8097/QuickMart/main/screenshots/cart.jpg" width="250"/>
  <img src="https://raw.githubusercontent.com/AtulGupta8097/QuickMart/main/screenshots/orders.jpg" width="250"/>
</p>

---

### Admin App Features  
<p float="left">
  <img src="https://raw.githubusercontent.com/AtulGupta8097/QuickMart/main/screenshots/admin_dashboard.jpg" width="250"/>
  <img src="https://raw.githubusercontent.com/AtulGupta8097/QuickMart/main/screenshots/order_list.jpg" width="250"/>
</p>

---

## 🔓 Test Login (OTP Issues)

Use these test credentials if OTP fails due to Firebase limits:

| Phone Number  | OTP     |
|---------------|---------|
| `9999999999`  | `111111` |
| `1111111111`  | `111111` |

> ⚠️ Ensure these numbers are **whitelisted** in your Firebase Auth settings.

---

## 🧱 Tech Stack
- Java (Android)  
- Firebase: Authentication, Realtime Database, Storage  
- Room Database for offline storage  
- UI: Glide, Lottie, RecyclerView  
- Architecture: MVVM (ViewModel, LiveData, Repository)  
- Google Maps API for location-based order tracking  

---

## 🔗 APK Downloads
- 📥 [User App – APK](https://github.com/AtulGupta8097/QuickMart/releases/download/v1.0.0/app-release.apk)  
- 📥 [Admin App – APK](https://github.com/AtulGupta8097/QuickMart/releases/download/v1.0.0-admin/app-debug.apk)

---

## 🙋 About the Author

**Atul Gupta**  
🔗 [Connect with me on LinkedIn – Atul Gupta](https://www.linkedin.com/in/atul-gupta8070)  
🔗 [View my GitHub Profile](https://github.com/AtulGupta8097)

---

**Made with ❤️ using modern Android tools and best practices.**
