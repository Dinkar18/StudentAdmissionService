# PadhoBihar — Business Requirements Document (BRD)

## 1. Overview

**App Name:** PadhoBihar  
**Package:** com.dp.padhobihar  
**Platform:** Android (Kotlin, MVVM, Clean Architecture)  
**Backend:** Firebase (Auth, Firestore, Storage)

### 1.1 Problem Statement

In Bihar, after 10th/12th, most students in rural areas are unaware of higher education opportunities and the government's Student Credit Card scheme (₹4 lakh loan). Education agents (consultants) manually visit villages to promote the scheme and facilitate admissions through Senior Representatives (SRs) who hold tie-ups with 50+ colleges. This entire process is:

- Manual and untracked
- Time-consuming (physical village visits)
- Prone to commission disputes
- Limited in scale (agent can handle only 30-40 students at a time)

### 1.2 Solution

A single Android app with role-based access that digitizes the existing agent-SR-college chain:

- **Admin** (Platform Owner) manages the entire ecosystem
- **SR** (Senior Representative) manages college tie-ups and their agents
- **Agent** (Field Worker) registers students and tracks admissions
- **Student** views their application status and uploads documents

### 1.3 Business Model

```
College pays platform per confirmed admission (₹3,000–₹10,000)
    → Admin keeps 15%
    → SR gets 35%
    → Agent gets 50%
```

Students and agents pay ₹0. Revenue comes from colleges only.

---

## 2. User Roles & Hierarchy

```
ADMIN (Platform Owner - You)
  └── SR (Senior Representative - has college tie-ups)
        └── AGENT (Field Agent - your father, others)
              └── STUDENT (End user - 10th/12th pass)
```

### 2.1 Role Permissions

| Feature | Admin | SR | Agent | Student |
|---------|:-----:|:--:|:-----:|:-------:|
| Manage SRs | ✅ | ❌ | ❌ | ❌ |
| Manage Agents | ✅ | ✅ (own) | ❌ | ❌ |
| Add/Edit Colleges | ✅ | ✅ | ❌ | ❌ |
| Approve Colleges | ✅ | ❌ | ❌ | ❌ |
| Add Students | ❌ | ❌ | ✅ | ❌ |
| View All Students | ✅ | ✅ (own agents) | ✅ (own) | ❌ |
| View Own Status | ❌ | ❌ | ❌ | ✅ |
| Upload Documents | ❌ | ❌ | ❌ | ✅ |
| Confirm Admission | ✅ | ✅ | ❌ | ❌ |
| View Revenue/Earnings | ✅ (all) | ✅ (own) | ✅ (own) | ❌ |
| Manage Payments | ✅ | ✅ (to agents) | ❌ | ❌ |

---

## 3. Feature Specification

### 3.1 Authentication

| Feature | Description |
|---------|-------------|
| Phone OTP Login | Firebase Phone Auth, 10-digit Indian number |
| Role Detection | After OTP, fetch user role from Firestore → route to correct dashboard |
| First-time Registration | Admin creates SR, SR creates Agent, Agent creates Student |
| Session Management | Auto-login until logout |

### 3.2 Admin Features

| # | Feature | Description |
|---|---------|-------------|
| A1 | Dashboard | Total SRs, Agents, Students, Admissions, Revenue (cards + charts) |
| A2 | Manage SRs | List, Add, Edit, Block/Unblock SRs |
| A3 | All Colleges | View all colleges added by SRs, Approve/Reject |
| A4 | All Students | View all students across all agents with filters (status, district, course) |
| A5 | Revenue Overview | Total earnings, breakdown by SR, pending payouts |
| A6 | Mark Payout | Mark payment as sent to SR |
| A7 | Notifications | Send push notifications to SRs/Agents |

### 3.3 SR Features

| # | Feature | Description |
|---|---------|-------------|
| S1 | Dashboard | My agents count, total students, admissions this month |
| S2 | My Colleges | Add college (name, address, courses, fees, seats, credit card accepted). Pending admin approval |
| S3 | Edit College | Update seat availability, fees, courses |
| S4 | My Agents | List agents under me, add new agent (name + phone → creates account) |
| S5 | Agent Performance | Students added, admissions confirmed per agent |
| S6 | Applications | View all students under my agents, filter by status |
| S7 | Confirm Admission | Mark student as "Admitted" → triggers commission |
| S8 | Payouts | View earnings from admin, mark payment to agent |

### 3.4 Agent Features

| # | Feature | Description |
|---|---------|-------------|
| G1 | Dashboard | My students (total, pending, admitted), earnings this month |
| G2 | Add Student | Name, phone, father name, village, district, class (10th/12th), marks, course interest |
| G3 | My Students | List with status badge (Inquiry → Docs Submitted → Loan Applied → Admitted → Rejected) |
| G4 | Student Detail | View full details, update status, add notes |
| G5 | College List | Browse colleges added by my SR (name, courses, fees, seats available) |
| G6 | Assign College | Link a student to a specific college |
| G7 | My Earnings | List of commissions (pending, confirmed, paid) |
| G8 | Share Form | Generate WhatsApp-shareable link for student self-registration |

### 3.5 Student Features

| # | Feature | Description |
|---|---------|-------------|
| T1 | My Profile | View/edit personal details |
| T2 | Upload Documents | Marksheet, Aadhaar, photo (camera + gallery) |
| T3 | My Application | View current status with timeline |
| T4 | College Info | See assigned college details |
| T5 | Help | Contact agent, helpline number |

---

## 4. Student Application Workflow

```
INQUIRY
  → Agent adds student or student self-registers
  
DOCS_SUBMITTED
  → Student uploads marksheet + Aadhaar + photo
  
COLLEGE_ASSIGNED
  → Agent assigns college based on course interest + eligibility

LOAN_APPLIED
  → Student Credit Card application submitted (tracked externally)

ADMITTED
  → SR confirms admission after college verification

REJECTED
  → If seat unavailable or docs invalid
```

---

## 5. Data Models

### 5.1 User
```
id: String (Firebase UID)
name: String
phone: String
role: Enum (ADMIN, SR, AGENT, STUDENT)
parentId: String (Agent→SR, SR→Admin, Student→Agent)
district: String
status: Enum (ACTIVE, BLOCKED)
createdAt: Timestamp
```

### 5.2 College
```
id: String (auto)
name: String
address: String
district: String
courses: List<Course>
feesRange: String
seatsAvailable: Int
creditCardAccepted: Boolean
approvedByAdmin: Boolean
addedBy: String (SR userId)
createdAt: Timestamp
```

### 5.3 Course
```
name: String
duration: String
fees: Long
seatsAvailable: Int
eligibility: String (e.g., "12th Science 50%+")
```

### 5.4 Student (Application)
```
id: String (auto)
userId: String (Firebase UID - if self-registered)
name: String
phone: String
fatherName: String
village: String
district: String
qualification: Enum (10TH, 12TH)
marks: Float (percentage)
courseInterest: String
agentId: String
srId: String
collegeId: String? (assigned later)
status: Enum (INQUIRY, DOCS_SUBMITTED, COLLEGE_ASSIGNED, LOAN_APPLIED, ADMITTED, REJECTED)
documents: List<DocumentRef>
notes: String
createdAt: Timestamp
updatedAt: Timestamp
```

### 5.5 Commission
```
id: String (auto)
studentId: String
agentId: String
srId: String
collegeId: String
totalAmount: Long
adminShare: Long
srShare: Long
agentShare: Long
adminToSrStatus: Enum (PENDING, PAID)
srToAgentStatus: Enum (PENDING, PAID)
createdAt: Timestamp
paidAt: Timestamp?
```

---

## 6. Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | XML + View Binding (existing setup) |
| Architecture | MVVM + Clean Architecture |
| Navigation | Jetpack Navigation Component (role-based nav graphs) |
| DI | Hilt |
| Backend | Firebase (Auth, Firestore, Storage, Cloud Messaging) |
| Auth | Firebase Phone Auth (OTP) |
| Database | Cloud Firestore |
| File Storage | Firebase Storage |
| Image Loading | Coil |
| State | LiveData / StateFlow |
| Async | Kotlin Coroutines + Flow |

---

## 7. Project Structure (Clean Architecture)

```
com.dp.padhobihar/
├── di/                          # Hilt modules
│   └── AppModule.kt
├── data/
│   ├── model/                   # Data classes (User, College, Student, Commission)
│   ├── repository/              # Repository implementations
│   └── remote/                  # Firebase data sources
├── domain/
│   ├── model/                   # Domain entities
│   ├── repository/              # Repository interfaces
│   └── usecase/                 # Use cases per feature
│       ├── auth/
│       ├── student/
│       ├── college/
│       └── commission/
├── ui/
│   ├── auth/                    # Login, OTP verification
│   ├── admin/                   # Admin screens
│   │   ├── dashboard/
│   │   ├── manageSr/
│   │   ├── colleges/
│   │   └── revenue/
│   ├── sr/                      # SR screens
│   │   ├── dashboard/
│   │   ├── colleges/
│   │   ├── agents/
│   │   └── applications/
│   ├── agent/                   # Agent screens
│   │   ├── dashboard/
│   │   ├── addStudent/
│   │   ├── students/
│   │   ├── colleges/
│   │   └── earnings/
│   └── student/                 # Student screens
│       ├── dashboard/
│       ├── profile/
│       └── documents/
├── utils/                       # Extensions, Constants, Helpers
└── PadhoBiharApp.kt            # Application class
```

---

## 8. Non-Functional Requirements

| Requirement | Target |
|-------------|--------|
| Min Android Version | API 24 (Android 7.0) |
| App Size | < 15 MB |
| Offline Support | Read cached data, queue writes |
| Language | Hindi UI (primary), English labels |
| Performance | Screen load < 2 seconds |
| Security | Firestore rules enforce role-based access |

---

## 9. MVP Scope (Phase 1)

Build only these in Phase 1:

| Priority | Feature |
|----------|---------|
| P0 | OTP Login + Role routing |
| P0 | Agent: Add Student |
| P0 | Agent: My Students list with status |
| P0 | SR: Add College |
| P0 | SR: View students, confirm admission |
| P1 | Admin: Dashboard (counts) |
| P1 | Admin: Manage SRs |
| P1 | Student: View own status |
| P1 | Agent: View colleges |
| P2 | Document upload |
| P2 | Commission tracking |
| P2 | Push notifications |

---

## 10. Future Scope (Phase 2+)

- Student self-registration via shared link
- Career guidance / course recommendation engine
- Loan application status tracking (MNSSBY integration)
- Multi-language support (Bhojpuri, Maithili)
- Agent leaderboard and gamification
- College rating system
- Analytics dashboard with charts
- WhatsApp Business API integration for notifications
- Referral system (student refers student)

---

## 11. Success Metrics

| Metric | Target (6 months) |
|--------|-------------------|
| Agents onboarded | 50+ |
| Students registered | 500+ |
| Successful admissions | 100+ |
| Colleges in platform | 30+ |
| Agent retention | 70%+ monthly active |

---

## 12. Risks & Mitigations

| Risk | Mitigation |
|------|-----------|
| Agents bypass platform | College MoU requires platform-tracked referrals |
| Low smartphone adoption in villages | Agent does everything, student just provides info |
| Trust deficit | Zero-charge guarantee, govt scheme branding, agent's personal touch |
| Commission disputes | Every transaction tracked in app with timestamps |
| College doesn't pay | Advance deposit or post-admission payment with agreement |

---

*Document Version: 1.0*  
*Created: 22 June 2026*  
*Author: PadhoBihar Team*
