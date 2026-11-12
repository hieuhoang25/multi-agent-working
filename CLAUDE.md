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

## Quy trình Git (agent chính phụ trách, subagent KHÔNG tự commit/push)
1. **Trước khi gọi `coder`**: đảm bảo working tree sạch (`git status`), cập nhật main (`git checkout main && git pull`), rồi tạo nhánh mới:
   - Tính năng: `feature/<mo-ta-ngan>` (vd. `feature/soft-delete`)
   - Sửa lỗi: `fix/<mo-ta-ngan>`
   - Refactor/khác: `refactor/<...>`, `chore/<...>`
   Không bao giờ code trực tiếp trên `main`.
2. **Sau khi `reviewer` không còn 🔴 Critical và `tester` báo toàn bộ test pass**: commit theo Conventional Commits:
   - `feat: ...`, `fix: ...`, `refactor: ...`, `test: ...`, `docs: ...`, `chore: ...`
   - Dòng đầu ≤ 72 ký tự, tiếng Anh; thân commit liệt kê ngắn các thay đổi chính.
   - Chia nhiều commit nếu có nhiều thay đổi độc lập (vd. code và test có thể chung, docs tách riêng).
3. Không commit file trong `target/`, file bí mật hay `.env`.
4. **Push**: chỉ push khi người dùng yêu cầu: `git push -u origin <ten-nhanh>`, sau đó đề xuất tạo Pull Request (dùng `gh pr create` nếu có) với mô tả gồm: mục tiêu, thay đổi chính, kết quả test.
5. Không bao giờ `git push --force` lên `main`, không merge vào `main` khi chưa được người dùng đồng ý.

## Quy ước
- Constructor injection, DTO là `record`, không trả entity ra ngoài controller.
- HTTP status: 201 tạo mới (kèm header Location), 204 xóa, 400 validation, 404 không tìm thấy, 409 trùng.
- Email lưu dạng lowercase và là duy nhất.
