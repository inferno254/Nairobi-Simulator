# Nairobi Sim - Android Business Simulation Game

A Nairobi-themed business simulation game where you start from a small kiosk and build your empire across Kenya's capital city.

## Features

- **20+ Business Categories** - Matatu Sacco, Kiosk, Butchery, Restaurant, Agrovet, Cyber Cafe, and more
- **21 Nairobi Areas** - CBD, Westlands, Karen, Kibera, Mathare, Online, and satellite towns
- **Realistic Financial System** - KES currency, M-Pesa, Bank, SACCO, Crypto wallets
- **Employee Management** - Hire drivers, managers, chefs, marketers, farm hands
- **Asset Investment** - Vehicles, equipment, technology, livestock, infrastructure
- **Loan System** - Bank loans, SACCO, Microfinance, Digital lenders, Chama/ROSCA, Asset financing
- **Chama Groups** - Join or create investment groups (ROSCA/merry-go-round)
- **Market Events** - Holidays, seasons, weather, economic policies, infrastructure, sports
- **Daily Simulation** - Advance time, collect revenue, process expenses, loan payments

## Architecture

- **Kotlin** with **Coroutines** and **Flow**
- **Room Database** for persistence
- **Hilt** for dependency injection
- **Navigation Component** with Safe Args
- **Data Binding** and **View Binding**
- **Material3** design system
- **MVVM** architecture with **ViewModel** and **LiveData**

## Project Structure

```
app/
├── src/main/
│   ├── java/com/nairobisim/
│   │   ├── model/           # Data models (Business, Employee, Loan, Chama, Event)
│   │   ├── data/
│   │   │   ├── dao/         # Room DAOs
│   │   │   ├── database/    # Room Database
│   │   │   ├── converters/  # Type converters
│   │   │   └── repository/  # SimulationRepository
│   │   ├── di/              # Hilt modules
│   │   └── ui/
│   │       ├── main/        # MainActivity, MainViewModel
│   │       ├── dashboard/   # DashboardFragment
│   │       ├── businesses/  # Businesses, BusinessDetail, StartBusiness
│   │       ├── finances/    # FinancesFragment
│   │       ├── loans/       # Loans, LoanApplication
│   │       ├── chama/       # ChamaFragment
│   │       ├── events/      # EventsFragment
│   │       └── settings/    # SettingsFragment
│   ├── res/
│   │   ├── layout/          # XML layouts with data binding
│   │   ├── navigation/      # Navigation graph
│   │   ├── drawable/        # Vector icons
│   │   ├── values/          # Colors, strings, themes
│   │   └── menu/            # Bottom nav, top app bar
│   └── assets/              # Game data (CSV/TSV from original APK)
└── build.gradle.kts
```

## Building

```bash
cd NairobiSim
./gradlew assembleDebug
```

## Original Game Analysis

This project was built by reverse engineering `com.ttterbagames.businesssimulatornew_1.10.apk`:

- **Original Package**: `com.ttterbagames.businesssimulatornew`
- **Size**: 80 MB, multi-DEX (7 DEX files, ~40K classes)
- **Language**: Kotlin with heavy string obfuscation
- **Features**: 20+ business types, 10+ ad networks, Firebase/AppsFlyer/Adjust/Sentry
- **Approach**: Built new Android project from scratch reusing game design concepts and data assets

## License

Educational/Reverse Engineering project. Original game copyright belongs to Ttterba Games.