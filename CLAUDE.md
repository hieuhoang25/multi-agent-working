# CLAUDE.md — User Management (Spring Boot)

## Tổng quan
REST API CRUD quản lý người dùng. Java 21, Spring Boot 3.5, Spring Data JPA, H2 in-memory, Jakarta Validation.

## Lệnh thường dùng
- Chạy app: `mvn spring-boot:run` → http://localhost:8080
- Build: `mvn -q compile`
- Test: `mvn -q test`
- H2 console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:userdb`, user `sa`)

## Kiến trúc
```
controller  → nhận request, validate DTO, trả DTO
service     → logic nghiệp vụ, @Transactional
repository  → Spring Data JPA
entity      → JPA entity
dto         → record request/response
exception   → exception nghiệp vụ + GlobalExceptionHandler (ProblemDetail)
```

## Quy trình multi-agent (BẮT BUỘC)
Khi người dùng yêu cầu tính năng mới hoặc thay đổi đáng kể:
1. Gọi subagent `planner` → trình bày kế hoạch cho người dùng và chờ duyệt.
2. Sau khi được duyệt → gọi `coder` để hiện thực.
3. Code xong → gọi **song song** `reviewer` và `tester`.
4. Nếu `reviewer` báo 🔴 Critical hoặc `tester` báo bug trong code chính → gọi lại `coder` để sửa, rồi lặp lại bước 3.
5. Kết thúc: tóm tắt cho người dùng những gì đã thay đổi và kết quả test.

Với sửa nhỏ (đổi tên, sửa typo, 1–2 dòng) thì làm trực tiếp, không cần qua planner.

## Quy ước
- Constructor injection, DTO là `record`, không trả entity ra ngoài controller.
- HTTP status: 201 tạo mới (kèm header Location), 204 xóa, 400 validation, 404 không tìm thấy, 409 trùng.
- Email lưu dạng lowercase và là duy nhất.
