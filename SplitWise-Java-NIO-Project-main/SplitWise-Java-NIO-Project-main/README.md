# SplitWise Clone (Java NIO & TCP/IP)

An efficient, distributed expense sharing system implemented using **Java Non-blocking I/O (NIO)** and **TCP Sockets**.

This project demonstrates a low-level implementation of a **Client-Server architecture**, focusing on core networking concepts and performance scalability.

## 🚀 Key Features
* **Custom TCP Protocol:** Communication relies on a custom text-based protocol over persistent TCP connections.
* **High Scalability:** Uses `java.nio.channels.Selector` to handle multiple concurrent client connections on a **single thread** (Event Loop model).
* **State Management:** Tracks active user sessions (`SelectionKey` mapping) and strictly validates user identity.
* **Robust Persistence:** Automatic data serialization (`.db` files) ensures data integrity across server restarts.
* **Command Pattern:** Extensible design where business logic is decoupled from network handling.

## ⚙️ Architecture & Network Communication
The system is built upon the **TCP/IP stack**:
1.  **Transport Layer:** Uses `ServerSocketChannel` and `SocketChannel` to establish reliable, persistent TCP connections.
2.  **Non-blocking I/O:** The server utilizes `configureBlocking(false)` and a **Selector** to multiplex IO events (`OP_ACCEPT`, `OP_READ`), allowing efficient resource usage.
3.  **Application Protocol:** A custom line-based protocol where:
    * Clients send text commands (UTF-8 encoded).
    * Server parses commands via a `CommandExecutor`.

## 🛠 Tech Stack
* **Language:** Javа
* **Networking:** Java NIO (`Selector`, `ByteBuffer`, `Channels`)
* **Protocol:** TCP/IP (Raw Sockets)
* **Testing:** JUnit 5, Mockito
* **Data Storage:** Java Binary Serialization (`ObjectOutputStream`)

## 📦 Application Capabilities
* **User System:** Register (`register`), Login (`login`), Logout (`logout`).
* **Social Graph:** Add friends (`add-friend`) and form groups (`create-group`).
* **Expense Tracking:**
    * `split <amount> <username> <reason>`: Split a bill with a specific friend.
    * `split-group <amount> <group_name> <reason>`: Distribute cost among all group members.
    * `payed <amount> <username>`: Settle debts.
* **Reporting:** `get-status` (current balance) and `get-history` (transaction logs).

## ▶️ How to Run
1. Run `SplitWiseServer.java` (listens on port 7777).
2. Run `SplitWiseClient.java` (you can launch multiple instances).
3. Use the following commands in the client console:
   - `register <user> <pass>`
   - `login <user> <pass>`
   - `help` (for a full list)
