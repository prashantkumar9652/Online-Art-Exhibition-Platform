# Online Art Exhibition Platform
Java Swing + JDBC + MySQL  |  3 roles: Admin, Artist, Art Enthusiast

## Setup
1. MySQL Workbench mein `sql/schema.sql` run karo (purana database delete hoke sample data ke saath naya banega).
2. MySQL Connector/J jar project libraries mein add karo.
3. `src/util/DBConnection.java` mein apna MySQL password daalo.
4. `src/` ko Sources Root mark karo aur `Main.java` run karo.
   (Images `images/` folder mein copy hoti hain, isliye project root se run karo.)

## Demo logins
| Role       | Email          | Password  |
|------------|----------------|-----------|
| Admin      | admin@art.com  | admin123  |
| Artist     | ravi@art.com   | ravi123   |
| Artist     | meera@art.com  | meera123  |
| Enthusiast | priya@art.com  | priya123  |

## Packages
- model/     User (abstract) -> Admin, Artist, Enthusiast | Artwork, Exhibition, Order (implements Payable), Feedback, Notification, Cart
- dao/       DAO<T> (generic interface) -> UserDAO, ArtworkDAO, ExhibitionDAO | OrderDAO, FeedbackDAO, NotificationDAO, SettingsDAO
- util/      DBConnection, DB (generic JDBC helper), NotificationWatcher (thread), CountdownThread (thread)
- exception/ InvalidLoginException, ArtworkAlreadySoldException
- ui/        Login, Register, BaseDashboard + 3 dashboards + all tab panels

## Rubric mapping (Java GUI Rubric)
- OOP: abstract User + 3 subclasses, interfaces (Payable, Refreshable, DAO<T>), custom exceptions, polymorphism (getRole / getDashboardTitle / BaseDashboard.buildContent)
- Collections & Generics: DAO<T>, DB.query<T> + RowMapper<T>, Map cart (Cart), List/LinkedHashMap everywhere
- Multithreading & Synchronization: NotificationWatcher thread, CountdownThread, synchronized purchase in OrderDAO
- Model + DB operations: model/ and dao/ packages
- JDBC + PreparedStatement: every query
- Transaction management: OrderDAO.purchase (setAutoCommit(false), commit, rollback, SELECT ... FOR UPDATE)
