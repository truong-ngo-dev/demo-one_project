# Discussion: <Topic>

<!-- ================================================================
AGENT RULES — đọc trước khi làm bất cứ điều gì
================================================================

## discussion.md: sẽ được chỉ định cụ thể khi bắt đầu conservation hoặc tự động tạo
- Append 1 entry khi có state change:
    - Option bị loại có lý do rõ
    - Constraint mới phát hiện
    - Quyết định được confirm (dù chưa final)
- Nếu thread vừa xong dài (>~20 exchanges): compact thành 1 structured
  entry trước khi tiếp tục — giữ nguyên decision + rationale, bỏ
  reasoning trace
- KHÔNG flush từng bước suy luận, KHÔNG flush khi đang explore

## final_design.md: cùng folder với file discussion.md tên thì có thể chỉ định bởi user hoặc tự động tạo
- Chỉ write khi user nói "chốt" hoặc tương đương
- Tổng hợp từ entries trong Log, KHÔNG copy raw discussion
- Overwrite toàn bộ mỗi lần

## Entry format (dùng cho cả append lẫn compact):
[TYPE] Tiêu đề ngắn
- What: kết quả / quyết định
- Why: lý do cốt lõi (1-2 dòng)
- Affects: component/file liên quan (bỏ qua nếu không có)

TYPE có thể là: DECISION | CONSTRAINT | ELIMINATED | OPEN
================================================================ -->

## Context
<!-- Mô tả vấn đề, scope, và những gì đã biết trước khi discuss -->

## Log
<!-- Agent append entries tại đây -->