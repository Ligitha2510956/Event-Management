# Event Registration System

Java Swing + JDBC + MySQL desktop app — mini project, fully working.

Features: create/view/search events, register/view participants, modify/cancel a
participant's registration, input validation, and a consistent, simple GUI theme
(see `src/gui/UIStyle.java`).

## Team ownership
| Person | Module | Files |
|---|---|---|
| P1 | Event Management | `model/Event.java`, `dao/EventDAO.java`, `gui/EventPanel.java` |
| P2 | Participant Management | `model/Participant.java`, `dao/ParticipantDAO.java`, `gui/ParticipantPanel.java` |
| P3 | Registration Management | `model/Registration.java`, `dao/RegistrationDAO.java`, `gui/RegistrationPanel.java` |
| P4 | Main GUI & Integration | `Main.java`, `gui/MainFrame.java` |

## One-time setup (everyone does this)

1. **Install MySQL** if not already installed, and make sure it's running.
2. **Create the database and tables**: run `database/schema.sql` in MySQL Workbench or the `mysql` CLI.
3. **Download the MySQL JDBC driver** ("mysql-connector-j", .jar file) from
   https://dev.mysql.com/downloads/connector/j/ and add it to your project's classpath in VS Code
   (Java Projects panel → Referenced Libraries → Add Jar).
4. **Set your password** in `src/dao/DatabaseConnection.java` (replace `YOUR_MYSQL_PASSWORD_HERE`).
   Do not commit your real password — see `.gitignore`.

## Running the app

From VS Code, run `src/Main.java` (right-click → Run Java). The main window opens
with 8 buttons; each opens the relevant screen (Event / Participant / Registration),
each screen has its own tabs for the actions listed in the brief.

Note: "Registration" (modify/cancel) works directly on the `participants` table —
there's no separate registrations table, since a participant row already links a
person to one event.

## Folder structure

```
EventRegistrationSystem/
├── database/
│   └── schema.sql
├── src/
│   ├── model/        <- plain Java objects (Event, Participant, Registration)
│   ├── dao/           <- database access classes + DatabaseConnection.java
│   ├── gui/            <- Swing panels + MainFrame.java
│   └── Main.java       <- entry point
├── .gitignore
└── README.md
```

## Git workflow

- `main` = stable, working code only
- Each person works on their own branch: `person1-event`, `person2-participant`,
  `person3-registration`, `person4-gui`
- Merge into `main` on integration day
