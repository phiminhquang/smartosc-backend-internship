# ex-rbac-complete

Adapted from the Devteria `course/authorize-with-jwt` branch, but kept compatible with this project's relational RBAC schema:

- `users.user_id` = `CHAR(36)` UUID
- `roles.id` = `INT AUTO_INCREMENT`
- `user_roles(user_id, role_id)` = many-to-many join table
- Public user registration always gets `VIEWER`
- Only `ADMIN` can create roles or assign roles
- BCrypt hashes passwords
- Login returns a signed JWT
- Spring Security Resource Server verifies `Bearer` JWTs
- `scope` claim contains role names; Spring converts them to `ROLE_*`
- Business errors use `ErrorCode` + `AppException` + `GlobalExceptionHandler`
- DTO/entity conversion uses MapStruct
- Boilerplate uses Lombok

## Endpoints

- `POST /api/users` — register user (public, auto VIEWER)
- `POST /api/auth/token` — login and get JWT (public)
- `POST /api/auth/introspect` — check token validity (public)
- `GET /api/users` — ADMIN or EDITOR
- `GET /api/users/{uuid}` — ADMIN or EDITOR
- `POST /api/users/{uuid}/roles/{roleId}` — ADMIN only
- `GET /api/roles` — ADMIN only
- `GET /api/roles/{id}` — ADMIN only
- `POST /api/roles` — ADMIN only

## MySQL schema expected

```sql
CREATE TABLE users (
    user_id CHAR(36) PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    password_hash TEXT NOT NULL
);

CREATE TABLE roles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT
);

CREATE TABLE user_roles (
    user_id CHAR(36) NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT user_roles_ibfk_1
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT user_roles_ibfk_2
        FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);
```

## First run

Edit `application.yml` or set `DB_PASSWORD`.
The app seeds `ADMIN`, `EDITOR`, `VIEWER` and creates a local admin using `ADMIN_EMAIL` / `ADMIN_PASSWORD`.

## Quick test

Register:

```json
POST /api/users
{
  "email": "viewer@gmail.com",
  "name": "Viewer",
  "password": "12345678"
}
```

Login:

```json
POST /api/auth/token
{
  "email": "admin@gmail.com",
  "password": "your_admin_password"
}
```

Then send the returned token as:

`Authorization: Bearer <token>`

