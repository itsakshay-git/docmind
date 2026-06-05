# Database Schema

## users

| Column        | Type      |
| ------------- | --------- |
| id            | UUID      |
| email         | VARCHAR   |
| password_hash | VARCHAR   |
| role          | VARCHAR   |
| created_at    | TIMESTAMP |
| updated_at    | TIMESTAMP |

---

## notebooks

| Column     | Type      |
| ---------- | --------- |
| id         | UUID      |
| user_id    | UUID      |
| title      | VARCHAR   |
| created_at | TIMESTAMP |

---

## documents

| Column      | Type      |
| ----------- | --------- |
| id          | UUID      |
| notebook_id | UUID      |
| file_name   | VARCHAR   |
| source_type | VARCHAR   |
| status      | VARCHAR   |
| created_at  | TIMESTAMP |

---

## chunks

| Column      | Type    |
| ----------- | ------- |
| id          | UUID    |
| document_id | UUID    |
| content     | TEXT    |
| embedding   | VECTOR  |
| page_number | INTEGER |

---

## chats

| Column      | Type      |
| ----------- | --------- |
| id          | UUID      |
| notebook_id | UUID      |
| created_at  | TIMESTAMP |

---

## messages

| Column     | Type      |
| ---------- | --------- |
| id         | UUID      |
| chat_id    | UUID      |
| role       | VARCHAR   |
| content    | TEXT      |
| created_at | TIMESTAMP |
