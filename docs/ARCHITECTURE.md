# PadhoBihar Architecture

## Layer Diagram

```
┌─────────────────────────────────────────────┐
│               UI Layer                       │
│  (Activities, Fragments, Adapters)           │
├─────────────────────────────────────────────┤
│            ViewModel Layer                   │
│  (StudentViewModel, AdminViewModel, etc.)    │
├─────────────────────────────────────────────┤
│       Domain Layer (Interfaces)              │
│  (Repository interfaces, UseCases, Models)   │
├─────────────────────────────────────────────┤
│         Data Layer (Implementations)         │
│  (FirebaseStudentRepository, etc.)           │
├─────────────────────────────────────────────┤
│           Firebase Backend                   │
│  (Auth, Firestore, Storage)                  │
└─────────────────────────────────────────────┘
```

## Package Structure

```
com.dp.padhobihar/
├── PadhoBiharApp.kt          # Application class (Hilt, Timber init)
├── di/                        # Dependency Injection
│   ├── FirebaseModule.kt      # Provides FirebaseAuth, Firestore, Storage
│   └── RepositoryModule.kt    # Binds repository interfaces to implementations
├── domain/
│   ├── model/                 # Data classes & enums
│   │   ├── Student.kt, User.kt, College.kt, Commission.kt
│   │   ├── Role.kt, ApplicationStatus.kt, UserStatus.kt
│   │   ├── PaymentStatus.kt, Qualification.kt
│   ├── repository/            # Interfaces (contracts)
│   │   ├── AuthRepository.kt, StudentRepository.kt
│   │   ├── CollegeRepository.kt, UserRepository.kt
│   │   └── CommissionRepository.kt
│   └── usecase/auth/          # Use cases (thin wrappers)
├── data/
│   ├── repository/            # Firebase implementations
│   │   ├── FirebaseAuthRepository.kt
│   │   ├── FirebaseStudentRepository.kt
│   │   ├── FirebaseCollegeRepository.kt
│   │   ├── FirebaseUserRepository.kt
│   │   └── FirebaseCommissionRepository.kt
│   └── local/
│       └── CollegeDataSource.kt  # In-memory college search
├── ui/
│   ├── auth/                  # Login/signup (AuthActivity, AuthViewModel)
│   ├── splash/                # Splash screen with role routing
│   ├── student/               # Student screens (Activity + Fragments + ViewModel)
│   ├── agent/                 # Agent screens (Activity + Fragments + ViewModel)
│   ├── admin/                 # Admin screens (Activity + ViewModel)
│   └── common/                # Shared adapters (StudentAdapter, CollegeAdapter, etc.)
└── utils/
    ├── Validator.kt           # Input validation (email, phone, password, sanitize)
    ├── Navigator.kt           # Role-based navigation helper
    ├── SecurePrefs.kt         # EncryptedSharedPreferences wrapper
    ├── NetworkUtils.kt        # Connectivity check
    ├── SafeCall.kt            # Coroutine try-catch wrapper
    ├── UiState.kt             # Sealed class for Loading/Success/Error
    └── WhatsAppHelper.kt      # WhatsApp intent helper
```

## How to Add a New Feature

1. **Define the model** — Add data class to `domain/model/`.

2. **Define repository interface** — Add interface in `domain/repository/` with suspend functions.

3. **Implement repository** — Create `Firebase<Name>Repository` in `data/repository/` implementing the interface. Inject `FirebaseFirestore` via constructor.

4. **Register in DI** — Add `@Binds` entry in `di/RepositoryModule.kt`.

5. **Create ViewModel** — Add in `ui/<role>/`. Use `@HiltViewModel` + `@Inject constructor`. Inject repositories, expose `LiveData`, keep methods under 30 lines.

6. **Create UI** — Add Activity/Fragment in `ui/<role>/`. Use ViewBinding. Observe ViewModel LiveData. Use `activityViewModels()` for shared ViewModels.

7. **Add navigation** — Update `Navigator.kt` or add menu items as needed.

8. **Write tests** — Add unit test in `app/src/test/java/com/dp/padhobihar/` covering logic without Android dependencies.

## Data Flow: Student Registration

```
StudentRegisterActivity
  │ user fills form (name, email, phone, referral code)
  ▼
Validator.isValidPhone(), isValidEmail(), etc.
  │ validation passes
  ▼
StudentViewModel.register(name, email, phone, referralCode)
  │ queries Firestore "agents" where referralCode matches
  │ creates student record in "students" collection
  │ creates user record in "users" collection
  ▼
LiveData<message> emits success → navigate to StudentActivity
```

## Data Flow: College Selection

```
StudentCollegesFragment
  │ observes viewModel.colleges
  ▼
StudentViewModel.loadColleges()
  │ calls collegeRepository.getActiveColleges()
  ▼
FirebaseCollegeRepository
  │ queries Firestore "colleges" where isActive == true
  ▼
LiveData<List<College>> updates UI via CollegeAdapter

Student taps "Request" on a college:
  ▼
StudentViewModel.requestCollege(collegeId, collegeName)
  │ calls studentRepository.getStudentByUserId(uid)
  │ calls studentRepository.updateFields(studentId, map)
  │   sets requestedCollegeId, requestedCollegeName, status → COLLEGE_REQUESTED
  ▼
LiveData<message> emits success → UI refreshes
```
