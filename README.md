# DailyExpense
Zobabe Assignment

## 📦 Dependencies Used

### 🔹 App-level `build.gradle.kts`

// Room Database
implementation(libs.androidx.room.runtime)

// If this project uses any Kotlin source, use Kotlin Symbol Processing (KSP)
ksp(libs.androidx.room.compiler)

// Hilt Dependency Injection
implementation(libs.hilt.android)
ksp(libs.hilt.compiler)
implementation(libs.androidx.hilt.navigation.compose)

## 🔹 Project-level `build.gradle.kts`

// KSP Plugin
alias(libs.plugins.devtools.ksp) apply false

// Hilt Plugin
alias(libs.plugins.hilt.android) apply false



## 🚀 App Overview
DailyExpense is an Android application for tracking daily expenditures. It is architected using **MVVM (Model-View-ViewModel)** with **Hilt** for dependency injection and **RoomDB** for local data storage. The user interface is built entirely with **Jetpack Compose**, following a **Unidirectional Data Flow (UDF)** pattern.

Key features include:
*   **ExpenseEntryScreen:** Allows users to input expense details such as title, amount, category, optional notes, and an optional receipt image. It also displays the total amount spent for the current day.
*   **ViewExpensesScreen:** Presents a list of all recorded expenses, offering options to view detailed information for each entry and potentially filter or sort the expenses.
*   **ExpenseReportScreen:** Provides a visual summary of expenses, often utilizing charts (e.g., a bar chart) to show spending patterns by category or over time.

## 📝 Prompt Used  

1. **Database Setup**  
   - Create a table with fields:  
     - `title: String`  
     - `uniqueId` (Primary Key)  
     - `amount`  
     - `category`  
     - `notes`  
     - `imageUri`  
     - `timestamp`  
   - Entity name: **ExpenseEntity**  
   - Create **Entity**, **DAO**, **Database**, and **Hilt module** for DB and DAO.  

2. **Repository Layer**  
   - Follow **Dependency Inversion Principle**.  
   - Define a repository **interface**.  
   - Implement it in a corresponding **Impl** class.  

3. **Theme Setup**  
   ```kotlin
   private val DarkColorScheme = darkColorScheme(
       primary = Purple80,
       secondary = PurpleGrey80,
       tertiary = Pink80
   )

   private val LightColorScheme = lightColorScheme(
       primary = Purple40,
       secondary = PurpleGrey40,
       tertiary = Pink40
       /* Other default colors to override
       background = Color(0xFFFFFBFE),
       surface = Color(0xFFFFFBFE),
       onPrimary = Color.White,
       onSecondary = Color.White,
       onTertiary = Color.White,
       onBackground = Color(0xFF1C1B1F),
       onSurface = Color(0xFF1C1B1F),
       */
   )


## 📥 Download APK
[![Download APK](https://img.shields.io/badge/Download-APK-brightgreen)](https://drive.google.com/file/d/1kNxtVRZM0kjEE-9Am-Hm2HZMkwdUxJYa/view?usp=sharing)
