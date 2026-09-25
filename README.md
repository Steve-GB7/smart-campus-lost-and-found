# 🎓 Unified Smart Campus Helpdesk — Web Application
### College of Engineering Chengannur (CEC)

A full-featured Java Web Application built with strict **Object-Oriented Programming (OOP) principles**, **MVC architecture**, **JDBC + MySQL**, and styled with the **"Warm Parchment Atelier"** design system.

---

## 🎨 Design System: "Warm Parchment Atelier"

- **Theme:** Light, editorial, flat. Cream canvas + ink text + single ember-orange accent for inline links/emphasis (never a button fill).
- **Surfaces & Cards:** Flat surfaces only, `#f2f1ed` bone cards with 24px padding, 1px hairline border, and a soft dual-layer warm shadow.
- **Typography:**
  - **Headings:** Inter / system-ui, weight 400 ONLY (never bold), progressive tightening letter-spacing (`0.01em` at 14px → `-0.005em` at 22px → `-0.02em` at 36px → `-0.03em` at 72px).
  - **Editorial Prose:** EB Garamond (Google Font), 16–19px, line-height 1.45.
  - **Metadata & IDs:** Monospace (JetBrains Mono / ui-monospace / Consolas), 12–13px.
- **Buttons (Strict 2-filled max):**
  - **Primary:** `#26251e` background, `#f7f7f4` text, 4px radius (e.g. Sign In, Post Notice).
  - **Secondary:** `#e6e5e0` background, `#26251e` text, 4px radius (e.g. Filter buttons, Cancel).
  - **Ghost:** Transparent background, ink at 60% opacity with underline on hover.
  - **Amber:** `#c08532` in-product action buttons (e.g. Submit Grievance, Update Status, Send Message).
- **Tags & Status Chips:**
  - `tag-lost`: Crimson border & text (`#cf2d56`)
  - `tag-found`: Forest border & text (`#34785c`)
  - `tag-pending`: Crimson border & text (`#cf2d56`)
  - `tag-progress`: Amber border & text (`#c08532`)
  - `tag-resolved`: Forest border & text (`#34785c`)
- **Inputs:** Terminal-style inputs with `>` prefix prompt and monospace styling.

---

## 📦 Project Structure

```
lostandfound/
├── schema.sql                                ← MySQL database schema
├── README.md                                 ← Documentation
├── build.bat                                 ← Windows build script
├── run.bat                                   ← Launches web server at http://localhost:8080/
├── setup_db.bat                              ← Database setup script
├── lib/
│   └── mysql-connector-j-8.3.0.jar           ← JDBC MySQL Driver
├── web/
│   ├── static/css/
│   │   └── style.css                         ← Warm Parchment Atelier design system
│   └── templates/                            ← Pure Markup + CSS View Layer
│       ├── header.html                       ← Role-aware navigation bar
│       ├── footer.html                       ← Editorial footer
│       ├── login.html                        ← Terminal-style auth form
│       ├── register.html                     ← Role-selection signup
│       ├── feed.html                         ← Lost & Found card grid & post modal
│       ├── tickets.html                      ← Grievance ticket dashboard
│       └── messages.html                     ← Private messaging panel & chat bubbles
└── src/com/cec/helpdesk/
    ├── App.java                              ← Web application bootstrap & HTTP server
    ├── model/                                ← Domain models (Encapsulation, Inheritance)
    │   ├── User.java                         ← Abstract base class
    │   ├── Student.java                      ← Extends User (Polymorphism)
    │   ├── Staff.java                        ← Extends User (Polymorphism)
    │   ├── Admin.java                        ← Extends User (Polymorphism)
    │   ├── Ticket.java
    │   ├── Post.java
    │   ├── Message.java
    │   └── enums/
    │       ├── Role.java
    │       ├── TicketStatus.java
    │       ├── TicketCategory.java
    │       └── PostType.java
    ├── dao/                                  ← Data Access Objects (JDBC ↔ MySQL)
    │   ├── DatabaseManager.java              ← ★ SINGLETON pattern (double-checked locking)
    │   ├── UserDAO.java
    │   ├── TicketDAO.java
    │   ├── PostDAO.java
    │   └── MessageDAO.java
    ├── service/                              ← Service interfaces (★ ABSTRACTION)
    │   ├── AuthService.java
    │   ├── TicketService.java
    │   ├── FeedService.java
    │   ├── MessageService.java
    │   └── impl/                             ← Concrete service implementations
    │       ├── AuthServiceImpl.java
    │       ├── TicketServiceImpl.java
    │       ├── FeedServiceImpl.java
    │       └── MessageServiceImpl.java
    ├── adapter/                              ← ★ ADAPTER pattern (View Models / Formatting)
    │   ├── DataAdapter.java                  ← Generic Adapter interface
    │   ├── TicketDisplayAdapter.java         ← Adapts Ticket to Warm Parchment card / row
    │   ├── PostDisplayAdapter.java           ← Adapts Post to Warm Parchment card
    │   └── MessageDisplayAdapter.java        ← Adapts Message to bone chat bubbles
    ├── controller/                           ← Controllers (HTTP orchestration)
    │   ├── AuthController.java
    │   ├── TicketController.java
    │   ├── FeedController.java
    │   ├── MessageController.java
    │   ├── WebRouter.java                    ← HTTP router & endpoint dispatcher
    │   ├── HttpUtils.java                    ← Form & query parsing, redirects, headers
    │   └── SessionManager.java               ← Cookie sessions & flash notifications
    └── view/                                 ← View Layer Renderer
        └── TemplateEngine.java               ← Merges data into HTML templates
```

---

## 🏗️ OOP Concepts & Design Patterns

| Pattern / Concept | Where Demonstrated | Description |
| :--- | :--- | :--- |
| **Singleton Pattern** | [`DatabaseManager.java`](file:///c:/Users/josep/OneDrive/Desktop/lostandfound/src/com/cec/helpdesk/dao/DatabaseManager.java) | Thread-safe, double-checked locking single JDBC connection manager with environment variable fallbacks (`DB_USER`, `DB_PASSWORD`). |
| **Adapter Pattern** | [`DataAdapter<T>`](file:///c:/Users/josep/OneDrive/Desktop/lostandfound/src/com/cec/helpdesk/adapter/DataAdapter.java) | Converts domain models into display tables and Warm Parchment HTML cards (`toHtml(item, currentUser)`). |
| **MVC Architecture** | Model (`model/`, `dao/`), View (`web/templates/`, `style.css`), Controller (`controller/`, `service/`) | Complete separation of concerns: Views contain **only markup and CSS** without business logic. |
| **Encapsulation** | All model classes (`User`, `Ticket`, `Post`, `Message`) | Private fields, validation, getters, and setters. |
| **Inheritance** | `User` (abstract) → `Student`, `Staff`, `Admin` | Shared attributes (`userId`, `username`, `email`) inherited by concrete role classes. |
| **Polymorphism** | `getDashboardTitle()`, `getPermissionLevel()` | Dynamic role titles and permission checks in the web navigation and grievance dashboards. |
| **Abstraction** | Service interfaces (`AuthService`, `TicketService`, `FeedService`, `MessageService`) | Abstract contracts decoupled from JDBC DAO implementations. |

---

## 🚀 Setup & Execution

### 1. Database Setup
Ensure MySQL is running on `localhost:3306`, then run:
```cmd
setup_db.bat
```
*(Or manually: `mysql -u root -p < schema.sql`)*

### 2. Configure Connection (Optional)
The application defaults to `root` with no password. You can configure via environment variables:
```cmd
set DB_USER=root
set DB_PASSWORD=your_password
```
Or pass JVM arguments:
```cmd
java -Ddb.user=root -Ddb.password=your_password -cp "out;lib/*" com.cec.helpdesk.App
```

### 3. Build & Run the Web Application

**Option A — Windows Scripts:**
```cmd
build.bat
run.bat
```

**Option B — Manual Commands:**
```bash
# Compile
javac -encoding UTF-8 -cp "lib/*" -d out src/com/cec/helpdesk/model/enums/*.java src/com/cec/helpdesk/model/*.java src/com/cec/helpdesk/dao/*.java src/com/cec/helpdesk/service/*.java src/com/cec/helpdesk/service/impl/*.java src/com/cec/helpdesk/adapter/*.java src/com/cec/helpdesk/view/*.java src/com/cec/helpdesk/controller/*.java src/com/cec/helpdesk/App.java

# Run
java -cp "out;lib/*" com.cec.helpdesk.App
```

4. **Open your web browser:**
   ```
   http://localhost:8080/
   ```

> **Default Administrator Credentials:**
> - Username: `admin`
> - Password: `admin123`

---

## 📋 Web Features

1. **Top Navigation Bar:**
   - Role-aware badge showing current user and role pill (`STUDENT`, `STAFF`, `ADMIN`).
   - Sticky navigation with brand tile.
2. **Lost & Found Feed (`/feed`):**
   - Public bulletin card grid.
   - Filter by All, Lost Only (`[LOST]`), or Found Only (`[FOUND]`).
   - "+ Post Notice" modal with category selection and attachment path.
   - Authors can mark their own notices as "RESOLVED".
   - Non-authors have "💬 Message Author" action to initiate direct chat.
3. **Grievance Ticketing Portal (`/tickets`):**
   - **Student View:** File grievance under IT, Electrical, or General Facilities; track status in real-time.
   - **Staff View:** Review grievances auto-routed by department; inline status dropdown (`PENDING` → `IN_PROGRESS` → `RESOLVED`) with remarks and amber update button.
   - **Admin View:** Overview of all tickets across all campus facilities.
4. **Private Messaging (`/messages`):**
   - Split layout with thread inbox list on the left and active chat pane on the right.
   - Chat bubbles with bone surfaces and ink text.
   - Monospace timestamps and terminal-style composer.
