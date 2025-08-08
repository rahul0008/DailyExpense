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



