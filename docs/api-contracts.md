# API Contracts

## Auth

POST /api/v1/auth/register

Request:

{
"email": "user@example.com",
"password": "Password123"
}

Response:

{
"accessToken": "",
"refreshToken": ""
}

---

POST /api/v1/auth/login

Request:

{
"email": "",
"password": ""
}

Response:

{
"accessToken": "",
"refreshToken": ""
}

---

## Notebook

POST /api/v1/notebooks

GET /api/v1/notebooks

GET /api/v1/notebooks/{id}

DELETE /api/v1/notebooks/{id}

---

## Documents

POST /api/v1/documents/upload

GET /api/v1/documents/{id}

DELETE /api/v1/documents/{id}

---

## Chat

POST /api/v1/chat/ask

Request:

{
"notebookId": "",
"question": ""
}

Response:

{
"answer": "",
"citations": []
}
