# SON Clinic & Pharmacy Management System

A JavaFX desktop application for basic clinic and pharmacy operations, built with Java 17+, Maven, JavaFX, SQLite, and BCrypt password hashing.

## Features in this starter project

- Login screen with a default administrator account
- Dashboard summary cards
- Patient registration, search, and patient list
- Appointment registration and appointment list
- Pharmacy inventory: add medicines, track quantity, price, expiry date, and low-stock status
- Simple medicine sales that reduce stock and save a sale record
- SQLite database stored locally as `son_clinic.db`
- Password hashes stored using BCrypt (not plaintext)
- Basic input validation and confirmation prompts

## Requirements

- JDK 17 or newer
- Maven 3.9+ recommended
- Internet connection the first time Maven downloads dependencies

## Run

From the project folder:

```bash
mvn clean javafx:run
```

Default login:
- Username: `admin`
- Password: `ChangeMe123!`

**Change the default password before using this for any real records.** This is a learning/demo starter, not a certified clinical or pharmacy system.

## Project structure

```text
son-clinic-pharmacy/
├── pom.xml
├── README.md
└── src/
    └── main/
        └── java/
            └── ke/
                └── sonclinic/
                    ├── App.java
                    ├── Database.java
                    ├── AuthService.java
                    └── Ui.java
```

## Important production notes

This is a local desktop starter application. Before using it with real patient or pharmacy data, have it reviewed and extended to include:
- Role-based access and staff accounts
- Audit trails for viewing/changing records
- Encrypted backups, data encryption, and secure recovery
- Strong session locking and password reset procedures
- Validation for medicine dispensing, prescriptions, interactions, batch numbers, and expiry
- Proper tax/receipt and accounting requirements
- Compliance with applicable Kenyan data-protection, health-record, pharmacy, and professional requirements
- Tested backup/restore and multi-user concurrency strategy

The app does not diagnose conditions or prescribe treatment. Dispensing and clinical decisions must remain with qualified professionals.
