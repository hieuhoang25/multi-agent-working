# User Management — Spring Boot CRUD + Claude Code subagents

Project mẫu REST API quản lý người dùng, được cấu hình sẵn để vibe coding với Claude Code theo mô hình multi-agent.

## Yêu cầu
- Java 21+
- Maven 3.9+
- Claude Code (để dùng subagent)

## Chạy thử
```bash
mvn spring-boot:run
```

## API
| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/users?keyword=&page=0&size=20&sort=id,asc` | Danh sách (tìm theo tên/email, phân trang) |
| GET | `/api/users/{id}` | Chi tiết |
| POST | `/api/users` | Tạo mới → 201 |
| PUT | `/api/users/{id}` | Cập nhật |
| DELETE | `/api/users/{id}` | Xóa → 204 |

Ví dụ:
```bash
curl -X POST localhost:8080/api/users -H "Content-Type: application/json" \
  -d '{"fullName":"Nguyen Van A","email":"a@example.com","phone":"0912345678","role":"USER"}'

curl localhost:8080/api/users
curl -X PUT localhost:8080/api/users/1 -H "Content-Type: application/json" \
  -d '{"fullName":"Nguyen Van B","email":"a@example.com","active":false}'
curl -X DELETE localhost:8080/api/users/1
```

Lỗi trả về dạng ProblemDetail (RFC 9457), ví dụ validation:
```json
{ "status": 400, "detail": "Validation failed", "errors": { "email": "email is invalid" } }
```

## Subagents (`.claude/agents/`)
| Agent | Vai trò | Tools | Model |
|---|---|---|---|
| `planner` | Phân tích yêu cầu, lập kế hoạch | Read, Grep, Glob (chỉ đọc) | opus |
| `coder` | Viết code theo kế hoạch, build | Read, Edit, Write, Bash | sonnet |
| `reviewer` | Review diff, bảo mật, JPA | Read, Grep, Glob, Bash (không sửa) | opus |
| `tester` | Viết + chạy test | Read, Edit, Write, Bash | sonnet |

Quy trình điều phối được mô tả trong `CLAUDE.md`.

## Vibe coding thử
Mở Claude Code tại thư mục project:
```bash
git init && git add . && git commit -m "init"
claude
```
Rồi thử các prompt:
- `Thêm chức năng đổi mật khẩu cho user (lưu mật khẩu đã hash bằng BCrypt)`
- `Thêm soft delete thay vì xóa cứng`
- `Thêm endpoint export danh sách user ra CSV`
- `Use the reviewer subagent to check my recent changes`

Gõ `/agents` để xem hoặc chỉnh các subagent.
